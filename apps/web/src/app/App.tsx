import { Navigate, Route, Routes, useLocation } from "react-router-dom";
import { AccountPage } from "../features/auth/pages/AccountPage";
import { AdminCharactersPage } from "../features/admin/pages/AdminCharactersPage";
import { AdminCharacterSheetPage } from "../features/admin/pages/AdminCharacterSheetPage";
import { AdminContentPage } from "../features/admin/pages/AdminContentPage";
import { AdminDashboardPage } from "../features/admin/pages/AdminDashboardPage";
import { AdminUsersPage } from "../features/admin/pages/AdminUsersPage";
import { ForgotPasswordPage } from "../features/auth/pages/ForgotPasswordPage";
import { LoginPage } from "../features/auth/pages/LoginPage";
import { OAuth2CallbackPage } from "../features/auth/pages/OAuth2CallbackPage";
import { RegisterPage } from "../features/auth/pages/RegisterPage";
import { ResetPasswordPage } from "../features/auth/pages/ResetPasswordPage";
import { VerifyEmailPage } from "../features/auth/pages/VerifyEmailPage";
import { useAuth } from "../features/auth/model/useAuth";
import { CharacterListPage } from "../features/character-sheet/pages/CharacterListPage";
import { CharacterSheetPage } from "../features/character-sheet/pages/CharacterSheetPage";
import { CreateCharacterPage } from "../features/character-sheet/pages/CreateCharacterPage";
import { GuestCharacterPage } from "../features/character-sheet/pages/GuestCharacterPage";
import { ContentBrowserPage } from "../features/content/pages/ContentBrowserPage";
import { HomePage } from "../pages/HomePage";

export function App() {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/verify-email" element={<VerifyEmailPage />} />
      <Route path="/forgot-password" element={<ForgotPasswordPage />} />
      <Route path="/reset-password" element={<ResetPasswordPage />} />
      <Route path="/oauth2/callback" element={<OAuth2CallbackPage />} />
      <Route path="/guest" element={<GuestCharacterPage />} />
      <Route
        path="/account"
        element={
          <RequireAuthentication>
            <AccountPage />
          </RequireAuthentication>
        }
      />
      <Route
        path="/characters"
        element={<RequireAuthentication><CharacterListPage /></RequireAuthentication>}
      />
      <Route
        path="/characters/new"
        element={<RequireAuthentication><CreateCharacterPage /></RequireAuthentication>}
      />
      <Route
        path="/characters/:characterId"
        element={<RequireAuthentication><CharacterSheetPage /></RequireAuthentication>}
      />
      <Route
        path="/admin"
        element={<RequireAdmin><AdminDashboardPage /></RequireAdmin>}
      />
      <Route
        path="/admin/content"
        element={<RequireAdmin><AdminContentPage /></RequireAdmin>}
      />
      <Route
        path="/admin/characters"
        element={<RequireAdmin><AdminCharactersPage /></RequireAdmin>}
      />
      <Route
        path="/admin/characters/:characterId"
        element={<RequireAdmin><AdminCharacterSheetPage /></RequireAdmin>}
      />
      <Route
        path="/admin/users"
        element={<RequireAdmin><AdminUsersPage /></RequireAdmin>}
      />
      <Route
        path="/lore"
        element={<ContentBrowserPage section="lore" title="Lore" description="Browse the known histories and places of the PfP world." />}
      />
      <Route
        path="/lore/:slug"
        element={<ContentBrowserPage section="lore" title="Lore" description="Browse the known histories and places of the PfP world." />}
      />
      <Route
        path="/rules"
        element={<ContentBrowserPage section="rules" title="Rule book" description="Browse categories and articles from the PfP rules archive." />}
      />
      <Route
        path="/rules/:slug"
        element={<ContentBrowserPage section="rules" title="Rule book" description="Browse categories and articles from the PfP rules archive." />}
      />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

function RequireAuthentication({ children }: { children: React.ReactNode }) {
  const { session } = useAuth();
  const location = useLocation();
  if (!session) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }
  return children;
}

function RequireAdmin({ children }: { children: React.ReactNode }) {
  const { session } = useAuth();
  const location = useLocation();
  if (!session) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }
  if (session.user.role !== "ROLE_ADMIN") {
    return <Navigate to="/account" replace />;
  }
  return children;
}
