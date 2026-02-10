import { Play } from 'lucide-react';

interface OfflineSetupProps {
    newPlayerName: string;
    setNewPlayerName: (name: string) => void;
    currentPlayers: string[];
    onAddPlayer: () => void;
    onStartGame: () => void;
    loading: boolean;
}

export function OfflineSetup({
    newPlayerName,
    setNewPlayerName,
    currentPlayers,
    onAddPlayer,
    onStartGame,
    loading
}: OfflineSetupProps) {
    return (
        <div className="offline-game">
            <h1>Offline Mode (Setup)</h1>
            <div className="setup-section">
                <div className="input-group">
                    <input
                        value={newPlayerName}
                        onChange={(e) => setNewPlayerName(e.target.value)}
                        placeholder="Player Name"
                        onKeyDown={(e) => e.key === 'Enter' && onAddPlayer()}
                    />
                    <button onClick={onAddPlayer}>Add Player</button>
                </div>
                <div className="player-list">
                    {currentPlayers.map(p => (
                        <div key={p} className="player-item">
                            <span>{p}</span>
                        </div>
                    ))}
                </div>
                {currentPlayers.length >= 4 && (
                    <button className="start-btn" onClick={onStartGame} disabled={loading}>
                        <Play size={16} /> {loading ? "Starting..." : "Start Game"}
                    </button>
                )}
            </div>
        </div>
    );
}
