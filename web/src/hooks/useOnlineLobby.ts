import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createGame, joinGame } from '../api/gameApi';

export function useOnlineLobby() {
    const navigate = useNavigate();
    const [hostName, setHostName] = useState('');
    const [joinName, setJoinName] = useState('');
    const [joinId, setJoinId] = useState('');
    const [error, setError] = useState('');

    const handleHost = async () => {
        if (!hostName) return setError("Name is required");
        try {
            const data = await createGame('ONLINE', hostName);
            localStorage.setItem('werewolf_player', hostName);
            navigate(`/online/game/${data.gameId}`);
        } catch {
            setError("Failed to create game");
        }
    };

    const handleJoin = async () => {
        if (!joinName || !joinId) return setError("Name and Game ID required");
        try {
            await joinGame(joinId, joinName);
            localStorage.setItem('werewolf_player', joinName);
            navigate(`/online/game/${joinId}`);
        } catch {
            setError("Failed to join game. Check ID or name uniqueness.");
        }
    };

    return {
        hostName, setHostName,
        joinName, setJoinName,
        joinId, setJoinId,
        error, setError, // Exposed in case UI needs to clear it
        handleHost,
        handleJoin
    };
}
