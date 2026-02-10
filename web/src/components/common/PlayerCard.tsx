import { Skull, CheckSquare } from 'lucide-react';
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

    return (
        <div
            className={`player-card ${!isAlive ? 'dead' : ''} ${isDisabled ? 'disabled-card' : ''} ${isActiveVoter ? 'active-voter' : ''} ${hasVoted ? 'has-voted' : ''} ${isVoteTarget ? 'vote-target' : ''}`}
            onClick={onClick}
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
                <div className="revealed-role">({revealedRole})</div>
            )}

            {!isAlive && <div className="dead-tag">DEAD</div>}

            {/* Action Buttons (Mostly for Offline or specific Online desktop layouts) */}
            {isAlive && (
                <div className="card-actions">
                    {showKill && (
                        <button className="icon-btn kill-btn" onClick={(e) => { e.stopPropagation(); onKill?.(); }} title="Kill Player">
                            <Skull size={16} /> Kill
                        </button>
                    )}
                    {showHeal && (
                        <button className="icon-btn heal-btn" onClick={(e) => { e.stopPropagation(); onHeal?.(); }} title="Heal Player">
                            ❤️ Heal
                        </button>
                    )}
                    {showPeek && (
                        <button className="icon-btn peek-btn" onClick={(e) => { e.stopPropagation(); onPeek?.(); }} title="Peek Player">
                            👁️ Peek
                        </button>
                    )}
                    {showVoteSelect && (
                        <button className="icon-btn vote-btn" onClick={(e) => { e.stopPropagation(); onVoteSelect?.(); }} title="Cast Vote">
                            <CheckSquare size={16} /> Vote
                        </button>
                    )}
                    {showVoteTarget && (
                        <button className="icon-btn vote-target-btn" onClick={(e) => { e.stopPropagation(); onVoteTarget?.(); }} title="Vote For">
                            Vote For
                        </button>
                    )}
                </div>
            )}
        </div>
    );
}
