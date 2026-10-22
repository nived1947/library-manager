# BookHub Library Desk — HTML, CSS and JavaScript

A responsive, librarian-friendly library management website.

## Files
- `index.html` — page structure and forms
- `style.css` — responsive visual design
- `script.js` — book catalogue, issuing, returns, waiting queue and local saving

## Features
- Dashboard with book, availability, active-loan and waiting-list counts
- Searchable book catalogue
- Add a new title or add copies to an existing ISBN
- Issue a book to a student
- Return an active loan
- FIFO waiting list when all copies of a title are issued
- Automatically assigns a returned copy to the next waiting reader
- Active loans and return history
- Responsive sidebar for desktop and mobile
- Saves changes in browser `localStorage`

## Run it
1. Extract the ZIP file.
2. Open `index.html` in a modern browser, or open the folder in VS Code and use Live Server.
3. No Java, JDK, backend, API or internet connection is required for the core features.

## Publish using GitHub Pages
1. Upload `index.html`, `style.css` and `script.js` to the **root** of your existing GitHub repository.
2. Keep your Java `.java` files; you do not need to delete them.
3. Open the repository's **Settings → Pages**.
4. Select **Deploy from a branch**, choose your current branch (for example `master`) and folder `/(root)`, then save if needed.
5. Wait for deployment, then open the GitHub Pages URL shown in Settings → Pages.

## Data and privacy note
This is a frontend demo. Records are stored in the browser's local storage on the device/browser used. They are not shared between computers or browsers and are not a secure, central database. Clearing browser data may remove them. For a real library deployment, connect a backend and database and add user authentication, backups and permissions.
