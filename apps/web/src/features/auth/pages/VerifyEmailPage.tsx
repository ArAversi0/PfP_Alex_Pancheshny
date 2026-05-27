import { useEffect, useRef, useState } from "react";
import { useForm } from "react-hook-form";
import { Link, useSearchParams } from "react-router-dom";
import { authApi, getApiErrorMessage } from "../api/authApi";
import { AuthLayout } from "../components/AuthLayout";
import { FormMessage } from "../components/FormMessage";

type VerificationState = "idle" | "verifying" | "verified" | "failed";

export function VerifyEmailPage() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token");
  const initialEmail = searchParams.get("email") ?? "";
  const [verification, setVerification] = useState<VerificationState>(
    token ? "verifying" : "idle",
  );
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const verifiedRef = useRef(false);
  const { register, handleSubmit, formState } = useForm<{ email: string }>({
    defaultValues: { email: initialEmail },
  });

  useEffect(() => {
    if (!token) {
      return;
    }
    setError("");
    authApi.verifyEmail(token).then(
      (response) => {
        verifiedRef.current = true;
        setError("");
        setMessage(response.message);
        setVerification("verified");
      },
      (verifyError: unknown) => {
        if (verifiedRef.current) {
          return;
        }
        setError(getApiErrorMessage(verifyError));
        setVerification("failed");
      },
    );
  }, [token]);

  async function resend({ email }: { email: string }) {
    setError("");
    try {
      setMessage((await authApi.resendVerification(email)).message);
    } catch (submitError) {
      setError(getApiErrorMessage(submitError));
    }
  }

  return (
    <AuthLayout
      title={verification === "verified" ? "Email verified" : "Check your inbox"}
      subtitle={
        verification === "verifying"
          ? "We are confirming your verification link."
          : "Use the link in your verification email before signing in."
      }
      footer={<p><Link to="/login">Return to sign in</Link></p>}
    >
      {verification === "verifying" && <FormMessage kind="info">Verifying email...</FormMessage>}
      {message && <FormMessage kind="success">{message}</FormMessage>}
      {error && <FormMessage kind="error">{error}</FormMessage>}
      {verification !== "verified" && (
        <form className="auth-form compact" onSubmit={handleSubmit(resend)}>
          <label>
            Need a new link?
            <input type="email" placeholder="Email address" {...register("email", {
              required: "Enter your email address.",
            })} />
            {formState.errors.email && <small>{formState.errors.email.message}</small>}
          </label>
          <button className="button ghost full" disabled={formState.isSubmitting}>
            {formState.isSubmitting ? "Sending..." : "Resend verification email"}
          </button>
        </form>
      )}
    </AuthLayout>
  );
}
