import { useNavigate } from 'react-router-dom';
import { Moon, Users, Wifi } from 'lucide-react';

export default function LandingPage() {
    const navigate = useNavigate();

    return (
        <div className="landing-page">
            <h1>Werewolf <Moon className="inline-icon" /></h1>
            <div className="mode-selection">
                <button className="mode-card" onClick={() => navigate('/offline')}>
                    <Users size={48} />
                    <h2>Offline Mode</h2>
                    <p>Master controls the game for valid players in the same room.</p>
                </button>
                <button className="mode-card" onClick={() => navigate('/online')}>
                    <Wifi size={48} />
                    <h2>Online Mode</h2>
                    <p>Join or Host a game to play with friends remotely.</p>
                </button>
            </div>
        </div>
    );
}
