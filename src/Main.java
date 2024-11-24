class Scoreboard {
    private ArrayList<ScoreEntry> scores;
    private final String scoreFilePath = "scoreboard.txt"; // File to store scores

    public Scoreboard() {
        scores = new ArrayList<>();
        loadScores(); // Load scores from file
    }
}