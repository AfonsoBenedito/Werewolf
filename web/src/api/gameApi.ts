import axios from 'axios';
import type { GameState } from '../types/game';

const API_URL = '/api/game';

export interface CreateGameResponse { gameId: string }
export interface JoinGameResponse { message?: string; playerId?: string }
export interface StartGameResponse { message?: string }
export interface ActionResponse { message?: string; peekResult?: string }

export const createGame = async (mode: 'OFFLINE' | 'ONLINE', playerName?: string, players?: string[]): Promise<CreateGameResponse> => {
    const response = await axios.post(API_URL, { mode, playerName, players });
    return response.data;
};

export const joinGame = async (gameId: string, playerName: string): Promise<JoinGameResponse> => {
    const response = await axios.post(`${API_URL}/${gameId}/join`, { playerName });
    return response.data;
};

export const startGame = async (gameId: string): Promise<StartGameResponse> => {
    const response = await axios.post(`${API_URL}/${gameId}/start`);
    return response.data;
};

export const getGameState = async (gameId: string, playerId?: string): Promise<GameState | null> => {
    const params = playerId ? { playerId } : {};
    const response = await axios.get(`${API_URL}/${gameId}`, { params });
    return response.data;
};

export const performAction = async (gameId: string, playerId: string, actionType: string, targetId?: string): Promise<ActionResponse | string> => {
    const response = await axios.post(`${API_URL}/${gameId}/action`, {
        playerId,
        actionType,
        targetId
    });
    return response.data;
};
