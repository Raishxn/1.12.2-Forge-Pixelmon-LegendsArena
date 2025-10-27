package com.raishxn.legendsarena.database;

import com.raishxn.legendsarena.data.IPlayerData.PlayerTierStats;

// Esta classe é um simples contentor de dados para uma entrada no leaderboard.
public class LeaderboardEntry {
    private final String playerName;
    private final PlayerTierStats stats;

    public LeaderboardEntry(String playerName, int elo, int wins, int losses) {
        this.playerName = playerName;
        this.stats = new PlayerTierStats(elo, wins, losses);
    }

    public String getPlayerName() {
        return playerName;
    }

    public PlayerTierStats getStats() {
        return stats;
    }
}