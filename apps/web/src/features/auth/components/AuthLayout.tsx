import type { PropsWithChildren, ReactNode } from "react";
import { Link } from "react-router-dom";
import { LanguageSelector } from "../../../shared/ui/LanguageSelector";

interface AuthLayoutProps extends PropsWithChildren {
  title: string;
  subtitle: string;
  footer?: ReactNode;
}

export function AuthLayout({ title, subtitle, footer, children }: AuthLayoutProps) {
  return (
    <div className="auth-page">
      <section className="auth-panel">
        <Link className="brand" to="/">
          PfP <span>Companion</span>
        </Link>
        <div className="auth-heading">
          <p className="eyebrow">Companion access</p>
          <h1>{title}</h1>
          <p>{subtitle}</p>
        </div>
        {children}
        {footer && <div className="auth-footer">{footer}</div>}
        <footer className="auth-language-footer">
          <LanguageSelector />
        </footer>
      </section>
      <aside className="auth-art" aria-hidden="true">
        <div className="auth-art-copy">
          <p className="eyebrow">PfP archive</p>
          <p className="auth-quote">
            A prepared adventurer keeps more than a blade within reach.
          </p>
        </div>
      </aside>
    </div>
  );
}
