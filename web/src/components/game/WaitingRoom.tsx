import { Play } from 'lucide-react';
import type { Player } from '../../types/game';

interface WaitingRoomProps {
    players: Player[];
    isHost: boolean;
    canStart: boolean;
    minPlayers: number;
    onStart: () => void;
}

export function WaitingRoom({ players, isHost, canStart, minPlayers, onStart }: WaitingRoomProps) {
    return (
        <div className="waiting-room">
            <h3>Waiting for players...</h3>
            <div className="player-list">
                {players.map(p => (
                    <div key={p.name} className="player-pill">{p.name}</div>
                ))}
            </div>
            {canStart && (
                <button className="start-btn" onClick={onStart}><Play size={16} /> Start Game</button>
            )}
            {isHost && !canStart && <p className="wait-msg">Need {minPlayers}+ players to start</p>}
        </div>
    );
}
