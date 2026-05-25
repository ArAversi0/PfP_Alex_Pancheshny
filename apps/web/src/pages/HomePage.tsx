import { Link, NavLink } from "react-router-dom";
import { useAuth } from "../features/auth/model/useAuth";
import { LanguageSelector } from "../shared/ui/LanguageSelector";

export function HomePage() {
  const { session } = useAuth();

  return (
    <div className="site-shell">
      <header className="site-header">
        <Link className="brand" to="/">
          PfP <span>Companion</span>
        </Link>
        <nav className="main-nav">
          {session && <NavLink to="/characters">Characters</NavLink>}
          <NavLink to="/lore">Lore</NavLink>
          <NavLink to="/rules">Rule book</NavLink>
          {session?.user.role === "ROLE_ADMIN" && <NavLink to="/admin">Admin</NavLink>}
          <NavLink to={session ? "/account" : "/login"}>{session ? "Profile" : "Sign in"}</NavLink>
        </nav>
      </header>
      <main className="hero">
        <div>
          <p className="eyebrow">Pain for Pleasure companion system</p>
          <h1>Your stories.<br />Your characters.</h1>
          <p className="intro">
            Keep every detail of your adventurer close at hand, from hard-won
            equipment to the spells that turn the tide.
          </p>
          <div className="hero-actions">
            <Link className="button primary" to={session ? "/characters" : "/register"}>
              {session ? "Open archive" : "Create account"}
            </Link>
            {!session && (
              <Link className="button ghost" to="/login">
                Sign in
              </Link>
            )}
            {!session && (
              <Link className="button ghost" to="/guest">
                Continue as guest
              </Link>
            )}
          </div>
        </div>
        <aside className="hero-card">
          <p className="eyebrow">Current chapter</p>
          <h2>Build the legend</h2>
          <p>
            Keep your archive close and open a character sheet whenever the
            next chapter begins.
          </p>
        </aside>
      </main>
      <footer className="language-footer">
        <LanguageSelector />
      </footer>
    </div>
  );
}
