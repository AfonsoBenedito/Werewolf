import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { getGameState, startGame, performAction } from '../api/gameApi';
import { RefreshCw, Play } from 'lucide-react';

interface Player {
    id: string; // name
    name: string;
    isAlive: boolean;
    role: string;
}

interface GameState {
    id: string;
    players: Player[];
    phase: string;
    status: string;
    dayCount: number;
    winner?: string;
}

export default function OnlineGame() {
    const { gameId } = useParams<{ gameId: string }>();
    const [gameState, setGameState] = useState<GameState | null>(null);
    const [myPlayer, setMyPlayer] = useState<Player | null>(null);
    const playerName = localStorage.getItem('werewolf_player');

    useEffect(() => {
        if (gameId) {
            const interval = setInterval(fetchState, 2000); // Poll every 2s
            fetchState();
            return () => clearInterval(interval);
        }
    }, [gameId]);

    const fetchState = async () => {
        if (!gameId) return;
        try {
            const data = await getGameState(gameId, playerName || undefined);
            setGameState(data);
            if (playerName) {
                const me = data.players.find((p: Player) => p.name === playerName);
                setMyPlayer(me || null);
            }
        } catch (e) {
            console.error(e);
        }
    };

    const handleStart = async () => {
        if (!gameId) return;
        await startGame(gameId);
        fetchState();
    };

    const handleAction = async (actionType: string, targetId?: string) => {
        if (!gameId || !playerName) return;
        try {
            await performAction(gameId, playerName, actionType, targetId);
            alert("Action submitted");
        } catch (e) {
            alert("Action failed");
        }
    };

    if (!gameState) return <div>Loading...</div>;

    const isHost = gameState.players[0]?.name === playerName; // Simple host check

    return (
        <div className="online-game">
            <div className="header">
                <h2>Game: {gameId}</h2>
                <div className="status-badge">{gameState.phase}</div>
                <div>Day {gameState.dayCount}</div>
            </div>

            {gameState.winner && <h1 className="winner">Winner: {gameState.winner}</h1>}

            {gameState.status === 'NOT_STARTED' && (
                <div className="waiting-room">
                    <h3>Waiting for players...</h3>
                    <div className="player-list">
                        {gameState.players.map(p => (
                            <div key={p.name} className="player-pill">{p.name}</div>
                        ))}
                    </div>
                    {isHost && gameState.players.length >= 4 && (
                        <button onClick={handleStart}><Play size={16} /> Start Game</button>
                    )}
                    {isHost && gameState.players.length < 4 && <p>Need 4+ players to start</p>}
                </div>
            )}

            {gameState.status === 'IN_PROGRESS' && myPlayer && (
                <div className="game-board">
                    <div className="my-role-card">
                        <h3>You are: <span className="role-reveal">{myPlayer.role}</span></h3>
                        {!myPlayer.isAlive && <div className="dead-tag">YOU ARE DEAD</div>}
                    </div>

                    <div className="action-area">
                        {/* Render checks based on phase and role */}
                        {gameState.phase === 'DAY_VOTING' && myPlayer.isAlive && (
                            <p>Select a player to VOTE to eliminate:</p>
                        )}
                        {gameState.phase === 'NIGHT' && myPlayer.isAlive && (
                            <p>It is Night. {myPlayer.role === 'Wolf' ? 'Choose a victim.' : myPlayer.role === 'Medic' ? 'Choose who to save.' : myPlayer.role === 'Seer' ? 'Choose who to peek.' : 'Sleep tight.'}</p>
                        )}
                    </div>

                    <div className="players-grid">
                        {gameState.players.map(p => (
                            <div key={p.name} className={`player-card ${!p.isAlive ? 'dead' : ''}`}
                                onClick={() => {
                                    if (!myPlayer.isAlive || p.name === playerName) return;
                                    // Logic for click action based on phase/role
                                    if (gameState.phase === 'DAY_VOTING') handleAction('VOTE', p.name); // Actually we need ELIMINATE for offline, but online needs voting logic.
                                    // For now, let's just trigger simple actions mapping
                                    // Note: Backend 'handleNightAction' isn't fully implemented for Online yet in GameService, 
                                    // but we hook it up here for structure.
                                    if (gameState.phase === 'NIGHT') {
                                        if (myPlayer.role === 'Wolf') handleAction('KILL', p.name); // Using KILL action for wolf vote
                                        if (myPlayer.role === 'Medic') handleAction('HEAL', p.name);
                                        if (myPlayer.role === 'Seer') handleAction('PEEK', p.name);
                                    }
                                }}
                            >
                                <div className="avatar">{p.name.charAt(0)}</div>
                                <div className="name">{p.name}</div>
                                {p.name === playerName && <small>(You)</small>}
                            </div>
                        ))}
                    </div>
                </div>
            )}

            <button className="refresh-btn" onClick={fetchState}><RefreshCw size={16} /></button>

            <style>{`
                .online-game { max-width: 600px; margin: 0 auto; }
                .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; }
                .status-badge { background: #646cff; padding: 0.2rem 0.5rem; borderRadius: 4px; font-weight: bold; }
                .player-pill { background: #444; padding: 0.5rem; border-radius: 20px; display: inline-block; margin: 0.2rem; }
                .my-role-card { background: #2a2a2a; padding: 1rem; border: 1px solid #646cff; margin-bottom: 2rem; border-radius: 8px; }
                .role-reveal { font-size: 1.2em; font-weight: bold; color: #aaddff; }
                .players-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(100px, 1fr)); gap: 1rem; }
                .player-card { background: #333; padding: 1rem; border-radius: 8px; cursor: pointer; transition: transform 0.2s; }
                .player-card:hover { transform: scale(1.05); background: #444; }
                .player-card.dead { opacity: 0.5; filter: grayscale(1); cursor: default; }
                .avatar { width: 40px; height: 40px; background: #555; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin: 0 auto 0.5rem; font-weight: bold; }
                .dead-tag { color: red; font-weight: bold; margin-top: 0.5rem; }
                .refresh-btn { position: fixed; bottom: 20px; right: 20px; border-radius: 50%; width: 50px; height: 50px; padding: 0; display: flex; align-items: center; justify-content: center; }
            `}</style>
        </div>
    );
}
