package com.game.ramudu_sita.model;

public class Player {
    private final String id;
    private final String name;
    private final boolean host;
    private int totalScore;

    public Player(String id, String name, boolean host) {
        this.id = id;
        this.name = name;
        this.host = host;
        this.totalScore = 0;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public boolean isHost() { return host; }

    public int getTotalScore() { return totalScore; }
    public void addScore(int delta) { this.totalScore += delta; }
}
