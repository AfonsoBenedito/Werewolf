interface DayVotingProps {
    onAction: (actionType: string, targetId?: string) => void;
}

export function DayVoting({ onAction }: DayVotingProps) {
    return (
        <div className="voting-controls">
            <p>Select a player to VOTE to eliminate:</p>
            <button className="skip-btn" onClick={() => onAction('SKIP')}>Abstain (Skip Vote)</button>
        </div>
    );
}
