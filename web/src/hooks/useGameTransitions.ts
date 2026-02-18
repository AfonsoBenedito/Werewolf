import { useState, useEffect, useRef, useCallback } from 'react';
import type { GameState } from '../types/game';

interface Transition {
    message: string;
    duration: number;
}

function buildPhaseTransitions(
    prevPhase: string,
    currentPhase: string,
    gameState: GameState,
    myPlayer: { name: string, isAlive: boolean } | null
): Transition[] {
    const transitions: Transition[] = [];

    // Night Phase Changes
    if (currentPhase.includes('NIGHT')) {
        const currentTurn = currentPhase.split(' - ')[1] || '';
        const prevTurn = prevPhase.includes('NIGHT') ? prevPhase.split(' - ')[1] || '' : '';

        if (currentTurn === 'Wolf' && prevPhase !== 'NOT_STARTED') {
            if (prevPhase === 'DAY_RESULTS') {
                transitions.push({ message: "The Village goes to sleep...", duration: 2000 });
            }
            transitions.push({ message: "The Werewolves wake up", duration: 2000 });
        }

        if (currentTurn === 'Seer') {
            if (prevTurn === 'Wolf') {
                transitions.push({ message: "The Werewolves go to sleep...", duration: 2000 });
            }
            transitions.push({ message: "The Seer wakes up", duration: 2000 });
        }

        if (currentTurn === 'Medic') {
            if (prevTurn === 'Seer') {
                transitions.push({ message: "The Seer goes to sleep...", duration: 2000 });
            } else if (prevTurn === 'Wolf') {
                transitions.push({ message: "The Werewolves go to sleep...", duration: 2000 });
            }
            transitions.push({ message: "The Medic wakes up", duration: 2000 });
        }
    }

    // Night -> Day
    else if (currentPhase === 'DAY_DISCUSSION') {
        const prevTurn = prevPhase.includes('NIGHT') ? prevPhase.split(' - ')[1] || '' : '';
        if (prevTurn === 'Medic') {
            transitions.push({ message: "The Medic goes to sleep...", duration: 2000 });
        } else if (prevTurn === 'Seer') {
            transitions.push({ message: "The Seer goes to sleep...", duration: 2000 });
        } else if (prevTurn === 'Wolf') {
            transitions.push({ message: "The Werewolves go to sleep...", duration: 2000 });
        }

        transitions.push({ message: "The Village wakes up with the news that...", duration: 2000 });

        const victim = gameState.lastDeadPlayerName;
        if (victim) {
            if (myPlayer && myPlayer.name === victim) {
                transitions.push({ message: "You died last night!", duration: 2500 });
            } else {
                transitions.push({ message: `${victim} died last night!`, duration: 2500 });
            }
        } else {
            transitions.push({ message: "It was a peaceful night.", duration: 2000 });
        }
    }

    // Day -> Voting
    else if (currentPhase === 'DAY_VOTING') {
        transitions.push({ message: "Village, Let's vote!", duration: 2000 });
    }

    // Voting -> Results
    else if (currentPhase === 'DAY_RESULTS') {
        transitions.push({ message: "Village, the votes are in...", duration: 2000 });

        const victim = gameState.lastDeadPlayerName;
        if (victim) {
            if (myPlayer && myPlayer.name === victim) {
                transitions.push({ message: "You were eliminated!", duration: 2500 });
            } else {
                transitions.push({ message: `${victim} was eliminated!`, duration: 2500 });
            }
        } else {
            transitions.push({ message: "No one was eliminated.", duration: 2500 });
        }
    }

    // Game Finished — show narrative before winner screen
    else if (currentPhase === 'FINISHED') {
        if (prevPhase.includes('NIGHT')) {
            const prevTurn = prevPhase.split(' - ')[1] || '';
            if (prevTurn === 'Medic') {
                transitions.push({ message: "The Medic goes to sleep...", duration: 2000 });
            } else if (prevTurn === 'Seer') {
                transitions.push({ message: "The Seer goes to sleep...", duration: 2000 });
            } else if (prevTurn === 'Wolf') {
                transitions.push({ message: "The Werewolves go to sleep...", duration: 2000 });
            }

            transitions.push({ message: "The Village wakes up with the news that...", duration: 2000 });

            const victim = gameState.lastDeadPlayerName;
            if (victim) {
                if (myPlayer && myPlayer.name === victim) {
                    transitions.push({ message: "You died last night!", duration: 2500 });
                } else {
                    transitions.push({ message: `${victim} died last night!`, duration: 2500 });
                }
            }
        }
        // Day vote ended the game (prev was DAY_RESULTS) — transitions already played
    }

    return transitions;
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

    // State machine effect — intentionally uses setState to drive the transition queue.
    // Deps are intentionally limited to avoid re-running on every isTransitioning/myPlayer change.
    useEffect(() => {
        if (!gameState) return;

        const prevPhase = lastPhaseRef.current;
        const currentPhase = gameState.phase;
        const prevStatus = lastStatusRef.current;
        const currentStatus = gameState.status;

        // Spectator Mode: dead players stop seeing transitions after encountering a night phase
        if (myPlayer && !myPlayer.isAlive && currentPhase.includes('NIGHT')) {
            hasSeenNightDead.current = true;
        }

        if (hasSeenNightDead.current && currentPhase !== 'FINISHED') {
            transitionQueue.current = [];
            setIsTransitioning(false);
            setCurrentTransition(null);
            lastPhaseRef.current = currentPhase;
            lastStatusRef.current = currentStatus;
            return;
        }

        let newTransitions: Transition[] = [];

        if ((prevStatus === 'NOT_STARTED' || prevStatus === '') && currentStatus === 'IN_PROGRESS') {
            newTransitions.push({ message: "The Village goes to sleep...", duration: 2000 });
            if (currentPhase.includes('Wolf')) {
                newTransitions.push({ message: "The Werewolves wake up", duration: 2000 });
            }
        } else if (prevPhase !== currentPhase) {
            newTransitions = buildPhaseTransitions(prevPhase, currentPhase, gameState, myPlayer);
        }

        if (newTransitions.length > 0) {
            transitionQueue.current = [...transitionQueue.current, ...newTransitions];

            if (!isTransitioning) {
                const next = transitionQueue.current.shift()!;
                setIsTransitioning(true);
                setCurrentTransition(next);
            }
        }

        lastPhaseRef.current = currentPhase;
        lastStatusRef.current = currentStatus;
        // eslint-disable-next-line react-hooks/exhaustive-deps -- intentional: only react to gameState/isAlive changes
    }, [gameState, myPlayer?.isAlive]);

    // Process queue when a transition completes and more items are waiting
    useEffect(() => {
        if (isPaused) return;
        if (isTransitioning) return;
        if (transitionQueue.current.length > 0) {
            const next = transitionQueue.current.shift()!;
            setIsTransitioning(true);
            setCurrentTransition(next);
        }
    }, [isTransitioning, isPaused]);

    const handleTransitionComplete = useCallback(() => {
        if (transitionQueue.current.length > 0) {
            const next = transitionQueue.current.shift()!;
            setCurrentTransition(next);
        } else {
            setIsTransitioning(false);
            setCurrentTransition(null);
        }
    }, []);

    return {
        isTransitioning,
        currentTransition,
        handleTransitionComplete
    };
}
