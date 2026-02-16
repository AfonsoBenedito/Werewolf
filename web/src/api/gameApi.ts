import axios from 'axios';

const API_URL = 'http://localhost:8080/api/game';

export const createGame = async (mode: 'OFFLINE' | 'ONLINE', playerName?: string, players?: string[]) => {
    const response = await axios.post(API_URL, { mode, playerName, players });
    return response.data;
};

export const joinGame = async (gameId: string, playerName: string) => {
    const response = await axios.post(`${API_URL}/${gameId}/join`, { playerName });
    return response.data;
};

export const startGame = async (gameId: string) => {
    const response = await axios.post(`${API_URL}/${gameId}/start`);
    return response.data;
};

export const getGameState = async (gameId: string, playerId?: string) => {
    const params = playerId ? { playerId } : {};
    const response = await axios.get(`${API_URL}/${gameId}`, { params });
    return response.data;
};

export const performAction = async (gameId: string, playerId: string, actionType: string, targetId?: string) => {
    const response = await axios.post(`${API_URL}/${gameId}/action`, {
        playerId,
        actionType,
        targetId
    });
    return response.data;
};
