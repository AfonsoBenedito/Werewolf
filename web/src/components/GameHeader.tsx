import React from 'react';
import { WifiOff } from 'lucide-react';

interface GameHeaderProps {
    gameId: string;
    phase: string;
    dayCount: number;
    isConnected?: boolean; // Online only
    showConnectionStatus?: boolean;
}

export const GameHeader: React.FC<GameHeaderProps> = ({
    gameId, phase, dayCount, isConnected = true, showConnectionStatus = false
}) => {
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

            <style>{`
                .game-header {
                    margin-bottom: 2rem;
                    background: #2a2a2a;
                    padding: 1rem;
                    border-radius: 8px;
                    border-bottom: 2px solid #646cff;
                }
                .header-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem; }
                .header-top h2 { margin: 0; font-size: 1.5rem; }
                .phase-container { display: flex; gap: 1rem; align-items: center; }
                .status-badge { background: #646cff; padding: 0.2rem 0.6rem; border-radius: 4px; font-weight: bold; color: white; }
                .day-count { color: #aaa; font-weight: bold; }
                .connection-alert { animation: pulse 1s infinite; }
                @keyframes pulse { 0% { opacity: 1; } 50% { opacity: 0.5; } 100% { opacity: 1; } }
            `}</style>
        </div>
    );
};
