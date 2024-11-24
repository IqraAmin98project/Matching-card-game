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





