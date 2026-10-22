import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * BookHub - Library Management System
 * A librarian-friendly Java Swing application demonstrating:
 * HashMap, Queue, Stack, ArrayList and Binary Search Tree.
 *
 * Compile: javac LibraryManagementSystem.java
 * Run:     java LibraryManagementSystem
 */
public class LibraryManagementSystem extends JFrame {
    private static final Color NAVY = new Color(25, 38, 66);
    private static final Color BLUE = new Color(55, 98, 210);
    private static final Color BG = new Color(246, 248, 252);
    private static final Color WHITE = Color.WHITE;
    private static final Color TEXT = new Color(42, 52, 72);
    private static final Color MUTED = new Color(112, 123, 143);
    private static final Color BORDER = new Color(225, 230, 239);
    private static final Color GREEN = new Color(28, 139, 95);
    private static final Font BODY = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font HEADING = new Font("Segoe UI", Font.BOLD, 23);

    private final Map<String, Book> books = new LinkedHashMap<>();
    private final Map<String, Student> issuedBooks = new LinkedHashMap<>();
    private final Map<String, Queue<Student>> waitingLists = new LinkedHashMap<>();
    private final Stack<String> recentlyReturned = new Stack<>();
    private final List<Book> bookList = new ArrayList<>();
    private TopicNode topicRoot;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel pages = new JPanel(cardLayout);
    private final JLabel totalValue = new JLabel("0");
    private final JLabel availableValue = new JLabel("0");
    private final JLabel issuedValue = new JLabel("0");
    private final JLabel waitingValue = new JLabel("0");
    private DefaultTableModel tableModel;
    private JTable bookTable;
    private JTextField bookSearch;
    private JTextArea waitingArea;
    private JTextArea returnedArea;
    private JTextArea topicResults;
    private JTextField topicSearch;
    private JLabel pageTitle;
    private String currentPage = "Dashboard";

    static class Book {
        String isbn, title, author, topic;
        int availableCopies;
        int totalCopies;

        Book(String isbn, String title, String author, String topic, int copies) {
            this.isbn = isbn;
            this.title = title;
            this.author = author;
            this.topic = topic;
            this.availableCopies = copies;
            this.totalCopies = copies;
        }
    }

    static class Student {
        String id, name, isbn;
        Student(String id, String name, String isbn) {
            this.id = id;
            this.name = name;
            this.isbn = isbn;
        }
    }

    static class TopicNode {
        String topic;
        List<Book> books = new ArrayList<>();
        TopicNode left, right;
        TopicNode(String topic) { this.topic = topic; }
    }

    public LibraryManagementSystem() {
        super("BookHub | Library Management");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1020, 680));
        setSize(1240, 790);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);
        seedCatalogue();
        buildInterface();
        refreshAll();
        showPage("Dashboard");
    }

    private void seedCatalogue() {
        addInitialBook("978-1", "Clean Code", "Robert C. Martin", "Software Engineering", 4);
        addInitialBook("978-2", "Introduction to Algorithms", "Cormen et al.", "Algorithms", 3);
        addInitialBook("978-3", "Database System Concepts", "Silberschatz et al.", "Database", 2);
        addInitialBook("978-4", "The Pragmatic Programmer", "Andrew Hunt", "Software Engineering", 5);
        addInitialBook("978-5", "Python Crash Course", "Eric Matthes", "Programming", 3);
    }

    private void addInitialBook(String isbn, String title, String author, String topic, int copies) {
        Book book = new Book(isbn, title, author, topic, copies);
        books.put(isbn, book);
        bookList.add(book);
        topicRoot = insertTopic(topicRoot, book);
    }

    private void buildInterface() {
        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(BG);
        main.add(buildTopbar(), BorderLayout.NORTH);

        pages.setBackground(BG);
        pages.add(buildDashboard(), "Dashboard");
        pages.add(buildBooksPage(), "All Books");
        pages.add(buildWaitingPage(), "Waiting List");
        pages.add(buildReturnedPage(), "Recently Returned");
        pages.add(buildTopicPage(), "Search by Topic");
        main.add(pages, BorderLayout.CENTER);
        add(main, BorderLayout.CENTER);
    }

    private JPanel buildSidebar() {
        JPanel side = new JPanel();
        side.setPreferredSize(new Dimension(228, 0));
        side.setBackground(NAVY);
        side.setBorder(new EmptyBorder(24, 14, 18, 14));
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));

        JLabel brand = new JLabel("  BOOKHUB");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 24));
        brand.setForeground(WHITE);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(brand);

        JLabel subtitle = new JLabel("  LIBRARY WORKSPACE");
        subtitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        subtitle.setForeground(new Color(173, 188, 216));
        subtitle.setBorder(new EmptyBorder(7, 0, 28, 0));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(subtitle);

        addNavButton(side, "▦", "Dashboard");
        addNavButton(side, "▤", "All Books");
        addNavButton(side, "＋", "Add Book");
        addNavButton(side, "↗", "Issue Book");
        addNavButton(side, "↙", "Return Book");
        side.add(Box.createVerticalStrut(14));

        JLabel manage = new JLabel("  COLLECTION TOOLS");
        manage.setFont(new Font("Segoe UI", Font.BOLD, 10));
        manage.setForeground(new Color(145, 161, 190));
        manage.setBorder(new EmptyBorder(6, 0, 8, 0));
        manage.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(manage);

        addNavButton(side, "☷", "Waiting List");
        addNavButton(side, "↶", "Recently Returned");
        addNavButton(side, "⌕", "Search by Topic");
        side.add(Box.createVerticalGlue());

        JLabel footer = new JLabel("<html><b>LIBRARY DESK</b><br><font color='#AEBBD3'>Simple • Organised • Reliable</font></html>");
        footer.setForeground(WHITE);
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footer.setBorder(new EmptyBorder(12, 5, 5, 5));
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(footer);
        return side;
    }

    private void addNavButton(JPanel side, String icon, String label) {
        JButton button = new JButton("  " + icon + "     " + label);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(new Color(228, 234, 245));
        button.setBackground(NAVY);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 43));
        button.setPreferredSize(new Dimension(198, 43));
        button.setBorder(new EmptyBorder(9, 10, 9, 7));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.addActionListener(e -> {
            if ("Add Book".equals(label)) addBookDialog();
            else if ("Issue Book".equals(label)) issueBookDialog();
            else if ("Return Book".equals(label)) returnBookDialog();
            else showPage(label);
        });
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(42, 59, 95));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(NAVY);
            }
        });
        side.add(button);
        side.add(Box.createVerticalStrut(4));
    }

    private JPanel buildTopbar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(WHITE);
        top.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(17, 27, 17, 27)));

        pageTitle = new JLabel("Dashboard");
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        pageTitle.setForeground(TEXT);
        top.add(pageTitle, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 9, 0));
        right.setBackground(WHITE);
        JLabel role = new JLabel("LIBRARIAN");
        role.setFont(new Font("Segoe UI", Font.BOLD, 10));
        role.setForeground(BLUE);
        role.setOpaque(true);
        role.setBackground(new Color(235, 241, 255));
        role.setBorder(new EmptyBorder(7, 10, 7, 10));
        JLabel date = new JLabel("Library Management Desk");
        date.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        date.setForeground(MUTED);
        right.add(date);
        right.add(role);
        top.add(right, BorderLayout.EAST);
        return top;
    }

    private JPanel pageShell() {
        JPanel p = new JPanel(new BorderLayout(0, 18));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(24, 27, 25, 27));
        return p;
    }

    private JPanel buildDashboard() {
        JPanel page = pageShell();
        JPanel intro = new JPanel();
        intro.setLayout(new BoxLayout(intro, BoxLayout.Y_AXIS));
        intro.setBackground(BG);

        JLabel welcome = new JLabel("Good day, Librarian");
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcome.setForeground(NAVY);
        JLabel hint = new JLabel("Here is a quick overview of your library collection.");
        hint.setFont(BODY);
        hint.setForeground(MUTED);
        hint.setBorder(new EmptyBorder(6, 0, 0, 0));
        intro.add(welcome);
        intro.add(hint);
        page.add(intro, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 20));
        body.setBackground(BG);
        JPanel stats = new JPanel(new GridLayout(1, 4, 14, 14));
        stats.setBackground(BG);
        stats.add(statCard("BOOK TITLES", totalValue, "Unique titles in catalogue", BLUE));
        stats.add(statCard("AVAILABLE COPIES", availableValue, "Ready to borrow", GREEN));
        stats.add(statCard("BOOKS ON LOAN", issuedValue, "Currently issued", new Color(143, 91, 190)));
        stats.add(statCard("WAITING READERS", waitingValue, "Waiting for a copy", new Color(205, 137, 37)));

        JPanel actions = new JPanel(new BorderLayout(0, 14));
        actions.setBackground(BG);
        JLabel heading = new JLabel("Quick actions");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 18));
        heading.setForeground(TEXT);
        actions.add(heading, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(2, 2, 14, 14));
        cards.setBackground(BG);
        cards.add(actionCard("＋", "Add a book", "Register a new title or extra copies.", this::addBookDialog));
        cards.add(actionCard("↗", "Issue a book", "Record a loan for a reader.", this::issueBookDialog));
        cards.add(actionCard("↙", "Return a book", "Complete a return and handle the queue.", this::returnBookDialog));
        cards.add(actionCard("⌕", "Find a title", "Search by title, author, ISBN or topic.", () -> showPage("All Books")));
        actions.add(cards, BorderLayout.CENTER);

        body.add(stats, BorderLayout.NORTH);
        body.add(actions, BorderLayout.CENTER);
        page.add(body, BorderLayout.CENTER);
        return page;
    }

    private JPanel statCard(String title, JLabel value, String description, Color accent) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true),
                new EmptyBorder(17, 17, 16, 17)));
        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.BOLD, 10));
        label.setForeground(MUTED);
        value.setFont(new Font("Segoe UI", Font.BOLD, 29));
        value.setForeground(accent);
        JLabel detail = new JLabel(description);
        detail.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        detail.setForeground(MUTED);
        card.add(label);
        card.add(Box.createVerticalStrut(11));
        card.add(value);
        card.add(Box.createVerticalStrut(6));
        card.add(detail);
        return card;
    }

    private JPanel actionCard(String symbol, String title, String description, Runnable action) {
        JPanel card = new JPanel(new BorderLayout(12, 8));
        card.setBackground(WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true),
                new EmptyBorder(15, 15, 15, 15)));
        JLabel icon = new JLabel(symbol, SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Symbol", Font.BOLD, 22));
        icon.setForeground(BLUE);
        icon.setOpaque(true);
        icon.setBackground(new Color(238, 243, 255));
        icon.setPreferredSize(new Dimension(45, 45));
        card.add(icon, BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setBackground(WHITE);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLabel.setForeground(TEXT);
        JLabel desc = new JLabel("<html><div style='width:220px'>" + description + "</div></html>");
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        desc.setForeground(MUTED);
        desc.setBorder(new EmptyBorder(5, 0, 0, 0));
        text.add(titleLabel);
        text.add(desc);
        card.add(text, BorderLayout.CENTER);

        JButton open = new JButton("Open  →");
        styleButton(open, false);
        open.addActionListener(e -> action.run());
        card.add(open, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildBooksPage() {
        JPanel page = pageShell();
        JPanel heading = new JPanel(new BorderLayout(10, 10));
        heading.setBackground(BG);
        JLabel title = sectionTitle("Book catalogue");
        JLabel subtitle = new JLabel("Search, review availability and manage the collection.");
        subtitle.setForeground(MUTED);
        subtitle.setFont(BODY);
        JPanel headingText = new JPanel();
        headingText.setBackground(BG);
        headingText.setLayout(new BoxLayout(headingText, BoxLayout.Y_AXIS));
        headingText.add(title);
        headingText.add(Box.createVerticalStrut(5));
        headingText.add(subtitle);

        JButton add = new JButton("+ Add book");
        styleButton(add, true);
        add.addActionListener(e -> addBookDialog());
        heading.add(headingText, BorderLayout.WEST);
        heading.add(add, BorderLayout.EAST);

        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setBackground(WHITE);
        searchPanel.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true),
                new EmptyBorder(10, 12, 10, 12)));
        bookSearch = new JTextField();
        bookSearch.setFont(BODY);
        bookSearch.setBorder(new EmptyBorder(7, 5, 7, 5));
        bookSearch.setToolTipText("Search by ISBN, title, author or topic");
        bookSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterBooks(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterBooks(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterBooks(); }
        });
        JLabel searchIcon = new JLabel("⌕  Search catalogue");
        searchIcon.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchIcon.setForeground(MUTED);
        searchPanel.add(searchIcon, BorderLayout.WEST);
        searchPanel.add(bookSearch, BorderLayout.CENTER);

        String[] columns = {"ISBN", "Book title", "Author", "Topic", "Available", "Total copies", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        bookTable = new JTable(tableModel);
        bookTable.setFont(BODY);
        bookTable.setRowHeight(36);
        bookTable.setSelectionBackground(new Color(230, 237, 255));
        bookTable.setSelectionForeground(TEXT);
        bookTable.setGridColor(new Color(238, 241, 246));
        bookTable.setShowVerticalLines(false);
        bookTable.setFillsViewportHeight(true);
        bookTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        bookTable.getTableHeader().setBackground(new Color(248, 250, 253));
        bookTable.getTableHeader().setForeground(MUTED);
        bookTable.setAutoCreateRowSorter(true);
        JScrollPane scroll = new JScrollPane(bookTable);
        scroll.setBorder(new LineBorder(BORDER, 1, true));
        scroll.getViewport().setBackground(WHITE);

        JPanel top = new JPanel(new BorderLayout(0, 14));
        top.setBackground(BG);
        top.add(heading, BorderLayout.NORTH);
        top.add(searchPanel, BorderLayout.SOUTH);
        page.add(top, BorderLayout.NORTH);
        page.add(scroll, BorderLayout.CENTER);
        return page;
    }

    private JPanel buildWaitingPage() {
        JPanel page = pageShell();
        page.add(sectionTitle("Waiting list"), BorderLayout.NORTH);
        waitingArea = new JTextArea();
        styleTextArea(waitingArea);
        page.add(new JScrollPane(waitingArea), BorderLayout.CENTER);
        return page;
    }

    private JPanel buildReturnedPage() {
        JPanel page = pageShell();
        page.add(sectionTitle("Recently returned"), BorderLayout.NORTH);
        returnedArea = new JTextArea();
        styleTextArea(returnedArea);
        page.add(new JScrollPane(returnedArea), BorderLayout.CENTER);
        return page;
    }

    private JPanel buildTopicPage() {
        JPanel page = pageShell();
        JPanel top = new JPanel(new BorderLayout(0, 14));
        top.setBackground(BG);
        top.add(sectionTitle("Search by topic"), BorderLayout.NORTH);
        JPanel search = new JPanel(new BorderLayout(10, 0));
        search.setBackground(BG);
        topicSearch = new JTextField();
        topicSearch.setFont(BODY);
        topicSearch.setToolTipText("Enter a topic, e.g. Programming");
        JButton find = new JButton("Search topic");
        styleButton(find, true);
        find.addActionListener(e -> searchTopicAction());
        topicSearch.addActionListener(e -> searchTopicAction());
        search.add(topicSearch, BorderLayout.CENTER);
        search.add(find, BorderLayout.EAST);
        top.add(search, BorderLayout.SOUTH);
        topicResults = new JTextArea();
        styleTextArea(topicResults);
        page.add(top, BorderLayout.NORTH);
        page.add(new JScrollPane(topicResults), BorderLayout.CENTER);
        return page;
    }

    private JLabel sectionTitle(String title) {
        JLabel label = new JLabel(title);
        label.setFont(HEADING);
        label.setForeground(NAVY);
        return label;
    }

    private void styleTextArea(JTextArea area) {
        area.setEditable(false);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        area.setForeground(TEXT);
        area.setBackground(WHITE);
        area.setMargin(new Insets(18, 18, 18, 18));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(new LineBorder(BORDER, 1, true));
    }

    private void styleButton(JButton button, boolean primary) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(10, 15, 10, 15));
        if (primary) {
            button.setBackground(BLUE);
            button.setForeground(WHITE);
        } else {
            button.setBackground(new Color(240, 244, 251));
            button.setForeground(TEXT);
        }
    }

    private void addBookDialog() {
        JTextField isbn = new JTextField();
        JTextField title = new JTextField();
        JTextField author = new JTextField();
        JTextField topic = new JTextField();
        JSpinner copies = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
        JPanel form = formPanel();
        addFormRow(form, 0, "ISBN / Book ID", isbn);
        addFormRow(form, 1, "Book title", title);
        addFormRow(form, 2, "Author", author);
        addFormRow(form, 3, "Topic / category", topic);
        addFormRow(form, 4, "Number of copies", copies);

        int choice = JOptionPane.showConfirmDialog(this, form, "Add a book",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) return;

        String id = isbn.getText().trim();
        String bookTitle = title.getText().trim();
        String bookAuthor = author.getText().trim();
        String bookTopic = topic.getText().trim();
        int count = (Integer) copies.getValue();

        if (id.isEmpty() || bookTitle.isEmpty() || bookAuthor.isEmpty() || bookTopic.isEmpty()) {
            showMessage("Please complete every field before saving.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Book existing = books.get(id);
        if (existing != null) {
            int addCount = JOptionPane.showConfirmDialog(this,
                    "This ISBN already exists. Add " + count + " copies to “" + existing.title + "”?",
                    "Existing book", JOptionPane.YES_NO_OPTION);
            if (addCount == JOptionPane.YES_OPTION) {
                existing.availableCopies += count;
                existing.totalCopies += count;
                refreshAll();
                showMessage("Copies added successfully.", JOptionPane.INFORMATION_MESSAGE);
            }
            return;
        }

        Book book = new Book(id, bookTitle, bookAuthor, bookTopic, count);
        books.put(id, book);
        bookList.add(book);
        topicRoot = insertTopic(topicRoot, book);
        refreshAll();
        showMessage("Book added to the catalogue.", JOptionPane.INFORMATION_MESSAGE);
    }

    private void issueBookDialog() {
        if (books.isEmpty()) {
            showMessage("Add a book to the catalogue before issuing one.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JTextField studentId = new JTextField();
        JTextField studentName = new JTextField();
        JComboBox<Book> bookChoice = new JComboBox<>();
        for (Book book : bookList) bookChoice.addItem(book);
        bookChoice.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, focus);
                if (value instanceof Book) {
                    Book b = (Book) value;
                    setText(b.title + "  |  " + b.isbn + "  |  available: " + b.availableCopies);
                }
                return this;
            }
        });

        JPanel form = formPanel();
        addFormRow(form, 0, "Student ID", studentId);
        addFormRow(form, 1, "Student name", studentName);
        addFormRow(form, 2, "Select book", bookChoice);

        int choice = JOptionPane.showConfirmDialog(this, form, "Issue a book",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) return;

        String id = studentId.getText().trim();
        String name = studentName.getText().trim();
        Book book = (Book) bookChoice.getSelectedItem();
        if (id.isEmpty() || name.isEmpty() || book == null) {
            showMessage("Enter the student's ID and name, and select a book.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (issuedBooks.containsKey(id)) {
            showMessage("This student already has a book on loan.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (isWaiting(id)) {
            showMessage("This student is already on a waiting list.", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Student student = new Student(id, name, book.isbn);
        if (book.availableCopies > 0) {
            book.availableCopies--;
            issuedBooks.put(id, student);
            refreshAll();
            showMessage("“" + book.title + "” issued to " + name + ".", JOptionPane.INFORMATION_MESSAGE);
        } else {
            int answer = JOptionPane.showConfirmDialog(this,
                    "No copies are currently available. Add " + name + " to the waiting list?",
                    "No copies available", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (answer == JOptionPane.YES_OPTION) {
                waitingLists.computeIfAbsent(book.isbn, k -> new LinkedList<>()).offer(student);
                refreshAll();
                showMessage("Student added to the waiting list.", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void returnBookDialog() {
        if (issuedBooks.isEmpty()) {
            showMessage("There are no books currently on loan.", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JComboBox<Student> studentChoice = new JComboBox<>();
        for (Student student : issuedBooks.values()) studentChoice.addItem(student);
        studentChoice.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, focus);
                if (value instanceof Student) {
                    Student s = (Student) value;
                    Book b = books.get(s.isbn);
                    setText(s.name + "  |  ID: " + s.id + "  |  " + (b == null ? s.isbn : b.title));
                }
                return this;
            }
        });
        JPanel form = formPanel();
        addFormRow(form, 0, "Select loan to return", studentChoice);
        int choice = JOptionPane.showConfirmDialog(this, form, "Return a book",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) return;
        Student student = (Student) studentChoice.getSelectedItem();
        if (student == null) return;

        Book book = books.get(student.isbn);
        issuedBooks.remove(student.id);
        recentlyReturned.push((book == null ? student.isbn : book.title)
                + " (ISBN: " + student.isbn + ") returned by " + student.name + " [" + student.id + "]");

        Queue<Student> queue = waitingLists.get(student.isbn);
        if (queue != null && !queue.isEmpty()) {
            Student next = queue.poll();
            issuedBooks.put(next.id, next);
            if (queue.isEmpty()) waitingLists.remove(student.isbn);
            refreshAll();
            showMessage("Return recorded. The book was automatically issued to the next reader in the queue:\n"
                    + next.name + " (" + next.id + ").", JOptionPane.INFORMATION_MESSAGE);
        } else {
            if (book != null) book.availableCopies++;
            refreshAll();
            showMessage("Book return recorded successfully.", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private JPanel formPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(WHITE);
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));
        panel.setPreferredSize(new Dimension(470, 260));
        return panel;
    }

    private void addFormRow(JPanel panel, int row, String label, JComponent input) {
        GridBagConstraints left = new GridBagConstraints();
        left.gridx = 0; left.gridy = row; left.weightx = 0;
        left.insets = new Insets(8, 6, 8, 14);
        left.anchor = GridBagConstraints.WEST;
        JLabel text = new JLabel(label);
        text.setFont(new Font("Segoe UI", Font.BOLD, 12));
        text.setForeground(TEXT);
        panel.add(text, left);

        GridBagConstraints right = new GridBagConstraints();
        right.gridx = 1; right.gridy = row; right.weightx = 1;
        right.fill = GridBagConstraints.HORIZONTAL;
        right.insets = new Insets(8, 6, 8, 6);
        input.setFont(BODY);
        input.setPreferredSize(new Dimension(260, 34));
        panel.add(input, right);
    }

    private boolean isWaiting(String studentId) {
        for (Queue<Student> queue : waitingLists.values()) {
            for (Student student : queue) if (student.id.equals(studentId)) return true;
        }
        return false;
    }

    private TopicNode insertTopic(TopicNode node, Book book) {
        if (node == null) {
            node = new TopicNode(book.topic);
            node.books.add(book);
            return node;
        }
        int compare = book.topic.compareToIgnoreCase(node.topic);
        if (compare < 0) node.left = insertTopic(node.left, book);
        else if (compare > 0) node.right = insertTopic(node.right, book);
        else node.books.add(book);
        return node;
    }

    private TopicNode findTopic(TopicNode node, String topic) {
        while (node != null) {
            int compare = topic.compareToIgnoreCase(node.topic);
            if (compare == 0) return node;
            node = compare < 0 ? node.left : node.right;
        }
        return null;
    }

    private void searchTopicAction() {
        String query = topicSearch.getText().trim();
        if (query.isEmpty()) {
            showMessage("Enter a topic, such as Programming or Database.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        TopicNode node = findTopic(topicRoot, query);
        if (node == null) {
            topicResults.setText("No books were found under the topic “" + query + "”.\n\n"
                    + "Tip: topic search matches the category name. Use the Book Catalogue search to find words in a title or author.");
            return;
        }
        StringBuilder result = new StringBuilder("TOPIC: " + node.topic + "\n");
        result.append("─".repeat(Math.max(12, node.topic.length() + 7))).append("\n\n");
        for (Book book : node.books) {
            result.append(book.title).append("\n")
                    .append("Author: ").append(book.author).append("\n")
                    .append("ISBN: ").append(book.isbn).append("\n")
                    .append("Available copies: ").append(book.availableCopies)
                    .append(" of ").append(book.totalCopies).append("\n\n");
        }
        topicResults.setText(result.toString());
        topicResults.setCaretPosition(0);
    }

    private void refreshAll() {
        totalValue.setText(String.valueOf(bookList.size()));
        int available = 0, waiting = 0;
        for (Book book : bookList) available += book.availableCopies;
        for (Queue<Student> queue : waitingLists.values()) waiting += queue.size();
        availableValue.setText(String.valueOf(available));
        issuedValue.setText(String.valueOf(issuedBooks.size()));
        waitingValue.setText(String.valueOf(waiting));
        refreshBookTable();
        refreshWaitingList();
        refreshReturnedList();
        if (topicResults != null && topicSearch != null && !topicSearch.getText().trim().isEmpty()) {
            searchTopicAction();
        }
    }

    private void refreshBookTable() {
        if (tableModel == null) return;
        tableModel.setRowCount(0);
        for (Book book : bookList) {
            String status = book.availableCopies > 0 ? "Available" : "On loan / waiting";
            tableModel.addRow(new Object[] {
                    book.isbn, book.title, book.author, book.topic,
                    book.availableCopies, book.totalCopies, status
            });
        }
        filterBooks();
    }

    private void filterBooks() {
        if (bookTable == null || bookSearch == null) return;
        String query = bookSearch.getText().trim().toLowerCase(Locale.ROOT);
        TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) bookTable.getRowSorter();
        if (sorter != null) {
            sorter.setRowFilter(query.isEmpty() ? null : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(query)));
        }
    }

    private void refreshWaitingList() {
        if (waitingArea == null) return;
        if (waitingLists.isEmpty()) {
            waitingArea.setText("No readers are currently waiting.\n\nWhen all copies of a book are on loan, readers can be added to a first-in, first-out waiting list.");
            return;
        }
        StringBuilder text = new StringBuilder("READERS WAITING FOR BOOKS\n\n");
        for (Map.Entry<String, Queue<Student>> entry : waitingLists.entrySet()) {
            Book book = books.get(entry.getKey());
            text.append(book == null ? entry.getKey() : book.title)
                    .append("  |  ISBN: ").append(entry.getKey()).append("\n");
            int position = 1;
            for (Student student : entry.getValue()) {
                text.append("   ").append(position++).append(". ")
                        .append(student.name).append("  •  ID: ").append(student.id).append("\n");
            }
            text.append("\n");
        }
        waitingArea.setText(text.toString());
        waitingArea.setCaretPosition(0);
    }

    private void refreshReturnedList() {
        if (returnedArea == null) return;
        if (recentlyReturned.isEmpty()) {
            returnedArea.setText("No returns recorded in this session yet.");
            return;
        }
        StringBuilder text = new StringBuilder("LATEST RETURNS FIRST\n\n");
        for (int i = recentlyReturned.size() - 1; i >= 0; i--) {
            text.append("• ").append(recentlyReturned.get(i)).append("\n\n");
        }
        returnedArea.setText(text.toString());
        returnedArea.setCaretPosition(0);
    }

    private void showPage(String name) {
        currentPage = name;
        pageTitle.setText(name);
        if ("Add Book".equals(name)) {
            addBookDialog();
            return;
        }
        cardLayout.show(pages, name);
        refreshAll();
    }

    private void showMessage(String message, int type) {
        JOptionPane.showMessageDialog(this, message, "BookHub", type);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("OptionPane.messageFont", BODY);
            UIManager.put("OptionPane.buttonFont", BODY);
        } catch (Exception ignored) { }
        SwingUtilities.invokeLater(() -> new LibraryManagementSystem().setVisible(true));
    }
}
