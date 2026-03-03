import axios from 'axios';
import type { GameState } from '../types/game';

const API_URL = '/api/game';
const TOKEN_HEADER = 'X-Player-Token';

export interface CreateGameResponse { gameId: string; token: string }
export interface JoinGameResponse { token?: string; message?: string }
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

export const getGameState = async (gameId: string, token?: string): Promise<GameState | null> => {
    const headers = token ? { [TOKEN_HEADER]: token } : {};
    const response = await axios.get(`${API_URL}/${gameId}`, { headers });
    return response.data;
};

export const performAction = async (gameId: string, token: string, actionType: string, targetId?: string): Promise<ActionResponse | string> => {
    const response = await axios.post(
        `${API_URL}/${gameId}/action`,
        { actionType, targetId },
        { headers: { [TOKEN_HEADER]: token } }
    );
    return response.data;
};
