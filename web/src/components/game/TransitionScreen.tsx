import React, { useEffect } from 'react';
import '../../styles/components/TransitionScreen.css';

interface TransitionScreenProps {
    message: string;
    description?: string;
    onComplete?: () => void;
    duration?: number;
    manualContinue?: boolean;
    actionLabel?: string;
}

export const TransitionScreen: React.FC<TransitionScreenProps> = ({
    message,
    description,
    onComplete,
    duration = 3000,
    manualContinue = false,
    actionLabel = "Next"
}) => {
    const [isExiting, setIsExiting] = React.useState(false);

    // Reset exit state when message changes
    useEffect(() => {
        setIsExiting(false);
    }, [message]);

    useEffect(() => {
        if (manualContinue) return;

        const timer = setTimeout(() => {
            onComplete?.();
        }, duration);

        return () => clearTimeout(timer);
    }, [onComplete, duration, manualContinue, message]);

    const handleNext = () => {
        setIsExiting(true);
        // Match the fadeOut animation duration (1s)
        setTimeout(() => {
            onComplete?.();
            setIsExiting(false);
        }, 1000);
    };

    const getAnimationClass = () => {
        if (isExiting) return 'exiting';
        if (manualContinue) return 'manual-mode';
        return '';
    };

    return (
        <div className="transition-screen">
            <div className="transition-content">
                <h1 className={`transition-message ${getAnimationClass()}`} key={message}>
                    {message}
                </h1>
                {description && <p className="transition-description">{description}</p>}
            </div>
            <div className="transition-btn-area">
                {manualContinue && !isExiting && (
                    <button className="transition-btn" onClick={handleNext}>
                        {actionLabel}
                    </button>
                )}
            </div>
        </div>
    );
};
