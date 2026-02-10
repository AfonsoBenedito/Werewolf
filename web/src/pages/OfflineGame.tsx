import { useState, useEffect } from 'react';
import { createGame, startGame, getGameState, performAction } from '../api/gameApi';
import { Play, RefreshCw } from 'lucide-react';
import { PlayerCard } from '../components/common/PlayerCard';
import { GameHeader } from '../components/common/GameHeader';
import '../styles/OfflineGame.css';

interface Player {
    id: string;
    name: string;
    isAlive: boolean;
    role: string;
}

interface GameState {
    id: string;
    status: string;
    players: Player[];
    phase: string;
    dayCount: number;
    winner?: string;
    lastDeadPlayerName?: string;
    votes?: Record<string, string>; // VoterID -> TargetName
}

export default function OfflineGame() {
    const [gameId, setGameId] = useState<string | null>(null);
    const [gameState, setGameState] = useState<GameState | null>(null);
    const [newPlayerName, setNewPlayerName] = useState('');
    const [loading, setLoading] = useState(false);
    const [revealRoles, setRevealRoles] = useState(false);

    // Local State for Setup Phase (before game is created in Backend)
    const [localPlayers, setLocalPlayers] = useState<string[]>([]);

    // Voting UI State
    const [activeVoter, setActiveVoter] = useState<string | null>(null);

    useEffect(() => {
        if (gameId) {
            const interval = setInterval(fetchGameState, 2000); // Polling every 2s
            return () => clearInterval(interval);
        }
    }, [gameId]); // Only poll once gameId is set

    const fetchGameState = async (id = gameId) => {
        if (!id) return;
        try {
            const data = await getGameState(id);
            setGameState(data);
        } catch (error) {
            console.error("Failed to fetch game state", error);
        }
    };

    const handleAddPlayer = async () => {
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
        if (!confirm("Are you sure you want to kill this player?")) return;
        try {
            await performAction(gameId, "Master", "KILL", playerId);
            await fetchGameState();
        } catch (error) {
            alert("Action failed: " + (error as any).response?.data?.message || "Unknown error");
        }
    };

    const handleNextPhase = async () => {
        if (!gameId) return;
        try {
            await performAction(gameId, "Master", "NEXT_PHASE");
            await fetchGameState();
        } catch (error) {
            alert("Action failed");
        }
    };

    const handleVote = async (targetId: string) => {
        if (!gameId || !activeVoter) return;
        try {
            await performAction(gameId, activeVoter, "VOTE", targetId);
            setActiveVoter(null);
            await fetchGameState();
        } catch (error) {
            alert("Vote failed");
        }
    };

    const getPhaseMessage = () => {
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
    };

    // SETUP UI
    if (!gameId) {
        return (
            <div className="offline-game">
                <h1>Offline Mode (Setup)</h1>
                <div className="setup-section">
                    <div className="input-group">
                        <input
                            value={newPlayerName}
                            onChange={(e) => setNewPlayerName(e.target.value)}
                            placeholder="Player Name"
                            onKeyDown={(e) => e.key === 'Enter' && handleAddPlayer()}
                        />
                        <button onClick={handleAddPlayer}>Add Player</button>
                    </div>
                    <div className="player-list">
                        {localPlayers.map(p => (
                            <div key={p} className="player-item">
                                <span>{p}</span>
                            </div>
                        ))}
                    </div>
                    {localPlayers.length >= 4 && (
                        <button className="start-btn" onClick={handleStartGame} disabled={loading}>
                            <Play size={16} /> {loading ? "Starting..." : "Start Game"}
                        </button>
                    )}
                </div>
            </div>
        );
    }


    if (!gameState) return <div className="loading">Loading game state...</div>;

    const isNight = gameState.phase.includes("NIGHT");
    const isWolfTurn = gameState.phase.includes("Wolf");
    const isSeerTurn = gameState.phase.includes("Seer");
    const isMedicTurn = gameState.phase.includes("Medic");
    const isVoting = gameState.phase.includes("VOTING");
    const isResults = gameState.phase === "DAY_RESULTS";

    return (
        <div className="offline-game">
            <h1>Offline Mode (Master)</h1>

            <GameHeader
                gameId={gameId}
                phase={getPhaseMessage()} // Offline mode uses descriptive phase message
                dayCount={gameState.dayCount}
                showConnectionStatus={false}
            />

            {gameState.winner && <h2 className="winner-banner">🏆 Winner: {gameState.winner}</h2>}

            {/* Voting Results Screen */}
            {isResults && (
                <div className="results-screen">
                    <div className={`death-announcement ${!gameState.lastDeadPlayerName ? 'good-news' : ''}`}>
                        <h3>🗳️ Voting Result</h3>
                        <p>
                            {gameState.lastDeadPlayerName
                                ? `${gameState.lastDeadPlayerName} was eliminated by the village.`
                                : "The village could not agree. No one was eliminated."
                            }
                        </p>
                    </div>
                    <button className="continue-btn" onClick={() => performAction(gameId!, "Master", "NEXT_PHASE").then(() => fetchGameState())}>
                        🌙 Continue to Night
                    </button>
                </div>
            )}

            {gameState.status === 'IN_PROGRESS' && (
                <div className="game-controls-wrapper">
                    {/* Announcement Banner (Voting Result or Night Kill) */}
                    {(gameState.phase === "DAY_DISCUSSION" || (gameState.phase.includes("NIGHT") && gameState.lastDeadPlayerName)) && (
                        <div className={`death-announcement ${!gameState.lastDeadPlayerName ? 'good-news' : ''}`}>
                            <h3>{gameState.phase.includes("NIGHT") ? "🗳️ Voting Result" : (gameState.lastDeadPlayerName ? "☠️ Tragic News!" : "☀️ Peaceful Night")}</h3>
                            <p>
                                {gameState.phase.includes("NIGHT")
                                    ? `${gameState.lastDeadPlayerName} was eliminated by the village.`
                                    : (gameState.lastDeadPlayerName
                                        ? `${gameState.lastDeadPlayerName} was killed last night.`
                                        : "No one died last night.")
                                }
                            </p>
                        </div>
                    )}

                    <div className="game-controls">
                        {/* Phase Transition Button */}
                        {gameState.phase === "DAY_DISCUSSION" ? (
                            <button className="vote-phase-btn" onClick={handleNextPhase}>
                                <Play size={16} /> Start Voting
                            </button>
                        ) : (
                            !isVoting && !isResults && <button onClick={handleNextPhase}><RefreshCw size={16} /> Force Next Phase</button>
                        )}

                        {/* Skip Turn for NIGHT only */}
                        {isNight && (
                            <button onClick={() => performAction(gameId!, "Master", "NEXT_TURN").then(() => fetchGameState())}>
                                Skip Turn
                            </button>
                        )}

                        {/* Cancel Vote Selection */}
                        {activeVoter && (
                            <button className="cancel-btn" onClick={() => setActiveVoter(null)}>
                                Cancel Vote Selection
                            </button>
                        )}

                        <button onClick={() => setRevealRoles(!revealRoles)}>
                            {revealRoles ? "Hide Roles" : "Reveal Roles (Master)"}
                        </button>
                    </div>

                    <div className="player-grid">
                        {gameState.players.map(p => {
                            const hasVoted = gameState.votes && gameState.votes[p.name];

                            // Offline specific flags for PlayerCard
                            const showKill = isNight && isWolfTurn && p.role !== "Werewolf";
                            const showHeal = isNight && isMedicTurn;
                            const showPeek = isNight && isSeerTurn && p.role !== "Seer";
                            const showVoteSelect = isVoting && !activeVoter && !hasVoted;
                            const showVoteTarget = isVoting && activeVoter && activeVoter !== p.name;

                            return (
                                <PlayerCard
                                    key={p.name}
                                    name={p.name}
                                    isAlive={p.isAlive}
                                    role={p.role}
                                    revealedRole={revealRoles ? p.role : undefined} // Only show if master reveals
                                    hasVoted={!!hasVoted}
                                    isVotingPhase={isVoting}
                                    isActiveVoter={activeVoter === p.name}
                                    isVoteTarget={false} // Offline logic is manual buttons, not click-to-target generally?
                                    // Actually, offline mode uses specific buttons, so we pass explicit 'onX' handlers
                                    // and set 'showX' flags.

                                    showKill={showKill}
                                    onKill={() => handleKill(p.name)}

                                    showHeal={showHeal}
                                    onHeal={() => performAction(gameId!, "Master", "HEAL", p.name).then(() => fetchGameState())}

                                    showPeek={showPeek}
                                    onPeek={() => {
                                        const isVillager = p.role === "Villager";
                                        const msg = isVillager ? "Regular Villager" : "Has Powers / Special Role";
                                        alert(`${p.name} is: ${msg}`);
                                        performAction(gameId!, "Master", "PEEK", p.name).then(() => fetchGameState());
                                    }}

                                    showVoteSelect={!!showVoteSelect}
                                    onVoteSelect={() => setActiveVoter(p.name)}

                                    showVoteTarget={!!showVoteTarget}
                                    onVoteTarget={() => handleVote(p.name)}
                                />
                            );
                        })}
                    </div>
                </div>
            )}


        </div>
    );
}
