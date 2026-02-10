import type { Player } from '../../../hooks/useOnlineGame';

interface NightActionProps {
    phase: string;
    myPlayer: Player;
    onAction: (actionType: string, targetId?: string) => void;
}

export function NightAction({ phase, myPlayer, onAction }: NightActionProps) {
    const isMyTurn = (
        (phase.includes("Wolf") && myPlayer.role === "Werewolf") ||
        (phase.includes("Seer") && myPlayer.role === "Seer") ||
        (phase.includes("Medic") && myPlayer.role === "Medic")
    );

    if (!isMyTurn) {
        return (
            <div className="sleep-banner">
                <h3>💤 Village is Asleep 💤</h3>
                <p>Waiting for other roles to act...</p>
            </div>
        );
    }

    return (
        <>
            <p className="turn-alert">
                It is your turn! {myPlayer.role === 'Werewolf' ? 'Choose a victim.' : myPlayer.role === 'Medic' ? 'Choose who to save.' : 'Choose who to peek.'}
            </p>
            {(myPlayer.role === 'Seer' || myPlayer.role === 'Medic') && (
                <button className="skip-btn" onClick={() => onAction('SKIP')}>Skip Action</button>
            )}
        </>
    );
}
