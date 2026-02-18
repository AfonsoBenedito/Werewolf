import { useNavigate } from 'react-router-dom';
import { Footer } from '../components/common/Footer';
import { useConnectionQuality } from '../hooks/useConnectionQuality';
import '../styles/pages/LandingPage.css';

export default function LandingPage() {
    const navigate = useNavigate();
    const { shouldLoadVideo } = useConnectionQuality();

    return (
        <div className="landing">
            {shouldLoadVideo ? (
                <video
                    className="landing__video-bg"
                    autoPlay
                    muted
                    loop
                    playsInline
                    poster="/werewolf.png"
                >
                    <source src="/background.mp4" type="video/mp4" />
                </video>
            ) : (
                <div className="landing__bg" aria-hidden="true" />
            )}
            <div className="landing__overlay" aria-hidden="true" />

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

            <Footer />
        </div>
    );
}
