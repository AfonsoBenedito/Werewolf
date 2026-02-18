import { PlayerCard } from '../common/PlayerCard';
import type { GameState } from '../../types/game';

interface OfflineGridProps {
    gameState: GameState;
    activeVoter: string | null;
    revealRoles: boolean;
    onKill: (id: string) => void;
    onHeal: (id: string) => void;
    onPeek: (id: string) => void;
    onVoteSelect: (id: string) => void;
    onVoteTarget: (id: string) => void;
}

export function OfflineGrid({
    gameState,
    activeVoter,
    revealRoles,
    onKill,
    onHeal,
    onPeek,
    onVoteSelect,
    onVoteTarget
}: OfflineGridProps) {
    const isNight = gameState.phaseKey === 'NIGHT';
    const isVoting = gameState.phaseKey === 'DAY_VOTING';
    const isDiscussion = gameState.phaseKey === 'DAY_DISCUSSION';
    const actions = gameState.availableActions;

    return (
        <div className={`players-grid ${isDiscussion ? 'discussion-grid' : ''}`}>
            {gameState.players.map(p => {
                const hasVoted = p.hasVoted;

                const showKill = isNight && actions.includes('KILL') && p.isTargetable;
                const showHeal = isNight && actions.includes('HEAL') && p.isTargetable;
                const showPeek = isNight && actions.includes('PEEK') && p.isTargetable;
                const showVoteSelect = isVoting && !activeVoter && !hasVoted && p.isAlive;
                const showVoteTarget = isVoting && activeVoter && activeVoter !== p.name && p.isAlive;

                // Disable card for the person whose turn it is (except Medic who can self-heal)
                const isDisabled = isNight && !p.isTargetable;

                return (
                    <PlayerCard
                        key={p.name}
                        name={p.name}
                        isAlive={p.isAlive}
                        revealedRole={revealRoles ? p.role : undefined}
                        hasVoted={hasVoted}
                        isVotingPhase={isVoting}
                        isActiveVoter={activeVoter === p.name}
                        isDisabled={isDisabled}

                        showKill={showKill}
                        onKill={() => onKill(p.name)}

                        showHeal={showHeal}
                        onHeal={() => onHeal(p.name)}

                        showPeek={showPeek}
                        onPeek={() => onPeek(p.name)}

                        showVoteSelect={showVoteSelect}
                        onVoteSelect={() => onVoteSelect(p.name)}

                        showVoteTarget={showVoteTarget}
                        onVoteTarget={() => onVoteTarget(p.name)}
                    />
                );
            })}
        </div>
    );
}
