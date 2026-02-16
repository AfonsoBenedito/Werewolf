import type { Player } from '../../../hooks/useOnlineGame';

interface NightActionProps {
    phase: string;
    myPlayer: Player;
    onAction: (actionType: string, targetId?: string) => void;
    nightActionFeedback?: string | null;
    seerResult?: string | null;
    nightStatus?: string | null;
    myNightTarget?: string | null;
}

export function NightAction({ phase, myPlayer, onAction, nightActionFeedback, seerResult, nightStatus }: NightActionProps) {
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

    const message = nightStatus || nightActionFeedback;

    return (
        <>
            <div className="turn-alert">
                It is your turn! {myPlayer.role === 'Werewolf' ? 'Choose a victim.' : myPlayer.role === 'Medic' ? 'Choose who to save.' : 'Choose who to peek.'}
            </div>
            {message && (
                <div className="action-feedback">
                    {message}
                </div>
            )}

            {/* Seer Result Banner */}
            {seerResult && myPlayer.role === 'Seer' && (
                <div className="seer-result-banner-wrapper">
                    <div className="seer-result-banner">
                        <span className="seer-result-icon">👁️</span>
                        <div className="seer-result-content">
                            <span className="seer-result-label">Seer's Vision</span>
                            <span className="seer-result-text">{seerResult}</span>
                        </div>
                    </div>
                    <button
                        className="btn-premium btn-premium--primary seer-ok-btn"
                        onClick={() => onAction('SKIP')}
                    >
                        OK
                    </button>
                </div>
            )}

            {(myPlayer.role === 'Seer' || myPlayer.role === 'Medic') && !seerResult && (
                <div className="skip-btn-container">
                    <button className="skip-btn" onClick={() => onAction('SKIP')}>Skip Action</button>
                </div>
            )}
        </>
    );
}
