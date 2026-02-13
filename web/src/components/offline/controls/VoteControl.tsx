interface VoteControlProps {
    activeVoter: string | null;
    onCancelVote: () => void;
    onAbstain: () => void;
}

export function VoteControl({ activeVoter, onCancelVote, onAbstain }: VoteControlProps) {
    if (!activeVoter) return null;

    return (
        <div className="vote-controls">
            <button className="abstain-btn" onClick={onAbstain}>
                Abstain from Voting
            </button>
            <button className="cancel-btn" onClick={onCancelVote}>
                Cancel Selection
            </button>
        </div>
    );
}
