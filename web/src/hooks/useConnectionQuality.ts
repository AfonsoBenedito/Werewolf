import { useState, useEffect } from 'react';

/**
 * Hook to detect if the user has a "good" connection for high-bandwidth content like video.
 * Checks for Network Information API support and looks at effectiveType and saveData.
 */
export function useConnectionQuality() {
    const [shouldLoadVideo, setShouldLoadVideo] = useState(false);

    useEffect(() => {
        const checkConnection = () => {
            // @ts-ignore - Network Information API is not yet in standard TS lib
            const connection = navigator.connection || navigator.mozConnection || navigator.webkitConnection;

            if (!connection) {
                // If the API is not supported, we default to "false" to be safe,
                // or we could default to "true" if we want to be aggressive.
                // Given the atmospheric theme, let's be conservative.
                setShouldLoadVideo(false);
                return;
            }

            // check effective connection type
            // types: 'slow-2g', '2g', '3g', '4g'
            const isFast = connection.effectiveType === '4g';
            const isSaveData = connection.saveData === true;

            setShouldLoadVideo(isFast && !isSaveData);
        };

        checkConnection();

        // Optional: Listen for changes (e.g. user moves from WiFi to slow mobile data)
        // @ts-ignore
        const connection = navigator.connection || navigator.mozConnection || navigator.webkitConnection;
        if (connection) {
            connection.addEventListener('change', checkConnection);
            return () => connection.removeEventListener('change', checkConnection);
        }
    }, []);

    return { shouldLoadVideo };
}
