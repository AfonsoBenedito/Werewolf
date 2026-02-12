import { GameHeader } from '../components/common/GameHeader';
import { useOfflineGame } from '../hooks/useOfflineGame';
import { OfflineSetup } from '../components/offline/OfflineSetup';
import { VotingResults } from '../components/offline/VotingResults';
import { TurnAnnouncement } from '../components/offline/TurnAnnouncement';
import { OfflineControls } from '../components/offline/OfflineControls';
import { OfflineGrid } from '../components/offline/OfflineGrid';
import { TransitionScreen } from '../components/game/TransitionScreen';
import { useGameTransitions } from '../hooks/useGameTransitions';
import { ArrowLeft, Home } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { Footer } from '../components/common/Footer';
import '../styles/pages/OfflineGame.css';

export default function OfflineGame() {
    const navigate = useNavigate();
    const {
        gameId,
        gameState,
        localPlayers,
        newPlayerName,
        setNewPlayerName,
        loading,
        revealRoles,
        setRevealRoles,
        activeVoter,
        setActiveVoter,
        handleAddPlayer,
        handleStartGame,
        handleKill,
        handleNextPhase,
        handleVote,
        handleAbstain,
        manualAction,
        phaseMessage
    } = useOfflineGame();

    const { isTransitioning, currentTransition, handleTransitionComplete } = useGameTransitions(gameState, null);

    // SETUP UI
    if (!gameId) {
        return (
            <>
                <div className="offline-setup-wrapper">
                    <button className="nav-btn-absolute" onClick={() => navigate('/')}><ArrowLeft /></button>
                    <OfflineSetup
                        newPlayerName={newPlayerName}
                        setNewPlayerName={setNewPlayerName}
                        currentPlayers={localPlayers}
                        onAddPlayer={handleAddPlayer}
                        onStartGame={handleStartGame}
                        loading={loading}
                    />
                </div>
                <Footer />
            </>
        );
    }

    if (!gameState) return <div className="loading">Loading game state...</div>;

    const isNight = gameState.phase.includes("NIGHT");
    const isWolfTurn = gameState.phase.includes("Wolf");
    const isSeerTurn = gameState.phase.includes("Seer");
    const isMedicTurn = gameState.phase.includes("Medic");
    const isVoting = gameState.phase.includes("VOTING");
    const isResults = gameState.phase === "DAY_RESULTS";

    return (
        <>
            <div className="offline-game">
                <button className="home-btn-fixed" onClick={() => navigate('/')}><Home /></button>

                {isTransitioning && currentTransition && (
                    <TransitionScreen
                        message={currentTransition.message}
                        duration={currentTransition.duration}
                        onComplete={handleTransitionComplete}
                        manualContinue={true}
                        actionLabel="Next"
                    />
                )}

                <h1>Offline Mode (Master)</h1>

                <GameHeader
                    gameId={gameId}
                    phase={phaseMessage}
                    dayCount={gameState.dayCount}
                    showConnectionStatus={false}
                />

                {gameState.winner && <h2 className="winner-banner">🏆 Winner: {gameState.winner}</h2>}

                {/* Voting Results Screen */}
                {isResults && (
                    <VotingResults
                        lastDeadPlayerName={gameState.lastDeadPlayerName}
                        onContinue={handleNextPhase}
                    />
                )}

                {gameState.status === 'IN_PROGRESS' && (
                    <div className="game-controls-wrapper">
                        <TurnAnnouncement
                            phase={gameState.phase}
                            lastDeadPlayerName={gameState.lastDeadPlayerName}
                        />

                        <OfflineControls
                            phase={gameState.phase}
                            activeVoter={activeVoter}
                            isNight={isNight}
                            isResults={isResults}
                            isVoting={isVoting}
                            revealRoles={revealRoles}
                            onNextPhase={handleNextPhase}
                            onSkipTurn={() => manualAction("NEXT_TURN")}
                            onCancelVote={() => setActiveVoter(null)}
                            onToggleReveal={() => setRevealRoles(!revealRoles)}
                            onAbstain={handleAbstain}
                        />

                        <OfflineGrid
                            gameState={gameState}
                            isNight={isNight}
                            isWolfTurn={isWolfTurn}
                            isMedicTurn={isMedicTurn}
                            isSeerTurn={isSeerTurn}
                            isVoting={isVoting}
                            activeVoter={activeVoter}
                            revealRoles={revealRoles}
                            onKill={handleKill}
                            onHeal={(id) => manualAction("HEAL", id)}
                            onPeek={(id) => manualAction("PEEK", id)}
                            onVoteSelect={setActiveVoter}
                            onVoteTarget={handleVote}
                        />
                    </div>
                )}
            </div>
            <Footer />
        </>
    );
}
