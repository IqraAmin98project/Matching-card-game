import.java.awt.event.ActionEvent;
import.java.awt.event.ActionListener;
class ScoreEntry {
    
    
    String playerName;
    double time;
    int errors;
    boolean isSinglePlayer; // New attribute to tag single or multiplayer mode
    ScoreEntry(String playerName, double time, int errors, boolean isSinglePlayer) {
    this.playerName = playerName;
    this.time = time;
    this.errors = errors;
    this.isSinglePlayer = isSinglePlayer;
}

public String getPlayerName() {
    return playerName;
}

public void setPlayerName(String playerName) {
    this.playerName = playerName;
}

public double getTime() {
    return time;
}

public void setTime(double time) {
    this.time = time;
}

public int getErrors() {
    return errors;
}

public void setErrors(int errors) {
    this.errors = errors;
}

public boolean isSinglePlayer() {
    return isSinglePlayer;
}

public void setSinglePlayer(boolean isSinglePlayer) {
    this.isSinglePlayer = isSinglePlayer;
}
}
class Scoreboard {
    private ArrayList<ScoreEntry> scores;
    private final String scoreFilePath = "scoreboard.txt";

    // Constructor to initialize scores
    public Scoreboard() {
        scores = new ArrayList<>();
        loadScores();
    }
    // Add score method encapsulates score insertion and sorting
    public void addScore(String playerName, double time, int errors, boolean isSinglePlayer) {
        scores.add(new ScoreEntry(playerName, time, errors, isSinglePlayer));
        sortScores();
        saveScores();
    }
    // Sort scores in ascending order based on time
    private void sortScores() {
        Collections.sort(scores, Comparator.comparingDouble(ScoreEntry::getTime));
    }
    // Load scores from the file
    private void loadScores() {
        try (BufferedReader br = new BufferedReader(new FileReader(scoreFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length == 4) {
                    String playerName = parts[0];
                    double time = Double.parseDouble(parts[1]);
                    int errors = Integer.parseInt(parts[2]);
                    boolean isSinglePlayer = Boolean.parseBoolean(parts[3]);
                    scores.add(new ScoreEntry(playerName, time, errors, isSinglePlayer));
                }
            }
        } catch (IOException e) {
            System.out.println("Score file not found, starting fresh.");
        }
    }
    // Save scores to the file
    private void saveScores() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(scoreFilePath))) {
            for (ScoreEntry entry : scores) {
                writer.println(entry.getPlayerName() + "\t" + entry.getTime() + "\t" + entry.getErrors() + "\t" + entry.isSinglePlayer());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Display single-player scores
    public void displaySinglePlayerScores(JFrame frame, String currentPlayerName, double currentPlayerTime) {
        JDialog scoreboardDialog = new JDialog(frame, "Scoreboard", true);
        scoreboardDialog.setSize(400, 300);
        scoreboardDialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        scoreboardDialog.setLayout(new BorderLayout());
        scoreboardDialog.setResizable(true);

        JTextPane scoreArea = new JTextPane();
        scoreArea.setEditable(false);

        // Define custom styles for highlighting and regular text
        StyleContext context = new StyleContext();
        StyledDocument doc = new DefaultStyledDocument(context);
        Style regularStyle = context.addStyle("regular", null);
        Style highlightedStyle = context.addStyle("highlighted", null);
        StyleConstants.setForeground(highlightedStyle, Color.PINK);
        StyleConstants.setForeground(regularStyle, Color.BLACK);

        boolean isCurrentPlayerHighlighted = false;

        // Add header row
        try {
            doc.insertString(doc.getLength(), "Player\tTime (s)\tErrors\n", regularStyle);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        // Loop through scores and add to document
        for (ScoreEntry entry : scores) {
            if (entry.isSinglePlayer()) {
                boolean isCurrentEntry = entry.getPlayerName().equals(currentPlayerName)
                        && entry.getTime() == currentPlayerTime;

                try {
                    if (isCurrentEntry && !isCurrentPlayerHighlighted) {
                        // Highlight current player's latest score
                        doc.insertString(doc.getLength(), ">> " + entry.getPlayerName() + " <<\t", highlightedStyle);
                        isCurrentPlayerHighlighted = true;
                    } else {
                        doc.insertString(doc.getLength(), entry.getPlayerName() + "\t", regularStyle);
                    }
                    doc.insertString(doc.getLength(), String.format("%.2f", entry.getTime()) + "\t" + entry.getErrors() + "\n", regularStyle);
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        }

        scoreArea.setDocument(doc);
        scoreboardDialog.add(new JScrollPane(scoreArea), BorderLayout.CENTER);
        scoreboardDialog.setLocationRelativeTo(frame);
        scoreboardDialog.setVisible(true);
    }
private void loadScores() {
    try (BufferedReader br = new BufferedReader(new FileReader(scoreFilePath))) {
        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split("\t");
            if (parts.length == 4) {
                String playerName = parts[0];
                double time = Double.parseDouble(parts[1]);
                int errors = Integer.parseInt(parts[2]); // Parse errors
                boolean isSinglePlayer = Boolean.parseBoolean(parts[3]);
                scores.add(new ScoreEntry(playerName, time, errors, isSinglePlayer));

            }
        }
    } catch (IOException e) {
        // Handle the exception (e.g., file not found)
        System.out.println("Score file not found, starting fresh.");
    }
}
    private void saveScores() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(scoreFilePath))) {
            for (ScoreEntry entry : scores) {
                writer.println(entry.playerName + "\t" + entry.time + "\t" + entry.errors + "\t" + entry.isSinglePlayer);
            }
        } catch (IOException e) {
            // Handle the exception
            e.printStackTrace();
        }
    }
}
public class MatchCards {
    private Scoreboard scoreboard;
    double currentPlayerTime = 0.0; // Initialize time

    class Card {
        String cardName;
        ImageIcon cardImageIcon;

        Card(String cardName, ImageIcon cardImageIcon) {
            this.cardName = cardName;
            this.cardImageIcon = cardImageIcon;
        }

        public String toString() {
            return cardName;
        }
    }
    // Game configuration
    String[] cardList = {
            "darkness",
            "double",
            "fairy",
            "fighting",
            "fire",
            "grass",
            "lightning",
            "metal",
            "psychic",
            "water"};

    int rows = 4;
    int columns = 5;
    int cardWidth = 90;
    int cardHeight = 128;
    // GUI components
    ArrayList<Card> cardSet;
    ImageIcon cardBackImageIcon;
    int boardWidth = columns * cardWidth;
    int boardHeight = rows * cardHeight;
    JFrame frame;
    JLabel textLabel = new JLabel();
    JLabel turnLabel = new JLabel("Turn: Player 1");
    JPanel textPanel = new JPanel();
    JPanel boardPanel = new JPanel();
    JPanel restartGamePanel = new JPanel();
    JButton restartButton = new JButton("Restart Game");
    JLabel timerLabel = new JLabel("Time: 0.0 seconds");
    // Game state variables
    boolean isSinglePlayer;
    int errorCount = 0;
    int player1Score = 0;
    int player2Score = 0;
    int currentPlayer = 1;
    ArrayList<JButton> board;
    Timer hideCardTimer, gameTimer;
    boolean gameReady = false;
    JButton card1Selected, card2Selected;
    double elapsedTime = 0.0;
    String player1Name;
    String player2Name;
    public MatchCards() {
        scoreboard = new Scoreboard();

        showOptionsMenu();
    }

    setupCards();
    shuffleCards();

    frame = new JFrame("Pokemon Match Cards - Player: " + player1Name);
        frame.setLayout(new BorderLayout());
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    // Error text label and timer label
        textLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setText("Errors: " + errorCount);
         if (isSinglePlayer) {
        turnLabel.setVisible(false);
    } else {
        turnLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        turnLabel.setHorizontalAlignment(JLabel.CENTER);
        turnLabel.setText("Turn: " + player1Name);
    }

        textPanel.setPreferredSize(new Dimension(boardWidth, 30));
        textPanel.add(textLabel);
        textPanel.add(timerLabel);
        if (!isSinglePlayer) {
        textPanel.add(turnLabel);
    }
        frame.add(textPanel, BorderLayout.NORTH);

        timerLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        timerLabel.setHorizontalAlignment(JLabel.CENTER);
    // Card game board setup
    board = new ArrayList<>();
        boardPanel.setLayout(new GridLayout(rows, columns));
        for (int i = 0; i < cardSet.size(); i++) {
        JButton tile = new JButton();
        tile.setPreferredSize(new Dimension(cardWidth, cardHeight));
        tile.setOpaque(true);
        tile.setIcon(cardSet.get(i).cardImageIcon);
        tile.setFocusable(false);
        tile.addActionListener(new CardActionListener(tile, i));
        board.add(tile);
        boardPanel.add(tile);
    }
        frame.add(boardPanel);
    // Restart game button setup
        restartButton.setFont(new Font("Arial", Font.PLAIN, 16));
        restartButton.setPreferredSize(new Dimension(boardWidth, 30));
        restartButton.setFocusable(false);
        restartButton.setEnabled(false);
        restartButton.addActionListener(e -> restartGame());

        restartGamePanel.add(restartButton);
        frame.add(restartGamePanel, BorderLayout.SOUTH);

        frame.pack();
        frame.setVisible(true);

    hideCardTimer = new Timer(1000, e -> hideCards());
        hideCardTimer.setRepeats(false);
        hideCardTimer.start();

    gameTimer = new Timer(10, e -> updateTime());
        gameTimer.start();
}
private void setupCards() {
    cardSet = new ArrayList<>();
    for (String cardName : cardList) {
        Image cardImg = new ImageIcon(getClass().getResource("./img/" + cardName + ".jpg")).getImage();
        ImageIcon cardImageIcon = new ImageIcon(cardImg.getScaledInstance(cardWidth, cardHeight, Image.SCALE_SMOOTH));
        cardSet.add(new Card(cardName, cardImageIcon));
    }
    cardSet.addAll(cardSet);

    Image cardBackImg = new ImageIcon(getClass().getResource("./img/back.jpg")).getImage();
    cardBackImageIcon = new ImageIcon(cardBackImg.getScaledInstance(cardWidth, cardHeight, Image.SCALE_SMOOTH));
}

private void shuffleCards() {
    for (int i = 0; i < cardSet.size(); i++) {
        int j = (int) (Math.random() * cardSet.size());
        Card temp = cardSet.get(i);
        cardSet.set(i, cardSet.get(j));
        cardSet.set(j, temp);
    }
}
private void hideCards() {
    if (gameReady && card1Selected != null && card2Selected != null) {
        card1Selected.setIcon(cardBackImageIcon);
        card1Selected = null;
        card2Selected.setIcon(cardBackImageIcon);
        card2Selected = null;

        if (!isSinglePlayer) {
            currentPlayer = (currentPlayer == 1) ? 2 : 1;
            turnLabel.setText("Turn: " + (currentPlayer == 1 ? player1Name : player2Name));
        }
    } else {
        for (JButton tile : board) {
            tile.setIcon(cardBackImageIcon);
        }
        gameReady = true;
        restartButton.setEnabled(true);
    }
}
private void restartGame() {
    boardPanel.removeAll();
    board.clear();
    setupCards();
    shuffleCards();

    for (int i = 0; i < cardSet.size(); i++) {
        JButton tile = new JButton();
        tile.setPreferredSize(new Dimension(cardWidth, cardHeight));
        tile.setOpaque(true);
        tile.setIcon(cardSet.get(i).cardImageIcon);
        tile.setFocusable(false);
        tile.addActionListener(new CardActionListener(tile, i));
        board.add(tile);
        boardPanel.add(tile);
    }

    errorCount = 0;
    player1Score = 0;
    player2Score = 0;
    elapsedTime = 0.0;
    textLabel.setText("Errors: " + errorCount);
    timerLabel.setText("Time: 0.0 seconds");
    if (!isSinglePlayer) {
        currentPlayer = 1;
        turnLabel.setText("Turn: " + player1Name);
    }
    hideCardTimer.start();
    gameTimer.start();
    boardPanel.revalidate();
    boardPanel.repaint();
}
private class CardActionListener implements ActionListener {
    JButton tile;
    int index;

    public CardActionListener(JButton tile, int index) {
        this.tile = tile;
        this.index = index;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameReady || tile.getIcon() != cardBackImageIcon) return;

        if (card1Selected == null) {
            card1Selected = tile;
            card1Selected.setIcon(cardSet.get(index).cardImageIcon);
        } else if (card2Selected == null) {
            card2Selected = tile;
            card2Selected.setIcon(cardSet.get(index).cardImageIcon);

            if (card1Selected.getIcon() == card2Selected.getIcon()) {
                if (!isSinglePlayer) {
                    if (currentPlayer == 1) player1Score++;
                    else player2Score++;

                }
                card1Selected = null;
                card2Selected = null;
                checkGameCompletion();
            } else {

                errorCount++;
                textLabel.setText("Errors: " + errorCount);

                hideCardTimer.start();
                hideCardTimer.start();
            }
        }
    }
}
@FunctionalInterface
interface SimpleDocumentListener extends javax.swing.event.DocumentListener {
    void update();

    @Override
    default void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
    @Override
    default void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
    @Override
    default void changedUpdate(javax.swing.event.DocumentEvent e){update();}
}}
























