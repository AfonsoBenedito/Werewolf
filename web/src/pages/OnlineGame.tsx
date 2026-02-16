import { RefreshCw, Home } from 'lucide-react';
import { GameHeader } from '../components/common/GameHeader';
import { WaitingRoom } from '../components/game/WaitingRoom';
import { GameBoard } from '../components/game/GameBoard';
import { useOnlineGame } from '../hooks/useOnlineGame';
import { useNavigate } from 'react-router-dom';
import { Footer } from '../components/common/Footer';
import '../styles/pages/OnlineGame.css';
import '../styles/components/common/SeerResult.css';
import '../styles/components/common/Buttons.css';


import { TransitionScreen } from '../components/game/TransitionScreen';
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
        myNightTarget,
        nightActionFeedback,
        seerResult,
        isLoading,
        error,
        fetchState,
        handleStart,
        handleAction
    } = useOnlineGame();

    const { isTransitioning, currentTransition, handleTransitionComplete } = useGameTransitions(gameState, myPlayer, !!seerResult);

    if (isLoading) return <div className="loading-screen">Loading Game...</div>;

    if (error || !gameState) {
        return (
            <div className="error-screen">
                <h2>Game Not Found</h2>
                <p>The game ID <strong>{gameId}</strong> does not exist or has expired.</p>
                <button className="home-btn" onClick={() => navigate('/')}>
                    <Home size={20} style={{ marginRight: '8px' }} />
                    Back to Home
                </button>
                <Footer />
            </div>
        );
    }

    const isHost = gameState.players[0]?.name === playerName;

    return (
        <div className="online-game">
            <button className="home-btn-fixed" onClick={() => navigate('/')}><Home /></button>

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
                    myNightTarget={myNightTarget}
                    nightActionFeedback={nightActionFeedback}
                    seerResult={seerResult}
                    nightStatus={gameState.nightStatus}
                />
            )}

            <button className="refresh-btn" onClick={fetchState}><RefreshCw size={16} /></button>
            <Footer />
        </div>
    );
}
