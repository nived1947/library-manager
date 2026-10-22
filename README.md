# BookHub – Library Management System

## Overview

BookHub is a modern, responsive web-based library management system designed to simplify everyday library operations. Built using HTML, CSS, and JavaScript, it provides an intuitive interface for managing book inventories, tracking borrowed books, processing returns, and organizing reader waiting lists.

## Features

- **Dashboard:** Displays key library statistics, including total books, available copies, active loans, and waiting readers.
- **Book Management:** Add new books, update inventory by adding copies, and maintain book details.
- **Book Search:** Search the catalogue to find books quickly.
- **Book Issuing:** Record book loans to students and manage borrowing availability.
- **Book Returns:** Process returned books and update availability automatically.
- **Waiting List Management:** Maintain a first-in, first-out (FIFO) waiting list for unavailable books.
- **Automatic Book Allocation:** Assign returned copies to the next eligible reader in the waiting list.
- **Loan Tracking:** View active loans and borrowing history.
- **Responsive Interface:** Provides a clean, professional layout for different screen sizes.
- **Local Data Storage:** Uses browser local storage to preserve changes between visits.

## Technologies Used

- HTML5 – Website structure
- CSS3 – Styling and responsive layout
- JavaScript – Application logic and interactive functionality
- Browser Local Storage – Client-side data persistence

## Getting Started

### Prerequisites

A modern web browser such as Google Chrome, Microsoft Edge, or Firefox.

### Installation

1. Clone or download this repository.
2. Open the project folder.
3. Locate `index.html`.
4. Open `index.html` in your web browser.

No additional software installation or backend server is required for the current frontend implementation.

## Project Structure

```text
BookHub/
├── index.html
├── style.css
├── script.js
└── README.md
```

## Data Storage

BookHub uses browser local storage to save library information. The data remains available in the same browser unless its stored data is cleared. Data is not automatically shared across different browsers or computers.

## Deployment

The website can be hosted using GitHub Pages.

1. Upload the project files to the repository.
2. Navigate to **Settings → Pages**.
3. Select **Deploy from a branch**.
4. Choose the branch containing the website files.
5. Select the root folder (`/root`) and save the settings.

## Future Enhancements

- Database integration for centralized storage
- User authentication and role-based access
- Student and librarian accounts
- Fine calculation and overdue notifications
- Advanced reporting and analytics
- Cloud synchronization across devices

## License

This project is intended for educational and demonstration purposes. Add a suitable open-source license if you plan to distribute it publicly.

## Author

Developed as a library management project to demonstrate web development concepts and practical inventory management functionality.
