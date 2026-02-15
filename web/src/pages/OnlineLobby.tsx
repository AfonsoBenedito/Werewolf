import { Home, Users, Play } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useOnlineLobby } from '../hooks/useOnlineLobby';
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
            <button className="home-btn-fixed" onClick={() => navigate('/')}>
                <Home size={20} />
            </button>

            <h1>Online Lobby</h1>

            <div className="lobby-container">
                {error && (
                    <div className="error-message">
                        {error}
                    </div>
                )}

                <div className="lobby-section">
                    <h2>Host New Game</h2>
                    <div className="lobby-input-group">
                        <input
                            type="text"
                            className="lobby-input"
                            placeholder="Your Name"
                            value={hostName}
                            onChange={(e) => setHostName(e.target.value)}
                        />
                        <button className="lobby-btn lobby-btn--primary" onClick={handleHost}>
                            <Play size={18} />
                            Create Game
                        </button>
                    </div>
                </div>

                <div className="lobby-section-divider"></div>

                <div className="lobby-section">
                    <h2>Join Existing Game</h2>
                    <div className="lobby-input-group">
                        <input
                            type="text"
                            className="lobby-input"
                            placeholder="Game ID"
                            value={joinId}
                            onChange={(e) => setJoinId(e.target.value.toUpperCase())}
                        />
                        <input
                            type="text"
                            className="lobby-input"
                            placeholder="Your Name"
                            value={joinName}
                            onChange={(e) => setJoinName(e.target.value)}
                        />
                        <button className="lobby-btn lobby-btn--secondary" onClick={handleJoin}>
                            <Users size={18} />
                            Join Game
                        </button>
                    </div>
                </div>
            </div>

            <Footer />
        </div>
    );
}
