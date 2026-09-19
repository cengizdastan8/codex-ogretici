import { STORAGE_KEY, createNote, filterNotes, readNotes, removeNote, updateNote } from "./notes.js";

const form = document.querySelector("#note-form");
const titleInput = document.querySelector("#note-title");
const bodyInput = document.querySelector("#note-body");
const notesList = document.querySelector("#notes-list");
const emptyState = document.querySelector("#empty-state");
const noResultsState = document.querySelector("#no-results-state");
const noteCount = document.querySelector("#note-count");
const searchInput = document.querySelector("#note-search");
const searchStatus = document.querySelector("#search-status");
const formStatus = document.querySelector("#form-status");
const saveNoteButton = document.querySelector("#save-note-button");
const cancelEditButton = document.querySelector("#cancel-edit-button");

let notes = readNotes(localStorage.getItem(STORAGE_KEY));
let editingId = null;

function saveNotes() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(notes));
}

function formatDate(value) {
  return new Intl.DateTimeFormat("tr-TR", {
    dateStyle: "short",
    timeStyle: "short"
  }).format(new Date(value));
}

function renderNotes() {
  const query = searchInput.value.trim();
  const visibleNotes = filterNotes(notes, query);

  notesList.replaceChildren();
  emptyState.hidden = notes.length > 0;
  noResultsState.hidden = notes.length === 0 || visibleNotes.length > 0;
  noteCount.textContent = String(notes.length);
  searchStatus.textContent = query ? `${visibleNotes.length} sonuç bulundu.` : "";

  for (const note of visibleNotes) {
    const item = document.createElement("li");
    item.className = "note-card";

    const heading = document.createElement("h3");
    heading.textContent = note.title;

    const body = document.createElement("p");
    body.textContent = note.body || "Açıklama eklenmedi.";

    const date = document.createElement("time");
    date.dateTime = note.createdAt;
    date.textContent = formatDate(note.createdAt);

    const actions = document.createElement("div");
    actions.className = "note-actions";

    const editButton = document.createElement("button");
    editButton.type = "button";
    editButton.textContent = "Düzenle";
    editButton.setAttribute("aria-label", `${note.title} notunu düzenle`);
    editButton.addEventListener("click", () => {
      editingId = note.id;
      titleInput.value = note.title;
      bodyInput.value = note.body;
      saveNoteButton.textContent = "Değişiklikleri kaydet";
      cancelEditButton.hidden = false;
      formStatus.textContent = "Not düzenleniyor.";
      titleInput.focus();
    });

    const deleteButton = document.createElement("button");
    deleteButton.type = "button";
    deleteButton.textContent = "Sil";
    deleteButton.setAttribute("aria-label", `${note.title} notunu sil`);
    deleteButton.addEventListener("click", () => {
      notes = removeNote(notes, note.id);
      saveNotes();
      if (editingId === note.id) resetEditMode();
      renderNotes();
      formStatus.textContent = "Not silindi.";
    });

    actions.append(editButton, deleteButton);
    item.append(heading, body, date, actions);
    notesList.append(item);
  }
}

function resetEditMode() {
  editingId = null;
  form.reset();
  saveNoteButton.textContent = "Notu ekle";
  cancelEditButton.hidden = true;
}

form.addEventListener("submit", (event) => {
  event.preventDefault();

  try {
    if (editingId) {
      notes = updateNote(notes, editingId, titleInput.value, bodyInput.value);
    } else {
      const note = createNote(titleInput.value, bodyInput.value);
      notes = [note, ...notes];
    }

    saveNotes();
    renderNotes();
    const statusMessage = editingId ? "Not güncellendi." : "Not kaydedildi.";
    resetEditMode();
    titleInput.focus();
    formStatus.textContent = statusMessage;
  } catch (error) {
    formStatus.textContent = error.message;
  }
});

cancelEditButton.addEventListener("click", () => {
  resetEditMode();
  formStatus.textContent = "Düzenleme iptal edildi.";
  titleInput.focus();
});

searchInput.addEventListener("input", renderNotes);

renderNotes();
