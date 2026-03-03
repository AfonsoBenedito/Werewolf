import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { useOnlineLobby } from '../useOnlineLobby';

const mockNavigate = vi.fn();
vi.mock('react-router-dom', () => ({
    useNavigate: () => mockNavigate,
}));

vi.mock('../../api/gameApi', () => ({
    createGame: vi.fn(),
    joinGame: vi.fn(),
}));

import { createGame, joinGame } from '../../api/gameApi';

beforeEach(() => {
    vi.clearAllMocks();
    sessionStorage.clear();
});

describe('useOnlineLobby', () => {
    it('initializes with empty state', () => {
        const { result } = renderHook(() => useOnlineLobby());

        expect(result.current.hostName).toBe('');
        expect(result.current.joinName).toBe('');
        expect(result.current.joinId).toBe('');
        expect(result.current.error).toBe('');
    });


    it('sets error when hosting with empty name', async () => {
        const { result } = renderHook(() => useOnlineLobby());

        await act(() => result.current.handleHost());

        expect(result.current.error).toBe('Name is required');
        expect(createGame).not.toHaveBeenCalled();
    });

    it('creates game, saves token, and navigates on host', async () => {
        vi.mocked(createGame).mockResolvedValue({ gameId: 'abc123', token: 'test-token' });

        const { result } = renderHook(() => useOnlineLobby());

        act(() => result.current.setHostName('Alice'));
        await act(() => result.current.handleHost());

        expect(createGame).toHaveBeenCalledWith('ONLINE', 'Alice');
        expect(sessionStorage.getItem('werewolf_token')).toBe('test-token');
        expect(sessionStorage.getItem('werewolf_player')).toBe('Alice');
        expect(mockNavigate).toHaveBeenCalledWith('/online/game/abc123');
    });

    it('sets error when createGame fails', async () => {
        vi.mocked(createGame).mockRejectedValue(new Error('network'));

        const { result } = renderHook(() => useOnlineLobby());

        act(() => result.current.setHostName('Alice'));
        await act(() => result.current.handleHost());

        expect(result.current.error).toBe('Failed to create game');
        expect(mockNavigate).not.toHaveBeenCalled();
    });


    it('sets error when joining with missing fields', async () => {
        const { result } = renderHook(() => useOnlineLobby());

        await act(() => result.current.handleJoin());
        expect(result.current.error).toBe('Name and Game ID required');

        act(() => result.current.setJoinName('Bob'));
        await act(() => result.current.handleJoin());
        expect(result.current.error).toBe('Name and Game ID required');
    });

    it('joins game, saves token, and navigates', async () => {
        vi.mocked(joinGame).mockResolvedValue({ token: 'join-token' });

        const { result } = renderHook(() => useOnlineLobby());

        act(() => {
            result.current.setJoinName('Bob');
            result.current.setJoinId('xyz789');
        });
        await act(() => result.current.handleJoin());

        expect(joinGame).toHaveBeenCalledWith('xyz789', 'Bob');
        expect(sessionStorage.getItem('werewolf_token')).toBe('join-token');
        expect(sessionStorage.getItem('werewolf_player')).toBe('Bob');
        expect(mockNavigate).toHaveBeenCalledWith('/online/game/xyz789');
    });

    it('sets error when joinGame fails', async () => {
        vi.mocked(joinGame).mockRejectedValue(new Error('dup'));

        const { result } = renderHook(() => useOnlineLobby());

        act(() => {
            result.current.setJoinName('Bob');
            result.current.setJoinId('xyz789');
        });
        await act(() => result.current.handleJoin());

        expect(result.current.error).toBe('Failed to join game. Check ID or name uniqueness.');
        expect(mockNavigate).not.toHaveBeenCalled();
    });
});
