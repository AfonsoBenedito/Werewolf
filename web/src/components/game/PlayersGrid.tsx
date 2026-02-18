import { PlayerCard } from '../common/PlayerCard';
import type { GameState, Player } from '../../types/game';

interface PlayersGridProps {
    gameState: GameState;
    myPlayer: Player;
    playerName: string | null;
    onAction: (actionType: string, targetId?: string) => void;
    myNightTarget?: string | null;
}

export function PlayersGrid({ gameState, myPlayer, playerName, onAction, myNightTarget }: PlayersGridProps) {
    const isNight = gameState.phaseKey === 'NIGHT';
    const isVoting = gameState.phaseKey === 'DAY_VOTING';
    const actions = gameState.availableActions;

    return (
        <div className="players-grid">
            {gameState.players.map(p => {
                const isMyVoteTarget = gameState.votes?.[playerName!] === p.name;
                const isMyNightTarget = myNightTarget === p.name;
                const isMe = p.name === playerName;

                const showKill = isNight && actions.includes('KILL') && p.isTargetable;
                const showHeal = isNight && actions.includes('HEAL') && p.isTargetable;
                const showPeek = isNight && actions.includes('PEEK') && p.isTargetable;
                const showVoteTarget = isVoting && p.isTargetable;

                return (
                    <PlayerCard
                        key={p.name}
                        name={p.name}
                        isAlive={p.isAlive}
                        isMe={isMe}
                        role={p.role}
                        revealedRole={p.role} // Online: role is masked by backend
                        hasVoted={p.hasVoted}
                        isVotingPhase={isVoting}
                        isDisabled={!p.isTargetable}
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
                    />
                );
            })}
        </div>
    );
}
