import { Home } from 'lucide-react';
import '../../styles/components/common/WinnerAnnouncement.css';

interface PlayerInfo {
    id: string;
    name: string;
    isAlive: boolean;
    role: string;
}

interface WinnerAnnouncementProps {
    winner: string;
    players: PlayerInfo[];
    onGoHome: () => void;
}

function getRoleClass(role: string): string {
    const r = role.toLowerCase();
    if (r === 'werewolf') return 'role-werewolf';
    if (r === 'seer') return 'role-seer';
    if (r === 'medic') return 'role-medic';
    return 'role-villager';
}

function isOnWinningTeam(player: PlayerInfo, winner: string): boolean {
    if (winner === 'WEREWOLVES') return player.role === 'Werewolf';
    return player.role !== 'Werewolf';
}

export function WinnerAnnouncement({ winner, players, onGoHome }: WinnerAnnouncementProps) {
    const displayWinner = winner === 'WEREWOLVES' ? 'Werewolves' : 'Villagers';

    return (
        <div className="winner-screen">
            <div className="winner-trophy">🏆</div>
            <h1 className="winner-title">{displayWinner} Win</h1>
            <p className="winner-subtitle">The game has ended</p>

            <div className="winner-roster">
                {players.map(player => (
                    <div
                        key={player.id}
                        className={`winner-roster-player ${isOnWinningTeam(player, winner) ? 'is-winner' : ''} ${!player.isAlive ? 'is-dead' : ''}`}
                    >
                        <span className="roster-name">
                            {player.name} {!player.isAlive ? '💀' : ''}
                        </span>
                        <span className={`roster-role ${getRoleClass(player.role)}`}>
                            {player.role}
                        </span>
                    </div>
                ))}
            </div>

            <button className="winner-home-btn" onClick={onGoHome}>
                <Home size={18} />
                Back to Home
            </button>
        </div>
    );
}
