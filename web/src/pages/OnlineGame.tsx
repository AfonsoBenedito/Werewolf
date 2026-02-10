import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
// @ts-ignore
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import { Play, RefreshCw } from 'lucide-react';
import { getGameState, performAction, startGame } from '../api/gameApi';
import { PlayerCard } from '../components/PlayerCard';
import { GameHeader } from '../components/GameHeader';
import '../styles/OnlineGame.css';

console.log("OnlineGame module evaluated");

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
    // const [bannerMsg, setBannerMsg] = useState<string | null>(null); // Removed banner state
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

    // ... existing imports ...

    // ... (inside the component)

    // Handle Phase Transitions & Alerts
    useEffect(() => {
        if (!gameState) return;

        // Detect Night -> Day Transition
        if (lastPhase.includes('NIGHT') && gameState.phase === 'DAY_DISCUSSION') {
            const victim = gameState.lastDeadPlayerName;
            if (victim) {
                if (victim === myPlayer?.name) {
                    alert("You died last night!");
                } else {
                    alert(`${victim} died last night!`);
                }
            } else {
                alert("It was a peaceful night.");
            }
            setHasVotedReady(false); // Reset ready vote for new day
        }

        // Detect Voting -> Results Transition (or just being in results)
        if (gameState.phase === 'DAY_RESULTS' && lastPhase !== 'DAY_RESULTS') {
            const victim = gameState.lastDeadPlayerName;

            if (victim) {
                if (victim === 'ABSTAIN') {
                    alert("No one was eliminated.");
                } else if (victim === myPlayer?.name) {
                    alert("You were eliminated!");
                } else {
                    alert(`${victim} was eliminated!`);
                }
            } else {
                alert("No one was eliminated.");
            }

            const isHost = gameState.players[0]?.name === playerName;
            if (isHost) {
                setTimeout(() => {
                    handleAction('CONTINUE');
                }, 1000); // Shorter delay since alert is blocking/acknowledged
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
            const response = await performAction(gameId, playerName, actionType, targetId);
            if (response && response.peekResult) {
                alert("Seer Result: " + response.peekResult);
            } else {
                // Success - silence
            }
        } catch (e: any) {
            const msg = e.response?.data?.message || e.message || "Action failed";
            alert("Action failed: " + msg);
        }
    };

    if (!gameState) return <div>Loading...</div>;

    const isHost = gameState.players[0]?.name === playerName; // Simple host check

    return (
        <div className="online-game">
            <GameHeader
                gameId={gameId || ''}
                phase={gameState.phase}
                dayCount={gameState.dayCount}
                isConnected={isConnected}
                showConnectionStatus={true}
            />

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
                        {/* ... (Existing Action Logic Kept Same for now, maybe componentize later if specific enough) ... */}
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

                            let isDisabled = false;
                            if (isMe) {
                                isDisabled = true;
                                if (gameState.phase.includes("Medic") && myPlayer.role === 'Medic') isDisabled = false;
                            }
                            if (!p.isAlive) isDisabled = true;

                            return (
                                <PlayerCard
                                    key={p.name}
                                    name={p.name}
                                    isAlive={p.isAlive}
                                    isMe={isMe}
                                    role={p.role}
                                    revealedRole={p.role} // Online: role is masked by backend
                                    hasVoted={hasVoted}
                                    isVotingPhase={gameState.phase === 'DAY_VOTING'}
                                    isDisabled={isDisabled}
                                    isVoteTarget={isMyVoteTarget}
                                    onClick={() => {
                                        if (!myPlayer.isAlive) return;
                                        if (isDisabled) return;

                                        if (gameState.phase === 'DAY_VOTING') handleAction('VOTE', p.name);

                                        if (gameState.phase.includes('NIGHT')) {
                                            if (gameState.phase.includes("Wolf") && myPlayer.role === 'Werewolf') handleAction('KILL', p.name);
                                            if (gameState.phase.includes("Medic") && myPlayer.role === 'Medic') handleAction('HEAL', p.name);
                                            if (gameState.phase.includes("Seer") && myPlayer.role === 'Seer') handleAction('PEEK', p.name);
                                        }
                                    }}
                                />
                            );
                        })}
                    </div>
                </div>
            )}

            <button className="refresh-btn" onClick={fetchState}><RefreshCw size={16} /></button>
        </div>
    );
}
