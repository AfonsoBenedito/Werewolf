import type { LucideIcon } from 'lucide-react';
import '../../styles/components/common/ModeCard.css';

interface ModeCardProps {
    title: string;
    description: string;
    Icon: LucideIcon;
    onClick: () => void;
}

const ICON_SIZE = 48;

export function ModeCard({ title, description, Icon, onClick }: ModeCardProps) {
    return (
        <button className="mode-card" onClick={onClick}>
            <Icon size={ICON_SIZE} />
            <h2>{title}</h2>
            <p>{description}</p>
        </button>
    );
}
