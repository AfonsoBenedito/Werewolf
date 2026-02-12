import { Instagram, Twitter, MessageCircle } from 'lucide-react';
import '../../styles/components/common/Footer.css';

export function Footer() {
    return (
        <footer className="site-footer">
            <div className="site-footer__inner">
                <p className="site-footer__copyright">&copy; 2026 Werewolf. All rights reserved.</p>
                <div className="site-footer__socials">
                    <a href="#" className="site-footer__social-link" aria-label="Instagram">
                        <Instagram size={18} />
                    </a>
                    <a href="#" className="site-footer__social-link" aria-label="Twitter / X">
                        <Twitter size={18} />
                    </a>
                    <a href="#" className="site-footer__social-link" aria-label="Discord">
                        <MessageCircle size={18} />
                    </a>
                </div>
            </div>
        </footer>
    );
}
