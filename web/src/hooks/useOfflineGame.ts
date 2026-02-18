import { useState, useEffect, useCallback } from 'react';
import { createGame, startGame, getGameState, performAction } from '../api/gameApi';
import type { GameState } from '../types/game';

export function useOfflineGame() {
    const [gameId, setGameId] = useState<string | null>(null);
    const [gameState, setGameState] = useState<GameState | null>(null);
    const [newPlayerName, setNewPlayerName] = useState('');
    const [loading, setLoading] = useState(false);
    const [revealRoles, setRevealRoles] = useState(false);

    const [localPlayers, setLocalPlayers] = useState<string[]>([]);

    const [activeVoter, setActiveVoter] = useState<string | null>(null);

    const [seerResult, setSeerResult] = useState<{ target: string, role: string } | null>(null);

    const fetchGameState = useCallback(async (id?: string | null) => {
        const resolvedId = id ?? gameId;
        if (!resolvedId) return;
        try {
            const data = await getGameState(resolvedId);
            setGameState(data);
        } catch (error) {
            console.error("Failed to fetch game state", error);
        }
    }, [gameId]);

    const isFinished = gameState?.phase === 'FINISHED';

    useEffect(() => {
        if (!gameId) return;
        if (isFinished) return;

        fetchGameState(gameId);
        const interval = setInterval(() => fetchGameState(gameId), 2000);
        return () => clearInterval(interval);
    }, [gameId, isFinished, fetchGameState]);

    const phase = gameState?.phase;
    const phaseKey = gameState?.phaseKey;
    const currentTurn = gameState?.currentTurn;

    useEffect(() => {
        if (phaseKey === "NIGHT" && currentTurn === "Wolf") {
            setSeerResult(null);
        }
        if (phase === "NOT_STARTED") {
            setSeerResult(null);
        }
        setActiveVoter(null);
    }, [phase, phaseKey, currentTurn]);

    const handleAddPlayer = () => {
        if (!newPlayerName.trim()) return;

        if (!gameId) {
            if (localPlayers.includes(newPlayerName)) {
                alert("Player name already exists!");
                return;
            }
            setLocalPlayers([...localPlayers, newPlayerName]);
            setNewPlayerName('');
        } else {
            alert("Game already started.");
        }
    };

    const handleRemovePlayer = (name: string) => {
        if (!gameId) {
            setLocalPlayers(localPlayers.filter(p => p !== name));
        }
    };

    const handleStartGame = async () => {
        if (localPlayers.length < 4) {
            alert("Need at least 4 players to start!");
            return;
        }

        setLoading(true);
        try {
            const data = await createGame('OFFLINE', undefined, localPlayers);
            const newGameId = data.gameId;
            setGameId(newGameId);

            await startGame(newGameId);

            await fetchGameState(newGameId);
        } catch (error) {
            alert("Failed to start game: " + error);
        } finally {
            setLoading(false);
        }
    };

    const handleKill = async (playerId: string) => {
        if (!gameId) return;
        try {
            await performAction(gameId, "Master", "KILL", playerId);
            await fetchGameState();
        } catch (error) {
            console.error("Kill failed", error);
        }
    };

    const handleNextPhase = async () => {
        if (!gameId) return;
        try {
            await performAction(gameId, "Master", "NEXT_PHASE");
            await fetchGameState();
        } catch (error) {
            console.error("Action failed", error);
        }
    };

    const handleVote = async (targetId: string) => {
        if (!gameId || !activeVoter) return;
        try {
            await performAction(gameId, activeVoter, "VOTE", targetId);
            setActiveVoter(null);
            await fetchGameState();
        } catch (error) {
            console.error("Vote failed", error);
        }
    };

    const handleAbstain = async () => {
        if (!gameId || !activeVoter) return;
        try {
            await performAction(gameId, activeVoter, "VOTE", "SKIP");
            setActiveVoter(null);
            await fetchGameState();
        } catch (error) {
            console.error("Abstain failed", error);
        }
    };

    const handlePeek = async (targetId: string) => {
        if (!gameId) return;
        try {
            const data = await performAction(gameId, "Master", "PEEK", targetId);
            if (data && data.peekResult) {
                setSeerResult({ target: targetId, role: data.peekResult });
            }
            await fetchGameState();
        } catch (error) {
            console.error("Peek failed", error);
        }
    };

    const advanceSeerTurn = async () => {
        if (!gameId) return;
        try {
            setSeerResult(null);
            await performAction(gameId, "Master", "NEXT_TURN");
            await fetchGameState();
        } catch (error) {
            console.error("Advance seer turn failed", error);
        }
    };

    const manualAction = async (action: string, targetId?: string) => {
        if (!gameId) return;
        try {
            await performAction(gameId, "Master", action, targetId);
            await fetchGameState();
        } catch (error) {
            console.error("Manual action failed", error);
        }
    };

    const phaseMessage = (() => {
        if (!gameState) return "";

        if (gameState.phaseKey === "NIGHT") {
            const turnEmoji = gameState.currentTurn === "Wolf" ? "🐺" : gameState.currentTurn === "Seer" ? "👁️" : "❤️";
            const roleFilter = gameState.currentTurn === "Wolf" ? "Werewolf" : gameState.currentTurn;
            const rolePlayers = gameState.players.filter(p => p.role === roleFilter && p.isAlive).map(p => p.name).join(", ");
            return `${turnEmoji} ${gameState.phaseDisplayName} (${rolePlayers}): ${gameState.turnInstruction || ""}`;
        }

        if (gameState.phaseKey === "DAY_VOTING" && activeVoter) {
            return `🗳️ Voting: Who is ${activeVoter} voting for? (Click a target)`;
        }

        const phaseEmojis: Record<string, string> = {
            "DAY_DISCUSSION": "☀️",
            "DAY_VOTING": "🗳️",
            "DAY_RESULTS": "📊"
        };
        const emoji = phaseEmojis[gameState.phaseKey] || "";
        const instruction = gameState.turnInstruction ? `: ${gameState.turnInstruction}` : "";
        return `${emoji} ${gameState.phaseDisplayName}${instruction}`;
    })();

    return {
        gameId,
        gameState,
        localPlayers,
        newPlayerName,
        setNewPlayerName,
        loading,
        revealRoles,
        setRevealRoles,
        activeVoter,
        setActiveVoter,
        handleAddPlayer,
        handleRemovePlayer,
        handleStartGame,
        handleKill,
        handleNextPhase,
        handleVote,
        handleAbstain,
        handlePeek,
        advanceSeerTurn,
        manualAction,
        phaseMessage,
        seerResult
    };
}
