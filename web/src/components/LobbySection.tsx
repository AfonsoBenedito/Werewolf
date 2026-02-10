import '../styles/components/LobbySection.css';

interface LobbySectionProps {
    title: string;
    children: React.ReactNode;
}

export function LobbySection({ title, children }: LobbySectionProps) {
    return (
        <div className="lobby-section">
            <h2>{title}</h2>
            {children}
        </div>
    );
}
