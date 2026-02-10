import { useOnlineLobby } from '../hooks/useOnlineLobby';
import { LobbySection } from '../components/LobbySection';
import '../styles/OnlineLobby.css';

export default function OnlineLobby() {
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
            <h1>Online Lobby</h1>
            {error && <p className="error">{error}</p>}

            <LobbySection title="Host Game">
                <input placeholder="Your Name" value={hostName} onChange={e => setHostName(e.target.value)} />
                <button onClick={handleHost}>Create & Host</button>
            </LobbySection>

            <div className="divider">OR</div>

            <LobbySection title="Join Game">
                <input placeholder="Your Name" value={joinName} onChange={e => setJoinName(e.target.value)} />
                <input placeholder="Game ID" value={joinId} onChange={e => setJoinId(e.target.value)} />
                <button onClick={handleJoin}>Join Game</button>
            </LobbySection>
        </div>
    );
}
