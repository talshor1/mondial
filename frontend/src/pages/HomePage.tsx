import { useEffect, useState } from "react";
import { useAuth } from "../AuthContext";
import { useNavigate } from "react-router-dom";
import api from "../api";

interface UserMe {
  id: number;
  email: string;
  role: string;
}

interface Match {
  id: number;
  home: string;
  away: string;
  homeFlag: string;
  awayFlag: string;
  date: string;
  group: string;
}

interface Bet {
  matchId: number;
  pick: "home" | "draw" | "away";
}

const MATCHES: Match[] = [
  { id: 1, home: "Brazil", away: "Argentina", homeFlag: "🇧🇷", awayFlag: "🇦🇷", date: "Jun 14", group: "Group A" },
  { id: 2, home: "France", away: "Germany", homeFlag: "🇫🇷", awayFlag: "🇩🇪", date: "Jun 15", group: "Group B" },
  { id: 3, home: "Spain", away: "Portugal", homeFlag: "🇪🇸", awayFlag: "🇵🇹", date: "Jun 15", group: "Group C" },
  { id: 4, home: "England", away: "Italy", homeFlag: "🏴󠁧󠁢󠁥󠁮󠁧󠁿", awayFlag: "🇮🇹", date: "Jun 16", group: "Group D" },
  { id: 5, home: "Netherlands", away: "Belgium", homeFlag: "🇳🇱", awayFlag: "🇧🇪", date: "Jun 17", group: "Group E" },
  { id: 6, home: "Morocco", away: "Senegal", homeFlag: "🇲🇦", awayFlag: "🇸🇳", date: "Jun 18", group: "Group F" },
];

export default function HomePage() {
  const { logout } = useAuth();
  const navigate = useNavigate();
  const [user, setUser] = useState<UserMe | null>(null);
  const [bets, setBets] = useState<Record<number, Bet["pick"]>>({});
  const [saved, setSaved] = useState(false);

  useEffect(() => {
    api.get("/api/users/me").then((res) => setUser(res.data));
  }, []);

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  const placeBet = (matchId: number, pick: Bet["pick"]) => {
    setBets((prev) => ({ ...prev, [matchId]: pick }));
    setSaved(false);
  };

  const handleSave = () => {
    setSaved(true);
    setTimeout(() => setSaved(false), 3000);
  };

  const betCount = Object.keys(bets).length;

  return (
    <div className="home-page">
      {/* Header */}
      <header className="site-header">
        <div className="header-inner">
          <div className="logo">⚽ Mondial</div>
          <div className="header-right">
            {user && <span className="user-email">{user.email}</span>}
            <button className="btn-logout" onClick={handleLogout}>Sign out</button>
          </div>
        </div>
      </header>

      {/* Hero */}
      <section className="hero">
        <h1>Welcome to Mondial</h1>
        <p>Bet on the FIFA World Cup matches and compete with friends!</p>
      </section>

      {/* Matches */}
      <main className="matches-section">
        <div className="matches-header">
          <h2>Upcoming Matches</h2>
          {betCount > 0 && (
            <button className="btn-save" onClick={handleSave}>
              Save {betCount} bet{betCount > 1 ? "s" : ""}
            </button>
          )}
        </div>

        {saved && <div className="success-banner">✅ Bets saved successfully!</div>}

        <div className="matches-grid">
          {MATCHES.map((match) => {
            const picked = bets[match.id];
            return (
              <div key={match.id} className={`match-card ${picked ? "match-card--bet" : ""}`}>
                <div className="match-meta">
                  <span className="match-group">{match.group}</span>
                  <span className="match-date">{match.date}</span>
                </div>
                <div className="match-teams">
                  <div className="team">
                    <span className="flag">{match.homeFlag}</span>
                    <span className="team-name">{match.home}</span>
                  </div>
                  <span className="vs">VS</span>
                  <div className="team team--right">
                    <span className="team-name">{match.away}</span>
                    <span className="flag">{match.awayFlag}</span>
                  </div>
                </div>
                <div className="bet-buttons">
                  <button
                    className={`bet-btn ${picked === "home" ? "bet-btn--active" : ""}`}
                    onClick={() => placeBet(match.id, "home")}
                  >
                    {match.home}
                  </button>
                  <button
                    className={`bet-btn ${picked === "draw" ? "bet-btn--active" : ""}`}
                    onClick={() => placeBet(match.id, "draw")}
                  >
                    Draw
                  </button>
                  <button
                    className={`bet-btn ${picked === "away" ? "bet-btn--active" : ""}`}
                    onClick={() => placeBet(match.id, "away")}
                  >
                    {match.away}
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      </main>
    </div>
  );
}