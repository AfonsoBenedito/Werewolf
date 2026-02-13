import { Skull } from 'lucide-react';
import '../../styles/components/common/PlayerCard.css';

interface PlayerCardProps {
    name: string;
    isAlive: boolean;
    role?: string;
    isMe?: boolean;
    revealedRole?: string;
    hasVoted?: boolean;
    isVotingPhase?: boolean;
    isActiveVoter?: boolean; // Offline: currently selected voter
    isDisabled?: boolean;
    isVoteTarget?: boolean;
    onClick?: () => void;
    // Interaction Buttons (Offline mainly, or Online actions)
    onKill?: () => void;
    onHeal?: () => void;
    onPeek?: () => void;
    onVoteSelect?: () => void; // Offline: Select this player AS the voter
    onVoteTarget?: () => void; // Offline: Select this player AS the target

    // Context flags for button visibility
    showKill?: boolean;
    showHeal?: boolean;
    showPeek?: boolean;
    showVoteSelect?: boolean;
    showVoteTarget?: boolean;
}

export function PlayerCard({
    name, isAlive, isMe, revealedRole, hasVoted, isVotingPhase,
    isActiveVoter, isDisabled, isVoteTarget, onClick,
    onKill, onHeal, onPeek, onVoteSelect, onVoteTarget,
    showKill, showHeal, showPeek, showVoteSelect, showVoteTarget
}: PlayerCardProps) {

    const handleCardClick = () => {
        if (isDisabled || !isAlive) return;

        // Instant Submit Logic: trigger the primary available action
        if (showKill) onKill?.();
        else if (showHeal) onHeal?.();
        else if (showPeek) onPeek?.();
        else if (showVoteTarget) onVoteTarget?.();
        else if (showVoteSelect) onVoteSelect?.();
        else onClick?.();
    };

    return (
        <div
            className={`player-card ${!isAlive ? 'dead' : ''} ${isDisabled ? 'disabled-card' : ''} ${isActiveVoter ? 'active-voter' : ''} ${hasVoted ? 'has-voted' : ''} ${isVoteTarget ? 'vote-target' : ''}`}
            onClick={handleCardClick}
        >
            <div className="card-header">
                <div className="avatar">{name.charAt(0)}</div>
                <div className="name-container">
                    <h3>{name} {isMe && <span className="me-tag">(You)</span>}</h3>
                </div>
                {hasVoted && isVotingPhase && !isMe && <span className="voted-badge">Voted</span>}
            </div>

            {/* Revealed Role (e.g. Wolf pack, or Offline reveal) */}
            {revealedRole && revealedRole !== "Unknown" && (
                <div className="revealed-role">{revealedRole}</div>
            )}

            {!isAlive && <div className="dead-tag">DEAD</div>}

            {/* Visual Action Indicators (Minimalist icons + text labels) */}
            {isAlive && (
                <div className="card-indicators">
                    {showKill && (
                        <div className="indicator kill-indicator" title="Kill Target">
                            <Skull size={14} />
                            <span>Kill</span>
                        </div>
                    )}
                    {showHeal && (
                        <div className="indicator heal-indicator" title="Heal Target">
                            <span>❤️ Heal</span>
                        </div>
                    )}
                    {showPeek && (
                        <div className="indicator peek-indicator" title="Peek Target">
                            <span>👁️ Peek</span>
                        </div>
                    )}
                    {showVoteTarget && (
                        <div className="indicator vote-target-indicator" title="Vote Target">
                            <span>🎯 Vote</span>
                        </div>
                    )}
                    {showVoteSelect && !hasVoted && (
                        <div className="indicator vote-select-indicator" title="Will Vote">
                            <span>🗳️ Select</span>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
}
