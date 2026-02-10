import { useNavigate } from 'react-router-dom';
import { Moon, Users, Wifi } from 'lucide-react';
import { ModeCard } from '../components/common/ModeCard';
import '../styles/pages/LandingPage.css';

export default function LandingPage() {
    const navigate = useNavigate();

    return (
        <div className="landing-page">
            <h1>Werewolf <Moon className="inline-icon" /></h1>
            <div className="mode-selection">
                <ModeCard
                    title="Offline Mode"
                    description="Master controls the game for valid players in the same room."
                    Icon={Users}
                    onClick={() => navigate('/offline')}
                />
                <ModeCard
                    title="Online Mode"
                    description="Join or Host a game to play with friends remotely."
                    Icon={Wifi}
                    onClick={() => navigate('/online')}
                />
            </div>
        </div>
    );
}
