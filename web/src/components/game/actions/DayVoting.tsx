interface DayVotingProps {
    hasAbstained: boolean;
    onAction: (actionType: string, targetId?: string) => void;
}

export function DayVoting({ hasAbstained, onAction }: DayVotingProps) {
    return (
        <div className="voting-controls">
            <p>Select a player to VOTE to eliminate:</p>
            {!hasAbstained && (
                <button className="skip-btn" onClick={() => onAction('SKIP')}>Abstain (Skip Vote)</button>
            )}
            {hasAbstained && (
                <p className="abstained-msg">You have abstained. Click a player to change vote.</p>
            )}
        </div>
    );
}
