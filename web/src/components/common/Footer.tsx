import { Instagram, Linkedin, Github, Mail } from 'lucide-react';
import '../../styles/components/common/Footer.css';

export function Footer() {
    return (
        <footer className="site-footer">
            <div className="site-footer__inner">
                <p className="site-footer__copyright">
                    Werewolf by <a href="https://afonsobenedito.com" target="_blank" rel="noopener noreferrer" className="site-footer__author-link">Afonso Benedito</a>
                </p>
                <div className="site-footer__socials">
                    <a href="http://github.afonsobenedito.com/" target="_blank" rel="noopener noreferrer" className="site-footer__social-link" aria-label="GitHub">
                        <Github size={18} />
                    </a>
                    <a href="https://linkedin.afonsobenedito.com/" target="_blank" rel="noopener noreferrer" className="site-footer__social-link" aria-label="LinkedIn">
                        <Linkedin size={18} />
                    </a>
                    <a href="mailto:afonso.benedito23@gmail.com" className="site-footer__social-link" aria-label="Email">
                        <Mail size={18} />
                    </a>
                    <a href="https://instagram.afonsobenedito.com/" target="_blank" rel="noopener noreferrer" className="site-footer__social-link" aria-label="Instagram">
                        <Instagram size={18} />
                    </a>
                </div>
            </div>
        </footer>
    );
}
