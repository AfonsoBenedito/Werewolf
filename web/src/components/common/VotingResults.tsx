import '../../styles/components/common/VotingResults.css';

interface VotingResultsProps {
    lastDeadPlayerName?: string;
    onContinue: () => void;
    waiting?: boolean;
    readyCount?: number;
    totalCount?: number;
    isAlive?: boolean;
}

export function VotingResults({ lastDeadPlayerName, onContinue, waiting, readyCount, totalCount, isAlive = true }: VotingResultsProps) {
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
            {isAlive && (
                waiting ? (
                    <p className="ready-wait-msg" style={{ marginTop: '1.5rem' }}>
                        Waiting for others... {readyCount !== undefined && totalCount !== undefined ? `(${readyCount}/${totalCount})` : ''}
                    </p>
                ) : (
                    <button className="btn-premium btn-premium--outline" style={{ marginTop: '1.5rem' }} onClick={onContinue}>
                        🌙 Continue to Night
                    </button>
                )
            )}
            {!isAlive && (
                <p className="ready-wait-msg" style={{ marginTop: '1.5rem' }}>
                    Waiting for the village... {readyCount !== undefined && totalCount !== undefined ? `(${readyCount}/${totalCount})` : ''}
                </p>
            )}
        </div>
    );
}
