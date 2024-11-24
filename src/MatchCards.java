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
// Encapsulate the logic of loading scores from the file
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
                scores.add(new ScoreEntry(playerName, time, errors, isSinglePlayer)); // Adding to private list
            }
        }
    } catch (IOException e) {
        System.out.println("Score file not found, starting fresh.");
    }
}

// Encapsulate the logic for saving scores to a file
private void saveScores() {
    try (PrintWriter writer = new PrintWriter(new FileWriter(scoreFilePath))) {
        for (ScoreEntry entry : scores) {
            writer.println(entry.getPlayerName() + "\t" + entry.getTime() + "\t" + entry.getErrors() + "\t" + entry.isSinglePlayer());
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
}

// Public method to get the list of scores (getter method)
public List<ScoreEntry> getScores() {
    return new ArrayList<>(scores); // Return a copy to maintain encapsulation
}

// Additional methods as needed for handling scores
}