import { useEffect, useState } from "react";
import { useAuth } from "../AuthContext";
import { useNavigate } from "react-router-dom";
import api, { updateTeamName, createLeague, joinLeague, getMyLeagues, getGames, getMyBets, placeBet } from "../api";

interface UserMe { id: number; email: string; role: string; teamName: string | null; }
interface MemberInfo { id: number; email: string; teamName: string | null; points: number; }
interface League { id: number; name: string; leagueCode: string; members: MemberInfo[]; }
interface Game { id: number; homeTeam: string; awayTeam: string; startsAt: string; status: string; homeScore: number | null; awayScore: number | null; }
interface BetRecord { id: number; gameId: number; homeGoals: number; awayGoals: number; points: number | null; }

const FLAG: Record<string, string> = {
  // Group A
  Mexico: "🇲🇽",
  "Korea Republic": "🇰🇷",
  "South Africa": "🇿🇦",
  Czechia: "🇨🇿",
  // Group B
  Canada: "🇨🇦",
  Qatar: "🇶🇦",
  Switzerland: "🇨🇭",
  "Bosnia-Herzegovina": "🇧🇦",
  // Group C
  Brazil: "🇧🇷",
  Haiti: "🇭🇹",
  Scotland: "🏴󠁧󠁢󠁳󠁣󠁴󠁿",
  Morocco: "🇲🇦",
  // Group D
  USA: "🇺🇸",
  Australia: "🇦🇺",
  Turkey: "🇹🇷",
  Paraguay: "🇵🇾",
  // Group E
  Germany: "🇩🇪",
  Ecuador: "🇪🇨",
  "Côte d'Ivoire": "🇨🇮",
  "Curaçao": "🇨🇼",
  // Group F
  Netherlands: "🇳🇱",
  Japan: "🇯🇵",
  Sweden: "🇸🇪",
  Tunisia: "🇹🇳",
  // Group G
  Belgium: "🇧🇪",
  Egypt: "🇪🇬",
  "IR Iran": "🇮🇷",
  "New Zealand": "🇳🇿",
  // Group H
  Spain: "🇪🇸",
  "Saudi Arabia": "🇸🇦",
  Uruguay: "🇺🇾",
  "Cabo Verde": "🇨🇻",
  // Group I
  France: "🇫🇷",
  Senegal: "🇸🇳",
  Norway: "🇳🇴",
  Iraq: "🇮🇶",
  // Group J
  Argentina: "🇦🇷",
  Algeria: "🇩🇿",
  Austria: "🇦🇹",
  Jordan: "🇯🇴",
  // Group K
  Portugal: "🇵🇹",
  Uzbekistan: "🇺🇿",
  Colombia: "🇨🇴",
  "Congo DR": "🇨🇩",
  // Group L
  Ghana: "🇬🇭",
  Panama: "🇵🇦",
  England: "🏴󠁧󠁢󠁥󠁮󠁧󠁿",
  Croatia: "🇭🇷",
};
const flag = (name: string) => FLAG[name] ?? "🏳️";

export default function HomePage() {
  const { logout } = useAuth();
  const navigate = useNavigate();
  const [user, setUser] = useState<UserMe | null>(null);
  const [games, setGames] = useState<Game[]>([]);
  const [bets, setBets] = useState<Record<number, BetRecord>>({});
  // pending score inputs per game [home, away]
  const [inputs, setInputs] = useState<Record<number, [string, string]>>({});
  const [saving, setSaving] = useState<Record<number, boolean>>({});
  const [saved, setSaved] = useState<Record<number, boolean>>({});
  const [betError, setBetError] = useState<Record<number, string>>({});

  // team name
  const [teamInput, setTeamInput] = useState("");
  const [teamError, setTeamError] = useState("");
  const [teamLoading, setTeamLoading] = useState(false);

  // leagues
  const [leagues, setLeagues] = useState<League[]>([]);
  const [showCreate, setShowCreate] = useState(false);
  const [newLeagueName, setNewLeagueName] = useState("");
  const [showJoin, setShowJoin] = useState(false);
  const [joinCode, setJoinCode] = useState("");
  const [leagueLoading, setLeagueLoading] = useState(false);
  const [leagueError, setLeagueError] = useState("");
  const [joinError, setJoinError] = useState("");
  const [expandedLeague, setExpandedLeague] = useState<number | null>(null);

  useEffect(() => {
    api.get("/api/users/me").then((r) => setUser(r.data));
    getMyLeagues().then((r) => setLeagues(r.data));
    getGames().then((r) => setGames(r.data));
    getMyBets().then((r) => {
      const map: Record<number, BetRecord> = {};
      (r.data as BetRecord[]).forEach((b) => { map[b.gameId] = b; });
      setBets(map);
      // pre-fill inputs from existing bets
      const inp: Record<number, [string, string]> = {};
      (r.data as BetRecord[]).forEach((b) => { inp[b.gameId] = [String(b.homeGoals), String(b.awayGoals)]; });
      setInputs(inp);
    });
  }, []);

  const handleLogout = () => { logout(); navigate("/login"); };

  const handleSetTeam = async (e: React.FormEvent) => {
    e.preventDefault(); setTeamError(""); setTeamLoading(true);
    try { const r = await updateTeamName(teamInput.trim()); setUser(r.data); setTeamInput(""); }
    catch { setTeamError("Failed to save team name."); }
    finally { setTeamLoading(false); }
  };

  const handleCreateLeague = async (e: React.FormEvent) => {
    e.preventDefault(); setLeagueError(""); setLeagueLoading(true);
    try {
      const r = await createLeague(newLeagueName.trim());
      setLeagues((p) => [...p, r.data]); setNewLeagueName(""); setShowCreate(false); setExpandedLeague(r.data.id);
    } catch { setLeagueError("Failed to create league."); }
    finally { setLeagueLoading(false); }
  };

  const handleJoinLeague = async (e: React.FormEvent) => {
    e.preventDefault(); setJoinError(""); setLeagueLoading(true);
    try {
      const r = await joinLeague(joinCode.trim());
      if (!leagues.find((l) => l.id === r.data.id)) setLeagues((p) => [...p, r.data]);
      setJoinCode(""); setShowJoin(false); setExpandedLeague(r.data.id);
    } catch (err: unknown) {
      const s = (err as { response?: { status?: number } })?.response?.status;
      setJoinError(s === 404 ? "League not found." : "Failed to join league.");
    } finally { setLeagueLoading(false); }
  };

  const handlePlaceBet = async (gameId: number) => {
    const [h, a] = inputs[gameId] ?? ["", ""];
    console.log("[BET] handlePlaceBet called", gameId, "h=", h, "a=", a);
    if (h === "" || a === "") { console.log("[BET] inputs empty, aborting"); return; }
    setSaving((p) => ({ ...p, [gameId]: true }));
    setBetError((p) => ({ ...p, [gameId]: "" }));
    try {
      const r = await placeBet(gameId, parseInt(h), parseInt(a));
      setBets((p) => ({ ...p, [gameId]: r.data }));
      setSaved((p) => ({ ...p, [gameId]: true }));
      setTimeout(() => setSaved((p) => ({ ...p, [gameId]: false })), 2000);
    } catch (err: unknown) {
      const status = (err as { response?: { status?: number } })?.response?.status;
      const msg = status === 400 ? "Betting closed for this match" : "Failed to save bet";
      setBetError((p) => ({ ...p, [gameId]: msg }));
      console.error("placeBet error", err);
    } finally {
      setSaving((p) => ({ ...p, [gameId]: false }));
    }
  };

  const isOpen = (game: Game) =>
    game.status === "OPEN" && new Date(game.startsAt) > new Date();

  const inputStyle: React.CSSProperties = {
    width: "48px", textAlign: "center", padding: "0.35rem 0.25rem",
    borderRadius: "6px", border: "1px solid #475569", background: "#0f172a",
    color: "#f1f5f9", fontSize: "1.1rem", fontWeight: 700,
  };

  return (
    <div className="home-page">
      {/* Header */}
      <header className="site-header">
        <div className="header-inner">
          <div className="logo">⚽ Mondial</div>
          <div className="header-right">
            {user && (
              <div style={{ display: "flex", flexDirection: "column", alignItems: "flex-end", gap: "2px" }}>
                <span className="user-email">{user.email}</span>
                {user.teamName && <span style={{ fontSize: "0.8rem", color: "#4ade80", fontWeight: 600 }}>🏆 {user.teamName}</span>}
              </div>
            )}
            <button className="btn-logout" onClick={handleLogout}>Sign out</button>
          </div>
        </div>
      </header>

      {/* Team name prompt */}
      {user && !user.teamName && (
        <div style={{ background: "#1e293b", borderBottom: "1px solid #334155", padding: "1rem", display: "flex", justifyContent: "center" }}>
          <div style={{ maxWidth: "480px", width: "100%", textAlign: "center" }}>
            <p style={{ color: "#f59e0b", fontWeight: 600, marginBottom: "0.5rem" }}>👋 Welcome! Set your team name to get started.</p>
            <form onSubmit={handleSetTeam} style={{ display: "flex", gap: "0.5rem" }}>
              <input type="text" value={teamInput} onChange={(e) => setTeamInput(e.target.value)} placeholder="Team name…" required
                style={{ flex: 1, padding: "0.5rem 0.75rem", borderRadius: "6px", border: "1px solid #475569", background: "#0f172a", color: "#f1f5f9", fontSize: "0.9rem" }} />
              <button type="submit" disabled={teamLoading} style={{ padding: "0.5rem 1rem", background: "#3b82f6", color: "#fff", border: "none", borderRadius: "6px", cursor: "pointer", fontWeight: 600 }}>
                {teamLoading ? "Saving…" : "Save"}
              </button>
            </form>
            {teamError && <p style={{ color: "#f87171", marginTop: "0.5rem", fontSize: "0.85rem" }}>{teamError}</p>}
          </div>
        </div>
      )}

      <section className="hero">
        <h1>Welcome to Mondial</h1>
        <p>Predict match scores and compete with friends!</p>
      </section>

      {/* Leagues */}
      <section style={{ maxWidth: "900px", margin: "0 auto 2rem", padding: "0 1rem" }}>
        <div style={{ display: "flex", flexDirection: "column", alignItems: "center", marginBottom: "1rem", gap: "0.75rem" }}>
          <h2 style={{ color: "#f1f5f9", margin: 0 }}>🏅 My Leagues</h2>
          <div style={{ display: "flex", gap: "0.75rem", justifyContent: "center" }}>
            <button onClick={() => { setShowJoin(!showJoin); setShowCreate(false); }}
              style={{ padding: "0.5rem 1rem", background: "#475569", color: "#f1f5f9", border: "none", borderRadius: "8px", cursor: "pointer", fontWeight: 600 }}>
              🔗 Join League
            </button>
            <button onClick={() => { setShowCreate(!showCreate); setShowJoin(false); }}
              style={{ padding: "0.75rem 1.5rem", background: "linear-gradient(135deg,#3b82f6,#6366f1)", color: "#fff", border: "none", borderRadius: "8px", cursor: "pointer", fontWeight: 700, fontSize: "1rem", boxShadow: "0 4px 14px rgba(99,102,241,0.4)" }}>
              ➕ Create New League
            </button>
          </div>
        </div>

        {showCreate && (
          <div style={{ background: "#1e293b", borderRadius: "10px", padding: "1rem", marginBottom: "1rem", border: "1px solid #334155" }}>
            <form onSubmit={handleCreateLeague} style={{ display: "flex", gap: "0.5rem" }}>
              <input type="text" value={newLeagueName} onChange={(e) => setNewLeagueName(e.target.value)} placeholder="League name…" required
                style={{ flex: 1, padding: "0.5rem 0.75rem", borderRadius: "6px", border: "1px solid #475569", background: "#0f172a", color: "#f1f5f9", fontSize: "0.9rem" }} />
              <button type="submit" disabled={leagueLoading} style={{ padding: "0.5rem 1rem", background: "#3b82f6", color: "#fff", border: "none", borderRadius: "6px", cursor: "pointer", fontWeight: 600 }}>{leagueLoading ? "Creating…" : "Create"}</button>
              <button type="button" onClick={() => setShowCreate(false)} style={{ padding: "0.5rem 0.75rem", background: "#334155", color: "#94a3b8", border: "none", borderRadius: "6px", cursor: "pointer" }}>Cancel</button>
            </form>
            {leagueError && <p style={{ color: "#f87171", marginTop: "0.5rem", fontSize: "0.85rem" }}>{leagueError}</p>}
          </div>
        )}

        {showJoin && (
          <div style={{ background: "#1e293b", borderRadius: "10px", padding: "1rem", marginBottom: "1rem", border: "1px solid #334155" }}>
            <form onSubmit={handleJoinLeague} style={{ display: "flex", gap: "0.5rem" }}>
              <input type="text" value={joinCode} onChange={(e) => setJoinCode(e.target.value)} placeholder="League code (e.g. AB12CD)…" required
                style={{ flex: 1, padding: "0.5rem 0.75rem", borderRadius: "6px", border: "1px solid #475569", background: "#0f172a", color: "#f1f5f9", fontSize: "0.9rem", textTransform: "uppercase" }} />
              <button type="submit" disabled={leagueLoading} style={{ padding: "0.5rem 1rem", background: "#10b981", color: "#fff", border: "none", borderRadius: "6px", cursor: "pointer", fontWeight: 600 }}>{leagueLoading ? "Joining…" : "Join"}</button>
              <button type="button" onClick={() => setShowJoin(false)} style={{ padding: "0.5rem 0.75rem", background: "#334155", color: "#94a3b8", border: "none", borderRadius: "6px", cursor: "pointer" }}>Cancel</button>
            </form>
            {joinError && <p style={{ color: "#f87171", marginTop: "0.5rem", fontSize: "0.85rem" }}>{joinError}</p>}
          </div>
        )}

        {leagues.length === 0 ? (
          <p style={{ color: "#64748b", textAlign: "center", padding: "2rem" }}>You're not in any league yet.</p>
        ) : (
          <div style={{ display: "flex", flexDirection: "column", gap: "0.75rem" }}>
            {leagues.map((league) => (
              <div key={league.id} style={{ background: "#1e293b", borderRadius: "10px", border: "1px solid #334155", overflow: "hidden" }}>
                <div onClick={() => setExpandedLeague(expandedLeague === league.id ? null : league.id)}
                  style={{ padding: "1rem", cursor: "pointer", display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                  <div>
                    <span style={{ color: "#f1f5f9", fontWeight: 700 }}>{league.name}</span>
                    <span style={{ marginLeft: "0.75rem", background: "#334155", color: "#94a3b8", padding: "0.15rem 0.5rem", borderRadius: "4px", fontSize: "0.8rem", fontFamily: "monospace", letterSpacing: "0.1em" }}>{league.leagueCode}</span>
                  </div>
                  <span style={{ color: "#64748b", fontSize: "0.85rem" }}>{league.members.length} member{league.members.length !== 1 ? "s" : ""} {expandedLeague === league.id ? "▲" : "▼"}</span>
                </div>
                {expandedLeague === league.id && (
                  <div style={{ borderTop: "1px solid #334155", padding: "0.75rem 1rem" }}>
                    <p style={{ color: "#64748b", fontSize: "0.8rem", marginBottom: "0.75rem" }}>Share code: <strong style={{ color: "#f1f5f9", fontFamily: "monospace" }}>{league.leagueCode}</strong></p>
                    {/* standings table */}
                    <table style={{ width: "100%", borderCollapse: "collapse", fontSize: "0.875rem" }}>
                      <thead>
                        <tr style={{ color: "#64748b", textAlign: "left" }}>
                          <th style={{ paddingBottom: "0.4rem", width: "2rem" }}>#</th>
                          <th style={{ paddingBottom: "0.4rem" }}>Team</th>
                          <th style={{ paddingBottom: "0.4rem", textAlign: "right" }}>Pts</th>
                        </tr>
                      </thead>
                      <tbody>
                        {league.members.map((m, i) => (
                          <tr key={m.id} style={{ borderTop: "1px solid #1e293b" }}>
                            <td style={{ padding: "0.3rem 0", color: "#64748b" }}>{i + 1}</td>
                            <td style={{ padding: "0.3rem 0" }}>
                              <span style={{ color: "#f1f5f9", fontWeight: 600 }}>{m.teamName || m.email}</span>
                              {m.teamName && <span style={{ marginLeft: "0.5rem", color: "#64748b", fontSize: "0.78rem" }}>{m.email}</span>}
                            </td>
                            <td style={{ padding: "0.3rem 0", textAlign: "right", color: "#4ade80", fontWeight: 700 }}>{m.points}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </section>

      {/* Matches */}
      <main className="matches-section">
        <div className="matches-header">
          <h2>Matches</h2>
        </div>

        {games.length === 0 && (
          <p style={{ color: "#64748b", textAlign: "center", padding: "2rem" }}>No matches yet. Ask an admin to add some!</p>
        )}

        <div className="matches-grid">
          {games.map((game) => {
            const open = isOpen(game);
            const myBet = bets[game.id];
            const [hIn, aIn] = inputs[game.id] ?? ["", ""];
            const isSaving = saving[game.id];
            const isSaved = saved[game.id];

            return (
              <div key={game.id} className={`match-card ${myBet ? "match-card--bet" : ""}`}>
                <div className="match-meta">
                  <span className="match-group">{new Date(game.startsAt).toLocaleDateString("en-GB", { day: "numeric", month: "short" })}</span>
                  <span className={`match-group`} style={{ color: game.status === "FINISHED" ? "#4ade80" : game.status === "OPEN" ? "#60a5fa" : "#f59e0b" }}>
                    {game.status === "FINISHED" ? "Final" : game.status === "OPEN" ? "Open" : game.status}
                  </span>
                </div>

                <div className="match-teams">
                  <div className="team">
                    <span className="flag">{flag(game.homeTeam)}</span>
                    <span className="team-name">{game.homeTeam}</span>
                  </div>
                  {game.status === "FINISHED" ? (
                    <span style={{ fontSize: "1.4rem", fontWeight: 800, color: "#f1f5f9", minWidth: "60px", textAlign: "center" }}>
                      {game.homeScore} – {game.awayScore}
                    </span>
                  ) : (
                    <span className="vs">VS</span>
                  )}
                  <div className="team team--right">
                    <span className="team-name">{game.awayTeam}</span>
                    <span className="flag">{flag(game.awayTeam)}</span>
                  </div>
                </div>

                {/* Bet area */}
                {game.status === "FINISHED" && myBet && (
                  <div style={{ textAlign: "center", marginTop: "0.5rem" }}>
                    <span style={{ color: "#94a3b8", fontSize: "0.85rem" }}>Your bet: {myBet.homeGoals}–{myBet.awayGoals} </span>
                    <span style={{ color: myBet.points === 3 ? "#4ade80" : myBet.points === 1 ? "#fbbf24" : "#f87171", fontWeight: 700 }}>
                      {myBet.points === 3 ? "🎯 +3 pts" : myBet.points === 1 ? "✅ +1 pt" : myBet.points === 0 ? "❌ 0 pts" : ""}
                    </span>
                  </div>
                )}

                {open && (
                  <div style={{ marginTop: "0.75rem" }}>
                    {myBet && (
                      <div style={{ textAlign: "center", marginBottom: "0.4rem", fontSize: "0.82rem", color: "#94a3b8" }}>
                        Current bet: <strong style={{ color: "#60a5fa" }}>{myBet.homeGoals} – {myBet.awayGoals}</strong>
                      </div>
                    )}
                    <div style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: "0.75rem" }}>
                      <input type="number" min={0} max={20} value={hIn}
                        onChange={(e) => setInputs((p) => ({ ...p, [game.id]: [e.target.value, p[game.id]?.[1] ?? ""] }))}
                        style={inputStyle} placeholder="0" />
                      <span style={{ color: "#64748b", fontWeight: 700 }}>–</span>
                      <input type="number" min={0} max={20} value={aIn}
                        onChange={(e) => setInputs((p) => ({ ...p, [game.id]: [p[game.id]?.[0] ?? "", e.target.value] }))}
                        style={inputStyle} placeholder="0" />
                      <button onClick={() => handlePlaceBet(game.id)} disabled={isSaving || hIn === "" || aIn === ""}
                        style={{ padding: "0.4rem 0.9rem", background: isSaved ? "#10b981" : "#3b82f6", color: "#fff", border: "none", borderRadius: "6px", cursor: "pointer", fontWeight: 600, fontSize: "0.85rem", transition: "background 0.2s" }}>
                        {isSaved ? "✓ Saved" : isSaving ? "…" : myBet ? "Update" : "Bet"}
                      </button>
                    </div>
                    {betError[game.id] && (
                      <p style={{ textAlign: "center", color: "#f87171", fontSize: "0.8rem", marginTop: "0.35rem" }}>{betError[game.id]}</p>
                    )}
                  </div>
                )}

                {!open && game.status === "OPEN" && (
                  <p style={{ textAlign: "center", color: "#64748b", fontSize: "0.8rem", marginTop: "0.5rem" }}>Match started – betting closed</p>
                )}
              </div>
            );
          })}
        </div>
      </main>
    </div>
  );
}