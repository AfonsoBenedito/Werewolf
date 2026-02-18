import { DayDiscussion } from './DayDiscussion';
import { DayVoting } from './DayVoting';
import { NightAction } from './NightAction';
import type { GameState, Player } from '../../../types/game';

interface ActionAreaProps {
    gameState: GameState;
    myPlayer: Player;
    hasVotedReady: boolean;
    onAction: (actionType: string, targetId?: string) => void;
    nightActionFeedback?: string | null;
    seerResult?: string | null;
    nightStatus?: string | null;
    myNightTarget?: string | null;
}

export function ActionArea({ gameState, myPlayer, hasVotedReady, onAction, nightActionFeedback, seerResult, nightStatus, myNightTarget }: ActionAreaProps) {
    if (!myPlayer.isAlive) return null;

    return (
        <div className="action-area">
            {gameState.phaseKey === 'NIGHT' && (
                <NightAction
                    gameState={gameState}
                    myPlayer={myPlayer}
                    onAction={onAction}
                    nightActionFeedback={nightActionFeedback}
                    seerResult={seerResult}
                    nightStatus={nightStatus}
                    myNightTarget={myNightTarget}
                />
            )}

            {gameState.phaseKey === 'DAY_DISCUSSION' && (
                <DayDiscussion
                    gameState={gameState}
                    hasVotedReady={hasVotedReady}
                    onAction={onAction}
                />
            )}

            {gameState.phaseKey === 'DAY_VOTING' && (
                <DayVoting
                    hasAbstained={gameState.votes?.[myPlayer.name] === 'ABSTAIN'}
                    onAction={onAction}
                />
            )}
        </div>
    );
}
