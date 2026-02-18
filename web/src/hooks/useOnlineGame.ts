import { useState, useEffect, useCallback } from 'react';
import { useParams } from 'react-router-dom';
// @ts-ignore
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import { getGameState, performAction, startGame } from '../api/gameApi';
import type { Player, GameState } from '../types/game';

export function useOnlineGame() {
    const { gameId } = useParams<{ gameId: string }>();
    const [gameState, setGameState] = useState<GameState | null>(null);
    const [myPlayer, setMyPlayer] = useState<Player | null>(null);
    const [hasVotedReady, setHasVotedReady] = useState(false);
    const [lastPhase, setLastPhase] = useState<string>('');
    const [isConnected, setIsConnected] = useState(false);
    const [seerResult, setSeerResult] = useState<string | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const [myNightTarget, setMyNightTarget] = useState<string | null>(null);
    const [nightActionFeedback, setNightActionFeedback] = useState<string | null>(null);

    const playerName = localStorage.getItem('werewolf_player');

    const fetchState = useCallback(async () => {
        if (!gameId) {
            setIsLoading(false);
            return;
        }
        try {
            const data = await getGameState(gameId, playerName || undefined);
            if (data) {
                setGameState(data);
                if (playerName) {
                    const me = data.players.find((p: Player) => p.name === playerName);
                    setMyPlayer(me || null);
                }
                setError(null);
            } else {
                setError("Game not found");
                setGameState(null);
            }
        } catch (e) {
            console.error(e);
            setError("Failed to load game");
        } finally {
            setIsLoading(false);
        }
    }, [gameId, playerName]);

    useEffect(() => {
        if (!gameId) return;

        // Initial fetch
        fetchState();

        const socket = new SockJS('/ws');
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
            debug: (_str: string) => { }
        });

        client.activate();

        return () => {
            client.deactivate();
        };
    }, [gameId, fetchState]);

    // Handle Phase Transitions & Alerts
    useEffect(() => {
        if (!gameState) return;

        // Reset local ready state when phase changes
        if (gameState.phase !== lastPhase) {
            setHasVotedReady(false);
            setMyNightTarget(null);
            setNightActionFeedback(null);
            setSeerResult(null);
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

        // Track local night vote (for Wolves, Medic, Seer)
        if (['KILL', 'HEAL', 'PEEK'].includes(actionType) && targetId) {
            setMyNightTarget(targetId);
            setNightActionFeedback(null); // Clear previous feedback on new attempt
        }

        try {
            const response = await performAction(gameId, playerName, actionType, targetId);
            if (response && response.peekResult) {
                setSeerResult(response.peekResult);
            } else if (typeof response === 'string' && response.length > 0) {
                // Handle Wolf consensus messages or other feedback
                setNightActionFeedback(response);
            } else {
                // Success - silence
                setNightActionFeedback(null);
            }
        } catch (e: unknown) {
            const err = e as { response?: { data?: { message?: string } }; message?: string };
            const msg = err.response?.data?.message || err.message || "Action failed";
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
        myNightTarget,
        nightActionFeedback,
        seerResult,
        isLoading,
        error,
        fetchState,
        handleStart,
        handleAction
    };
}
