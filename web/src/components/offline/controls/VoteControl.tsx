interface VoteControlProps {
    activeVoter: string | null;
    onCancelVote: () => void;
}

export function VoteControl({ activeVoter, onCancelVote }: VoteControlProps) {
    if (!activeVoter) return null;

    return (
        <button className="cancel-btn" onClick={onCancelVote}>
            Cancel Vote Selection
        </button>
    );
}
