import { PlayerCard } from '../common/PlayerCard';
import type { GameState, Player } from '../../hooks/useOnlineGame';

interface PlayersGridProps {
    gameState: GameState;
    myPlayer: Player;
    playerName: string | null;
    onAction: (actionType: string, targetId?: string) => void;
}

export function PlayersGrid({ gameState, myPlayer, playerName, onAction }: PlayersGridProps) {
    return (
        <div className="players-grid">
            {gameState.players.map(p => {
                const isMyVoteTarget = gameState.votes?.[playerName!] === p.name;
                const hasVoted = gameState.votes?.[p.name] !== undefined;
                const isMe = p.name === playerName;

                let isDisabled = false;
                if (isMe) {
                    isDisabled = true;
                    if (gameState.phase.includes("Medic") && myPlayer.role === 'Medic') isDisabled = false;
                }
                if (!p.isAlive) isDisabled = true;

                return (
                    <PlayerCard
                        key={p.name}
                        name={p.name}
                        isAlive={p.isAlive}
                        isMe={isMe}
                        role={p.role}
                        revealedRole={p.role} // Online: role is masked by backend
                        hasVoted={hasVoted}
                        isVotingPhase={gameState.phase === 'DAY_VOTING'}
                        isDisabled={isDisabled}
                        isVoteTarget={isMyVoteTarget}
                        onClick={() => {
                            if (!myPlayer.isAlive) return;
                            if (isDisabled) return;
                            if (gameState.phase === 'DAY_VOTING') onAction('VOTE', p.name);
                            if (gameState.phase.includes('NIGHT')) {
                                if (gameState.phase.includes("Wolf") && myPlayer.role === 'Werewolf') onAction('KILL', p.name);
                                if (gameState.phase.includes("Medic") && myPlayer.role === 'Medic') onAction('HEAL', p.name);
                                if (gameState.phase.includes("Seer") && myPlayer.role === 'Seer') onAction('PEEK', p.name);
                            }
                        }}
                    />
                );
            })}
        </div>
    );
}
