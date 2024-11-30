import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javax.swing.text.*;
import java.awt.Color;
import javax.swing.border.LineBorder;

class ScoreEntry {
    private String playerName;
    private double time;
    private int errors;
    private boolean isSinglePlayer;

    ScoreEntry(String playerName, double time, int errors, boolean isSinglePlayer) {
        this.playerName = playerName;
        this.time = time;
        this.errors = errors;
        this.isSinglePlayer = isSinglePlayer;
    }

    public String getPlayerName() { return playerName; }
    public double getTime() { return time; }
    public int getErrors() { return errors; }
    public boolean isSinglePlayer() { return isSinglePlayer; }
}

class Scoreboard {
    private ArrayList<ScoreEntry> scores;
    private final String scoreFilePath = "scoreboard.txt";

    Scoreboard() {
        scores = new ArrayList<>();
        loadScores();
    }

    void addScore(String playerName, double time, int errors, boolean isSinglePlayer) {
        scores.add(new ScoreEntry(playerName, time, errors, isSinglePlayer));
        Collections.sort(scores, Comparator.comparingDouble(s -> s.getTime()));
        saveScores();
    }

    void displaySinglePlayerScores(JFrame frame, String currentPlayerName, double currentPlayerTime) {
        JDialog scoreboardDialog = new JDialog(frame, "Scoreboard", true);
        scoreboardDialog.setSize(400, 300);
        scoreboardDialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        scoreboardDialog.setLayout(new BorderLayout());
        scoreboardDialog.setResizable(true);

        JTextPane scoreArea = new JTextPane();
        scoreArea.setEditable(false);

        StyleContext context = new StyleContext();
        StyledDocument doc = new DefaultStyledDocument(context);
        Style regularStyle = context.addStyle("regular", null);
        Style highlightedStyle = context.addStyle("highlighted", null);
        StyleConstants.setForeground(highlightedStyle, Color.PINK);
        StyleConstants.setForeground(regularStyle, Color.BLACK);

        boolean isCurrentPlayerHighlighted = false;

        try {
            doc.insertString(doc.getLength(), "Player\tTime (s)\tErrors\n", regularStyle);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        for (ScoreEntry entry : scores) {
            if (entry.isSinglePlayer()) {
                boolean isCurrentEntry = entry.getPlayerName().equals(currentPlayerName)
                        && entry.getTime() == currentPlayerTime;

                try {
                    if (isCurrentEntry && !isCurrentPlayerHighlighted) {
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
                    int errors = Integer.parseInt(parts[2]);
                    boolean isSinglePlayer = Boolean.parseBoolean(parts[3]);
                    scores.add(new ScoreEntry(playerName, time, errors, isSinglePlayer));
                }
            }
        } catch (IOException e) {
            System.out.println("Score file not found, starting fresh.");
        }
    }

    private void saveScores() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(scoreFilePath))) {
            for (ScoreEntry entry : scores) {
                writer.println(entry.getPlayerName() + "\t" + entry.getTime() + "\t" + entry.getErrors() + "\t" + entry.isSinglePlayer());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

public class MatchCards {
    private Scoreboard scoreboard;
    double currentPlayerTime = 0.0;
    private ImageIcon icon; // Declare the icon here

    class Card {
        private String cardName;
        private ImageIcon cardImageIcon;

        Card(String cardName, ImageIcon cardImageIcon) {
            this.cardName = cardName;
            this.cardImageIcon = cardImageIcon;
        }

        public String getCardName() { return cardName; }
        public ImageIcon getCardImageIcon() { return cardImageIcon; }
        public String toString() { return cardName; }
    }
    // Add this method in the MatchCards class
    private void playFanfareSound() {
        String fanfareSoundPath = "src/sounds/fanfare sound.wav"; // Update with your correct path
        try {
            File soundFile = new File(fanfareSoundPath);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    String[] cardList = {
            "darkness", "double", "fairy", "fighting", "fire",
            "grass", "lightning", "metal", "psychic", "water"
    };

    int rows = 4;
    int columns = 5;
    int cardWidth = 90;
    int cardHeight = 128;

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

    private boolean gameCompleted = false;
    private boolean isTimeExceeded = false;
    private double gameTimeLimit;

    private static final String BACKGROUND_IMAGE_PATH = "C:\\Users\\Ameen\\Downloads\\3386016.jpg";
    private static final String CLICK_SOUND_PATH = "src/sounds/click (1) sound.wav";
    private static final String APPLAUSE_SOUND_PATH = "src/sounds/applause sound.wav";

    public MatchCards() {
        // Load the icon once
        icon = new ImageIcon("C:\\Users\\Ameen\\Downloads\\3408506.png");
        scoreboard = new Scoreboard();

        showWelcomePage();
    }

    private static final String WELCOME_BACKGROUND_IMAGE_PATH = "C:\\Users\\Ameen\\Downloads\\2423da50-6daf-4428-853e-2065be5ba49f.png";

    private void showWelcomePage() {
        playFanfareSound(); // Play fanfare sound when the welcome page is displayed
        JFrame welcomeFrame = new JFrame("Welcome to Match Cards");
        welcomeFrame.setIconImage(icon.getImage()); // Set the icon image for the welcome JFrame
        welcomeFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        welcomeFrame.setSize(800, 600);
        welcomeFrame.setLocationRelativeTo(null);

        JPanel welcomePanel = createBackgroundPanel(WELCOME_BACKGROUND_IMAGE_PATH); // Use the new image path
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Welcome to Match Cards");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton singlePlayerButton = createStyledButton("Single Player", new Color(0, 123, 255));
        JButton twoPlayerButton = createStyledButton("Two Players", new Color(40, 167, 69));
        JButton scoreboardButton = createStyledButton("View Scoreboard", new Color(108, 117, 125));

        welcomePanel.add(Box.createVerticalGlue());
        welcomePanel.add(titleLabel);
        welcomePanel.add(Box.createRigidArea(new Dimension(0, 40)));
        welcomePanel.add(singlePlayerButton);
        welcomePanel.add(Box.createRigidArea(new Dimension(0, 20)));
        welcomePanel.add(twoPlayerButton);
        welcomePanel.add(Box.createRigidArea(new Dimension(0, 20)));
        welcomePanel.add(scoreboardButton);
        welcomePanel.add(Box.createVerticalGlue());

        welcomeFrame.add(welcomePanel);
        welcomeFrame.setVisible(true);

        singlePlayerButton.addActionListener(e -> {
            welcomeFrame.dispose();
            showSinglePlayerScreen();
        });

        twoPlayerButton.addActionListener(e -> {
            welcomeFrame.dispose();
            showTwoPlayerScreen();
        });

        scoreboardButton.addActionListener(e ->
                scoreboard.displaySinglePlayerScores(welcomeFrame, player1Name, currentPlayerTime));
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(250, 50));
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new LineBorder(Color.WHITE, 2));
        button.setOpaque(true);
        return button;
    }

    private JPanel createBackgroundPanel(String imagePath) {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageIcon backgroundImage = new ImageIcon(imagePath);
                g.drawImage(backgroundImage.getImage(), 0, 0, getWidth(), getHeight(), this);
            }
        };
    }

    private void showSinglePlayerScreen() {
        JFrame singlePlayerFrame = new JFrame("Single Player - Enter Name");
        singlePlayerFrame.setIconImage(icon.getImage()); // Set the icon image for the single player JFrame
        singlePlayerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        singlePlayerFrame.setSize(600, 400);
        singlePlayerFrame.setLocationRelativeTo(null);

        JPanel panel = createBackgroundPanel(BACKGROUND_IMAGE_PATH);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Enter Your Name");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField nameField = new JTextField(20);
        nameField.setMaximumSize(new Dimension(300, 40));
        nameField.setFont(new Font("Arial", Font.PLAIN, 18));
        nameField.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton startButton = createStyledButton("Start Game", new Color(0, 123, 255));
        startButton.setEnabled(false);

        panel.add(Box.createVerticalGlue());
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));
        panel.add(nameField);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(startButton);
        panel.add(Box.createVerticalGlue());

        singlePlayerFrame.add(panel);
        singlePlayerFrame.setVisible(true);

        nameField.getDocument().addDocumentListener((SimpleDocumentListener) () ->
                startButton.setEnabled(!nameField.getText().trim().isEmpty())
        );

        startButton.addActionListener(e -> {
            player1Name = nameField.getText();
            isSinglePlayer = true;
            singlePlayerFrame.dispose();
            showDifficultyScreen();
        });
    }

    private void showTwoPlayerScreen() {
        JFrame twoPlayerFrame = new JFrame("Two Players - Enter Names");
        twoPlayerFrame.setIconImage(icon.getImage()); // Set the icon image for the two player JFrame
        twoPlayerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        twoPlayerFrame.setSize(600, 500);
        twoPlayerFrame.setLocationRelativeTo(null);

        JPanel panel = createBackgroundPanel(BACKGROUND_IMAGE_PATH);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Enter Player Names");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField player1Field = createStyledTextField("Player 1 Name");
        JTextField player2Field = createStyledTextField("Player 2 Name");

        JButton startButton = createStyledButton("Start Game", new Color(0, 123, 255));
        startButton.setEnabled(false);

        String[] difficulties = {"Easy", "Medium", "Hard"};
        JComboBox<String> difficultyComboBox = new JComboBox<>(difficulties);
        difficultyComboBox.setMaximumSize(new Dimension(300, 40));
        difficultyComboBox.setFont(new Font("Arial", Font.PLAIN, 18));
        difficultyComboBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(Box.createVerticalGlue());
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));
        panel.add(player1Field);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(player2Field);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(difficultyComboBox);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(startButton);
        panel.add(Box.createVerticalGlue());

        twoPlayerFrame.add(panel);
        twoPlayerFrame.setVisible(true);

        SimpleDocumentListener nameListener = () ->
                startButton.setEnabled(!player1Field.getText().trim().isEmpty() &&
                        !player2Field.getText().trim().isEmpty());
        player1Field.getDocument().addDocumentListener(nameListener);
        player2Field.getDocument().addDocumentListener(nameListener);

        startButton.addActionListener(e -> {
            player1Name = player1Field.getText();
            player2Name = player2Field.getText();
            isSinglePlayer = false;
            twoPlayerFrame.dispose();

            String selectedDifficulty = (String) difficultyComboBox.getSelectedItem();
            startGame(selectedDifficulty);
        });
    }

    private JTextField createStyledTextField(String placeholder) {
        JTextField field = new JTextField(20);
        field.setMaximumSize(new Dimension(300, 40));
        field.setFont(new Font("Arial", Font.PLAIN, 18));
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
        field.setText(placeholder);
        field.setForeground(Color.GRAY);
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setForeground(Color.GRAY);
                    field.setText(placeholder);
                }
            }
        });
        return field;
    }

    private void showDifficultyScreen() {
        JFrame difficultyFrame = new JFrame("Select Difficulty");
        difficultyFrame.setIconImage(icon.getImage()); // Set the icon image for the two player JFrame

        difficultyFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        difficultyFrame.setSize(600, 400);
        difficultyFrame.setLocationRelativeTo(null);

        JPanel difficultyPanel = createBackgroundPanel(BACKGROUND_IMAGE_PATH);
        difficultyPanel.setLayout(new BoxLayout(difficultyPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Select Difficulty");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton easyButton = createStyledButton("Easy", new Color(40, 167, 69));
        JButton mediumButton = createStyledButton("Medium", new Color(255, 193, 7));
        JButton hardButton = createStyledButton("Hard", new Color(220, 53, 69));

        difficultyPanel.add(Box.createVerticalGlue());
        difficultyPanel.add(titleLabel);
        difficultyPanel.add(Box.createRigidArea(new Dimension(0, 40)));
        difficultyPanel.add(easyButton);
        difficultyPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        difficultyPanel.add(mediumButton);
        difficultyPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        difficultyPanel.add(hardButton);
        difficultyPanel.add(Box.createVerticalGlue());

        difficultyFrame.add(difficultyPanel);
        difficultyFrame.setVisible(true);

        easyButton.addActionListener(e -> {
            difficultyFrame.dispose();
            startGame("Easy");
        });

        mediumButton.addActionListener(e -> {
            difficultyFrame.dispose();
            startGame("Medium");
        });

        hardButton.addActionListener(e -> {
            difficultyFrame.dispose();
            startGame("Hard");
        });
    }
    private String difficultyLevel; // Define globally
    private void startGame(String difficulty) {
        difficultyLevel = difficulty; // Assign the passed value
        switch (difficulty) {
            case "Easy":
                rows = 3;
                columns = 3;
                gameTimeLimit = Double.MAX_VALUE;
                break;
            case "Medium":
                rows = 4;
                columns = 4;
                gameTimeLimit = 50.0;
                break;
            case "Hard":
                rows = 4;
                columns = 5;
                gameTimeLimit = 40.0;
                break;
        }

        String message = "You have to complete the game in " + (gameTimeLimit == Double.MAX_VALUE ? "unlimited" : gameTimeLimit) + " seconds to win.";
        JOptionPane.showMessageDialog(null, message);
        playFanfareSound();
        setupCards();
        shuffleCards();

        // Create the JFrame for the game first
        frame = new JFrame("Pokemon Match Cards - Player: " + player1Name);
        frame.setIconImage (icon.getImage()); // Set the icon image for the game JFrame
        frame.setLayout(new BorderLayout());
        frame.setSize(boardWidth + 200, boardHeight + 100);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = createBackgroundPanel(BACKGROUND_IMAGE_PATH);
        mainPanel.setLayout(new BorderLayout());

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
        mainPanel.add(textPanel, BorderLayout.NORTH);

        timerLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        timerLabel.setHorizontalAlignment(JLabel.CENTER);

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
        mainPanel.add(boardPanel, BorderLayout.CENTER);

        restartButton.setFont(new Font("Arial", Font.PLAIN, 16));
        restartButton.setPreferredSize(new Dimension(boardWidth, 30));
        restartButton.setFocusable(false);
        restartButton.setEnabled(false);
        restartButton.addActionListener(e -> restartGame());

        restartGamePanel.add(restartButton);
        mainPanel.add(restartGamePanel, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.pack();
        frame.setVisible(true);

        hideCardTimer = new Timer(2000, e -> hideCards());
        hideCardTimer.setRepeats(false);
        hideCardTimer.start();

        gameTimer = new Timer(10, e -> updateTime());
        gameTimer.start();
    }

    private void updateTime() {
        elapsedTime += 0.01;
        currentPlayerTime = elapsedTime;

        timerLabel.setText("Time: " + String.format("%.2f", elapsedTime) + " seconds");

        if (elapsedTime >= gameTimeLimit) {
            isTimeExceeded = true;
            gameTimer.stop();
            endGame(false);
        }
    }

    private void endGame(boolean completed) {
        gameCompleted = completed;
        gameTimer.stop(); // Stop the game timer
        String message;

        // Check if the game is completed
        if (completed) {
            String formattedTime = String.format("%.2f", elapsedTime);

            // Different behavior based on difficulty level
            if (difficultyLevel.equals("Easy") || (!isTimeExceeded && difficultyLevel.equals("Medium") || difficultyLevel.equals("Hard"))) {
                // For Easy mode or completed within time limit for Medium/Hard
                message = "Congratulations! You completed the game in " + formattedTime + " seconds.";

                // Add score to the scoreboard for single player
                if (isSinglePlayer) {
                    scoreboard.addScore(player1Name, elapsedTime, errorCount, true);
                    scoreboard.displaySinglePlayerScores(frame, player1Name, elapsedTime);
                    showScoreboard(player1Name, elapsedTime);
                }

                // Play applause sound for successful completion
                playSound(APPLAUSE_SOUND_PATH);
            } else {
                // Time exceeded for Medium/Hard levels
                message = "Game Over! You didn't complete the game in time.";
            }
        } else {
            // Game not completed
            message = "Game Over! You didn't complete the game.";
        }

        JOptionPane.showMessageDialog(frame, message);
        showGameResult();

        frame.dispose(); // Close the game window
    }


    private void showGameResult() {
        JFrame resultFrame = new JFrame("Game Result");
        resultFrame.setIconImage(icon.getImage()); // Set the icon image for the two player JFrame

        resultFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        resultFrame.setSize(800, 600);
        resultFrame.setLocationRelativeTo(null);

        JPanel resultPanel = createBackgroundPanel("C:\\Users\\Ameen\\Downloads\\success.png");
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));

        JLabel winnerLabel = new JLabel(getWinnerMessage());
        winnerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        winnerLabel.setForeground(Color.WHITE);
        winnerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel scoreLabel = new JLabel(getScoreMessage());
        scoreLabel.setFont(new Font("Arial", Font.PLAIN, 20 ));
        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton playAgainButton = createStyledButton("Play Again", new Color(0, 123, 255));
        JButton exitButton = createStyledButton("Exit", new Color(220, 53, 69));

        resultPanel.add(Box.createVerticalGlue());
        resultPanel.add(winnerLabel);
        resultPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        resultPanel.add(scoreLabel);
        resultPanel.add(Box.createRigidArea(new Dimension(0, 40)));
        resultPanel.add(playAgainButton);
        resultPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        resultPanel.add(exitButton);
        resultPanel.add(Box.createVerticalGlue());

        resultFrame.add(resultPanel);
        resultFrame.setVisible(true);

        playAgainButton.addActionListener(e -> {
            resultFrame.dispose();
            frame.dispose();
            restartGame();
        });

        exitButton.addActionListener(e -> {
            resultFrame.dispose();
            frame.dispose();
            System.exit(0);
        });


    }

    private String getWinnerMessage() {
        if (isSinglePlayer) {
            return gameCompleted ? player1Name + " would you like to play again or exit!" : "Game Over!";
        } else {
            if (player1Score > player2Score) {
                return player1Name + " Wins!";
            } else if (player2Score > player1Score) {
                return player2Name + " Wins!";
            } else {
                return "It's a Tie!";
            }
        }
    }

    private String getScoreMessage() {
        if (isSinglePlayer) {
            return "Time: " + String.format("%.2f", elapsedTime) + " seconds, Errors: " + errorCount;
        } else {
            return player1Name + ": " + player1Score + " | " + player2Name + ": " + player2Score;
        }
    }

    private void showScoreboard(String playerName, double playerTime) {
        if (gameCompleted) {
            scoreboard.addScore(playerName, playerTime, errorCount, true);
        }
    }

    private void setupCards() {
        cardSet = new ArrayList<>();
        int totalCards = rows * columns;
        for (int i = 0; i < totalCards / 2; i++) {
            String cardName = cardList[i % cardList.length];
            Image cardImg = new ImageIcon(getClass().getResource("./img/" + cardName + ".jpg")).getImage();
            ImageIcon cardImageIcon = new ImageIcon(cardImg.getScaledInstance(cardWidth, cardHeight, Image.SCALE_SMOOTH));
            cardSet.add(new Card(cardName, cardImageIcon));
            cardSet.add(new Card(cardName, cardImageIcon));
        }

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

    private void checkGameCompletion() {
        boolean allMatched = board.stream().allMatch(button -> button.getIcon() != cardBackImageIcon);

        if (allMatched) {
            gameTimer.stop();
            endGame(true);
        }
    }

    private void restartGame() {
        // Reset game state
        errorCount = 0;
        player1Score = 0;
        player2Score = 0;
        elapsedTime = 0.0;
        textLabel.setText("Errors: " + errorCount);
        timerLabel.setText("Time: 0.0 seconds");

        // Clear the board
        boardPanel.removeAll();
        board.clear();

        // Setup and shuffle cards
        setupCards();
        shuffleCards();

        // Re-add buttons to the board
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

        // Reset the game state
        card1Selected = null;
        card2Selected = null;
        gameReady = false;
        restartButton.setEnabled(false);

        // Revalidate and repaint the board
        boardPanel.revalidate();
        boardPanel.repaint();

        // Restart timers
        hideCardTimer.start();
        elapsedTime = 0.0; // Reset elapsed time
        gameTimer.restart(); // Restart the game timer
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

            // Play click sound
            playSound(CLICK_SOUND_PATH);

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
                }
            }
        }
    }

    private void playSound(String soundFilePath) {
        try {
            File soundFile = new File(soundFilePath);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
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
        default void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
    }

}