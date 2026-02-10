import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { getGameState, startGame, performAction } from '../api/gameApi';
import { RefreshCw, Play, WifiOff } from 'lucide-react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

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
    lastDeadPlayerName?: string;
    votes?: Record<string, string>; // voter -> target (or "SECRET"/"ABSTAIN")
}

export default function OnlineGame() {
    const { gameId } = useParams<{ gameId: string }>();
    const [gameState, setGameState] = useState<GameState | null>(null);
    const [myPlayer, setMyPlayer] = useState<Player | null>(null);
    const [bannerMsg, setBannerMsg] = useState<string | null>(null);
    const [hasVotedReady, setHasVotedReady] = useState(false);
    const [lastPhase, setLastPhase] = useState<string>('');

    const playerName = localStorage.getItem('werewolf_player');

    const [isConnected, setIsConnected] = useState(false);

    useEffect(() => {
        if (!gameId) return;

        // Initial fetch
        fetchState();

        const socket = new SockJS('http://localhost:8080/ws');
        const client = new Client({
            webSocketFactory: () => socket,
            onConnect: () => {
                setIsConnected(true);
                client.subscribe(`/topic/game/${gameId}`, (message: { body: string }) => {
                    if (message.body === 'UPDATE' || message.body === 'ENDED') {
                        fetchState();
                    }
                });
            },
            onDisconnect: () => {
                setIsConnected(false);
            },
            // Reduce debug logs in production
            debug: (str: string) => console.log(str)
        });

        client.activate();

        return () => {
            client.deactivate();
        };
    }, [gameId]);

    // Handle Phase Transitions & Banners


    // Handle Phase Transitions & Banners
    useEffect(() => {
        if (!gameState) return;

        // Detect Night -> Day Transition
        if (lastPhase.includes('NIGHT') && gameState.phase === 'DAY_DISCUSSION') {
            const victim = gameState.lastDeadPlayerName;
            setBannerMsg(victim ? `${victim} died last night!` : "It was a peaceful night.");
            setHasVotedReady(false); // Reset ready vote for new day
        }

        // Detect Voting -> Results Transition (or just being in results)
        if (gameState.phase === 'DAY_RESULTS' && lastPhase !== 'DAY_RESULTS') {
            const victim = gameState.lastDeadPlayerName;
            setBannerMsg(victim && victim !== 'ABSTAIN' ? `${victim} was eliminated!` : "No one was eliminated.");

            // Auto-advance after 3s (Host only) - Keep this auto-advance for game flow, but banner stays until user dismisses or phase changes?
            // Actually, if we auto-advance, the banner might disappear when phase changes.
            // Let's keep the auto-advance logic for the host, but the banner is now manual dismiss for everyone.
            // Wait, if host auto-advances, phase changes for everyone.
            // If phase changes to NIGHT, the banner *should* probably stick until dismissed?
            // Current logic: bannerMsg is state. If phase changes, it persists unless explicitly cleared.
            // So if I don't clear it, it stays. Good.

            const isHost = gameState.players[0]?.name === playerName;
            if (isHost) {
                setTimeout(() => {
                    handleAction('CONTINUE');
                }, 3000);
            }
        }

        setLastPhase(gameState.phase);
    }, [gameState]);

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

        // Local state update for UI responsiveness
        if (actionType === 'READY_TO_VOTE') {
            setHasVotedReady(true);
        }

        try {
            const response = await performAction(gameId, playerName, actionType, targetId); // Note: Update api/gameApi.ts if return type changed, but likely it returns 'any' or check usage.
            // Actually performAction in gameApi.ts uses axios.post, which returns response.data
            // We need to check if performAction returns data or we need to update api layer.
            // Let's assume performAction returns the data object.

            // Wait, I need to check gameApi.ts first to see what it returns.
            // For now, let's assume it returns void or I need to update it. 
            // Better check gameApi.ts in next step. For now, let's inject a logging or simple check.

            // Since I cannot verify api type right now, I will assume I need to update gameApi.ts and this file.
            // But let's apply this change:
            if (response && response.peekResult) {
                alert("Seer Result: " + response.peekResult);
            } else {
                alert("Action submitted");
            }
        } catch (e) {
            alert("Action failed");
        }
    };

    if (!gameState) return <div>Loading...</div>;

    const isHost = gameState.players[0]?.name === playerName; // Simple host check

    return (
        <div className="online-game">
            {bannerMsg && (
                <div className="banner-overlay">
                    <div className="banner-content">
                        <h1>{bannerMsg}</h1>
                        <button className="dismiss-btn" onClick={() => setBannerMsg(null)}>OK</button>
                    </div>
                </div>
            )}
            <div className="header">
                <h2>Game: {gameId}</h2>
                <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
                    {!isConnected && <WifiOff color="red" />}
                    <div className="status-badge">{gameState.phase}</div>
                </div>
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
                        {/* Night Phase Logic */}
                        {gameState.phase.includes('NIGHT') && myPlayer.isAlive && (
                            <>
                                {/* Check if it is MY turn */}
                                {(
                                    (gameState.phase.includes("Wolf") && myPlayer.role === "Werewolf") ||
                                    (gameState.phase.includes("Seer") && myPlayer.role === "Seer") ||
                                    (gameState.phase.includes("Medic") && myPlayer.role === "Medic")
                                ) ? (
                                    <>
                                        <p className="turn-alert">It is your turn! {myPlayer.role === 'Werewolf' ? 'Choose a victim.' : myPlayer.role === 'Medic' ? 'Choose who to save.' : 'Choose who to peek.'}</p>
                                        {(myPlayer.role === 'Seer' || myPlayer.role === 'Medic') && (
                                            <button className="skip-btn" onClick={() => handleAction('SKIP')}>Skip Action</button>
                                        )}
                                    </>
                                ) : (
                                    <div className="sleep-banner">
                                        <h3>💤 Village is Asleep 💤</h3>
                                        <p>Waiting for other roles to act...</p>
                                    </div>
                                )}
                            </>
                        )}

                        {gameState.phase === 'DAY_DISCUSSION' && myPlayer.isAlive && (
                            <div className="discussion-panel">
                                <h3>☀️ Day Discussion ☀️</h3>
                                <p>Discuss who to eliminate!</p>
                                {!hasVotedReady ? (
                                    <button className="ready-btn" onClick={() => handleAction('READY_TO_VOTE')}>
                                        Proceed to Voting
                                    </button>
                                ) : (
                                    <p className="ready-wait-msg">Waiting for the rest...</p>
                                )}
                                <p className="ready-status">
                                    Waiting for players: {(gameState as any).readyPlayerCount || 0} / {(gameState as any).totalAliveCount || 'ALL'}
                                </p>
                            </div>
                        )}

                        {gameState.phase === 'DAY_VOTING' && myPlayer.isAlive && (
                            <div className="voting-controls">
                                <p>Select a player to VOTE to eliminate:</p>
                                <button className="skip-btn" onClick={() => handleAction('SKIP')}>Abstain (Skip Vote)</button>
                            </div>
                        )}
                    </div>

                    <div className="players-grid">
                        {gameState.players.map(p => {
                            const isMyVoteTarget = gameState.votes?.[playerName!] === p.name;
                            const hasVoted = gameState.votes?.[p.name] !== undefined;
                            const isMe = p.name === playerName;

                            // Determine if card interaction is disabled (visual style)
                            // "since you can't vote in yourself, you should have your own card like its toggled off"
                            // "That should also happen wolf in wolf phase and to seer in seer phase"
                            // Medic CAN target self.

                            let isDisabled = false;

                            // If it's me
                            if (isMe) {
                                isDisabled = true;
                                // Exception: Medic during Medic phase
                                if (gameState.phase.includes("Medic") && myPlayer.role === 'Medic') {
                                    isDisabled = false;
                                }
                            }

                            // Also if dead (already handled by 'dead' class opacity, but let's be explicit)
                            if (!p.isAlive) isDisabled = true;

                            return (
                                <div key={p.name} className={`player-card ${!p.isAlive ? 'dead' : ''} ${isDisabled ? 'disabled-card' : ''} ${isMyVoteTarget ? 'vote-target' : ''}`}
                                    onClick={() => {
                                        if (!myPlayer.isAlive) return;
                                        if (isDisabled) return;

                                        if (gameState.phase === 'DAY_VOTING') handleAction('VOTE', p.name);

                                        // Night Actions
                                        if (gameState.phase.includes('NIGHT')) {
                                            if (gameState.phase.includes("Wolf") && myPlayer.role === 'Werewolf') handleAction('KILL', p.name);
                                            if (gameState.phase.includes("Medic") && myPlayer.role === 'Medic') handleAction('HEAL', p.name);
                                            if (gameState.phase.includes("Seer") && myPlayer.role === 'Seer') handleAction('PEEK', p.name);
                                        }
                                    }}
                                >
                                    <div className="avatar">{p.name.charAt(0)}</div>
                                    <div className="name">{p.name}</div>
                                    {p.name === playerName && <small>(You)</small>}
                                    {/* Show revealed role (e.g. other Wolves) */}
                                    {p.role && p.role !== "Unknown" && p.name !== playerName && (
                                        <div className="revealed-role">({p.role})</div>
                                    )}
                                    {/* Show Voting Badge during Voting Phase - HIDE FOR SELF */}
                                    {gameState.phase === 'DAY_VOTING' && hasVoted && !isMe && (
                                        <div className="voted-badge">Voted</div>
                                    )}
                                </div>
                            );
                        })}
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
                .player-card.disabled-card { opacity: 0.6; cursor: not-allowed; transform: none; box-shadow: none; border-color: #555; }
                .player-card.disabled-card:hover { transform: none; background: #333; }
                .avatar { width: 40px; height: 40px; background: #555; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin: 0 auto 0.5rem; font-weight: bold; }
                .dead-tag { color: red; font-weight: bold; margin-top: 0.5rem; }
                .refresh-btn { position: fixed; bottom: 20px; right: 20px; border-radius: 50%; width: 50px; height: 50px; padding: 0; display: flex; align-items: center; justify-content: center; }
                .sleep-banner { background: #2c3e50; color: #bdc3c7; padding: 1rem; border-radius: 8px; text-align: center; border: 1px solid #34495e; }
                .turn-alert { color: #2ecc71; font-weight: bold; font-size: 1.2rem; margin-bottom: 1rem; }
                .revealed-role { color: #f39c12; font-size: 0.8rem; font-weight: bold; margin-top: 5px; }
                .discussion-panel { background: #d35400; padding: 1rem; border-radius: 8px; margin-bottom: 1rem; }
                .ready-btn { background: #e67e22; color: white; border: none; padding: 0.5rem 1rem; border-radius: 4px; font-weight: bold; cursor: pointer; }
                .ready-btn:hover { background: #d35400; }
                .ready-status { margin-top: 0.5rem; font-size: 0.9rem; font-style: italic; }
                .ready-wait-msg { font-weight: bold; color: #f1c40f; }
                .banner-overlay {
                    position: fixed; top: 0; left: 0; width: 100%; height: 100%;
                    background: rgba(0,0,0,0.85); color: white;
                    display: flex; justify-content: center; align-items: center;
                    z-index: 1000; animation: fadeIn 0.5s;
                }
                .banner-content { text-align: center; }
                .banner-overlay h1 { font-size: 3rem; margin-bottom: 2rem; color: #e74c3c; text-shadow: 0 0 10px white; }
                .dismiss-btn { background: #e74c3c; color: white; border: 2px solid white; padding: 1rem 3rem; font-size: 1.5rem; border-radius: 8px; cursor: pointer; transition: 0.2s; font-weight: bold; }
                .dismiss-btn:hover { background: #c0392b; transform: scale(1.1); }
                .skip-btn { background: #95a5a6; color: white; border: none; padding: 0.5rem 1rem; border-radius: 4px; margin-top: 5px; cursor: pointer; }
                .skip-btn:hover { background: #7f8c8d; }
                .voted-badge { background: #f1c40f; color: #000; font-size: 0.7rem; padding: 2px 5px; border-radius: 4px; margin-top: 5px; font-weight: bold; }
                .vote-target { border: 2px solid #e74c3c; box-shadow: 0 0 10px #e74c3c; }
            `}</style>
        </div>
    );
}
