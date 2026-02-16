import { useState, useEffect, useMemo } from 'react';
import { createGame, startGame, getGameState, performAction } from '../api/gameApi';

export interface Player {
    id: string;
    name: string;
    isAlive: boolean;
    role: string;
}

export interface GameState {
    id: string;
    status: string;
    players: Player[];
    phase: string;
    dayCount: number;
    winner?: string;
    lastDeadPlayerName?: string;
    votes?: Record<string, string>; // VoterID -> TargetName
}

export function useOfflineGame() {
    const [gameId, setGameId] = useState<string | null>(null);
    const [gameState, setGameState] = useState<GameState | null>(null);
    const [newPlayerName, setNewPlayerName] = useState('');
    const [loading, setLoading] = useState(false);
    const [revealRoles, setRevealRoles] = useState(false);

    // Local State for Setup Phase (before game is created in Backend)
    const [localPlayers, setLocalPlayers] = useState<string[]>([]);

    // Voting UI State
    const [activeVoter, setActiveVoter] = useState<string | null>(null);

    // Seer Result UI State
    const [seerResult, setSeerResult] = useState<{ target: string, role: string } | null>(null);

    useEffect(() => {
        if (gameId) {
            fetchGameState(gameId);
            const interval = setInterval(() => fetchGameState(gameId), 2000); // Polling every 2s
            return () => clearInterval(interval);
        }
    }, [gameId]);

    // Reset UI states when phase changes
    useEffect(() => {
        // Only clear Seer result when starting a NEW night cycle
        // to ensure it stays visible during the Day transition
        if (gameState?.phase === "NIGHT - Wolf" || gameState?.phase === "NOT_STARTED") {
            setSeerResult(null);
        }
        setActiveVoter(null);
    }, [gameState?.phase]);

    const fetchGameState = async (id = gameId) => {
        if (!id) return;
        try {
            const data = await getGameState(id);
            setGameState(data);
        } catch (error) {
            console.error("Failed to fetch game state", error);
        }
    };

    const handleAddPlayer = () => {
        if (!newPlayerName.trim()) return;

        // If game not started, add locally
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
            // 1. Create Game with all players
            const data = await createGame('OFFLINE', undefined, localPlayers);
            const newGameId = data.gameId;
            setGameId(newGameId);

            // 2. Start Game
            await startGame(newGameId);

            // 3. Fetch Initial State
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
            // "SKIP" is treated as ABSTAIN by the backend (Vote Action)
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

    const phaseMessage = useMemo(() => {
        if (!gameState) return "";

        if (gameState.phase.includes("NIGHT")) {
            if (gameState.phase.includes("Wolf")) {
                const wolves = gameState.players.filter(p => p.role === "Werewolf" && p.isAlive).map(p => p.name).join(", ");
                return `🐺 Werewolves' Turn (${wolves}): Choose a victim to kill.`;
            }
            if (gameState.phase.includes("Seer")) {
                const seers = gameState.players.filter(p => p.role === "Seer" && p.isAlive).map(p => p.name).join(", ");
                return `👁️ Seer's Turn (${seers}): Choose a player to inspect.`;
            }
            if (gameState.phase.includes("Medic")) {
                const medics = gameState.players.filter(p => p.role === "Medic" && p.isAlive).map(p => p.name).join(", ");
                return `❤️ Medic's Turn (${medics}): Choose a player to save.`;
            }
            return "Night Phase";
        }

        if (gameState.phase === "DAY_DISCUSSION") return "☀️ Day Discussion: Discuss who might be a wolf.";

        if (gameState.phase === "DAY_VOTING") {
            if (activeVoter) {
                return `🗳️ Voting: Who is ${activeVoter} voting for? (Click a target)`;
            }
            return "🗳️ Voting Phase: Select a player to cast their vote.";
        }

        if (gameState.phase === "DAY_RESULTS") {
            return "📊 Voting Results";
        }

        return gameState.phase;
    }, [gameState, activeVoter]);

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
