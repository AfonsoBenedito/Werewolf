interface GameMasterControlProps {
    revealRoles: boolean;
    onToggleReveal: () => void;
}

export function GameMasterControl({ revealRoles, onToggleReveal }: GameMasterControlProps) {
    return (
        <button onClick={onToggleReveal}>
            {revealRoles ? "Hide Roles" : "Reveal Roles (Master)"}
        </button>
    );
}
