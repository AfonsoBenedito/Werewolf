import { Play, RefreshCw } from 'lucide-react';

interface PhaseControlProps {
    phase: string;
    isVoting: boolean;
    isResults: boolean;
    onNextPhase: () => void;
}

export function PhaseControl({ phase, isVoting, isResults, onNextPhase }: PhaseControlProps) {
    if (phase === "DAY_DISCUSSION") {
        return (
            <button className="vote-phase-btn" onClick={onNextPhase}>
                <Play size={16} /> Start Voting
            </button>
        );
    }

    if (isVoting) {
        return (
            <button className="skip-phase-btn" onClick={onNextPhase}>
                <RefreshCw size={16} /> Skip Voting (Force Results)
            </button>
        );
    }

    if (!isVoting && !isResults) {
        return (
            <button onClick={onNextPhase}>
                <RefreshCw size={16} /> Force Next Phase
            </button>
        );
    }

    return null;
}
