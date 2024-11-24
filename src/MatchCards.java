import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;


public class MatchCards {
    double currentPlayerTime = 0.0; // initialization of time
    private Scoreboard scoreboard;


    class Card {
        private String cardName;
        private ImageIcon cardImageIcon;

        public Card(String cardName, ImageIcon cardImageIcon) {
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

    //Game configuration
    String[] cardlist = {
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
    ArrayList<Button> board;
    Timer hideCardTimer, gameTimer;
    boolean gameReady = false;
    JButton card1Selected, card2Selected;
    double elapsedTime = 0.0;
    String player1Name;
    String player2Name;


    public MatchCards() {
        scoreboard = new Scoreboard();
        ShowOptionMenu();
    }

    private void ShowOptionMenu() {
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
                scoreboard.displaySinglePlayerScore(optionsFrame, player1Name, currentPlayerTime));

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

    private void showDifficultyScreen() {
        JFrame difficultyFrame = new JFrame("Select Difficulty");
        difficultyFrame.setResizable(false);
        difficultyFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        difficultyFrame.setSize(350, 200);
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

        // start button is enable when name is enter
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
        JFrame singlePlayerFrame = new JFrame("Two Player - Enter Name");
        singlePlayerFrame.setResizable(false);
        singlePlayerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        singlePlayerFrame.setSize(300, 150);
        singlePlayerFrame.setLayout(new FlowLayout());

        JLabel playerLabel = new JLabel("Enter player 1 name");
        JTextField player1Field = new JTextField(15);
        JLabel player2label = new JLabel("Enter player 2 name ");
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
        switch (difficulty) {
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
        JOptionPane.showMessageDialog(null, message);


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

        //board setup of card game
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

    private void updateTime() {
        elapsedTime += 0.01;
        currentPlayerTime = elapsedTime;

        timerLabel.setText("Time: " + String.format("%.2f", elapsedTime) + "seconds");

        // if the time exceeds the limited time, terminate the game
        if (elapsedTime >= gameTimeLimit) {
            isTimeExceeded = true;  // time exceed,player failed to complete the game in time
            gameTimer.stop(); // game timer stop
            endGame(false); // time up game over
        }

    }

    private void endGame(boolean completed) {
        gameCompleted = completed;
        gameTimer.stop();
        String message;
        if (completed && !isTimeExceeded) {
            String formattedTime = String.format("%.2f", elapsedTime);
            message = "Congratulations! You completed the game in " + formattedTime + " seconds.";
            if (isSinglePlayer) {
                scoreboard.addScore(player1Name, elapsedTime, errorCount, true)
                scoreboard.displaySinglePlayerScores(frame, player1Name, currentPlayerTime);
                showScoreboard(player1Name, elapsedTime); // the player whose scores are going to be shown on scoreboard
            }
        } else {
            message = "Game Over! You didn't complete the game in time.";
            if (isSinglePlayer) {
                if (completed) {
                    scoreboard.addScore(player1Name, elapsedTime, errorCount, true); //scores are added to scoreboard
                    scoreboard.displaySinglePlayerScores(frame, player1Name, currentPlayerTime); // updated score are displayed now

                } else {
                }
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


    private void checkGameCompletion() {
        boolean allMatched = board.stream().allMatch(button -> button.getIcon() != cardBackImageIcon);

        if (allMatched) {
            gameTimer.stop();
            String formattedTime = String.format("%.2f", elapsedTime);

            if (isSinglePlayer) {
                scoreboard.addScore(player1Name, elapsedTime, errorCount, true);
                // this show the scoreboard after each completion of game
                scoreboard.displaySinglePlayerScore(frame, player1Name, currentPlayerTime);
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


}



