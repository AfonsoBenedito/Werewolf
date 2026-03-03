import { useState, useEffect, useCallback } from 'react';
import { useParams } from 'react-router-dom';
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

    const token = sessionStorage.getItem('werewolf_token');
    const playerName = sessionStorage.getItem('werewolf_player');

    const fetchState = useCallback(async () => {
        if (!gameId) {
            setIsLoading(false);
            return;
        }
        try {
            const data = await getGameState(gameId, token || undefined);
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
    }, [gameId, token, playerName]);

    useEffect(() => {
        if (!gameId) return;

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
            debug: () => { }
        });

        client.activate();

        return () => {
            client.deactivate();
        };
    }, [gameId, fetchState]);

    useEffect(() => {
        if (!gameState) return;

        if (gameState.phase !== lastPhase) {
            setHasVotedReady(false);
            setMyNightTarget(null);
            setNightActionFeedback(null);
            setSeerResult(null);
        }

        setLastPhase(gameState.phase);
    }, [gameState, lastPhase]);

    const handleStart = async () => {
        if (!gameId) return;
        await startGame(gameId);
        fetchState();
    };

    const handleAction = async (actionType: string, targetId?: string) => {
        if (!gameId || !token) return;

        if (actionType === 'READY_TO_VOTE') {
            setHasVotedReady(true);
        }

        if (['KILL', 'HEAL', 'PEEK'].includes(actionType) && targetId) {
            setMyNightTarget(targetId);
            setNightActionFeedback(null);
        }

        try {
            const response = await performAction(gameId, token, actionType, targetId);
            if (typeof response === 'object' && response && response.peekResult) {
                setSeerResult(response.peekResult);
            } else if (typeof response === 'string' && response.length > 0) {
                setNightActionFeedback(response);
            } else {
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
