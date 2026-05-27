import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { Link, useNavigate } from "react-router-dom";
import { getApiErrorMessage } from "../../auth/api/authApi";
import { FormMessage } from "../../auth/components/FormMessage";
import { SiteLayout } from "../../../shared/ui/SiteLayout";
import { characterApi } from "../api/characterApi";
import { CHARACTER_LIMIT, type CharacterInfoUpdate } from "../model/characterTypes";

export function CreateCharacterPage() {
  const navigate = useNavigate();
  const [error, setError] = useState("");
  const [characterCount, setCharacterCount] = useState<number | null>(null);
  const { register, handleSubmit, formState } = useForm<CharacterInfoUpdate>({
    defaultValues: {
      level: 1,
      name: "",
      origin: "",
      background: "",
      className: "",
      specialization: "",
    },
  });
  const limitReached = characterCount !== null && characterCount >= CHARACTER_LIMIT;

  useEffect(() => {
    characterApi.list().then(
      (characters) => setCharacterCount(characters.length),
      (loadError: unknown) => setError(getApiErrorMessage(loadError)),
    );
  }, []);

  async function submit(values: CharacterInfoUpdate) {
    if (limitReached) {
      setError("Character limit reached. Delete a character before creating another.");
      return;
    }
    setError("");
    try {
      const created = await characterApi.create(values.name);
      await characterApi.updateInfo(created.id, values);
      navigate(`/characters/${created.id}`, { replace: true });
    } catch (submitError) {
      setError(getApiErrorMessage(submitError));
    }
  }

  return (
    <SiteLayout>
      <div className="page-heading">
        <p className="eyebrow">New character</p>
        <h1>Begin a legend</h1>
        <p className="intro">Start with the details that shape the top of your sheet.</p>
        <div className={limitReached ? "character-count limit" : "character-count"}>
          <strong>{characterCount ?? "..."}</strong>
          <span>/ {CHARACTER_LIMIT}</span>
        </div>
      </div>
      {limitReached && (
        <FormMessage kind="error">Character limit reached. Delete a character before creating another.</FormMessage>
      )}
      <form className="sheet-form create-character-form" onSubmit={handleSubmit(submit)}>
        <label>
          Character name
          <input disabled={limitReached} {...register("name", { required: "Enter a character name." })} />
          {formState.errors.name && <small>{formState.errors.name.message}</small>}
        </label>
        <label>
          Level
          <input type="number" min="1" disabled={limitReached} {...register("level", {
            required: true,
            valueAsNumber: true,
            min: { value: 1, message: "Level must be at least 1." },
          })} />
          {formState.errors.level && <small>{formState.errors.level.message}</small>}
        </label>
        <label>
          Origin
          <input disabled={limitReached} {...register("origin")} />
        </label>
        <label>
          Background
          <input disabled={limitReached} {...register("background")} />
        </label>
        <label>
          Class
          <input disabled={limitReached} {...register("className")} />
        </label>
        <label>
          Specialization
          <input disabled={limitReached} {...register("specialization")} />
        </label>
        {error && <div className="form-span"><FormMessage kind="error">{error}</FormMessage></div>}
        <div className="form-span hero-actions">
          <button className="button primary" disabled={formState.isSubmitting || limitReached}>
            {formState.isSubmitting ? "Creating..." : "Create and open sheet"}
          </button>
          <Link className="button ghost" to="/characters">Cancel</Link>
        </div>
      </form>
    </SiteLayout>
  );
}
