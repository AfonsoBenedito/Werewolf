import React from 'react';
import { Skull, CheckSquare } from 'lucide-react';

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

export const PlayerCard: React.FC<PlayerCardProps> = ({
    name, isAlive, isMe, revealedRole, hasVoted, isVotingPhase,
    isActiveVoter, isDisabled, isVoteTarget, onClick,
    onKill, onHeal, onPeek, onVoteSelect, onVoteTarget,
    showKill, showHeal, showPeek, showVoteSelect, showVoteTarget
}) => {

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

            <style>{`
                .player-card {
                    background: #333;
                    padding: 1rem;
                    border-radius: 8px;
                    border: 1px solid #444;
                    position: relative;
                    cursor: pointer;
                    transition: transform 0.2s;
                }
                .player-card:hover:not(.disabled-card) { transform: scale(1.02); background: #3a3a3a; }
                .player-card.dead { opacity: 0.5; filter: grayscale(1); border-color: red; cursor: default; }
                .player-card.disabled-card { opacity: 0.6; cursor: not-allowed; transform: none; box-shadow: none; border-color: #555; }
                .player-card.active-voter { border-color: #f39c12; box-shadow: 0 0 10px #f39c12; }
                .player-card.has-voted { border-color: #2ecc71; background: #25412e; }
                .player-card.vote-target { border: 2px solid #e74c3c; box-shadow: 0 0 10px #e74c3c; }
                
                .card-header { display: flex; align-items: center; gap: 10px; margin-bottom: 0.5rem; }
                .avatar { width: 32px; height: 32px; background: #555; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: bold; flex-shrink: 0; }
                .name-container h3 { margin: 0; font-size: 1rem; }
                .me-tag { font-size: 0.8rem; color: #aaa; font-weight: normal; }
                .revealed-role { color: #f39c12; font-size: 0.8rem; font-weight: bold; margin-bottom: 5px; }
                .dead-tag { color: red; font-weight: bold; font-size: 0.8rem; }
                
                /* Action Buttons */
                .card-actions { display: flex; flex-direction: column; gap: 5px; margin-top: 10px; }
                .icon-btn { display: flex; align-items: center; justify-content: center; gap: 5px; padding: 5px; border: none; border-radius: 4px; cursor: pointer; color: white; width: 100%; font-size: 0.9rem; }
                .kill-btn { background-color: #e74c3c; }
                .heal-btn { background-color: #2ecc71; }
                .peek-btn { background-color: #3498db; }
                .vote-btn { background-color: #f39c12; color: black; }
                .vote-target-btn { background-color: #9b59b6; }
                
                .voted-badge { background: #f1c40f; color: #000; font-size: 0.7rem; padding: 2px 5px; border-radius: 4px; font-weight: bold; margin-left: auto; }
            `}</style>
        </div>
    );
};
