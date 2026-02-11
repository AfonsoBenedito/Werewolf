import { WifiOff } from 'lucide-react';
import '../../styles/components/common/GameHeader.css';

interface GameHeaderProps {
    gameId: string;
    phase: string;
    dayCount: number;
    isConnected?: boolean; // Online only
    showConnectionStatus?: boolean;
}

export function GameHeader({
    gameId, phase: _phase, dayCount, isConnected = true, showConnectionStatus = false
}: GameHeaderProps) {
    return (
        <div className="game-header">
            <div className="header-top">
                <h2>Game ID: {gameId}</h2>
                {showConnectionStatus && !isConnected && (
                    <div className="connection-alert" title="Disconnected">
                        <WifiOff color="red" />
                    </div>
                )}
            </div>

            <div className="phase-container">
                {/* Phase hidden as per request */}
                {/* <div className="status-badge">{phase}</div> */}
                {dayCount > 0 && <div className="day-count">Day {dayCount}</div>}
            </div>
        </div>
    );
}
