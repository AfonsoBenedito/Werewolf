import { RefreshCw, Home } from 'lucide-react';
import { GameHeader } from '../components/common/GameHeader';
import { WaitingRoom } from '../components/game/WaitingRoom';
import { GameBoard } from '../components/game/GameBoard';
import { useOnlineGame } from '../hooks/useOnlineGame';
import { useNavigate } from 'react-router-dom';
import '../styles/pages/OnlineGame.css';

console.log("OnlineGame module evaluated");

import { TransitionScreen } from '../components/game/TransitionScreen';
import { SeerResultModal } from '../components/game/SeerResultModal';
import { useGameTransitions } from '../hooks/useGameTransitions';

export default function OnlineGame() {
    const navigate = useNavigate();
    const {
        gameId,
        gameState,
        myPlayer,
        playerName,
        isConnected,
        hasVotedReady,
        seerResult,
        dismissSeerResult,
        fetchState,
        handleStart,
        handleAction
    } = useOnlineGame();

    const { isTransitioning, currentTransition, handleTransitionComplete } = useGameTransitions(gameState, myPlayer, !!seerResult);

    if (!gameState) return <div>Loading...</div>;

    const isHost = gameState.players[0]?.name === playerName;

    return (
        <div className="online-game">
            <button className="home-btn-fixed" onClick={() => navigate('/')}><Home /></button>
            {seerResult && (
                <SeerResultModal
                    result={seerResult}
                    onDismiss={dismissSeerResult}
                />
            )}

            {isTransitioning && currentTransition && (
                <TransitionScreen
                    message={currentTransition.message}
                    duration={currentTransition.duration}
                    onComplete={handleTransitionComplete}
                />
            )}

            <GameHeader
                gameId={gameId || ''}
                phase={gameState.phase}
                dayCount={gameState.dayCount}
                isConnected={isConnected}
                showConnectionStatus={true}
            />

            {gameState.winner && <h1 className="winner">Winner: {gameState.winner}</h1>}

            {gameState.status === 'NOT_STARTED' && (
                <WaitingRoom
                    players={gameState.players}
                    isHost={isHost}
                    onStart={handleStart}
                />
            )}

            {gameState.status === 'IN_PROGRESS' && myPlayer && (
                <GameBoard
                    gameState={gameState}
                    myPlayer={myPlayer}
                    playerName={playerName}
                    hasVotedReady={hasVotedReady}
                    onAction={handleAction}
                />
            )}

            <button className="refresh-btn" onClick={fetchState}><RefreshCw size={16} /></button>
        </div>
    );
}
