// Constructor to initialize scores list and load scores from file
public Scoreboard() {
    scores = new ArrayList<>();
    loadScores(); // Encapsulate the loading logic
}
//Public method to add a new score; external code interacts with this method
public void addScore(String playerName, double time, int errors, boolean isSinglePlayer) {
    // Create a new score entry and add to the list
    ScoreEntry newEntry = new ScoreEntry(playerName, time, errors, isSinglePlayer);
    scores.add(newEntry);
    Collections.sort(scores, Comparator.comparingDouble(ScoreEntry::getTime)); // Encapsulated sorting
    saveScores(); // Encapsulated saving logic
}
