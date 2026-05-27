import { useState } from "react";
import { useForm } from "react-hook-form";
import { Link, useLocation, useNavigate } from "react-router-dom";
import {
  getApiErrorMessage,
  getOAuth2AuthorizationUrl,
} from "../api/authApi";
import { AuthLayout } from "../components/AuthLayout";
import { FormMessage } from "../components/FormMessage";
import { useAuth } from "../model/useAuth";

interface LoginForm {
  email: string;
  password: string;
}

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [error, setError] = useState("");
  const { register, handleSubmit, formState } = useForm<LoginForm>();

  async function submit(values: LoginForm) {
    setError("");
    try {
      await login(values.email, values.password);
      const from = (location.state as { from?: string } | null)?.from;
      navigate(from ?? "/account", { replace: true });
    } catch (submitError) {
      setError(getApiErrorMessage(submitError));
    }
  }

  return (
    <AuthLayout
      title="Welcome back"
      subtitle="Return to your character archive."
      footer={
        <p>
          New to the archive? <Link to="/register">Create an account</Link>
        </p>
      }
    >
      <form className="auth-form" onSubmit={handleSubmit(submit)}>
        <label>
          Email
          <input
            type="email"
            autoComplete="email"
            {...register("email", { required: "Enter your email address." })}
          />
          {formState.errors.email && <small>{formState.errors.email.message}</small>}
        </label>
        <label>
          Password
          <input
            type="password"
            autoComplete="current-password"
            {...register("password", { required: "Enter your password." })}
          />
          {formState.errors.password && <small>{formState.errors.password.message}</small>}
        </label>
        {error && <FormMessage kind="error">{error}</FormMessage>}
        <div className="form-row spread">
          <Link className="subtle-link" to="/forgot-password">
            Forgot password?
          </Link>
        </div>
        <button className="button primary full" disabled={formState.isSubmitting}>
          {formState.isSubmitting ? "Signing in..." : "Sign in"}
        </button>
      </form>
      <div className="divider"><span>or</span></div>
      <button
        className="button google full"
        type="button"
        onClick={() => window.location.assign(getOAuth2AuthorizationUrl())}
      >
        <strong>G</strong> Continue with Google
      </button>
      <Link className="button ghost full guest-login-link" to="/guest">
        Continue as guest
      </Link>
    </AuthLayout>
  );
}
