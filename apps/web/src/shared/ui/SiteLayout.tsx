import type { PropsWithChildren, ReactNode } from "react";
import { NavLink, Link } from "react-router-dom";
import { useAuth } from "../../features/auth/model/useAuth";
import { LanguageSelector } from "./LanguageSelector";

interface SiteLayoutProps extends PropsWithChildren {
  actions?: ReactNode;
  wide?: boolean;
}

export function SiteLayout({ actions, wide = false, children }: SiteLayoutProps) {
  const { session } = useAuth();

  return (
    <div className="site-shell">
      <header className="site-header">
        <Link className="brand" to="/">PfP <span>Companion</span></Link>
        <nav className="main-nav">
          <NavLink to={session ? "/characters" : "/guest"}>{session ? "Characters" : "Guest sheet"}</NavLink>
          <NavLink to="/lore">Lore</NavLink>
          <NavLink to="/rules">Rule book</NavLink>
          {session?.user.role === "ROLE_ADMIN" && <NavLink to="/admin">Admin</NavLink>}
          <NavLink to="/account">{session ? "Profile" : "Sign in"}</NavLink>
        </nav>
        {actions && <div className="header-actions">{actions}</div>}
      </header>
      <main className={wide ? "page-shell wide" : "page-shell"}>{children}</main>
      <footer className="language-footer">
        <LanguageSelector />
      </footer>
    </div>
  );
}
