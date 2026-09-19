export const STORAGE_KEY = "codex-egitim-notlari-v1";

export function cleanText(value, maxLength) {
  return String(value ?? "").trim().slice(0, maxLength);
}

export function createNote(title, body, options = {}) {
  const cleanTitle = cleanText(title, 80);
  const cleanBody = cleanText(body, 500);

  if (!cleanTitle) {
    throw new Error("Başlık boş bırakılamaz.");
  }

  return {
    id: options.id ?? crypto.randomUUID(),
    title: cleanTitle,
    body: cleanBody,
    createdAt: options.now ?? new Date().toISOString()
  };
}

export function removeNote(notes, id) {
  return notes.filter((note) => note.id !== id);
}

export function updateNote(notes, id, title, body) {
  const cleanTitle = cleanText(title, 80);
  const cleanBody = cleanText(body, 500);

  if (!cleanTitle) {
    throw new Error("Başlık boş bırakılamaz.");
  }

  return notes.map((note) =>
    note.id === id
      ? { ...note, title: cleanTitle, body: cleanBody }
      : note
  );
}

export function filterNotes(notes, query) {
  const normalizedQuery = String(query ?? "").trim().normalize("NFC").toLocaleLowerCase("tr-TR");

  if (!normalizedQuery) return notes;

  return notes.filter((note) => {
    const searchableText = `${note.title ?? ""} ${note.body ?? ""}`
      .normalize("NFC")
      .toLocaleLowerCase("tr-TR");

    return searchableText.includes(normalizedQuery);
  });
}

export function readNotes(rawValue) {
  if (!rawValue) return [];

  try {
    const parsed = JSON.parse(rawValue);
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
}
