import { useState, useEffect } from 'react';
import { createGame, joinGame, startGame, getGameState, performAction } from '../api/gameApi';
import { Play, Skull, RefreshCw, CheckSquare } from 'lucide-react';

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

    // Voting UI State
    const [activeVoter, setActiveVoter] = useState<string | null>(null);

    useEffect(() => {
        if (!gameId) {
            initializeGame();
        } else {
            const interval = setInterval(fetchGameState, 2000); // Polling every 2s
            return () => clearInterval(interval);
        }
    }, [gameId]);

    const initializeGame = async () => {
        try {
            const data = await createGame('OFFLINE');
            setGameId(data.gameId);
            fetchGameState(data.gameId);
        } catch (error) {
            console.error("Failed to create game", error);
        }
    };

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
        if (!newPlayerName.trim() || !gameId) return;
        setLoading(true);
        try {
            await joinGame(gameId, newPlayerName);
            setNewPlayerName('');
            await fetchGameState();
        } catch (error) {
            alert("Failed to add player");
        } finally {
            setLoading(false);
        }
    };

    const handleStartGame = async () => {
        if (!gameId) return;
        try {
            await startGame(gameId);
            await fetchGameState();
        } catch (error) {
            alert("Failed to start game: " + error);
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
            // Need to change backend to accept real player ID for VOTE action even in Offline Mode
            // because "Master" voting doesn't make sense for individual outcomes.
            // GameService.kt: if (voterId == "Master") return
            // So we MUST send activeVoter as playerId
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
            <div className="game-info">
                <p>Game ID: <strong>{gameId}</strong></p>
                <div className="phase-banner">
                    <h2>{getPhaseMessage()}</h2>
                </div>
                {gameState.status !== 'NOT_STARTED' && <p>Day: {gameState.dayCount}</p>}
                {gameState.winner && <h2 className="winner-banner">🏆 Winner: {gameState.winner}</h2>}
            </div>

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

            {gameState.status === 'NOT_STARTED' && (
                <div className="setup-section">
                    <div className="input-group">
                        <input
                            value={newPlayerName}
                            onChange={(e) => setNewPlayerName(e.target.value)}
                            placeholder="Player Name"
                            onKeyDown={(e) => e.key === 'Enter' && handleAddPlayer()}
                        />
                        <button onClick={handleAddPlayer} disabled={loading}>Add Player</button>
                    </div>
                    <div className="player-list">
                        {gameState.players.map(p => (
                            <div key={p.name} className="player-item">
                                <span>{p.name}</span>
                            </div>
                        ))}
                    </div>
                    {gameState.players.length >= 4 && (
                        <button className="start-btn" onClick={handleStartGame}>
                            <Play size={16} /> Start Game
                        </button>
                    )}
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
                            // Clean Manual Advance logic?
                            // Only show Force Next Phase if really needed, but mostly automated now.
                            // Giving user 'Next Phase' button as escape hatch.
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
                            return (
                                <div key={p.name} className={`player-card ${!p.isAlive ? 'dead' : ''} ${activeVoter === p.name ? 'active-voter' : ''} ${hasVoted ? 'has-voted' : ''}`}>
                                    <div className="card-header">
                                        <h3>{p.name}</h3>
                                        {hasVoted && isVoting && <span className="voted-badge">Has Voted</span>}
                                    </div>

                                    {revealRoles && <p className="role">{p.role}</p>}

                                    {p.isAlive && (
                                        <div className="actions">
                                            {isNight && (
                                                <>
                                                    {isWolfTurn && p.role !== "Werewolf" && (
                                                        <button className="icon-btn kill-btn" onClick={() => handleKill(p.name)} title="Kill Player">
                                                            <Skull size={16} /> Kill
                                                        </button>
                                                    )}

                                                    {isMedicTurn && (
                                                        <button className="icon-btn heal-btn" onClick={() => performAction(gameId!, "Master", "HEAL", p.name).then(() => fetchGameState())} title="Heal Player">
                                                            ❤️ Heal
                                                        </button>
                                                    )}

                                                    {isSeerTurn && p.role !== "Seer" && (
                                                        <button className="icon-btn peek-btn" onClick={() => {
                                                            const isVillager = p.role === "Villager";
                                                            const msg = isVillager ? "Regular Villager" : "Has Powers / Special Role";
                                                            alert(`${p.name} is: ${msg}`);
                                                            performAction(gameId!, "Master", "PEEK", p.name).then(() => fetchGameState());
                                                        }} title="Peek Player">
                                                            👁️ Peek
                                                        </button>
                                                    )}
                                                </>
                                            )}

                                            {isVoting && !activeVoter && !hasVoted && (
                                                <button className="icon-btn vote-btn" onClick={() => setActiveVoter(p.name)} title="Cast Vote">
                                                    <CheckSquare size={16} /> Vote
                                                </button>
                                            )}

                                            {isVoting && activeVoter && activeVoter !== p.name && (
                                                <button className="icon-btn vote-target-btn" onClick={() => handleVote(p.name)} title={`Vote for ${p.name}`}>
                                                    Vote For
                                                </button>
                                            )}
                                        </div>
                                    )}
                                </div>
                            );
                        })}
                    </div>
                </div>
            )}

            <style>{`
                .player-grid {
                    display: grid;
                    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
                    gap: 1rem;
                    margin-top: 1rem;
                }
                .player-card {
                    background: #333;
                    padding: 1rem;
                    border-radius: 8px;
                    border: 1px solid #444;
                    position: relative;
                }
                .player-card.dead {
                    opacity: 0.5;
                    border-color: red;
                }
                .player-card.active-voter {
                    border-color: #f39c12;
                    box-shadow: 0 0 10px #f39c12;
                }
                .player-card.has-voted {
                    border-color: #2ecc71;
                    background: #25412e;
                    opacity: 0.8;
                }
                .role {
                    font-weight: bold;
                    color: #aaa;
                    font-size: 0.9rem;
                }
                .winner-banner {
                    color: gold;
                    font-size: 2rem;
                }
                .game-controls-wrapper {
                    display: flex;
                    flex-direction: column;
                    gap: 1rem;
                }
                .game-controls {
                    display: flex;
                    gap: 1rem;
                    flex-wrap: wrap;
                }
                .icon-btn {
                    display: flex;
                    align-items: center;
                    gap: 0.5rem;
                    padding: 0.5rem 1rem;
                    border: none;
                    border-radius: 4px;
                    cursor: pointer;
                    color: white;
                    width: 100%;
                    justify-content: center;
                    margin-top: 0.5rem;
                }
                .kill-btn { background-color: #e74c3c; }
                .heal-btn { background-color: #2ecc71; }
                .peek-btn { background-color: #3498db; }
                .vote-btn { background-color: #f39c12; color: black; }
                .vote-target-btn { background-color: #9b59b6; }
                
                .death-announcement {
                    background: #c0392b;
                    padding: 1rem;
                    border-radius: 8px;
                    text-align: center;
                    animation: fadeIn 1s;
                }
                .death-announcement.good-news {
                    background: #27ae60;
                }
                .vote-phase-btn {
                    display: flex;
                    align-items: center;
                    gap: 0.5rem;
                    background-color: #f39c12;
                    color: black;
                    font-weight: bold;
                }
                .action-note {
                    font-size: 0.8rem;
                    color: #777;
                    font-style: italic;
                }
                .voted-badge {
                    background: #2ecc71;
                    color: white;
                    padding: 0.2rem 0.5rem;
                    border-radius: 4px;
                    font-size: 0.7rem;
                    float: right;
                }
                .card-header {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                }
                .cancel-btn {
                    background-color: #95a5a6;
                }
                .results-screen {
                    text-align: center;
                    margin: 20px 0;
                    padding: 20px;
                    background: #2c3e50;
                    border-radius: 8px;
                    border: 1px solid #34495e;
                }
                .continue-btn {
                    background: #3498db;
                    color: white;
                    border: none;
                    padding: 12px 24px;
                    border-radius: 4px;
                    font-size: 1.1rem;
                    font-weight: bold;
                    cursor: pointer;
                    margin-top: 15px;
                    display: inline-flex;
                    align-items: center;
                    gap: 8px;
                }
                .continue-btn:hover {
                    background: #2980b9;
                }
            `}</style>
        </div>
    );
}
