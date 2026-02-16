import { PlayerCard } from '../common/PlayerCard';
import type { GameState, Player } from '../../hooks/useOnlineGame';

interface PlayersGridProps {
    gameState: GameState;
    myPlayer: Player;
    playerName: string | null;
    onAction: (actionType: string, targetId?: string) => void;
    myNightTarget?: string | null;
}

export function PlayersGrid({ gameState, myPlayer, playerName, onAction, myNightTarget }: PlayersGridProps) {
    return (
        <div className="players-grid">
            {gameState.players.map(p => {
                const isMyVoteTarget = gameState.votes?.[playerName!] === p.name;
                // Night vote highlighting (local state)
                const isMyNightTarget = myNightTarget === p.name;

                const isMe = p.name === playerName;
                const hasVoted = !isMe && gameState.votes?.[p.name] !== undefined;

                let isDisabled = false;
                if (isMe) {
                    isDisabled = true;
                    if (gameState.phase.includes("Medic") && myPlayer.role === 'Medic') isDisabled = false;
                }

                // Disable fellow werewolves during Night
                if (gameState.phase.includes("NIGHT") && myPlayer.role === 'Werewolf') {
                    if (p.role === 'Werewolf' || p.role === 'Wolf') {
                        isDisabled = true;
                    }
                }

                if (!p.isAlive) isDisabled = true;

                // Action indicators (match offline mode)
                const isNight = gameState.phase.includes('NIGHT');
                const isVoting = gameState.phase === 'DAY_VOTING';
                const showKill = isNight && gameState.phase.includes('Wolf') && myPlayer.role === 'Werewolf' && p.isAlive && !isDisabled;
                const showHeal = isNight && gameState.phase.includes('Medic') && myPlayer.role === 'Medic' && p.isAlive && !isDisabled;
                const showPeek = isNight && gameState.phase.includes('Seer') && myPlayer.role === 'Seer' && p.isAlive && !isDisabled;
                const showVoteTarget = isVoting && p.isAlive && !isMe && !isDisabled;

                return (
                    <PlayerCard
                        key={p.name}
                        name={p.name}
                        isAlive={p.isAlive}
                        isMe={isMe}
                        role={p.role}
                        revealedRole={p.role} // Online: role is masked by backend
                        hasVoted={hasVoted}
                        isVotingPhase={isVoting}
                        isDisabled={isDisabled}
                        isVoteTarget={isMyVoteTarget || isMyNightTarget}
                        showKill={showKill}
                        showHeal={showHeal}
                        showPeek={showPeek}
                        showVoteTarget={showVoteTarget}
                        onKill={() => onAction('KILL', p.name)}
                        onHeal={() => onAction('HEAL', p.name)}
                        onPeek={() => onAction('PEEK', p.name)}
                        onVoteTarget={() => {
                            if (isMyVoteTarget) {
                                onAction('UNVOTE');
                            } else {
                                onAction('VOTE', p.name);
                            }
                        }}
                        onClick={() => {
                            if (!myPlayer.isAlive) return;
                            if (isDisabled) return;
                            if (gameState.phase === 'DAY_VOTING') {
                                if (isMyVoteTarget) {
                                    onAction('UNVOTE');
                                } else {
                                    onAction('VOTE', p.name);
                                }
                            }
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
