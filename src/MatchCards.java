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
    / Load scores from the file
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





