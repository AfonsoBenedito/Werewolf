import { PlayerCard } from '../common/PlayerCard';
import type { GameState } from '../../hooks/useOfflineGame';

interface OfflineGridProps {
    gameState: GameState;
    isNight: boolean;
    isWolfTurn: boolean;
    isMedicTurn: boolean;
    isSeerTurn: boolean;
    isVoting: boolean;
    isDiscussion?: boolean;
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
    isNight,
    isWolfTurn,
    isMedicTurn,
    isSeerTurn,
    isVoting,
    isDiscussion,
    activeVoter,
    revealRoles,
    onKill,
    onHeal,
    onPeek,
    onVoteSelect,
    onVoteTarget
}: OfflineGridProps) {
    return (
        <div className={`player-grid ${isDiscussion ? 'discussion-grid' : ''}`}>
            {gameState.players.map(p => {
                const hasVoted = gameState.votes && gameState.votes[p.name];

                // Offline specific flags for PlayerCard
                const showKill = isNight && isWolfTurn && p.role !== "Werewolf";
                const showHeal = isNight && isMedicTurn;
                const showPeek = isNight && isSeerTurn && p.role !== "Seer";
                const showVoteSelect = isVoting && !activeVoter && !hasVoted && p.isAlive;
                const showVoteTarget = isVoting && activeVoter && activeVoter !== p.name && p.isAlive;

                // Disable card for the person whose turn it is
                // EXCEPT for Medic, who can heal themselves
                const isMyTurn = (isWolfTurn && p.role === "Werewolf") ||
                    (isSeerTurn && p.role === "Seer") ||
                    (activeVoter === p.name);

                return (
                    <PlayerCard
                        key={p.name}
                        name={p.name}
                        isAlive={p.isAlive}
                        role={p.role}
                        revealedRole={revealRoles ? p.role : undefined}
                        hasVoted={!!hasVoted}
                        isVotingPhase={isVoting}
                        isActiveVoter={activeVoter === p.name}
                        isDisabled={isMyTurn}
                        isVoteTarget={false}

                        showKill={showKill}
                        onKill={() => onKill(p.name)}

                        showHeal={showHeal}
                        onHeal={() => onHeal(p.name)}

                        showPeek={showPeek}
                        onPeek={() => onPeek(p.name)}

                        showVoteSelect={!!showVoteSelect}
                        onVoteSelect={() => onVoteSelect(p.name)}

                        showVoteTarget={!!showVoteTarget}
                        onVoteTarget={() => onVoteTarget(p.name)}
                    />
                );
            })}
        </div>
    );
}
