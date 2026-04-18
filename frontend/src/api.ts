import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080",
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const getMe = () => api.get("/api/users/me");
export const updateTeamName = (teamName: string) =>
  api.put("/api/users/me/team", { teamName });

export const createLeague = (name: string) =>
  api.post("/api/leagues", { name });
export const joinLeague = (leagueCode: string) =>
  api.post("/api/leagues/join", { leagueCode });
export const getMyLeagues = () => api.get("/api/leagues/mine");

export const getGames = () => api.get("/api/games");
export const getMyBets = () => api.get("/api/games/my-bets");
export const placeBet = (gameId: number, homeGoals: number, awayGoals: number) =>
  api.post(`/api/games/${gameId}/bet`, { homeGoals, awayGoals });

export default api;
