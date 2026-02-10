import { PhaseControl } from './controls/PhaseControl';
import { TurnControl } from './controls/TurnControl';
import { VoteControl } from './controls/VoteControl';
import { GameMasterControl } from './controls/GameMasterControl';

interface OfflineControlsProps {
    phase: string;
    activeVoter: string | null;
    isNight: boolean;
    isResults: boolean;
    isVoting: boolean;
    revealRoles: boolean;
    onNextPhase: () => void;
    onSkipTurn: () => void;
    onCancelVote: () => void;
    onToggleReveal: () => void;
}

export function OfflineControls({
    phase,
    activeVoter,
    isNight,
    isResults,
    isVoting,
    revealRoles,
    onNextPhase,
    onSkipTurn,
    onCancelVote,
    onToggleReveal
}: OfflineControlsProps) {
    return (
        <div className="game-controls">
            <PhaseControl
                phase={phase}
                isVoting={isVoting}
                isResults={isResults}
                onNextPhase={onNextPhase}
            />

            <TurnControl
                isNight={isNight}
                onSkipTurn={onSkipTurn}
            />

            <VoteControl
                activeVoter={activeVoter}
                onCancelVote={onCancelVote}
            />

            <GameMasterControl
                revealRoles={revealRoles}
                onToggleReveal={onToggleReveal}
            />
        </div>
    );
}
