import { useState, useEffect } from 'react';
import { ActionArea } from './actions/ActionArea';
import { PlayersGrid } from './PlayersGrid';
import { RoleInfo } from './RoleInfo';
import { VotingResults } from '../common/VotingResults';
import type { GameState, Player } from '../../hooks/useOnlineGame';

interface GameBoardProps {
    gameState: GameState;
    myPlayer: Player;
    playerName: string | null;
    hasVotedReady: boolean;
    onAction: (actionType: string, targetId?: string) => void;
    myNightTarget: string | null;
    nightActionFeedback: string | null;
    seerResult: string | null;
    nightStatus?: string | null;
}

export function GameBoard({
    gameState,
    myPlayer,
    playerName,
    hasVotedReady,
    onAction,
    myNightTarget,
    nightActionFeedback,
    seerResult,
    nightStatus
}: GameBoardProps) {
    const [hasConfirmedResults, setHasConfirmedResults] = useState(false);

    useEffect(() => {
        setHasConfirmedResults(false);
    }, [gameState.phase]);

    const isNight = gameState.phase.includes('NIGHT');
    const currentTurnRole = isNight ? gameState.phase.split(' - ')[1] : null;

    // Logic: Hide board if it is Night, I am alive, and it is NOT my turn.
    // Exception: Werewolves share a turn, so all Wolves see the board during "NIGHT - Wolf".
    let isMyTurn = false;
    if (currentTurnRole === 'Wolf' && myPlayer.role === 'Werewolf') isMyTurn = true;
    else if (currentTurnRole === myPlayer.role) isMyTurn = true;

    const shouldHideBoard = isNight && myPlayer.isAlive && !isMyTurn;

    // Voting Results page during DAY_RESULTS
    if (gameState.phase === 'DAY_RESULTS') {
        return (
            <div className="game-board">
                <RoleInfo role={myPlayer.role} isAlive={myPlayer.isAlive} />
                <VotingResults
                    lastDeadPlayerName={gameState.lastDeadPlayerName}
                    waiting={hasConfirmedResults}
                    readyCount={gameState.readyPlayerCount}
                    totalCount={gameState.totalAliveCount}
                    isAlive={myPlayer.isAlive}
                    onContinue={() => {
                        setHasConfirmedResults(true);
                        onAction('CONTINUE');
                    }}
                />
            </div>
        );
    }

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
                        nightActionFeedback={nightActionFeedback}
                        seerResult={seerResult}
                        nightStatus={nightStatus}
                        myNightTarget={myNightTarget}
                    />

                    {/* Hide PlayersGrid during Discussion phase and when seer result is showing */}
                    {gameState.phase !== 'DAY_DISCUSSION' && !seerResult && (
                        <PlayersGrid
                            gameState={gameState}
                            myPlayer={myPlayer}
                            playerName={playerName}
                            onAction={onAction}
                            myNightTarget={myNightTarget}
                        />
                    )}
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
