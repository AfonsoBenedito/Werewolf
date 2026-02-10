import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createGame, joinGame } from '../api/gameApi';
import '../styles/OnlineLobby.css';

export default function OnlineLobby() {
    const navigate = useNavigate();
    const [hostName, setHostName] = useState('');
    const [joinName, setJoinName] = useState('');
    const [joinId, setJoinId] = useState('');
    const [error, setError] = useState('');

    const handleHost = async () => {
        if (!hostName) return setError("Name is required");
        try {
            const data = await createGame('ONLINE', hostName);
            // Save playerId/name to local storage or context if needed, 
            // but for now we just pass it via URL or assume simple session
            localStorage.setItem('werewolf_player', hostName);
            navigate(`/online/game/${data.gameId}`);
        } catch (e) {
            setError("Failed to create game");
        }
    };

    const handleJoin = async () => {
        if (!joinName || !joinId) return setError("Name and Game ID required");
        try {
            await joinGame(joinId, joinName);
            localStorage.setItem('werewolf_player', joinName);
            navigate(`/online/game/${joinId}`);
        } catch (e) {
            setError("Failed to join game. Check ID or name uniqueness.");
        }
    };

    return (
        <div className="online-lobby">
            <h1>Online Lobby</h1>
            {error && <p className="error">{error}</p>}

            <div className="lobby-section">
                <h2>Host Game</h2>
                <input placeholder="Your Name" value={hostName} onChange={e => setHostName(e.target.value)} />
                <button onClick={handleHost}>Create & Host</button>
            </div>

            <div className="divider">OR</div>

            <div className="lobby-section">
                <h2>Join Game</h2>
                <input placeholder="Your Name" value={joinName} onChange={e => setJoinName(e.target.value)} />
                <input placeholder="Game ID" value={joinId} onChange={e => setJoinId(e.target.value)} />
                <button onClick={handleJoin}>Join Game</button>
            </div>


        </div>
    );
}
