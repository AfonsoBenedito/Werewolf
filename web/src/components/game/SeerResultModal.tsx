import React from 'react';
import '../../styles/components/SeerResultModal.css';

interface SeerResultModalProps {
    result: string;
    onDismiss: () => void;
}

export const SeerResultModal: React.FC<SeerResultModalProps> = ({ result, onDismiss }) => {
    return (
        <div className="seer-result-overlay">
            <div className="seer-result-content">
                <h2>Seer Vision</h2>
                <p className="seer-result-text">{result}</p>
                <button className="seer-result-btn" onClick={onDismiss}>OK</button>
            </div>
        </div>
    );
};
