interface TurnControlProps {
    isNight: boolean;
    onSkipTurn: () => void;
}

export function TurnControl({ isNight, onSkipTurn }: TurnControlProps) {
    if (!isNight) return null;

    return (
        <button onClick={onSkipTurn}>
            Skip Turn
        </button>
    );
}
