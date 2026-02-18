import { useState, useEffect } from 'react';

/**
 * Hook to detect if the user has a "good" connection for high-bandwidth content like video.
 * Checks for Network Information API support and looks at effectiveType and saveData.
 */
export function useConnectionQuality() {
    const [shouldLoadVideo, setShouldLoadVideo] = useState(false);

    useEffect(() => {
        // @ts-expect-error - Network Information API is not yet in standard TS lib
        const connection = navigator.connection || navigator.mozConnection || navigator.webkitConnection;

        const checkConnection = () => {
            if (!connection) {
                setShouldLoadVideo(false);
                return;
            }

            const isFast = connection.effectiveType === '4g';
            const isSaveData = connection.saveData === true;

            setShouldLoadVideo(isFast && !isSaveData);
        };

        checkConnection();

        if (connection) {
            connection.addEventListener('change', checkConnection);
            return () => connection.removeEventListener('change', checkConnection);
        }
    }, []);

    return { shouldLoadVideo };
}
