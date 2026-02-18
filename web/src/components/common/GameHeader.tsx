import { WifiOff } from 'lucide-react';
import '../../styles/components/common/GameHeader.css';

interface GameHeaderProps {
    gameId: string;
    dayCount: number;
    isConnected?: boolean; // Online only
    showConnectionStatus?: boolean;
    hideGameId?: boolean;
}

export function GameHeader({
    gameId, dayCount, isConnected = true, showConnectionStatus = false, hideGameId = false
}: GameHeaderProps) {
    return (
        <div className="game-header">
            <div className={`header-top ${hideGameId ? 'game-id-hidden' : ''}`}>
                <h2>Game ID: {gameId}</h2>
                {showConnectionStatus && !isConnected && (
                    <div className="connection-alert" title="Disconnected">
                        <WifiOff color="red" />
                    </div>
                )}
            </div>

            <div className="phase-container">
                {dayCount > 0 && <div className="day-count">Day {dayCount}</div>}
            </div>
        </div>
    );
}
