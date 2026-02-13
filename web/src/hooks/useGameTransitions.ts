import { useState, useEffect, useRef, useCallback } from 'react';
import type { GameState } from './useOnlineGame';

interface Transition {
    message: string;
    duration: number;
}

export function useGameTransitions(
    gameState: GameState | null,
    myPlayer: { name: string, isAlive: boolean } | null,
    isPaused: boolean = false
) {
    const [currentTransition, setCurrentTransition] = useState<Transition | null>(null);
    const [isTransitioning, setIsTransitioning] = useState(false);
    const lastPhaseRef = useRef<string>('');
    const lastStatusRef = useRef<string>('');
    const transitionQueue = useRef<Transition[]>([]);
    const hasSeenNightDead = useRef(false);

    useEffect(() => {
        if (!gameState) return;

        const prevPhase = lastPhaseRef.current;
        const currentPhase = gameState.phase; // E.g., "NIGHT - Wolf", "DAY_DISCUSSION", etc.
        const prevStatus = lastStatusRef.current;
        const currentStatus = gameState.status;

        // Spectator Mode Logic:
        // Spectators (Dead players) should still see the transitions for the "day" they died.
        // They only stop seeing transitions once they encounter a Night phase while dead.
        if (myPlayer && !myPlayer.isAlive && currentPhase.includes('NIGHT')) {
            hasSeenNightDead.current = true;
        }

        if (hasSeenNightDead.current) {
            transitionQueue.current = [];
            setIsTransitioning(false);
            setCurrentTransition(null);
            lastPhaseRef.current = currentPhase;
            lastStatusRef.current = currentStatus;
            return;
        }

        if ((prevStatus === 'NOT_STARTED' || prevStatus === '') && currentStatus === 'IN_PROGRESS') {
            const newTransitions: Transition[] = [
                { message: "The Village goes to sleep...", duration: 3000 }
            ];
            if (currentPhase.includes('Wolf')) {
                newTransitions.push({ message: "The Werewolves wake up", duration: 3000 });
            }
            transitionQueue.current = [...transitionQueue.current, ...newTransitions];

            // Start processing the first transition immediately
            if (!isTransitioning) {
                const next = transitionQueue.current.shift();
                setIsTransitioning(true);
                setCurrentTransition(next || null);
            }
        } else if (prevPhase !== currentPhase) {
            const newTransitions: Transition[] = [];

            // 1. Initial Start
            if (prevPhase === 'NOT_STARTED' && currentPhase.includes('NIGHT')) {
                newTransitions.push({ message: "The Village goes to sleep...", duration: 3000 });
                if (currentPhase.includes('Wolf')) {
                    newTransitions.push({ message: "The Werewolves wake up", duration: 3000 });
                }
            }

            // 2. Night Phase Changes
            else if (currentPhase.includes('NIGHT')) {
                // Determine current role turn
                const currentTurn = currentPhase.split(' - ')[1] || '';
                const prevTurn = prevPhase.includes('NIGHT') ? prevPhase.split(' - ')[1] || '' : '';

                // Wolf Turn
                if (currentTurn === 'Wolf' && prevPhase !== 'NOT_STARTED') {
                    // If coming from DayResults -> Night, usually "Village Sleep" first
                    if (prevPhase === 'DAY_RESULTS') {
                        newTransitions.push({ message: "The Village goes to sleep...", duration: 3000 });
                    }
                    newTransitions.push({ message: "The Werewolves wake up", duration: 3000 });
                }

                // Seer Turn
                if (currentTurn === 'Seer') {
                    if (prevTurn === 'Wolf') {
                        newTransitions.push({ message: "The Werewolves go to sleep...", duration: 3000 });
                    }
                    newTransitions.push({ message: "The Seer wakes up", duration: 3000 });
                }

                // Medic Turn
                if (currentTurn === 'Medic') {
                    if (prevTurn === 'Seer') {
                        newTransitions.push({ message: "The Seer goes to sleep...", duration: 3000 });
                    }
                    // If coming from Wolf directly? (If Seer dead)
                    else if (prevTurn === 'Wolf') {
                        newTransitions.push({ message: "The Werewolves go to sleep...", duration: 3000 });
                    }
                    newTransitions.push({ message: "The Medic wakes up", duration: 3000 });
                }
            }

            // 3. Night -> Day
            else if (currentPhase === 'DAY_DISCUSSION') {
                const prevTurn = prevPhase.includes('NIGHT') ? prevPhase.split(' - ')[1] || '' : '';
                if (prevTurn === 'Medic') {
                    newTransitions.push({ message: "The Medic goes to sleep...", duration: 3000 });
                } else if (prevTurn === 'Seer') {
                    newTransitions.push({ message: "The Seer goes to sleep...", duration: 3000 });
                } else if (prevTurn === 'Wolf') {
                    newTransitions.push({ message: "The Werewolves go to sleep...", duration: 3000 });
                }

                newTransitions.push({ message: "The Village wakes up with the news that...", duration: 3000 });

                // Announce night death
                const victim = gameState.lastDeadPlayerName;
                if (victim) {
                    if (myPlayer && myPlayer.name === victim) {
                        newTransitions.push({ message: "You died last night!", duration: 4000 });
                    } else {
                        newTransitions.push({ message: `${victim} died last night!`, duration: 4000 });
                    }
                } else {
                    newTransitions.push({ message: "It was a peaceful night.", duration: 3000 });
                }
            }

            // 4. Day -> Voting
            else if (currentPhase === 'DAY_VOTING') {
                newTransitions.push({ message: "Village, Let's vote!", duration: 3000 });
            }

            // 5. Voting -> Results
            else if (currentPhase === 'DAY_RESULTS') {

                newTransitions.push({ message: "Village, the votes are in...", duration: 3000 });

                // Announce vote result
                const victim = gameState.lastDeadPlayerName;
                if (victim) {
                    if (myPlayer && myPlayer.name === victim) {
                        newTransitions.push({ message: "You were eliminated!", duration: 4000 });
                    } else {
                        newTransitions.push({ message: `${victim} was eliminated!`, duration: 4000 });
                    }
                } else {
                    newTransitions.push({ message: "No one was eliminated.", duration: 4000 });
                }
            }

            if (newTransitions.length > 0) {
                transitionQueue.current = [...transitionQueue.current, ...newTransitions];

                // If not already transitioning, start the first one immediately
                if (!isTransitioning) {
                    const next = transitionQueue.current.shift();
                    setIsTransitioning(true);
                    setCurrentTransition(next || null);
                }
            }
        }

        lastPhaseRef.current = currentPhase;
        lastStatusRef.current = currentStatus;
    }, [gameState, myPlayer?.isAlive]);

    // Process queue
    useEffect(() => {
        if (isPaused) return;
        if (isTransitioning) return;
        if (transitionQueue.current.length > 0) {
            const next = transitionQueue.current.shift();
            setIsTransitioning(true);
            setCurrentTransition(next || null);
        }
    }, [isTransitioning, transitionQueue.current.length, isPaused]); // Re-run when transitioning finishes or queue grows


    const handleTransitionComplete = useCallback(() => {
        if (transitionQueue.current.length > 0) {
            // If there are more transitions, show the next one immediately
            // This prevents the component from unmounting and causing "blinks"
            const next = transitionQueue.current.shift();
            setCurrentTransition(next || null);
            // isTransitioning stays true
        } else {
            // Queue empty, finish transitioning
            setIsTransitioning(false);
            setCurrentTransition(null);
        }
    }, [isTransitioning]);

    return {
        isTransitioning,
        currentTransition,
        handleTransitionComplete
    };
}
