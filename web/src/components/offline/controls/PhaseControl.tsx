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
            <button className="btn-premium btn-premium--primary" onClick={onNextPhase}>
                <Play size={18} /> Start Voting
            </button>
        );
    }

    if (isVoting) {
        return (
            <button className="btn-premium btn-premium--outline" onClick={onNextPhase}>
                <RefreshCw size={18} /> Skip Voting
            </button>
        );
    }

    if (!isVoting && !isResults) {
        return (
            <button className="btn-premium btn-premium--outline" onClick={onNextPhase}>
                <RefreshCw size={18} /> Next Phase
            </button>
        );
    }

    return null;
}
