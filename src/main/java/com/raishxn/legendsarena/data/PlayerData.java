package com.raishxn.legendsarena.data;

// Esta é a implementação real dos nossos dados.
public class PlayerData implements IPlayerData {
    private int elo = 1000; // ELO inicial padrão
    private int wins = 0;
    private int losses = 0;

    @Override
    public int getElo() {
        return this.elo;
    }

    @Override
    public void setElo(int elo) {
        this.elo = elo;
    }

    @Override
    public int getWins() {
        return this.wins;
    }

    @Override
    public void setWins(int wins) {
        this.wins = wins;
    }

    @Override
    public int getLosses() {
        return this.losses;
    }

    @Override
    public void setLosses(int losses) {
        this.losses = losses;
    }
}