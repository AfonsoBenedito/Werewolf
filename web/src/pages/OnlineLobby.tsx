import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createGame, joinGame } from '../api/gameApi';

export default function OnlineLobby() {
    const navigate = useNavigate();
    const [name, setName] = useState('');
    const [joinId, setJoinId] = useState('');
    const [error, setError] = useState('');

    const handleHost = async () => {
        if (!name) return setError("Name is required");
        try {
            const data = await createGame('ONLINE', name);
            // Save playerId/name to local storage or context if needed, 
            // but for now we just pass it via URL or assume simple session
            localStorage.setItem('werewolf_player', name);
            navigate(`/online/game/${data.gameId}`);
        } catch (e) {
            setError("Failed to create game");
        }
    };

    const handleJoin = async () => {
        if (!name || !joinId) return setError("Name and Game ID required");
        try {
            await joinGame(joinId, name);
            localStorage.setItem('werewolf_player', name);
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
                <input placeholder="Your Name" value={name} onChange={e => setName(e.target.value)} />
                <button onClick={handleHost}>Create & Host</button>
            </div>

            <div className="divider">OR</div>

            <div className="lobby-section">
                <h2>Join Game</h2>
                <input placeholder="Your Name" value={name} onChange={e => setName(e.target.value)} />
                <input placeholder="Game ID" value={joinId} onChange={e => setJoinId(e.target.value)} />
                <button onClick={handleJoin}>Join Game</button>
            </div>

            <style>{`
                .online-lobby {
                    max-width: 400px;
                    margin: 0 auto;
                    display: flex;
                    flex-direction: column;
                    gap: 2rem;
                }
                .lobby-section {
                    display: flex;
                    flex-direction: column;
                    gap: 1rem;
                    background: #333;
                    padding: 1.5rem;
                    border-radius: 8px;
                }
                .divider {
                    font-weight: bold;
                    color: #666;
                }
                .error { color: #ff6b6b; }
            `}</style>
        </div>
    );
}
