interface VotingResultsProps {
    lastDeadPlayerName?: string;
    onContinue: () => void;
}

export function VotingResults({ lastDeadPlayerName, onContinue }: VotingResultsProps) {
    return (
        <div className="results-screen">
            <div className={`death-announcement ${!lastDeadPlayerName ? 'good-news' : ''}`}>
                <h3>🗳️ Voting Result</h3>
                <p>
                    {lastDeadPlayerName
                        ? `${lastDeadPlayerName} was eliminated by the village.`
                        : "The village could not agree. No one was eliminated."
                    }
                </p>
            </div>
            <button className="continue-btn" onClick={onContinue}>
                🌙 Continue to Night
            </button>
        </div>
    );
}
