    public ScoreEntry(String playerName, double time, int errors, boolean isSinglePlayer) {
        this.playerName = playerName;
        this.time = time;
        this.errors = errors;
        this.isSinglePlayer = isSinglePlayer;
    }

    // Getter methods
    public String getPlayerName() {
        return playerName;
    }

    public double getTime() {
        return time;
    }

    public int getErrors() {
        return errors;
    }

    public boolean isSinglePlayer() {
        return isSinglePlayer;
    }
}
