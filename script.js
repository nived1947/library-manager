const STORAGE_KEY = "bookhub-library-v1";
const starterBooks = [
  { isbn: "978-1", title: "Clean Code", author: "Robert C. Martin", category: "Software Engineering", total: 4, available: 4 },
  { isbn: "978-2", title: "Introduction to Algorithms", author: "Cormen et al.", category: "Algorithms", total: 3, available: 3 },
  { isbn: "978-3", title: "Database System Concepts", author: "Silberschatz et al.", category: "Database", total: 2, available: 2 },
  { isbn: "978-4", title: "The Pragmatic Programmer", author: "Andrew Hunt", category: "Software Engineering", total: 5, available: 5 },
  { isbn: "978-5", title: "Python Crash Course", author: "Eric Matthes", category: "Programming", total: 3, available: 3 }
];

let state = loadState();
let toastTimer;

function loadState() {
  try {
    const saved = localStorage.getItem(STORAGE_KEY);
    if (saved) {
      const parsed = JSON.parse(saved);
      if (Array.isArray(parsed.books) && Array.isArray(parsed.loans) && Array.isArray(parsed.waiting) && Array.isArray(parsed.history)) return parsed;
    }
  } catch (error) {
    console.warn("Could not read saved BookHub data.", error);
  }
  return { books: starterBooks.map(book => ({...book})), loans: [], waiting: [], history: [] };
}

function saveState() {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
  } catch (error) {
    showToast("Your browser could not save data. Check its storage settings.", "error");
  }
}

function escapeHTML(value) {
  return String(value ?? "").replace(/[&<>"']/g, char => ({
    "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
  })[char]);
}

function formatDate(dateString) {
  if (!dateString) return "—";
  return new Date(dateString).toLocaleDateString(undefined, {day:"numeric", month:"short", year:"numeric"});
}

function totalWaiting() {
  return state.waiting.reduce((sum, entry) => sum + entry.readers.length, 0);
}

function showToast(message, type = "success") {
  const toast = document.getElementById("toast");
  toast.textContent = message;
  toast.className = `toast show ${type}`;
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => { toast.className = "toast"; }, 3200);
}

function bookStatus(book) {
  return book.available > 0
    ? '<span class="pill available">Available</span>'
    : '<span class="pill unavailable">All copies issued</span>';
}

function bookName(isbn) {
  return state.books.find(book => book.isbn === isbn)?.title || isbn;
}

function bookIcon(title) {
  const clean = String(title || "?").trim();
  return escapeHTML(clean.charAt(0).toUpperCase() || "?");
}

function bookCell(book) {
  return `<div class="book-cell"><span class="book-cover">${bookIcon(book.title)}</span><span><strong>${escapeHTML(book.title)}</strong><small>${escapeHTML(book.author)}</small></span></div>`;
}

function emptyRow(colspan, title, detail) {
  return `<tr><td colspan="${colspan}"><div class="empty-state"><strong>${escapeHTML(title)}</strong><small>${escapeHTML(detail)}</small></div></td></tr>`;
}

function renderStats() {
  document.getElementById("statTitles").textContent = state.books.length;
  document.getElementById("statAvailable").textContent = state.books.reduce((sum, book) => sum + Number(book.available || 0), 0);
  document.getElementById("statLoans").textContent = state.loans.length;
  document.getElementById("statWaiting").textContent = totalWaiting();
  document.getElementById("waitingNavCount").textContent = totalWaiting();
}

function renderDashboard() {
  const query = document.getElementById("dashboardSearch").value.trim().toLowerCase();
  const filtered = state.books.filter(book => [book.isbn, book.title, book.author, book.category].some(value => String(value).toLowerCase().includes(query))).slice(0, 6);
  document.getElementById("dashboardBookRows").innerHTML = filtered.length ? filtered.map(book => `
    <tr>
      <td>${bookCell(book)}</td>
      <td>${escapeHTML(book.category)}</td>
      <td><strong>${book.available}</strong> / ${book.total}</td>
      <td>${bookStatus(book)}</td>
    </tr>`).join("") : emptyRow(4, "No matching books", "Try another title, author, ISBN or category.");
  document.getElementById("catalogueSummary").textContent = `${state.books.length} title${state.books.length === 1 ? "" : "s"} in collection`;
}

function renderCatalogue() {
  const query = document.getElementById("catalogueSearch").value.trim().toLowerCase();
  const filtered = state.books.filter(book => [book.isbn, book.title, book.author, book.category].some(value => String(value).toLowerCase().includes(query)));
  document.getElementById("catalogueCount").textContent = `${filtered.length} of ${state.books.length} title${state.books.length === 1 ? "" : "s"}`;
  document.getElementById("catalogueRows").innerHTML = filtered.length ? filtered.map(book => `
    <tr>
      <td>${escapeHTML(book.isbn)}</td>
      <td>${bookCell(book)}</td>
      <td>${escapeHTML(book.author)}</td>
      <td>${escapeHTML(book.category)}</td>
      <td><strong>${book.available}</strong></td>
      <td>${book.total}</td>
      <td>${bookStatus(book)}</td>
    </tr>`).join("") : emptyRow(7, "No books found", "Change your search or add a new title.");
}

function renderLoanOptions() {
  const issueSelect = document.getElementById("issueBookSelect");
  const previous = issueSelect.value;
  issueSelect.innerHTML = state.books.map(book => `<option value="${escapeHTML(book.isbn)}">${escapeHTML(book.title)} — ${book.available} available</option>`).join("");
  if (state.books.some(book => book.isbn === previous)) issueSelect.value = previous;

  const returnSelect = document.getElementById("returnLoanSelect");
  returnSelect.innerHTML = state.loans.map(loan => `<option value="${escapeHTML(loan.studentId)}">${escapeHTML(loan.studentName)} — ${escapeHTML(bookName(loan.isbn))} (${escapeHTML(loan.studentId)})</option>`).join("");
  if (!state.loans.length) returnSelect.innerHTML = '<option value="">No active loans</option>';
}

function renderLoans() {
  const rows = document.getElementById("loanRows");
  rows.innerHTML = state.loans.length ? state.loans.map(loan => `
    <tr>
      <td><strong>${escapeHTML(loan.studentName)}</strong></td>
      <td>${escapeHTML(loan.studentId)}</td>
      <td>${escapeHTML(bookName(loan.isbn))}</td>
      <td>${escapeHTML(loan.isbn)}</td>
      <td>${formatDate(loan.issuedOn)}</td>
      <td><button class="action-link" data-return-student="${escapeHTML(loan.studentId)}">Process return</button></td>
    </tr>`).join("") : emptyRow(6, "No active loans", "Issued books will appear here.");
}

function renderWaiting() {
  const container = document.getElementById("waitingGroups");
  const groups = state.waiting.filter(group => group.readers.length);
  if (!groups.length) {
    container.innerHTML = `<section class="panel"><div class="empty-state"><strong>No readers are waiting</strong><small>When a title has no available copies, readers can be added to a first-in, first-out queue.</small></div></section>`;
    return;
  }
  container.innerHTML = groups.map(group => `
    <section class="waiting-card">
      <div class="waiting-card-header"><div><strong>${escapeHTML(bookName(group.isbn))}</strong><small>ISBN: ${escapeHTML(group.isbn)} · ${group.readers.length} waiting reader${group.readers.length === 1 ? "" : "s"}</small></div><span class="pill waiting">Queue active</span></div>
      ${group.readers.map((reader, index) => `<div class="reader-row"><span class="queue-position">${index + 1}</span><span class="reader-avatar">${escapeHTML(reader.name.trim().charAt(0).toUpperCase())}</span><span><strong>${escapeHTML(reader.name)}</strong><small>Student ID: ${escapeHTML(reader.studentId)} · Added ${formatDate(reader.addedOn)}</small></span><span class="position">Position ${index + 1}</span></div>`).join("")}
    </section>`).join("");
}

function renderHistory() {
  const history = [...state.history].reverse();
  document.getElementById("historyRows").innerHTML = history.length ? history.map(entry => `
    <tr><td><strong>${escapeHTML(entry.title)}</strong><br><small>${escapeHTML(entry.isbn)}</small></td><td>${escapeHTML(entry.studentName)}</td><td>${escapeHTML(entry.studentId)}</td><td>${formatDate(entry.returnedOn)}</td></tr>`).join("") : emptyRow(4, "No returns recorded yet", "Completed returns will appear here.");
}

function renderAll() {
  renderStats();
  renderDashboard();
  renderCatalogue();
  renderLoanOptions();
  renderLoans();
  renderWaiting();
  renderHistory();
}

function navigate(page) {
  const validPages = ["dashboard", "books", "loans", "waiting", "history"];
  if (!validPages.includes(page)) return;
  document.querySelectorAll(".page").forEach(section => section.classList.toggle("active", section.id === `page-${page}`));
  document.querySelectorAll(".nav-item[data-page]").forEach(button => button.classList.toggle("active", button.dataset.page === page));
  const labels = {dashboard:"Dashboard", books:"Book catalogue", loans:"Active loans", waiting:"Waiting list", history:"Return history"};
  document.getElementById("pageHeading").textContent = labels[page];
  document.getElementById("sidebar").classList.remove("open");
  window.scrollTo({top:0, behavior:"smooth"});
}

function openDialog(id) {
  if (id === "issueDialog") renderLoanOptions();
  if (id === "returnDialog") renderLoanOptions();
  const dialog = document.getElementById(id);
  if (id === "returnDialog" && !state.loans.length) {
    showToast("There are no active loans to return.", "error");
    return;
  }
  if (id === "issueDialog" && !state.books.length) {
    showToast("Add a book to the catalogue before issuing one.", "error");
    return;
  }
  dialog.showModal();
}

function closeDialog(id) {
  document.getElementById(id).close();
}

function addBook(form) {
  const data = new FormData(form);
  const isbn = String(data.get("isbn")).trim();
  const title = String(data.get("title")).trim();
  const author = String(data.get("author")).trim();
  const category = String(data.get("category")).trim();
  const copies = Number(data.get("copies"));
  if (!isbn || !title || !author || !category || !Number.isInteger(copies) || copies < 1) {
    showToast("Please enter valid details in every field.", "error");
    return;
  }
  const existing = state.books.find(book => book.isbn.toLowerCase() === isbn.toLowerCase());
  if (existing) {
    if (existing.title.toLowerCase() !== title.toLowerCase()) {
      showToast("That ISBN already belongs to a different title. Check the book ID.", "error");
      return;
    }
    existing.total += copies;
    existing.available += copies;
    showToast(`${copies} additional copy/copies added to ${existing.title}.`);
  } else {
    state.books.push({isbn, title, author, category, total:copies, available:copies});
    showToast(`${title} added to the catalogue.`);
  }
  saveState();
  renderAll();
  form.reset();
  form.elements.copies.value = "1";
  closeDialog("bookDialog");
  navigate("books");
}

function issueBook(form) {
  const data = new FormData(form);
  const studentId = String(data.get("studentId")).trim();
  const studentName = String(data.get("studentName")).trim();
  const isbn = String(data.get("isbn")).trim();
  if (!studentId || !studentName || !isbn) {
    showToast("Complete the student details and select a book.", "error");
    return;
  }
  if (state.loans.some(loan => loan.studentId.toLowerCase() === studentId.toLowerCase()) ||
      state.waiting.some(group => group.readers.some(reader => reader.studentId.toLowerCase() === studentId.toLowerCase()))) {
    showToast("This student already has a loan or is on a waiting list.", "error");
    return;
  }
  const book = state.books.find(item => item.isbn === isbn);
  if (!book) {
    showToast("Please select a valid book.", "error");
    return;
  }
  if (book.available > 0) {
    book.available--;
    state.loans.push({studentId, studentName, isbn, issuedOn:new Date().toISOString()});
    showToast(`${book.title} issued to ${studentName}.`);
    saveState(); renderAll(); closeDialog("issueDialog"); form.reset();
    return;
  }
  const joinQueue = window.confirm(`No copies of "${book.title}" are available.\n\nAdd ${studentName} to the waiting list?`);
  if (joinQueue) {
    let group = state.waiting.find(entry => entry.isbn === isbn);
    if (!group) {
      group = {isbn, readers:[]};
      state.waiting.push(group);
    }
    group.readers.push({studentId, name:studentName, addedOn:new Date().toISOString()});
    showToast(`${studentName} added to the waiting list.`);
    saveState(); renderAll(); closeDialog("issueDialog"); form.reset();
  }
}

function returnBook(studentId) {
  const index = state.loans.findIndex(loan => loan.studentId === studentId);
  if (index < 0) {
    showToast("That active loan could not be found.", "error");
    return;
  }
  const [loan] = state.loans.splice(index, 1);
  const book = state.books.find(item => item.isbn === loan.isbn);
  state.history.push({isbn:loan.isbn, title:book?.title || loan.isbn, studentId:loan.studentId, studentName:loan.studentName, returnedOn:new Date().toISOString()});
  const group = state.waiting.find(entry => entry.isbn === loan.isbn && entry.readers.length);
  if (group) {
    const nextReader = group.readers.shift();
    state.loans.push({studentId:nextReader.studentId, studentName:nextReader.name, isbn:loan.isbn, issuedOn:new Date().toISOString()});
    if (!group.readers.length) state.waiting = state.waiting.filter(entry => entry !== group);
    showToast(`Return recorded. ${book?.title || "The book"} is now assigned to ${nextReader.name}.`);
  } else if (book) {
    book.available = Math.min(book.total, book.available + 1);
    showToast(`${book?.title || "Book"} returned successfully.`);
  }
  saveState(); renderAll();
}

document.addEventListener("click", event => {
  const pageButton = event.target.closest("[data-page]");
  const actionButton = event.target.closest("[data-action]");
  const closeButton = event.target.closest("[data-close]");
  const returnButton = event.target.closest("[data-return-student]");
  if (pageButton) {
    event.preventDefault();
    navigate(pageButton.dataset.page);
  }
  if (actionButton) {
    const actions = {add:"bookDialog", issue:"issueDialog", return:"returnDialog"};
    openDialog(actions[actionButton.dataset.action]);
  }
  if (closeButton) closeDialog(closeButton.dataset.close);
  if (returnButton) {
    const studentId = returnButton.dataset.returnStudent;
    if (window.confirm("Process the return for this reader?")) returnBook(studentId);
  }
});

document.getElementById("bookForm").addEventListener("submit", event => {
  event.preventDefault();
  addBook(event.currentTarget);
});
document.getElementById("issueForm").addEventListener("submit", event => {
  event.preventDefault();
  issueBook(event.currentTarget);
});
document.getElementById("returnForm").addEventListener("submit", event => {
  event.preventDefault();
  const studentId = String(new FormData(event.currentTarget).get("studentId") || "");
  if (!studentId) {
    showToast("There is no active loan selected.", "error");
    return;
  }
  returnBook(studentId);
  closeDialog("returnDialog");
});
document.getElementById("dashboardSearch").addEventListener("input", renderDashboard);
document.getElementById("catalogueSearch").addEventListener("input", renderCatalogue);
document.getElementById("menuToggle").addEventListener("click", () => document.getElementById("sidebar").classList.toggle("open"));
document.querySelectorAll("dialog").forEach(dialog => dialog.addEventListener("click", event => {
  if (event.target === dialog) dialog.close();
}));

document.getElementById("todayLabel").textContent = new Date().toLocaleDateString(undefined, {weekday:"short", day:"numeric", month:"short", year:"numeric"});
renderAll();
