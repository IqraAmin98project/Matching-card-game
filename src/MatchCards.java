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





