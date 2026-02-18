import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { useOfflineGame } from '../useOfflineGame';
import type { GameState } from '../../types/game';

vi.mock('../../api/gameApi', () => ({
    createGame: vi.fn(),
    startGame: vi.fn(),
    getGameState: vi.fn(),
    performAction: vi.fn(),
}));

import { createGame, startGame, getGameState, performAction } from '../../api/gameApi';

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
        phaseDisplayName: 'Night - Werewolf Turn',
        canStart: false,
        isHost: false,
        minPlayers: 4,
        currentTurn: 'Wolf',
        turnInstruction: 'Choose a victim',
        ...overrides,
    };
}

beforeEach(() => {
    vi.clearAllMocks();
    vi.useFakeTimers();
});

describe('useOfflineGame', () => {

    it('starts with empty local players', () => {
        const { result } = renderHook(() => useOfflineGame());

        expect(result.current.localPlayers).toEqual([]);
        expect(result.current.gameId).toBeNull();
        expect(result.current.gameState).toBeNull();
    });

    it('adds players locally', () => {
        const { result } = renderHook(() => useOfflineGame());

        act(() => result.current.setNewPlayerName('Alice'));
        act(() => result.current.handleAddPlayer());

        expect(result.current.localPlayers).toEqual(['Alice']);
        expect(result.current.newPlayerName).toBe('');
    });

    it('prevents duplicate player names', () => {
        window.alert = vi.fn();
        const alertSpy = vi.spyOn(window, 'alert');
        const { result } = renderHook(() => useOfflineGame());

        act(() => result.current.setNewPlayerName('Alice'));
        act(() => result.current.handleAddPlayer());
        act(() => result.current.setNewPlayerName('Alice'));
        act(() => result.current.handleAddPlayer());

        expect(result.current.localPlayers).toEqual(['Alice']);
        expect(alertSpy).toHaveBeenCalledWith('Player name already exists!');
        alertSpy.mockRestore();
    });

    it('ignores empty player names', () => {
        const { result } = renderHook(() => useOfflineGame());

        act(() => result.current.setNewPlayerName('   '));
        act(() => result.current.handleAddPlayer());

        expect(result.current.localPlayers).toEqual([]);
    });

    it('removes players locally', () => {
        const { result } = renderHook(() => useOfflineGame());

        act(() => result.current.setNewPlayerName('Alice'));
        act(() => result.current.handleAddPlayer());
        act(() => result.current.setNewPlayerName('Bob'));
        act(() => result.current.handleAddPlayer());

        act(() => result.current.handleRemovePlayer('Alice'));

        expect(result.current.localPlayers).toEqual(['Bob']);
    });


    it('requires at least 4 players to start', async () => {
        window.alert = vi.fn();
        const alertSpy = vi.spyOn(window, 'alert');
        const { result } = renderHook(() => useOfflineGame());

        for (const name of ['A', 'B', 'C']) {
            act(() => result.current.setNewPlayerName(name));
            act(() => result.current.handleAddPlayer());
        }

        await act(() => result.current.handleStartGame());

        expect(alertSpy).toHaveBeenCalledWith('Need at least 4 players to start!');
        expect(createGame).not.toHaveBeenCalled();
        alertSpy.mockRestore();
    });

    it('creates and starts game with 4+ players', async () => {
        vi.mocked(createGame).mockResolvedValue({ gameId: 'game1' });
        vi.mocked(startGame).mockResolvedValue({});
        vi.mocked(getGameState).mockResolvedValue(makeState());

        const { result } = renderHook(() => useOfflineGame());

        for (const name of ['A', 'B', 'C', 'D']) {
            act(() => result.current.setNewPlayerName(name));
            act(() => result.current.handleAddPlayer());
        }

        await act(() => result.current.handleStartGame());

        expect(createGame).toHaveBeenCalledWith('OFFLINE', undefined, ['A', 'B', 'C', 'D']);
        expect(startGame).toHaveBeenCalledWith('game1');
        expect(getGameState).toHaveBeenCalledWith('game1');
        expect(result.current.loading).toBe(false);
    });


    it('handleKill calls performAction and fetches state', async () => {
        vi.mocked(createGame).mockResolvedValue({ gameId: 'game1' });
        vi.mocked(startGame).mockResolvedValue({});
        vi.mocked(getGameState).mockResolvedValue(makeState());
        vi.mocked(performAction).mockResolvedValue({});

        const { result } = renderHook(() => useOfflineGame());

        for (const name of ['A', 'B', 'C', 'D']) {
            act(() => result.current.setNewPlayerName(name));
            act(() => result.current.handleAddPlayer());
        }
        await act(() => result.current.handleStartGame());

        await act(() => result.current.handleKill('A'));

        expect(performAction).toHaveBeenCalledWith('game1', 'Master', 'KILL', 'A');
    });

    it('handleVote requires activeVoter', async () => {
        vi.mocked(createGame).mockResolvedValue({ gameId: 'game1' });
        vi.mocked(startGame).mockResolvedValue({});
        vi.mocked(getGameState).mockResolvedValue(makeState({ phaseKey: 'DAY_VOTING', phase: 'DAY_VOTING' }));
        vi.mocked(performAction).mockResolvedValue({});

        const { result } = renderHook(() => useOfflineGame());

        for (const name of ['A', 'B', 'C', 'D']) {
            act(() => result.current.setNewPlayerName(name));
            act(() => result.current.handleAddPlayer());
        }
        await act(() => result.current.handleStartGame());

        await act(() => result.current.handleVote('B'));
        expect(performAction).not.toHaveBeenCalledWith('game1', expect.any(String), 'VOTE', 'B');

        act(() => result.current.setActiveVoter('A'));
        await act(() => result.current.handleVote('B'));
        expect(performAction).toHaveBeenCalledWith('game1', 'A', 'VOTE', 'B');
    });

    it('handleAbstain sends VOTE with SKIP target', async () => {
        vi.mocked(createGame).mockResolvedValue({ gameId: 'game1' });
        vi.mocked(startGame).mockResolvedValue({});
        vi.mocked(getGameState).mockResolvedValue(makeState({ phaseKey: 'DAY_VOTING', phase: 'DAY_VOTING' }));
        vi.mocked(performAction).mockResolvedValue({});

        const { result } = renderHook(() => useOfflineGame());

        for (const name of ['A', 'B', 'C', 'D']) {
            act(() => result.current.setNewPlayerName(name));
            act(() => result.current.handleAddPlayer());
        }
        await act(() => result.current.handleStartGame());

        act(() => result.current.setActiveVoter('A'));
        await act(() => result.current.handleAbstain());

        expect(performAction).toHaveBeenCalledWith('game1', 'A', 'VOTE', 'SKIP');
    });

    it('handlePeek stores seer result', async () => {
        vi.mocked(createGame).mockResolvedValue({ gameId: 'game1' });
        vi.mocked(startGame).mockResolvedValue({});
        vi.mocked(getGameState).mockResolvedValue(makeState({ currentTurn: 'Seer', phase: 'NIGHT - Seer' }));
        vi.mocked(performAction).mockResolvedValue({ peekResult: 'Wolf' });

        const { result } = renderHook(() => useOfflineGame());

        for (const name of ['A', 'B', 'C', 'D']) {
            act(() => result.current.setNewPlayerName(name));
            act(() => result.current.handleAddPlayer());
        }
        await act(() => result.current.handleStartGame());

        await act(() => result.current.handlePeek('B'));

        expect(performAction).toHaveBeenCalledWith('game1', 'Master', 'PEEK', 'B');
        expect(result.current.seerResult).toEqual({ target: 'B', role: 'Wolf' });
    });


    it('shows night phase message with role info', async () => {
        vi.mocked(createGame).mockResolvedValue({ gameId: 'game1' });
        vi.mocked(startGame).mockResolvedValue({});
        vi.mocked(getGameState).mockResolvedValue(makeState({
            phaseKey: 'NIGHT',
            currentTurn: 'Wolf',
            phaseDisplayName: 'Night - Werewolf Turn',
            turnInstruction: 'Choose a victim',
            players: [
                { name: 'A', isAlive: true, role: 'Werewolf', isTargetable: false, hasVoted: false },
                { name: 'B', isAlive: true, role: 'Villager', isTargetable: true, hasVoted: false },
            ],
        }));

        const { result } = renderHook(() => useOfflineGame());

        for (const name of ['A', 'B', 'C', 'D']) {
            act(() => result.current.setNewPlayerName(name));
            act(() => result.current.handleAddPlayer());
        }
        await act(() => result.current.handleStartGame());

        expect(result.current.phaseMessage).toContain('Night - Werewolf Turn');
        expect(result.current.phaseMessage).toContain('A');
        expect(result.current.phaseMessage).toContain('Choose a victim');
    });

    it('shows voting message with active voter', async () => {
        vi.mocked(createGame).mockResolvedValue({ gameId: 'game1' });
        vi.mocked(startGame).mockResolvedValue({});
        vi.mocked(getGameState).mockResolvedValue(makeState({
            phaseKey: 'DAY_VOTING',
            phase: 'DAY_VOTING',
            phaseDisplayName: 'Day - Voting',
        }));

        const { result } = renderHook(() => useOfflineGame());

        for (const name of ['A', 'B', 'C', 'D']) {
            act(() => result.current.setNewPlayerName(name));
            act(() => result.current.handleAddPlayer());
        }
        await act(() => result.current.handleStartGame());

        act(() => result.current.setActiveVoter('A'));

        expect(result.current.phaseMessage).toContain('Who is A voting for?');
    });


    it('stops polling when game is FINISHED', async () => {
        vi.mocked(createGame).mockResolvedValue({ gameId: 'game1' });
        vi.mocked(startGame).mockResolvedValue({});
        vi.mocked(getGameState).mockResolvedValue(makeState({ phase: 'FINISHED' }));

        const { result } = renderHook(() => useOfflineGame());

        for (const name of ['A', 'B', 'C', 'D']) {
            act(() => result.current.setNewPlayerName(name));
            act(() => result.current.handleAddPlayer());
        }
        await act(() => result.current.handleStartGame());

        vi.mocked(getGameState).mockClear();

        await act(() => vi.advanceTimersByTime(4000));

        expect(getGameState).not.toHaveBeenCalled();
    });
});
