import { WifiOff } from 'lucide-react';
import '../../styles/components/GameHeader.css';

interface GameHeaderProps {
    gameId: string;
    phase: string;
    dayCount: number;
    isConnected?: boolean; // Online only
    showConnectionStatus?: boolean;
}

export function GameHeader({
    gameId, phase, dayCount, isConnected = true, showConnectionStatus = false
}: GameHeaderProps) {
    return (
        <div className="game-header">
            <div className="header-top">
                <h2>Game: {gameId}</h2>
                {showConnectionStatus && !isConnected && (
                    <div className="connection-alert" title="Disconnected">
                        <WifiOff color="red" />
                    </div>
                )}
            </div>

            <div className="phase-container">
                <div className="status-badge">{phase}</div>
                <div className="day-count">Day {dayCount}</div>
            </div>
        </div>
    );
}
