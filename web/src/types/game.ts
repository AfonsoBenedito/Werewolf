export interface Player {
    name: string;
    isAlive: boolean;
    role: string;
    isTargetable: boolean;
    hasVoted: boolean;
    isOnWinningTeam?: boolean | null;
}

export interface GameState {
    players: Player[];
    status: string;
    phase: string; // Legacy format for useGameTransitions
    dayCount: number;
    winner?: string;
    lastDeadPlayerName?: string;
    votes?: Record<string, string>;
    nightStatus?: string | null;
    readyPlayerCount?: number;
    totalAliveCount?: number;
    phaseKey: string;
    currentTurn?: string | null;
    isMyTurn: boolean;
    availableActions: string[];
    canSkip: boolean;
    phaseDisplayName: string;
    turnInstruction?: string | null;
    canStart: boolean;
    isHost: boolean;
    minPlayers: number;
}
