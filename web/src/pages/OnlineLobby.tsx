import { useOnlineLobby } from '../hooks/useOnlineLobby';
import { LobbySection } from '../components/common/LobbySection';
import { ArrowLeft } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { Footer } from '../components/common/Footer';
import '../styles/pages/OnlineLobby.css';

export default function OnlineLobby() {
    const navigate = useNavigate();
    const {
        hostName, setHostName,
        joinName, setJoinName,
        joinId, setJoinId,
        error,
        handleHost,
        handleJoin
    } = useOnlineLobby();

    return (
        <div className="online-lobby">
            <div className="header-nav">
                <button className="nav-btn" onClick={() => navigate('/')}><ArrowLeft /></button>
                <h1>Online Lobby</h1>
            </div>
            {error && <p className="error">{error}</p>}

            <LobbySection title="Host Game">
                <input
                    placeholder="Your Name"
                    value={hostName}
                    onChange={e => setHostName(e.target.value)}
                    onKeyDown={e => e.key === 'Enter' && handleHost()}
                />
                <button onClick={handleHost}>Create & Host</button>
            </LobbySection>

            <div className="divider">OR</div>

            <LobbySection title="Join Game">
                <input
                    placeholder="Your Name"
                    value={joinName}
                    onChange={e => setJoinName(e.target.value)}
                    onKeyDown={e => e.key === 'Enter' && handleJoin()}
                />
                <input
                    placeholder="Game ID"
                    value={joinId}
                    onChange={e => setJoinId(e.target.value)}
                    onKeyDown={e => e.key === 'Enter' && handleJoin()}
                />
                <button onClick={handleJoin}>Join Game</button>
            </LobbySection>
            <Footer />
        </div>
    );
}
