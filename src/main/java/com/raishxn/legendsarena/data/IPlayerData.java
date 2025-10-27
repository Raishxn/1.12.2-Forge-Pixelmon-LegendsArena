package com.raishxn.legendsarena.data;

import java.util.Map;

// A interface agora define como vamos interagir com os dados de múltiplos tiers.
public interface IPlayerData {

    // Em vez de getElo(), agora temos um método que retorna as estatísticas de um tier específico.
    PlayerTierStats getStatsForTier(String tier);

    // Retorna o mapa completo com todos os tiers que o jogador já jogou.
    Map<String, PlayerTierStats> getAllTierStats();

    // Define o mapa completo de estatísticas (útil ao carregar do banco de dados).
    void setAllTierStats(Map<String, PlayerTierStats> stats);

    /**
     * Uma classe interna simples para guardar as estatísticas de um único tier.
     * Isto ajuda a manter o código organizado.
     */
    class PlayerTierStats {
        private int elo;
        private int wins;
        private int losses;

        public PlayerTierStats(int elo, int wins, int losses) {
            this.elo = elo;
            this.wins = wins;
            this.losses = losses;
        }

        // Construtor para novos jogadores, com valores padrão.
        public PlayerTierStats() {
            this(1000, 0, 0);
        }

        // Getters
        public int getElo() { return elo; }
        public int getWins() { return wins; }
        public int getLosses() { return losses; }

        // Setters
        public void setElo(int elo) { this.elo = elo; }
        public void setWins(int wins) { this.wins = wins; }
        public void setLosses(int losses) { this.losses = losses; }
    }
}