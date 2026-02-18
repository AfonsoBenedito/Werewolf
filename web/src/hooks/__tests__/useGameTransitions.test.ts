import { describe, it, expect } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { useGameTransitions } from '../useGameTransitions';
import type { GameState } from '../../types/game';

function makeState(overrides: Partial<GameState> = {}): GameState {
    return {
        players: [],
        status: 'IN_PROGRESS',
        phase: 'NIGHT - Wolf',
        dayCount: 1,
        phaseKey: 'NIGHT',
        isMyTurn: false,
        availableActions: [],
        canSkip: false,
        phaseDisplayName: '',
        canStart: false,
        isHost: false,
        minPlayers: 4,
        ...overrides,
    };
}

function drainTransitions(result: { current: ReturnType<typeof useGameTransitions> }) {
    const messages: string[] = [];
    while (result.current.isTransitioning && result.current.currentTransition) {
        messages.push(result.current.currentTransition.message);
        act(() => result.current.handleTransitionComplete());
    }
    return messages;
}

/**
 * Sets up the hook already at a given phase, draining any game-start transitions.
 * This avoids the prevStatus='' → 'IN_PROGRESS' game-start trigger on initial render.
 */
function setupAtPhase(
    phase: string,
    player: { name: string; isAlive: boolean } | null = null
) {
    const hook = renderHook(
        ({ gs, p }) => useGameTransitions(gs, p),
        { initialProps: { gs: makeState({ status: 'NOT_STARTED', phase: 'NOT_STARTED' }), p: player } }
    );
    hook.rerender({ gs: makeState({ phase }), p: player });
    drainTransitions(hook.result);
    return hook;
}

describe('useGameTransitions', () => {

    it('produces transitions on game start (NOT_STARTED -> IN_PROGRESS)', () => {
        const { result, rerender } = renderHook(
            ({ gs, player }) => useGameTransitions(gs, player),
            { initialProps: { gs: makeState({ status: 'NOT_STARTED', phase: 'NOT_STARTED' }), player: null } }
        );

        rerender({ gs: makeState({ status: 'IN_PROGRESS', phase: 'NIGHT - Wolf' }), player: null });

        const messages = drainTransitions(result);
        expect(messages).toContain('The Village goes to sleep...');
        expect(messages).toContain('The Werewolves wake up');
    });

    it('no transitions when gameState is null', () => {
        const { result } = renderHook(() => useGameTransitions(null, null));

        expect(result.current.isTransitioning).toBe(false);
        expect(result.current.currentTransition).toBeNull();
    });


    it('produces transition for Wolf -> Seer turn change', () => {
        const { result, rerender } = setupAtPhase('NIGHT - Wolf');

        rerender({ gs: makeState({ phase: 'NIGHT - Seer' }), p: null });

        const messages = drainTransitions(result);
        expect(messages).toContain('The Werewolves go to sleep...');
        expect(messages).toContain('The Seer wakes up');
    });

    it('produces transition for Seer -> Medic turn change', () => {
        const { result, rerender } = setupAtPhase('NIGHT - Seer');

        rerender({ gs: makeState({ phase: 'NIGHT - Medic' }), p: null });

        const messages = drainTransitions(result);
        expect(messages).toContain('The Seer goes to sleep...');
        expect(messages).toContain('The Medic wakes up');
    });

    it('produces transition for Wolf -> Medic (Seer dead)', () => {
        const { result, rerender } = setupAtPhase('NIGHT - Wolf');

        rerender({ gs: makeState({ phase: 'NIGHT - Medic' }), p: null });

        const messages = drainTransitions(result);
        expect(messages).toContain('The Werewolves go to sleep...');
        expect(messages).toContain('The Medic wakes up');
    });


    it('produces transitions for Night -> Day with death', () => {
        const { result, rerender } = setupAtPhase('NIGHT - Medic');

        rerender({ gs: makeState({ phase: 'DAY_DISCUSSION', lastDeadPlayerName: 'Alice' }), p: null });

        const messages = drainTransitions(result);
        expect(messages).toContain('The Medic goes to sleep...');
        expect(messages).toContain('The Village wakes up with the news that...');
        expect(messages).toContain('Alice died last night!');
    });

    it('produces peaceful night transition when no death', () => {
        const { result, rerender } = setupAtPhase('NIGHT - Medic');

        rerender({ gs: makeState({ phase: 'DAY_DISCUSSION', lastDeadPlayerName: undefined }), p: null });

        const messages = drainTransitions(result);
        expect(messages).toContain('It was a peaceful night.');
    });

    it('shows personalized message when you died', () => {
        const alivePlayer = { name: 'Bob', isAlive: true };
        const deadPlayer = { name: 'Bob', isAlive: false };
        const { result, rerender } = setupAtPhase('NIGHT - Medic', alivePlayer);

        rerender({ gs: makeState({ phase: 'DAY_DISCUSSION', lastDeadPlayerName: 'Bob' }), p: deadPlayer });

        const messages = drainTransitions(result);
        expect(messages).toContain('You died last night!');
    });


    it('produces voting transition', () => {
        const { result, rerender } = setupAtPhase('DAY_DISCUSSION');

        rerender({ gs: makeState({ phase: 'DAY_VOTING' }), p: null });

        const messages = drainTransitions(result);
        expect(messages).toEqual(["Village, Let's vote!"]);
    });

    it('produces results transition with elimination', () => {
        const { result, rerender } = setupAtPhase('DAY_VOTING');

        rerender({ gs: makeState({ phase: 'DAY_RESULTS', lastDeadPlayerName: 'Charlie' }), p: null });

        const messages = drainTransitions(result);
        expect(messages).toContain('Village, the votes are in...');
        expect(messages).toContain('Charlie was eliminated!');
    });

    it('produces results transition with no elimination', () => {
        const { result, rerender } = setupAtPhase('DAY_VOTING');

        rerender({ gs: makeState({ phase: 'DAY_RESULTS' }), p: null });

        const messages = drainTransitions(result);
        expect(messages).toContain('No one was eliminated.');
    });

    it('shows personalized message when you are eliminated', () => {
        const alivePlayer = { name: 'Dave', isAlive: true };
        const deadPlayer = { name: 'Dave', isAlive: false };
        const { result, rerender } = setupAtPhase('DAY_VOTING', alivePlayer);

        rerender({ gs: makeState({ phase: 'DAY_RESULTS', lastDeadPlayerName: 'Dave' }), p: deadPlayer });

        const messages = drainTransitions(result);
        expect(messages).toContain('You were eliminated!');
    });


    it('produces transitions for new night round from DAY_RESULTS', () => {
        const { result, rerender } = setupAtPhase('DAY_RESULTS');

        rerender({ gs: makeState({ phase: 'NIGHT - Wolf' }), p: null });

        const messages = drainTransitions(result);
        expect(messages).toContain('The Village goes to sleep...');
        expect(messages).toContain('The Werewolves wake up');
    });


    it('produces transitions when game ends from night', () => {
        const { result, rerender } = setupAtPhase('NIGHT - Wolf');

        rerender({ gs: makeState({ phase: 'FINISHED', lastDeadPlayerName: 'Eve' }), p: null });

        const messages = drainTransitions(result);
        expect(messages).toContain('The Werewolves go to sleep...');
        expect(messages).toContain('The Village wakes up with the news that...');
        expect(messages).toContain('Eve died last night!');
    });

    it('produces no extra transitions when game ends from DAY_RESULTS', () => {
        const { result, rerender } = setupAtPhase('DAY_RESULTS');

        rerender({ gs: makeState({ phase: 'FINISHED' }), p: null });

        expect(result.current.isTransitioning).toBe(false);
    });


    it('suppresses transitions after dead player sees night', () => {
        const deadPlayer = { name: 'Ghost', isAlive: false };
        const { result, rerender } = setupAtPhase('DAY_DISCUSSION', deadPlayer);

        rerender({ gs: makeState({ phase: 'NIGHT - Wolf' }), p: deadPlayer });
        drainTransitions(result);

        rerender({ gs: makeState({ phase: 'NIGHT - Seer' }), p: deadPlayer });
        expect(result.current.isTransitioning).toBe(false);

        rerender({ gs: makeState({ phase: 'DAY_DISCUSSION' }), p: deadPlayer });
        expect(result.current.isTransitioning).toBe(false);
    });

    it('does not suppress transitions for alive players', () => {
        const alivePlayer = { name: 'Alive', isAlive: true };
        const { result, rerender } = setupAtPhase('NIGHT - Wolf', alivePlayer);

        rerender({ gs: makeState({ phase: 'NIGHT - Seer' }), p: alivePlayer });

        const messages = drainTransitions(result);
        expect(messages.length).toBeGreaterThan(0);
    });

    it('still shows FINISHED transitions for dead spectators', () => {
        const deadPlayer = { name: 'Ghost', isAlive: false };
        const { result, rerender } = setupAtPhase('DAY_RESULTS', deadPlayer);

        rerender({ gs: makeState({ phase: 'NIGHT - Wolf' }), p: deadPlayer });
        drainTransitions(result);

        rerender({ gs: makeState({ phase: 'FINISHED', lastDeadPlayerName: 'Someone' }), p: deadPlayer });

        const messages = drainTransitions(result);
        expect(messages.length).toBeGreaterThan(0);
    });


    it('handleTransitionComplete advances through queue', () => {
        const { result, rerender } = renderHook(
            ({ gs }) => useGameTransitions(gs, null),
            { initialProps: { gs: makeState({ status: 'NOT_STARTED', phase: 'NOT_STARTED' }) } }
        );

        rerender({ gs: makeState({ status: 'IN_PROGRESS', phase: 'NIGHT - Wolf' }) });

        expect(result.current.isTransitioning).toBe(true);
        const first = result.current.currentTransition?.message;
        expect(first).toBe('The Village goes to sleep...');

        act(() => result.current.handleTransitionComplete());

        expect(result.current.isTransitioning).toBe(true);
        expect(result.current.currentTransition?.message).toBe('The Werewolves wake up');

        act(() => result.current.handleTransitionComplete());

        expect(result.current.isTransitioning).toBe(false);
        expect(result.current.currentTransition).toBeNull();
    });


    it('pauses queue processing when isPaused is true', () => {
        const { result, rerender } = renderHook(
            ({ gs, paused }) => useGameTransitions(gs, null, paused),
            { initialProps: { gs: makeState({ status: 'NOT_STARTED', phase: 'NOT_STARTED' }), paused: false } }
        );

        rerender({ gs: makeState({ status: 'IN_PROGRESS', phase: 'NIGHT - Wolf' }), paused: true });

        expect(result.current.isTransitioning).toBe(true);
        expect(result.current.currentTransition?.message).toBe('The Village goes to sleep...');

        act(() => result.current.handleTransitionComplete());

        expect(result.current.currentTransition?.message).toBe('The Werewolves wake up');
    });


    it('does not produce transitions when phase stays the same', () => {
        const { result, rerender } = setupAtPhase('NIGHT - Wolf');

        rerender({ gs: makeState({ phase: 'NIGHT - Wolf' }), p: null });

        expect(result.current.isTransitioning).toBe(false);
    });
});
