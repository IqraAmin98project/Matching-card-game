import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

class ScoreEntry {
    private String playerName;
    private double time;
    private int errors;
    private boolean isSinglePlayer;
    private String difficulty;

    ScoreEntry(String playerName, double time, int errors, boolean isSinglePlayer, String difficulty) {
        this.playerName = playerName;
        this.time = time;
        this.errors = errors;
        this.isSinglePlayer = isSinglePlayer;
        this.difficulty = difficulty;
    }

    public String getPlayerName() { return playerName; }
    public double getTime() { return time; }
    public int getErrors() { return errors; }
    public boolean isSinglePlayer() { return isSinglePlayer; }
    public String getDifficulty() { return difficulty; }
}

class Scoreboard {
    private ArrayList<ScoreEntry> scores;
    private final String scoreFilePath = "scoreboard.txt";

    Scoreboard() {
        scores = new ArrayList<>();
        loadScores();
    }

    private boolean areDoublesEqual(double a, double b) {
        return Math.abs(a - b) < 0.01; // Adjust the precision as necessary
    }

    void addScore(String playerName, double time, int errors, boolean isSinglePlayer, String difficulty) {
        scores.add(new ScoreEntry(playerName, time, errors, isSinglePlayer, difficulty));
        Collections.sort(scores, Comparator.comparingDouble(ScoreEntry::getTime)
                .thenComparingInt(ScoreEntry::getErrors));
        saveScores();
    }

    void displayScores(JFrame frame, String currentPlayerName, double currentPlayerTime) {
        JDialog scoreboardDialog = new JDialog(frame, "SCOREBOARD", true);
        scoreboardDialog.setSize(800, 600);
        scoreboardDialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        scoreboardDialog.setLocationRelativeTo(frame);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(230, 230, 250)); // Light purple background

        // Create a custom model to prevent editing
        DefaultTableModel model = new DefaultTableModel(new Object[]{"Player Name", "Time (s)", "Errors", "Difficulty"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
        };

        for (ScoreEntry entry : scores) {
            model.addRow(new Object[]{
                    entry.getPlayerName(),
                    String.format("%.2f", entry.getTime()),
                    entry.getErrors(),
                    entry.getDifficulty()
            });
        }

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setBackground(new Color(230, 230, 250));
        table.setForeground(Color.BLACK);
        table.setSelectionBackground(new Color(180, 180, 250));
        table.setSelectionForeground(Color.BLACK);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(180, 180, 250));
        table.getTableHeader().setForeground(Color.BLACK);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Custom renderer to highlight the current player
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String playerName = table.getValueAt(row, 0).toString();
                double playerTime = Double.parseDouble(table.getValueAt(row, 1).toString());

                if (playerName.equals(currentPlayerName) && areDoublesEqual(playerTime, currentPlayerTime)) {
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                    c.setForeground(new Color(0, 100, 0)); // Dark green for the current player
                } else {
                    c.setFont(c.getFont().deriveFont(Font.PLAIN));
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JLabel titleLabel = new JLabel("SCOREBOARD");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(75, 0, 130)); // Indigo color
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JButton backButton = new JButton("Back");
        backButton.setFont(new Font("Arial", Font.BOLD, 16));
        backButton.setForeground(Color.WHITE);
        backButton.setBackground(new Color(75, 0, 130)); // Indigo color
        backButton.addActionListener(e -> scoreboardDialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(new Color(230, 230, 250));
        buttonPanel.add(backButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        scoreboardDialog.add(mainPanel);
        scoreboardDialog.setVisible(true);
    }
    private void loadScores() {
        try (BufferedReader br = new BufferedReader(new FileReader(scoreFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length == 5) {
                    String playerName = parts[0];
                    double time = Double.parseDouble(parts[1]);
                    int errors = Integer.parseInt(parts[2]);
                    boolean isSinglePlayer = Boolean.parseBoolean(parts[3]);
                    String difficulty = parts[4];
                    scores.add(new ScoreEntry(playerName, time, errors, isSinglePlayer, difficulty));
                }
            }
        } catch (IOException e) {
            System.out.println("Score file not found, starting fresh.");
        }
    }

    private void saveScores() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(scoreFilePath))) {
            for (ScoreEntry entry : scores) {
                writer.println(entry.getPlayerName() + "\t" + entry.getTime() + "\t" +
                        entry.getErrors() + "\t" + entry.isSinglePlayer() + "\t" +
                        entry.getDifficulty());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

public class MatchCards {
    private Scoreboard scoreboard;
    private ImageIcon icon;
    private double currentPlayerTime = 0.0;
    private String currentPlayerName;
    private String difficultyLevel;



    class Card {
        private String cardName;
        private ImageIcon cardImageIcon;

        Card(String cardName, ImageIcon cardImageIcon) {
            this.cardName = cardName;
            this.cardImageIcon = cardImageIcon;
        }

        public String getCardName() {
            return cardName;
        }

        public ImageIcon getCardImageIcon() {
            return cardImageIcon;
        }

        public String toString() {
            return cardName;
        }
    }

    private void playFanfareSound() {
        String fanfareSoundPath = "src/sounds/fanfare sound.wav";
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

    int rows = 3;
    int columns = 4;
    int cardWidth = 90;
    int cardHeight = 128;

    ArrayList<Card> cardSet;
    ImageIcon cardBackImageIcon;
    JFrame frame;
    JLabel textLabel = new JLabel();
    JLabel turnLabel = new JLabel("Turn: Player 1");
    JPanel boardPanel = new JPanel();
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

    private static final String BACKGROUND_IMAGE_PATH = "src/resources/image-6.png";
    private static final String CLICK_SOUND_PATH = "src/sounds/click (1) sound.wav";
    private static final String APPLAUSE_SOUND_PATH = "src/sounds/applause sound.wav";

    public MatchCards() {
        icon = new ImageIcon("src/resources/icon image.png");
        scoreboard = new Scoreboard();
        showWelcomePage();
    }

    private static final String WELCOME_BACKGROUND_IMAGE_PATH = "src/resources/background.jpg";

    private void showWelcomePage() {
        playFanfareSound();
        JFrame welcomeFrame = new JFrame("Welcome to Match Cards");
        welcomeFrame.setIconImage(icon.getImage());
        welcomeFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        welcomeFrame.setSize(800, 600);
        welcomeFrame.setLocationRelativeTo(null);

        JPanel welcomePanel = createBackgroundPanel(WELCOME_BACKGROUND_IMAGE_PATH);
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(" ");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setForeground(Color.BLACK);
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
                scoreboard.displayScores(welcomeFrame, currentPlayerName, currentPlayerTime));
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(250, 50));
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
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
        singlePlayerFrame.setIconImage(icon.getImage());
        singlePlayerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        singlePlayerFrame.setSize(600, 400);
        singlePlayerFrame.setLocationRelativeTo(null);

        JPanel panel = createBackgroundPanel(BACKGROUND_IMAGE_PATH);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Enter Your Name");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Color.BLACK);
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

        nameField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                update();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                update();
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                update();
            }

            public void update() {
                startButton.setEnabled(!nameField.getText().trim().isEmpty());
            }
        });

        startButton.addActionListener(e -> {
            player1Name = nameField.getText();
            isSinglePlayer = true;
            singlePlayerFrame.dispose();
            showDifficultyScreen();
        });
    }

    private void showTwoPlayerScreen() {
        JFrame twoPlayerFrame = new JFrame("Two Players - Enter Names");
        twoPlayerFrame.setIconImage(icon.getImage());
        twoPlayerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        twoPlayerFrame.setSize(600, 500);
        twoPlayerFrame.setLocationRelativeTo(null);

        JPanel panel = createBackgroundPanel(BACKGROUND_IMAGE_PATH);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Enter Player Names");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Color.BLACK);
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

        javax.swing.event.DocumentListener nameListener = new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                update();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                update();
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                update();
            }

            public void update() {
                startButton.setEnabled(!player1Field.getText().trim().isEmpty() &&
                        !player2Field.getText().trim().isEmpty());
            }
        };
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
        difficultyFrame.setIconImage(icon.getImage());
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

    private void startGame(String difficulty) {
        difficultyLevel = difficulty;
        switch (difficulty) {
            case "Easy":
                rows = 3;
                columns = 4;
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

        String message = "Complete the game in " + (gameTimeLimit == Double.MAX_VALUE ? "unlimited" : gameTimeLimit) + " seconds.";
        JOptionPane.showMessageDialog(frame, message, "Game Instructions", JOptionPane.INFORMATION_MESSAGE);
        playFanfareSound();
        setupCards();
        shuffleCards();

        frame = new JFrame("Pokemon Match Cards - Player: " + player1Name);
        frame.setIconImage(icon.getImage());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1024, 768);
        frame.setLocationRelativeTo(null);
        frame.setResizable(true);

        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageIcon backgroundImage = new ImageIcon(BACKGROUND_IMAGE_PATH);
                g.drawImage(backgroundImage.getImage(), 0, 0, getWidth(), getHeight(), this);
            }
        };
        mainPanel.setLayout(new BorderLayout());

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        boardPanel = new JPanel(new GridLayout(rows, columns, 10, 10));
        boardPanel.setOpaque(false);

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        infoPanel.setOpaque(false);

        textLabel.setFont(new Font("Arial", Font.BOLD, 20));
        textLabel.setForeground(Color.WHITE);
        textLabel.setText("Errors: " + errorCount);

        timerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        timerLabel.setForeground(Color.WHITE);

        if (!isSinglePlayer) {
            turnLabel.setFont(new Font("Arial", Font.BOLD, 20));
            turnLabel.setForeground(Color.WHITE);
            turnLabel.setText("Turn: " + player1Name);
            infoPanel.add(turnLabel);
        }

        infoPanel.add(textLabel);
        infoPanel.add(timerLabel);

        board = new ArrayList<>();
        for (int i = 0; i < cardSet.size(); i++) {
            JButton tile = new JButton();
            tile.setPreferredSize(new Dimension(cardWidth, cardHeight));
            tile.setIcon(cardSet.get(i).cardImageIcon);
            tile.setFocusable(false);
            tile.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            tile.addActionListener(new CardActionListener(tile, i));
            board.add(tile);
            boardPanel.add(tile);
        }

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        restartButton.setFont(new Font("Arial", Font.BOLD, 16));
        restartButton.setFocusable(false);
        restartButton.setEnabled(false);
        restartButton.addActionListener(e -> restartGame());
        buttonPanel.add(restartButton);

        centerWrapper.add(boardPanel);

        mainPanel.add(infoPanel, BorderLayout.NORTH);
        mainPanel.add(centerWrapper, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.setVisible(true);

        hideCardTimer = new Timer(1500, e -> hideCards());
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

    private void endGame(boolean successfulCompletion) {
        gameCompleted = successfulCompletion;
        gameTimer.stop();
        String message;

        if (successfulCompletion) {
            String formattedTime = String.format("%.2f", elapsedTime);

            currentPlayerName = player1Name;
            currentPlayerTime = elapsedTime;

            if (difficultyLevel.equals("Easy") || (!isTimeExceeded && (difficultyLevel.equals("Medium") || difficultyLevel.equals("Hard")))) {
                message = "Congratulations! You completed the game in " + formattedTime + " seconds.";

                if (isSinglePlayer) {
                    scoreboard.addScore(player1Name, elapsedTime, errorCount, true, difficultyLevel);
                    scoreboard.displayScores(frame, currentPlayerName, currentPlayerTime);
                }

                playSound(APPLAUSE_SOUND_PATH);
            } else {
                message = "Game Over! You didn't complete the game in time.";
                successfulCompletion = false;
            }
        } else {
            message = "Game Over! You didn't complete the game.";
        }

        showGameResult(successfulCompletion);
        frame.dispose();
    }

    private void showGameResult(boolean successfulCompletion) {
        JFrame resultFrame = new JFrame("Game Result");
        resultFrame.setIconImage(icon.getImage());
        resultFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        resultFrame.setSize(800, 600);
        resultFrame.setLocationRelativeTo(null);

        JPanel resultPanel;
        if (successfulCompletion) {
            resultPanel = createBackgroundPanel("src/resources/success.png");
        } else {
            resultPanel = createBackgroundPanel("src/resources/game_over.png");
        }
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));

        JLabel winnerLabel = new JLabel(getWinnerMessage());
        winnerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        winnerLabel.setForeground(Color.WHITE);
        winnerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel scoreLabel = new JLabel(getScoreMessage());
        scoreLabel.setFont(new Font("Arial", Font.PLAIN, 20));
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
            showWelcomePage();
            frame.dispose();
        });

        exitButton.addActionListener(e -> {
            resultFrame.dispose();
            frame.dispose();
            System.exit(0);
        });
    }

    private String getWinnerMessage() {
        if (isSinglePlayer) {
            return gameCompleted ? player1Name + ", would you like to play again or exit?" : "Game Over!";
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
                turnLabel.setForeground(currentPlayer == 1 ? Color.YELLOW : Color.CYAN);
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
        errorCount = 0;
        player1Score = 0;
        player2Score = 0;
        elapsedTime = 0.0;
        textLabel.setText("Errors: " + errorCount);
        timerLabel.setText("Time: 0.0 seconds");

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

        card1Selected = null;
        card2Selected = null;
        gameReady = false;
        restartButton.setEnabled(false);

        boardPanel.revalidate();
        boardPanel.repaint();

        hideCardTimer.start();
        elapsedTime = 0.0;
        gameTimer.restart();
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

}