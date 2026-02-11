import { DayDiscussion } from './DayDiscussion';
import { DayVoting } from './DayVoting';
import { NightAction } from './NightAction';
import type { GameState, Player } from '../../../hooks/useOnlineGame';

interface ActionAreaProps {
    gameState: GameState;
    myPlayer: Player;
    hasVotedReady: boolean;
    onAction: (actionType: string, targetId?: string) => void;
}

export function ActionArea({ gameState, myPlayer, hasVotedReady, onAction }: ActionAreaProps) {
    if (!myPlayer.isAlive) return null;

    return (
        <div className="action-area">
            {gameState.phase.includes('NIGHT') && (
                <NightAction
                    phase={gameState.phase}
                    myPlayer={myPlayer}
                    onAction={onAction}
                />
            )}

            {gameState.phase === 'DAY_DISCUSSION' && (
                <DayDiscussion
                    gameState={gameState}
                    hasVotedReady={hasVotedReady}
                    onAction={onAction}
                />
            )}

            {gameState.phase === 'DAY_VOTING' && (
                <DayVoting
                    hasAbstained={gameState.votes?.[myPlayer.name] === 'ABSTAIN'}
                    onAction={onAction}
                />
            )}
        </div>
    );
}
