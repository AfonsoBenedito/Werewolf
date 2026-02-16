import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import LandingPage from './pages/LandingPage';
import OfflineGame from './pages/OfflineGame';
import OnlineLobby from './pages/OnlineLobby';
import OnlineGame from './pages/OnlineGame';

function getBasename() {
  const path = window.location.pathname;
  if (path.startsWith('/werewolf')) return '/werewolf';
  return '/';
}

function App() {
  return (
    <Router basename={getBasename()}>
      <div className="app-container">
        <Routes>
          <Route path="/" element={<LandingPage />} />
          <Route path="/offline" element={<OfflineGame />} />
          <Route path="/online" element={<OnlineLobby />} />
          <Route path="/online/game/:gameId" element={<OnlineGame />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
