import { RefreshCw } from 'lucide-react';
import { GameHeader } from '../components/common/GameHeader';
import { WaitingRoom } from '../components/game/WaitingRoom';
import { GameBoard } from '../components/game/GameBoard';
import { useOnlineGame } from '../hooks/useOnlineGame';
import '../styles/OnlineGame.css';

console.log("OnlineGame module evaluated");

export default function OnlineGame() {
    const {
        gameId,
        gameState,
        myPlayer,
        playerName,
        isConnected,
        hasVotedReady,
        fetchState,
        handleStart,
        handleAction
    } = useOnlineGame();

    if (!gameState) return <div>Loading...</div>;

    const isHost = gameState.players[0]?.name === playerName;

    return (
        <div className="online-game">
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
