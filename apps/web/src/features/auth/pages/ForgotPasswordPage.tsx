import { useState } from "react";
import { useForm } from "react-hook-form";
import { Link } from "react-router-dom";
import { authApi, getApiErrorMessage } from "../api/authApi";
import { AuthLayout } from "../components/AuthLayout";
import { FormMessage } from "../components/FormMessage";

export function ForgotPasswordPage() {
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const { register, handleSubmit, formState } = useForm<{ email: string }>();

  async function submit({ email }: { email: string }) {
    setError("");
    try {
      setMessage((await authApi.forgotPassword(email)).message);
    } catch (submitError) {
      setError(getApiErrorMessage(submitError));
    }
  }

  return (
    <AuthLayout
      title="Reset your password"
      subtitle="We will send a recovery link if an account exists for this email."
      footer={<p><Link to="/login">Return to sign in</Link></p>}
    >
      <form className="auth-form" onSubmit={handleSubmit(submit)}>
        <label>
          Email
          <input type="email" autoComplete="email" {...register("email", {
            required: "Enter your email address.",
          })} />
          {formState.errors.email && <small>{formState.errors.email.message}</small>}
        </label>
        {message && <FormMessage kind="success">{message}</FormMessage>}
        {error && <FormMessage kind="error">{error}</FormMessage>}
        <button className="button primary full" disabled={formState.isSubmitting}>
          {formState.isSubmitting ? "Sending..." : "Send recovery link"}
        </button>
      </form>
    </AuthLayout>
  );
}
