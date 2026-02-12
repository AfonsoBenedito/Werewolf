import { useNavigate } from 'react-router-dom';
import { Footer } from '../components/common/Footer';
import '../styles/pages/LandingPage.css';

export default function LandingPage() {
    const navigate = useNavigate();

    return (
        <div className="landing">
            {/* Background layer */}
            <div className="landing__bg" aria-hidden="true" />
            <div className="landing__overlay" aria-hidden="true" />

            {/* Hero content */}
            <main className="landing__hero">
                <h1 className="landing__title">WEREWOLF</h1>
                <p className="landing__tagline">The classic social deduction game</p>

                <div className="landing__ctas">
                    <button
                        className="landing__btn landing__btn--primary"
                        onClick={() => navigate('/online')}
                    >
                        <span className="landing__btn-pulse" aria-hidden="true" />
                        Online Mode
                    </button>
                    <button
                        className="landing__btn landing__btn--outline"
                        onClick={() => navigate('/offline')}
                    >
                        Offline Mode
                    </button>
                </div>
            </main>

            {/* Footer */}
            <Footer />
        </div>
    );
}
