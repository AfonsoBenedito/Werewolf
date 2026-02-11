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
    const isNight = gameState.phase.includes('NIGHT');
    const currentTurnRole = isNight ? gameState.phase.split(' - ')[1] : null;

    // Logic: Hide board if it is Night, I am alive, and it is NOT my turn.
    // Exception: Werewolves share a turn, so all Wolves see the board during "NIGHT - Wolf".
    let isMyTurn = false;
    if (currentTurnRole === 'Wolf' && myPlayer.role === 'Werewolf') isMyTurn = true;
    else if (currentTurnRole === myPlayer.role) isMyTurn = true;

    const shouldHideBoard = isNight && myPlayer.isAlive && !isMyTurn;

    return (
        <div className="game-board">
            <RoleInfo role={myPlayer.role} isAlive={myPlayer.isAlive} />

            {!shouldHideBoard && (
                <>
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
                </>
            )}

            {shouldHideBoard && (
                <div className="sleeping-banner">
                    <h2>The Village is sleeping...</h2>
                    <p>Waiting for other players to act.</p>
                </div>
            )}
        </div>
    );
}
