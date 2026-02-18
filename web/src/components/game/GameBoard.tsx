import { useState } from 'react';
import { ActionArea } from './actions/ActionArea';
import { PlayersGrid } from './PlayersGrid';
import { RoleInfo } from './RoleInfo';
import { VotingResults } from '../common/VotingResults';
import type { GameState, Player } from '../../types/game';

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
    const [prevPhase, setPrevPhase] = useState(gameState.phase);

    if (gameState.phase !== prevPhase) {
        setPrevPhase(gameState.phase);
        setHasConfirmedResults(false);
    }

    const isNight = gameState.phaseKey === 'NIGHT';
    const shouldHideBoard = isNight && myPlayer.isAlive && !gameState.isMyTurn;

    // Voting Results page during DAY_RESULTS
    if (gameState.phaseKey === 'DAY_RESULTS') {
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
                    />

                    {/* Hide PlayersGrid during Discussion, when seer result is showing, and for dead players in voting */}
                    {gameState.phaseKey !== 'DAY_DISCUSSION'
                        && !(seerResult && myPlayer.role === 'Seer')
                        && !(gameState.phaseKey === 'DAY_VOTING' && !myPlayer.isAlive)
                        && (
                        <PlayersGrid
                            gameState={gameState}
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
