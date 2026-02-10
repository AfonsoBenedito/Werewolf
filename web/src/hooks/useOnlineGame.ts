import { useState, useEffect, useCallback } from 'react';
import { useParams } from 'react-router-dom';
// @ts-ignore
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import { getGameState, performAction, startGame } from '../api/gameApi';

export interface Player {
    id: string; // name
    name: string;
    isAlive: boolean;
    role: string;
}

export interface GameState {
    id: string;
    players: Player[];
    phase: string;
    status: string;
    dayCount: number;
    winner?: string;
    lastDeadPlayerName?: string;
    votes?: Record<string, string>; // voter -> target (or "SECRET"/"ABSTAIN")
}

export function useOnlineGame() {
    const { gameId } = useParams<{ gameId: string }>();
    const [gameState, setGameState] = useState<GameState | null>(null);
    const [myPlayer, setMyPlayer] = useState<Player | null>(null);
    const [hasVotedReady, setHasVotedReady] = useState(false);
    const [lastPhase, setLastPhase] = useState<string>('');
    const [isConnected, setIsConnected] = useState(false);

    const playerName = localStorage.getItem('werewolf_player');

    const fetchState = useCallback(async () => {
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
    }, [gameId, playerName]);

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
    }, [gameId, fetchState]);

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
    }, [gameState, lastPhase, myPlayer?.name, playerName]);

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

    return {
        gameId,
        gameState,
        myPlayer,
        playerName,
        isConnected,
        hasVotedReady,
        fetchState,
        handleStart,
        handleAction
    };
}
