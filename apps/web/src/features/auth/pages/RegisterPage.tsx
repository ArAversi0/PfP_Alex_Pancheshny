import { useState } from "react";
import { useForm } from "react-hook-form";
import { Link, useNavigate } from "react-router-dom";
import { authApi, getApiErrorMessage } from "../api/authApi";
import { AuthLayout } from "../components/AuthLayout";
import { FormMessage } from "../components/FormMessage";

interface RegisterForm {
  email: string;
  password: string;
  confirmPassword: string;
}

export function RegisterPage() {
  const navigate = useNavigate();
  const [error, setError] = useState("");
  const { register, handleSubmit, getValues, formState } = useForm<RegisterForm>();

  async function submit(values: RegisterForm) {
    setError("");
    try {
      await authApi.register(values.email, values.password, values.confirmPassword);
      navigate(`/verify-email?sent=1&email=${encodeURIComponent(values.email)}`);
    } catch (submitError) {
      setError(getApiErrorMessage(submitError));
    }
  }

  return (
    <AuthLayout
      title="Begin your archive"
      subtitle="Create an account to keep your characters available on every device."
      footer={<p>Already registered? <Link to="/login">Sign in</Link></p>}
    >
      <form className="auth-form" onSubmit={handleSubmit(submit)}>
        <label>
          Email
          <input type="email" autoComplete="email" {...register("email", {
            required: "Enter your email address.",
          })} />
          {formState.errors.email && <small>{formState.errors.email.message}</small>}
        </label>
        <label>
          Password
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
        {error && <FormMessage kind="error">{error}</FormMessage>}
        <button className="button primary full" disabled={formState.isSubmitting}>
          {formState.isSubmitting ? "Creating account..." : "Create account"}
        </button>
      </form>
    </AuthLayout>
  );
}
