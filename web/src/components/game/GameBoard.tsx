import { ActionArea } from './actions/ActionArea';
import { PlayersGrid } from './PlayersGrid';
import { RoleInfo } from './RoleInfo';
import type { GameState, Player } from '../../hooks/useOnlineGame';

interface GameBoardProps {
    gameState: GameState;
    myPlayer: Player;
    playerName: string | null;
    hasVotedReady: boolean;
    onAction: (actionType: string, targetId?: string) => void;
}

export function GameBoard({ gameState, myPlayer, playerName, hasVotedReady, onAction }: GameBoardProps) {
    return (
        <div className="game-board">
            <RoleInfo role={myPlayer.role} isAlive={myPlayer.isAlive} />

            <ActionArea
                gameState={gameState}
                myPlayer={myPlayer}
                hasVotedReady={hasVotedReady}
                onAction={onAction}
            />

            <PlayersGrid
                gameState={gameState}
                myPlayer={myPlayer}
                playerName={playerName}
                onAction={onAction}
            />
        </div>
    );
}
