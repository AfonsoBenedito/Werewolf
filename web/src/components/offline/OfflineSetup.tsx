import { Play, Plus, Users, X } from 'lucide-react';

interface OfflineSetupProps {
    newPlayerName: string;
    setNewPlayerName: (name: string) => void;
    currentPlayers: string[];
    onAddPlayer: () => void;
    onRemovePlayer: (name: string) => void;
    onStartGame: () => void;
    loading: boolean;
}

export function OfflineSetup({
    newPlayerName,
    setNewPlayerName,
    currentPlayers,
    onAddPlayer,
    onRemovePlayer,
    onStartGame,
    loading
}: OfflineSetupProps) {
    return (
        <div className="setup-section">
            <div className="input-group">
                <input
                    value={newPlayerName}
                    onChange={(e) => setNewPlayerName(e.target.value)}
                    placeholder="Enter player name..."
                    onKeyDown={(e) => e.key === 'Enter' && onAddPlayer()}
                />
                <button
                    className="btn-premium btn-premium--primary"
                    onClick={onAddPlayer}
                    disabled={!newPlayerName.trim()}
                >
                    <Plus size={18} />
                </button>
            </div>

            <div className="player-list-header">
                <Users size={14} />
                <span>Players ({currentPlayers.length})</span>
            </div>

            <div className="player-list">
                {currentPlayers.map(p => (
                    <div key={p} className="player-item">
                        <span>{p}</span>
                        <button
                            className="remove-player-btn"
                            onClick={() => onRemovePlayer(p)}
                            title="Remove player"
                        >
                            <X size={14} />
                        </button>
                    </div>
                ))}
            </div>

            {currentPlayers.length >= 4 && (
                <button
                    className="btn-premium btn-premium--primary"
                    style={{ width: '100%', marginTop: '0.5rem' }}
                    onClick={onStartGame}
                    disabled={loading}
                >
                    <Play size={18} />
                    {loading ? "Starting..." : "Begin Game"}
                </button>
            )}
        </div>
    );
}
