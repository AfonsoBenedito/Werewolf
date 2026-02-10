import type { GameState } from '../../../hooks/useOnlineGame';

interface DayDiscussionProps {
    gameState: GameState;
    hasVotedReady: boolean;
    onAction: (actionType: string, targetId?: string) => void;
}

export function DayDiscussion({ gameState, hasVotedReady, onAction }: DayDiscussionProps) {
    return (
        <div className="discussion-panel">
            <h3>☀️ Day Discussion ☀️</h3>
            <p>Discuss who to eliminate!</p>
            {!hasVotedReady ? (
                <button className="ready-btn" onClick={() => onAction('READY_TO_VOTE')}>
                    Proceed to Voting
                </button>
            ) : (
                <p className="ready-wait-msg">Waiting for the rest...</p>
            )}
            <p className="ready-status">
                Waiting for players: {(gameState as any).readyPlayerCount || 0} / {(gameState as any).totalAliveCount || 'ALL'}
            </p>
        </div>
    );
}
