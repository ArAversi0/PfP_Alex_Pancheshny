import { useState } from "react";
import { useForm } from "react-hook-form";
import { Link, useSearchParams } from "react-router-dom";
import { authApi, getApiErrorMessage } from "../api/authApi";
import { AuthLayout } from "../components/AuthLayout";
import { FormMessage } from "../components/FormMessage";

interface ResetPasswordForm {
  password: string;
  confirmPassword: string;
}

export function ResetPasswordPage() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const { register, handleSubmit, getValues, formState } = useForm<ResetPasswordForm>();

  async function submit(values: ResetPasswordForm) {
    if (!token) {
      setError("The password reset link is incomplete.");
      return;
    }
    setError("");
    try {
      setMessage((await authApi.resetPassword(token, values.password, values.confirmPassword)).message);
    } catch (submitError) {
      setError(getApiErrorMessage(submitError));
    }
  }

  return (
    <AuthLayout
      title="Choose a new password"
      subtitle="Use at least eight characters. Your other sessions will be signed out."
      footer={<p><Link to="/login">Return to sign in</Link></p>}
    >
      <form className="auth-form" onSubmit={handleSubmit(submit)}>
        <label>
          New password
          <input type="password" autoComplete="new-password" {...register("password", {
            required: "Create a password.",
            minLength: { value: 8, message: "Use at least 8 characters." },
          })} />
          {formState.errors.password && <small>{formState.errors.password.message}</small>}
        </label>
        <label>
          Confirm password
          <input type="password" autoComplete="new-password" {...register("confirmPassword", {
            required: "Repeat your password.",
            validate: (value) => value === getValues("password") || "Passwords do not match.",
          })} />
          {formState.errors.confirmPassword && <small>{formState.errors.confirmPassword.message}</small>}
        </label>
        {message && <FormMessage kind="success">{message}</FormMessage>}
        {error && <FormMessage kind="error">{error}</FormMessage>}
        {!message && (
          <button className="button primary full" disabled={formState.isSubmitting}>
            {formState.isSubmitting ? "Updating..." : "Update password"}
          </button>
        )}
      </form>
    </AuthLayout>
  );
}
