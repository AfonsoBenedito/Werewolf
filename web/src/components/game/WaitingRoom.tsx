import { Play } from 'lucide-react';
import type { Player } from '../../hooks/useOnlineGame';

interface WaitingRoomProps {
    players: Player[];
    isHost: boolean;
    onStart: () => void;
}

export function WaitingRoom({ players, isHost, onStart }: WaitingRoomProps) {
    return (
        <div className="waiting-room">
            <h3>Waiting for players...</h3>
            <div className="player-list">
                {players.map(p => (
                    <div key={p.name} className="player-pill">{p.name}</div>
                ))}
            </div>
            {isHost && players.length >= 4 && (
                <button onClick={onStart}><Play size={16} /> Start Game</button>
            )}
            {isHost && players.length < 4 && <p>Need 4+ players to start</p>}
        </div>
    );
}
