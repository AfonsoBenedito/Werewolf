import { Skull, Heart, Eye, Crosshair, Vote } from 'lucide-react';
import '../../styles/components/common/PlayerCard.css';

interface ActionIndicator {
    show: boolean;
    className: string;
    label: string;
    icon: React.ReactNode;
    onAction?: () => void;
}

interface PlayerCardProps {
    name: string;
    isAlive: boolean;
    isMe?: boolean;
    revealedRole?: string;
    hasVoted?: boolean;
    isVotingPhase?: boolean;
    isActiveVoter?: boolean;
    isDisabled?: boolean;
    isVoteTarget?: boolean;
    onKill?: () => void;
    onHeal?: () => void;
    onPeek?: () => void;
    onVoteSelect?: () => void;
    onVoteTarget?: () => void;
    showKill?: boolean;
    showHeal?: boolean;
    showPeek?: boolean;
    showVoteSelect?: boolean;
    showVoteTarget?: boolean;
}

export function PlayerCard({
    name, isAlive, isMe, revealedRole, hasVoted, isVotingPhase,
    isActiveVoter, isDisabled, isVoteTarget,
    onKill, onHeal, onPeek, onVoteSelect, onVoteTarget,
    showKill, showHeal, showPeek, showVoteSelect, showVoteTarget
}: PlayerCardProps) {

    const indicators: ActionIndicator[] = [
        { show: !!showKill, className: 'kill-indicator', label: 'Kill', icon: <Skull size={14} />, onAction: onKill },
        { show: !!showHeal, className: 'heal-indicator', label: 'Heal', icon: <Heart size={14} />, onAction: onHeal },
        { show: !!showPeek, className: 'peek-indicator', label: 'Peek', icon: <Eye size={14} />, onAction: onPeek },
        { show: !!showVoteTarget, className: 'vote-target-indicator', label: 'Vote', icon: <Crosshair size={14} />, onAction: onVoteTarget },
        { show: !!showVoteSelect && !hasVoted, className: 'vote-select-indicator', label: 'Select', icon: <Vote size={14} />, onAction: onVoteSelect },
    ];

    const activeIndicator = indicators.find(i => i.show);

    const handleCardClick = () => {
        if (isDisabled || !isAlive) return;
        activeIndicator?.onAction?.();
    };

    const cardClass = [
        'player-card',
        !isAlive && 'dead',
        isDisabled && 'disabled-card',
        isActiveVoter && 'active-voter',
        hasVoted && 'has-voted',
        isVoteTarget && 'vote-target',
    ].filter(Boolean).join(' ');

    return (
        <div className={cardClass} onClick={handleCardClick}>
            <div className="card-header">
                <div className="avatar">{name.charAt(0)}</div>
                <div className="name-container">
                    <h3>{name} {isMe && <span className="me-tag">(You)</span>}</h3>
                    {revealedRole && revealedRole !== "Unknown" && (
                        <div className="revealed-role">{revealedRole}</div>
                    )}
                </div>
                {hasVoted && isVotingPhase && !isMe && <span className="voted-badge">Voted</span>}
            </div>

            {!isAlive && <div className="dead-tag">DEAD</div>}

            {isAlive && indicators.some(i => i.show) && (
                <div className="card-indicators">
                    {indicators.filter(i => i.show).map(i => (
                        <div key={i.className} className={`indicator ${i.className}`} title={i.label}>
                            {i.icon}
                            <span>{i.label}</span>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}
