package com.raishxn.legendsarena.ranked;

// Este enum define os ranks e seus respectivos ELOs mínimos.
public enum Rank {
    BRONZE("Bronze", 0),
    PRATA("Prata", 1200),
    OURO("Ouro", 1500),
    PLATINA("Platina", 1800),
    DIAMANTE("Diamante", 2100),
    LENDARIO("Lendário", 2400);

    private final String displayName;
    private final int minElo;

    Rank(String displayName, int minElo) {
        this.displayName = displayName;
        this.minElo = minElo;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMinElo() {
        return minElo;
    }

    // Função auxiliar para obter um rank a partir de um ELO
    public static Rank getRankFromElo(int elo) {
        Rank determinedRank = BRONZE;
        for (Rank rank : values()) {
            if (elo >= rank.getMinElo()) {
                determinedRank = rank;
            } else {
                break; // Como os ranks estão em ordem, podemos parar assim que encontrarmos um ELO maior.
            }
        }
        return determinedRank;
    }
}