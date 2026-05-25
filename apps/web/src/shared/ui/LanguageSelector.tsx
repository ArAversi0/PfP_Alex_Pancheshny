import { useEffect, useState } from "react";

const STORAGE_KEY = "pfp-ui-language";

export function LanguageSelector() {
  const [language, setLanguage] = useState("ru");

  useEffect(() => {
    const storedLanguage = window.localStorage.getItem(STORAGE_KEY);
    if (storedLanguage === "ru" || storedLanguage === "en") {
      setLanguage(storedLanguage);
    }
  }, []);

  function updateLanguage(value: string) {
    setLanguage(value);
    window.localStorage.setItem(STORAGE_KEY, value);
  }

  return (
    <label className="language-selector">
      <span>Language</span>
      <select value={language} onChange={(event) => updateLanguage(event.target.value)}>
        <option value="ru">Русский</option>
        <option value="en">Английский</option>
      </select>
    </label>
  );
}
