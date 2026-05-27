import type { GuestCharacter } from "./guestCharacterTypes";
import { createBlankGuestCharacter } from "./pfpGuestRules";

const STORAGE_KEY = "pfp.guest.character";

export function loadGuestCharacter(): GuestCharacter {
  const stored = sessionStorage.getItem(STORAGE_KEY);
  if (!stored) return createBlankGuestCharacter();
  try {
    return JSON.parse(stored) as GuestCharacter;
  } catch {
    return createBlankGuestCharacter();
  }
}

export function saveGuestCharacter(character: GuestCharacter): void {
  sessionStorage.setItem(STORAGE_KEY, JSON.stringify(character));
}

export function clearGuestCharacter(): GuestCharacter {
  const character = createBlankGuestCharacter();
  saveGuestCharacter(character);
  return character;
}
