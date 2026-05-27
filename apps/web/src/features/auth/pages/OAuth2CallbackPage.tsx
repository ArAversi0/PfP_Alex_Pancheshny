import { useEffect, useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { getApiErrorMessage } from "../api/authApi";
import { AuthLayout } from "../components/AuthLayout";
import { FormMessage } from "../components/FormMessage";
import { useAuth } from "../model/useAuth";

export function OAuth2CallbackPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { completeOAuth2Login } = useAuth();
  const [error, setError] = useState("");

  useEffect(() => {
    const providerError = searchParams.get("error");
    const code = searchParams.get("code");
    if (providerError || !code) {
      setError("Google sign-in could not be completed.");
      return;
    }
    completeOAuth2Login(code).then(
      () => navigate("/account", { replace: true }),
      (exchangeError: unknown) => setError(getApiErrorMessage(exchangeError)),
    );
  }, [completeOAuth2Login, navigate, searchParams]);

  return (
    <AuthLayout
      title="Completing sign-in"
      subtitle="Your Google account has returned safely to the archive."
      footer={error ? <p><Link to="/login">Return to sign in</Link></p> : undefined}
    >
      {error ? (
        <FormMessage kind="error">{error}</FormMessage>
      ) : (
        <FormMessage kind="info">Opening your account...</FormMessage>
      )}
    </AuthLayout>
  );
}
