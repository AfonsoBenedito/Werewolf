interface TurnAnnouncementProps {
    phase: string;
    lastDeadPlayerName?: string;
}

export function TurnAnnouncement({ phase, lastDeadPlayerName }: TurnAnnouncementProps) {
    if (phase !== "DAY_DISCUSSION" && !(phase.includes("NIGHT") && lastDeadPlayerName)) {
        return null;
    }

    const isNightHeader = phase.includes("NIGHT");
    const title = isNightHeader ? "🗳️ Voting Result" : (lastDeadPlayerName ? "☠️ Tragic News!" : "☀️ Peaceful Night");
    const isGoodNews = !lastDeadPlayerName;

    let message = "";
    if (isNightHeader) {
        message = `${lastDeadPlayerName} was eliminated by the village.`;
    } else {
        message = lastDeadPlayerName
            ? `${lastDeadPlayerName} was killed last night.`
            : "No one died last night.";
    }

    return (
        <div className={`death-announcement ${isGoodNews ? 'good-news' : ''}`}>
            <h3>{title}</h3>
            <p>{message}</p>
        </div>
    );
}
