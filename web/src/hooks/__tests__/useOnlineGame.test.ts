import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { renderHook, act, waitFor } from '@testing-library/react';
import { useOnlineGame } from '../useOnlineGame';
const mockUseParams = vi.fn(() => ({ gameId: 'game1' }));
vi.mock('react-router-dom', () => ({
    useParams: () => mockUseParams(),
}));

let stompMessageCallback: ((msg: { body: string }) => void) | null = null;

vi.mock('sockjs-client', () => {
    return {
        default: function SockJS() { return {}; },
    };
});

vi.mock('@stomp/stompjs', () => ({
    Client: function MockClient(opts: Record<string, unknown>) {
        const onConnect = opts.onConnect as () => void;
        return {
            activate: vi.fn(() => onConnect?.()),
            deactivate: vi.fn(),
            subscribe: vi.fn((_dest: string, cb: (msg: { body: string }) => void) => {
                stompMessageCallback = cb;
            }),
        };
    },
}));

vi.mock('../../api/gameApi', () => ({
    getGameState: vi.fn(),
    performAction: vi.fn(),
    startGame: vi.fn(),
}));

import { getGameState, performAction, startGame } from '../../api/gameApi';

beforeEach(() => {
    vi.clearAllMocks();
    stompMessageCallback = null;
    sessionStorage.clear();
    sessionStorage.setItem('werewolf_token', 'test-token');
    sessionStorage.setItem('werewolf_player', 'Alice');
    mockUseParams.mockReturnValue({ gameId: 'game1' });
});

afterEach(() => {
    vi.restoreAllMocks();
});

function mockGameState(overrides = {}) {
    return {
        players: [
            { name: 'Alice', isAlive: true, role: 'Villager', isTargetable: false, hasVoted: false },
            { name: 'Bob', isAlive: true, role: 'Werewolf', isTargetable: true, hasVoted: false },
        ],
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

/** Renders the hook and waits for initial loading to complete */
async function setupHook() {
    vi.mocked(getGameState).mockResolvedValue(mockGameState());
    const hook = renderHook(() => useOnlineGame());
    await waitFor(() => {
        expect(hook.result.current.isLoading).toBe(false);
    });
    return hook;
}

describe('useOnlineGame', () => {
    it('fetches initial game state and finds myPlayer', async () => {
        const state = mockGameState();
        vi.mocked(getGameState).mockResolvedValue(state);

        const { result } = renderHook(() => useOnlineGame());

        await waitFor(() => {
            expect(result.current.isLoading).toBe(false);
        });

        expect(getGameState).toHaveBeenCalledWith('game1', 'test-token');
        expect(result.current.gameState).toEqual(state);
        expect(result.current.myPlayer?.name).toBe('Alice');
        expect(result.current.error).toBeNull();
    });

    it('sets error when game is not found', async () => {
        vi.mocked(getGameState).mockResolvedValue(null);

        const { result } = renderHook(() => useOnlineGame());

        await waitFor(() => {
            expect(result.current.isLoading).toBe(false);
        });

        expect(result.current.error).toBe('Game not found');
        expect(result.current.gameState).toBeNull();
    });

    it('sets error when fetch fails', async () => {
        vi.mocked(getGameState).mockRejectedValue(new Error('network'));

        const { result } = renderHook(() => useOnlineGame());

        await waitFor(() => {
            expect(result.current.isLoading).toBe(false);
        });

        expect(result.current.error).toBe('Failed to load game');
    });

    it('skips fetch when gameId is missing', () => {
        mockUseParams.mockReturnValue({} as { gameId: string });

        const { result } = renderHook(() => useOnlineGame());

        expect(getGameState).not.toHaveBeenCalled();
        expect(result.current.gameState).toBeNull();
    });


    it('handleStart calls startGame and refetches', async () => {
        const { result } = await setupHook();
        vi.mocked(startGame).mockResolvedValue({});

        vi.mocked(getGameState).mockClear();
        await act(() => result.current.handleStart());

        expect(startGame).toHaveBeenCalledWith('game1');
        expect(getGameState).toHaveBeenCalled();
    });


    it('handleAction sets hasVotedReady for READY_TO_VOTE', async () => {
        vi.mocked(performAction).mockResolvedValue({});
        const { result } = await setupHook();

        expect(result.current.hasVotedReady).toBe(false);

        await act(() => result.current.handleAction('READY_TO_VOTE'));

        expect(result.current.hasVotedReady).toBe(true);
    });

    it('handleAction tracks night target for KILL/HEAL/PEEK', async () => {
        vi.mocked(performAction).mockResolvedValue({});
        const { result } = await setupHook();

        await act(() => result.current.handleAction('KILL', 'Bob'));

        expect(result.current.myNightTarget).toBe('Bob');
        expect(performAction).toHaveBeenCalledWith('game1', 'test-token', 'KILL', 'Bob');
    });

    it('handleAction stores seer peekResult', async () => {
        vi.mocked(performAction).mockResolvedValue({ peekResult: 'Werewolf' });
        const { result } = await setupHook();

        await act(() => result.current.handleAction('PEEK', 'Bob'));

        expect(result.current.seerResult).toBe('Werewolf');
    });

    it('handleAction stores night action feedback from string response', async () => {
        vi.mocked(performAction).mockResolvedValue('Waiting for other wolves...');
        const { result } = await setupHook();

        await act(() => result.current.handleAction('KILL', 'Bob'));

        expect(result.current.nightActionFeedback).toBe('Waiting for other wolves...');
    });

    it('handleAction alerts on error', async () => {
        window.alert = vi.fn();
        vi.mocked(performAction).mockRejectedValue({
            response: { data: { message: 'Not your turn' } },
        });
        const { result } = await setupHook();

        await act(() => result.current.handleAction('KILL', 'Bob'));

        expect(window.alert).toHaveBeenCalledWith('Action failed: Not your turn');
    });


    it('resets local state on phase change', async () => {
        vi.mocked(performAction).mockResolvedValue({});
        const { result } = await setupHook();

        await act(() => result.current.handleAction('READY_TO_VOTE'));
        expect(result.current.hasVotedReady).toBe(true);

        vi.mocked(getGameState).mockResolvedValue(mockGameState({ phase: 'NIGHT - Seer' }));
        await act(() => result.current.fetchState());

        expect(result.current.hasVotedReady).toBe(false);
        expect(result.current.myNightTarget).toBeNull();
        expect(result.current.seerResult).toBeNull();
    });

    it('does not call performAction when token is missing', async () => {
        sessionStorage.clear();
        vi.mocked(getGameState).mockResolvedValue(mockGameState());

        const { result } = renderHook(() => useOnlineGame());

        await waitFor(() => {
            expect(result.current.isLoading).toBe(false);
        });

        await act(() => result.current.handleAction('KILL', 'Bob'));

        expect(performAction).not.toHaveBeenCalled();
    });


    it('subscribes to WebSocket and refetches on UPDATE', async () => {
        const { result } = await setupHook();

        expect(stompMessageCallback).not.toBeNull();
        expect(result.current.isConnected).toBe(true);

        vi.mocked(getGameState).mockClear();

        await act(async () => {
            stompMessageCallback!({ body: 'UPDATE' });
        });

        await waitFor(() => {
            expect(getGameState).toHaveBeenCalled();
        });
    });
});
