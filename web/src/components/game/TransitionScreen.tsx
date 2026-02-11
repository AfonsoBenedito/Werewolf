import React, { useEffect } from 'react';
import '../../styles/components/TransitionScreen.css';

interface TransitionScreenProps {
    message: string;
    description?: string;
    onComplete?: () => void;
    duration?: number;
}

export const TransitionScreen: React.FC<TransitionScreenProps> = ({
    message,
    description,
    onComplete,
    duration = 3000
}) => {
    useEffect(() => {
        const timer = setTimeout(() => {
            onComplete?.();
        }, duration);

        return () => clearTimeout(timer);
    }, [onComplete, duration]);

    return (
        <div className="transition-screen" key={message}>
            <h1 className="transition-message">{message}</h1>
            {description && <p className="transition-description">{description}</p>}
        </div>
    );
};
