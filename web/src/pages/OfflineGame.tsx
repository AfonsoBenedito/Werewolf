import { GameHeader } from '../components/common/GameHeader';
import { useOfflineGame } from '../hooks/useOfflineGame';
import { OfflineSetup } from '../components/offline/OfflineSetup';
import { VotingResults } from '../components/common/VotingResults';
import { WinnerAnnouncement } from '../components/common/WinnerAnnouncement';
import { TurnAnnouncement } from '../components/offline/TurnAnnouncement';
import { OfflineControls } from '../components/offline/OfflineControls';
import { OfflineGrid } from '../components/offline/OfflineGrid';
import { TransitionScreen } from '../components/game/TransitionScreen';
import { useGameTransitions } from '../hooks/useGameTransitions';
import { ArrowLeft, Home } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { Footer } from '../components/common/Footer';
import '../styles/pages/OfflineGame.css';
import '../styles/components/common/SeerResult.css';
import '../styles/components/common/Buttons.css';

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
        handleRemovePlayer,
        handleStartGame,
        handleKill,
        handleNextPhase,
        handleVote,
        handleAbstain,
        handlePeek,
        advanceSeerTurn,
        manualAction,
        phaseMessage,
        seerResult
    } = useOfflineGame();

    const { isTransitioning, currentTransition, handleTransitionComplete } = useGameTransitions(gameState, null);

    // SETUP UI
    if (!gameId) {
        return (
            <>
                <div className="offline-game">
                    <button className="nav-btn-absolute" onClick={() => navigate('/')} title="Back to menu">
                        <ArrowLeft size={24} strokeWidth={2.5} />
                    </button>

                    <div className="offline-setup-wrapper">
                        <h1>OFFLINE MODE</h1>
                        <OfflineSetup
                            newPlayerName={newPlayerName}
                            setNewPlayerName={setNewPlayerName}
                            currentPlayers={localPlayers}
                            onAddPlayer={handleAddPlayer}
                            onRemovePlayer={handleRemovePlayer}
                            onStartGame={handleStartGame}
                            loading={loading}
                        />
                    </div>
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
                <button className="home-btn-fixed" onClick={() => navigate('/')} title="Back to menu">
                    <Home size={24} strokeWidth={2.5} />
                </button>

                {isTransitioning && currentTransition && (
                    <TransitionScreen
                        message={currentTransition.message}
                        duration={currentTransition.duration}
                        onComplete={handleTransitionComplete}
                        manualContinue={true}
                        actionLabel="Proceed"
                    />
                )}

                <div className="game-container">
                    <h1>GAME MASTER</h1>

                    <GameHeader
                        gameId={gameId}
                        phase={phaseMessage}
                        dayCount={gameState.dayCount}
                        showConnectionStatus={false}
                        hideGameId={true}
                    />

                    {gameState.winner && (
                        <WinnerAnnouncement
                            winner={gameState.winner}
                            players={gameState.players}
                            onGoHome={() => navigate('/')}
                        />
                    )}

                    {/* Voting Results Screen */}
                    {isResults && (
                        <VotingResults
                            lastDeadPlayerName={gameState.lastDeadPlayerName}
                            onContinue={handleNextPhase}
                        />
                    )}

                    {gameState.status === 'IN_PROGRESS' && !isResults && (
                        <>
                            {/* Turn Indicator */}
                            <div className="turn-indicator">
                                <h2>Current Turn</h2>
                                <p>{phaseMessage}</p>
                            </div>

                            {/* Seer Result Banner - Show during Seer turn after peeking */}
                            {seerResult && isSeerTurn && !isTransitioning && (
                                <div className="seer-result-banner-wrapper">
                                    <div className="seer-result-banner">
                                        <span className="seer-result-icon">👁️</span>
                                        <div className="seer-result-content">
                                            <span className="seer-result-label">Seer's Investigation:</span>
                                            <span className="seer-result-text">
                                                <strong>{seerResult.target}</strong> is a <strong>{seerResult.role}</strong>
                                            </span>
                                        </div>
                                    </div>
                                    <button
                                        className="btn-premium btn-premium--primary seer-ok-btn"
                                        onClick={advanceSeerTurn}
                                    >
                                        OK
                                    </button>
                                </div>
                            )}

                            {/* Death Announcement Overlay */}
                            {gameState.lastDeadPlayerName && !isTransitioning && (
                                <div className="death-announcement-overlay">
                                    <TurnAnnouncement
                                        phase={gameState.phase}
                                        lastDeadPlayerName={gameState.lastDeadPlayerName}
                                    />
                                    <div className="game-controls" style={{ marginTop: '2rem' }}>
                                        <button className="btn-premium btn-premium--primary" onClick={handleNextPhase}>
                                            Acknowledge & Continue
                                        </button>
                                    </div>
                                </div>
                            )}

                            {/* Game Action Grid - hide when seer result is showing */}
                            {!isTransitioning && !gameState.lastDeadPlayerName && !(seerResult && isSeerTurn) && (
                                <OfflineGrid
                                    gameState={gameState}
                                    isNight={isNight}
                                    isWolfTurn={isWolfTurn}
                                    isMedicTurn={isMedicTurn}
                                    isSeerTurn={isSeerTurn}
                                    isVoting={isVoting}
                                    isDiscussion={gameState.phase === 'DAY_DISCUSSION'}
                                    activeVoter={activeVoter}
                                    revealRoles={revealRoles}
                                    onKill={handleKill}
                                    onHeal={(id) => manualAction("HEAL", id)}
                                    onPeek={handlePeek}
                                    onVoteSelect={setActiveVoter}
                                    onVoteTarget={handleVote}
                                />
                            )}

                            {/* Bottom Controls - hide when seer result is showing */}
                            {!gameState.lastDeadPlayerName && !isTransitioning && !(seerResult && isSeerTurn) && (
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
                            )}
                        </>
                    )}
                </div>
            </div>
            <Footer />
        </>
    );
}
