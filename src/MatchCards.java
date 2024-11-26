import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Comparator;
import javax.swing.text.*;
import java.awt.Color;


class ScoreEntry {
    private String playerName;
    private double time;
    private int errors;
    private boolean isSinglePlayer; // New attribute to tag single or multiplayer mode

    ScoreEntry(String playerName, double time, int errors,boolean isSinglePlayer) {
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

    public void setSinglePlayer(boolean singlePlayer) {
        isSinglePlayer = singlePlayer;
    }
}

class Scoreboard {
    private ArrayList<ScoreEntry> scores;
    private final String scoreFilePath = "scoreboard.txt"; // File to store scores

    Scoreboard() {
        scores = new ArrayList<>();
        loadScores(); // Load scores from file
    }

    void addScore(String playerName, double time, int errors,boolean isSinglePlayer) {
        scores.add(new ScoreEntry(playerName, time, errors, isSinglePlayer));
        Collections.sort(scores, Comparator.comparingDouble(s -> s.getTime()));
        saveScores(); // Save scores to file
    }

    void displaySinglePlayerScores(JFrame frame, String currentPlayerName, double currentPlayerTime) {
        JDialog scoreboardDialog = new JDialog(frame, "Scoreboard", true);
        scoreboardDialog.setSize(400, 300);
        scoreboardDialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        scoreboardDialog.setLayout(new BorderLayout());
        scoreboardDialog.setResizable(true);  // Allow resizing

        // Create a JTextPane with custom styles for highlighting
        JTextPane scoreArea = new JTextPane();
        scoreArea.setEditable(false);

        // Define custom styles for highlighting and regular text
        StyleContext context = new StyleContext();
        StyledDocument doc = new DefaultStyledDocument(context);
        Style regularStyle = context.addStyle("regular", null);
        Style highlightedStyle = context.addStyle("highlighted", null);
        StyleConstants.setForeground(highlightedStyle, Color.PINK); // Set highlighted player name color
        StyleConstants.setForeground(regularStyle, Color.BLACK); // Set regular text color

        // Flag to ensure only the current player's latest progress is highlighted once
        boolean isCurrentPlayerHighlighted = false;

// Add header row
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
                        // Highlight only the most recent score of the current player
                        doc.insertString(doc.getLength(), ">> " + entry.getPlayerName() + " <<\t", highlightedStyle);
                        isCurrentPlayerHighlighted = true;
                    } else {
                        // Regular style for other entries
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
                writer.println(entry.getPlayerName() + "\t" + entry.getTime() + "\t" + entry.getErrors() + "\t" + entry.isSinglePlayer());
            }
        } catch (IOException e) {
            // Handle the exception
            e.printStackTrace();
        }
    }
}

public class MatchCards {
    private Scoreboard scoreboard;
    double currentPlayerTime = 0.0; // initialization of time


    class Card {
        private String cardName;
        private ImageIcon cardImageIcon;

        public Card(String cardName, ImageIcon cardImageIcon) {
            this.cardName = cardName;
            this.cardImageIcon = cardImageIcon;
        }

        public void setCardName(String cardName) {
            this.cardName = cardName;
        }

        public void setCardImageIcon(ImageIcon cardImageIcon) {
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

    //Game configuration
    String[] cardList = {
            "darkness",
            "double",
            "fairy",
            "fighting",
            "fire",
            "grass",
            "lighting",
            "metal",
            "psychic",
            "water"
    };
    int rows = 4;
    int columns = 5;
    int cardWidth = 90;
    int cardHeight = 128;

    // GUI COMPONENT
    ArrayList<Card> cardSet;
    ImageIcon cardBackImageIcon;
    int boardWidth = columns * cardWidth;
    int boardHeight = rows + cardHeight;
    JFrame frame;
    JLabel textLabel = new JLabel();
    JLabel turnLabel = new JLabel("Turn: Player 1");
    JPanel textPanel = new JPanel();
    JPanel boardPanel = new JPanel();
    JPanel restartGamePanel = new JPanel();
    JButton restartButton = new JButton("Restart Game");
    JLabel timerLabel = new JLabel("Time 0.0 seconds");


    //Game start variable
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

    private void showOptionsMenu() {
        JFrame optionsFrame = new JFrame("Game Menu");
        optionsFrame.setResizable(false);
        optionsFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        optionsFrame.setSize(350, 200);
        optionsFrame.setLayout(new FlowLayout());

        //PLAYER OPTION BUTTON
        JButton singlePlayerButton = new JButton("Single Player");
        JButton twoPlayerButton = new JButton("Two Player");
        JButton scoreboardButton = new JButton("View Scoreboard"); // declaration and initialization of scoreboard

        // components to add in option frame
        optionsFrame.add(singlePlayerButton);
        optionsFrame.add(twoPlayerButton);
        optionsFrame.add(scoreboardButton);
        optionsFrame.setLocationRelativeTo(null);
        optionsFrame.setVisible(true);

        scoreboardButton.addActionListener(e ->
                scoreboard.displaySinglePlayerScores(optionsFrame, player1Name, currentPlayerTime));

        // single-player selection
        singlePlayerButton.addActionListener(e -> {
            optionsFrame.dispose();
            showSinglePlayerScreen();

        });
        // Two player selection
        twoPlayerButton.addActionListener(e -> {
            optionsFrame.dispose();
            showTwoPlayerScreen();

        });
    }

    private void showDifficultyScreen(){
        JFrame difficultyFrame = new JFrame("Select Difficulty");
        difficultyFrame.setResizable(false);
        difficultyFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        difficultyFrame.setSize(350,200);
        difficultyFrame.setLayout(new FlowLayout());

        // BUTTONS OF DIFFICULTY LEVEL
        JButton easyButton = new JButton("Easy");
        JButton mediumButton = new JButton("Medium");
        JButton hardButton = new JButton("Hard");

        // add components of difficulty frame
        difficultyFrame.add(easyButton);
        difficultyFrame.add(mediumButton);
        difficultyFrame.add(hardButton);
        difficultyFrame.setLocationRelativeTo(null);
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

    // Single-player screen with name input
    private void showSinglePlayerScreen() {
        JFrame singlePlayerFrame = new JFrame("Single Player - Enter Name");
        singlePlayerFrame.setResizable(false);
        singlePlayerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        singlePlayerFrame.setSize(300, 150);
        singlePlayerFrame.setLayout(new FlowLayout());

        JLabel nameLabel = new JLabel("Enter your name");
        JTextField nameField = new JTextField(15);
        JButton startButton = new JButton("Start Game");
        startButton.setEnabled(false);

        // Enable Start button only when name is entered
        nameField.getDocument().addDocumentListener((SimpleDocumentListener) () ->
                startButton.setEnabled(!nameField.getText().trim().isEmpty())
        );


        // start button action
        startButton.addActionListener(e -> {
            player1Name = nameField.getText();
            isSinglePlayer = true;
            singlePlayerFrame.dispose();
            showDifficultyScreen();
        });

        // add components to frame
        singlePlayerFrame.add(nameLabel);
        singlePlayerFrame.add(nameField);
        singlePlayerFrame.add(startButton);
        singlePlayerFrame.setLocationRelativeTo(null);
        singlePlayerFrame.setVisible(true);
    }

    // two player screen with name inputs
    private void showTwoPlayerScreen() {
        JFrame twoPlayerFrame = new JFrame("Two Player - Enter Name");
        twoPlayerFrame.setResizable(false);
        twoPlayerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        twoPlayerFrame.setSize(300, 150);
        twoPlayerFrame.setLayout(new FlowLayout());

        JLabel player1Label = new JLabel("Enter player 1 name");
        JTextField player1Field = new JTextField(15);
        JLabel player2Label = new JLabel("Enter player 2 name ");
        JTextField player2Field = new JTextField(15);
        JButton startButton = new JButton("Start Gmae");
        startButton.setEnabled(false);

        // Enable Start button only when both names are entered
        SimpleDocumentListener nameListener = () ->
                startButton.setEnabled(!player1Field.getText().trim().isEmpty() &&
                        !player2Field.getText().trim().isEmpty());
        player1Field.getDocument().addDocumentListener(nameListener);
        player2Field.getDocument().addDocumentListener(nameListener);

        // add difficulty selection option  (e.g.,dropdown and buttons)
        JComboBox<String> difficultyComboBox = new JComboBox<>(new String[]{"Easy", "Medium", "Hard"});
        difficultyComboBox.setSelectedIndex(1);

        // Start button action
        startButton.addActionListener(e -> {
            player1Name = player1Field.getText();
            player2Name = player2Field.getText();
            isSinglePlayer = false;
            twoPlayerFrame.dispose();

            // difficulty level selected start the game
            String selectedDifficulty = (String) difficultyComboBox.getSelectedItem();
            startGame(selectedDifficulty);
        });

        // Add components to frame
        twoPlayerFrame.add(player1Label);
        twoPlayerFrame.add(player1Field);
        twoPlayerFrame.add(player2Label);
        twoPlayerFrame.add(player2Field);
        twoPlayerFrame.add(difficultyComboBox);
        twoPlayerFrame.add(startButton);
        twoPlayerFrame.setLocationRelativeTo(null);
        twoPlayerFrame.setVisible(true);
    }

    private boolean gameCompleted = false;
    private boolean isTimeExceeded = false;

    private void startGame(String difficulty) {
        // difficulty level based on time
        switch (difficulty){
            case "Easy":
                gameTimeLimit = Double.MAX_VALUE; // NO TIME LIMIT FOR EASY ONE
                break;
            case "Medium":
                gameTimeLimit = 50.0; // 50 seconds
                break;
            case "Hard":
                gameTimeLimit = 40.0; // 40 seconds
                break;
        }

        // message that is shown on based on difficulty
        String message = " You have to complete the game in " + gameTimeLimit + " seconds to win.";
        JOptionPane.showMessageDialog(null,message);


        setupCards();
        shuffleCards();

        frame = new JFrame("Pokemon Match Cards - Player:" + player1Name + " " + player2Name);
        frame.setLayout(new BorderLayout());
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // ERROR TEXT LABEL AND TIMER
        textLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setText("Errors:" + errorCount);

        if (isSinglePlayer) {
            turnLabel.setVisible(false);
        } else {
            turnLabel.setFont(new Font("Arial", Font.PLAIN, 20));
            turnLabel.setHorizontalAlignment(JLabel.CENTER);
            turnLabel.setText("Turn:" + player1Name);
        }

        textPanel.setPreferredSize(new Dimension(boardWidth, 30));
        textPanel.add(timerLabel);
        textPanel.add(timerLabel);
        if (isSinglePlayer) {
            turnLabel.add(turnLabel);
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

        //restart button setup
        restartButton.setFont(new Font("Arial", Font.PLAIN, 16));
        restartButton.setPreferredSize(new Dimension(boardWidth, 30));
        restartButton.setFocusable(false);
        restartButton.setEnabled(false);
        restartButton.addActionListener(e -> restartGame());

        restartGamePanel.add(restartButton);
        frame.add(restartGamePanel, BorderLayout.SOUTH);

        frame.pack();
        frame.setVisible(true);

        hideCardTimer = new Timer(1500, e -> hideCards());
        hideCardTimer.setRepeats(false);
        hideCardTimer.start();

        gameTimer = new Timer(10, e -> updateTime());
        gameTimer.start();
    }

    private double gameTimeLimit;

    private void updateTime(){
        elapsedTime += 0.01;
        currentPlayerTime = elapsedTime;

        timerLabel.setText("Time: "+String.format("%.2f" , elapsedTime) + "seconds");

        // if the time exceeds the limited time, terminate the game
        if (elapsedTime >= gameTimeLimit){
            isTimeExceeded = true;  // time exceed,player failed to complete the game in time
            gameTimer.stop(); // game timer stop
            endGame(false); // time up game over
        }

    }

    private void endGame(boolean completed){
        gameCompleted = completed;
        gameTimer.stop();
        String message;
        if (completed && !isTimeExceeded) {
            String formattedTime = String.format("%.2f", elapsedTime);
            message = "Congratulations! You completed the game in " + formattedTime + " seconds.";
            if (isSinglePlayer) {
                scoreboard.addScore(player1Name, elapsedTime, errorCount, true);
                scoreboard.displaySinglePlayerScores(frame, player1Name, currentPlayerTime);
                showScoreboard(player1Name, elapsedTime); // the player whose scores are going to be shown on scoreboard
            }
        }  else {
            message = "Game Over! You didn't complete the game in time.";
            if (isSinglePlayer){
                if (completed){
                    scoreboard.addScore(player1Name, elapsedTime, errorCount, true); //scores are added to scoreboard
                    scoreboard.displaySinglePlayerScores(frame, player1Name, currentPlayerTime); // updated score are displayed now

                }else {}
            }
        }


        JOptionPane.showMessageDialog(frame, message);
        int response = JOptionPane.showOptionDialog(frame,
                "Would you like to play again?",
                "Game Over",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new Object[]{"Play Again", "Exit"},
                JOptionPane.YES_OPTION);

        if (response == 0) { // "Play Again"
            restartGame();
        } else { // "Exit"
            frame.dispose(); // Close the game window
        }
    }
    private void showScoreboard(String playerName, double playerTime) {
        // only add those score to scoreboard if the game was completed in time
        if (gameCompleted) {
            scoreboard.addScore(playerName, playerTime, errorCount, true);// true indicates game was completed successfully
        }

    }

    private void setupCards() {
        cardSet = new ArrayList<>();
        for (String cardName : cardList) {
            Image cardImg = new ImageIcon(getClass().getResource("/img/" + cardName + ".jpg")).getImage();
            System.out.println(getClass().getResource("/img/" + cardName + ".jpg"));

            ImageIcon cardImageIcon = new ImageIcon(cardImg.getScaledInstance(cardWidth, cardHeight, Image.SCALE_SMOOTH));
            cardSet.add(new Card(cardName, cardImageIcon));
        }
        cardSet.addAll(cardSet);

        Image cardBackImg = new ImageIcon(getClass().getResource("/img/back.jpg")).getImage();
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
            String formattedTime = String.format("%.2f", elapsedTime);

            if (isSinglePlayer) {
                scoreboard.addScore(player1Name, elapsedTime, errorCount, true);
                // this show the scoreboard after each completion of game
                scoreboard.displaySinglePlayerScores(frame, player1Name, currentPlayerTime);
                int response = JOptionPane.showOptionDialog(frame,
                        player1Name + "You finished the game in" + formattedTime + "seconds.\n" +
                                "Would you like to play again" + player1Name + "?",
                        "Game 0ver",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        new Object[]{"Play Again", "Exit"},
                        JOptionPane.YES_OPTION
                );

                if (response == 0) {  //"play again!"
                    restartGame();
                } else {//  "now this one for exit"
                    frame.dispose();
                }
            } else {
                String winner;
                if (player1Score > player2Score) winner = player1Name + "wins the game congratulation!";
                else if (player2Score > player1Score) winner = player2Name + "wins the game congratulation!";
                else winner = "It's a tie!";
                scoreboard.addScore(player1Name, elapsedTime, errorCount, false);
                scoreboard.addScore(player2Name, elapsedTime, errorCount, false);
                JOptionPane.showMessageDialog(frame, "Game over!\n" + player1Name + " Score: " + player1Score +
                        "\n" + player2Name + " Score: " + player2Score +
                        "\n" + winner + "\nTime: " + formattedTime + " seconds.");
                int response = JOptionPane.showOptionDialog(frame,
                        "Would you like to play another match?",
                        "Game Over",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        new Object[]{"Play Again", "Exit"},
                        JOptionPane.YES_OPTION);

                if (response == 0) { // "Play Again"
                    restartGame();
                } else { // "Exit"
                    frame.dispose(); // Close the game window

                }
            }
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




