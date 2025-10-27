package com.raishxn.legendsarena.ranked;

import com.raishxn.legendsarena.LegendsArena;
import com.raishxn.legendsarena.config.rank.Rank;

import java.util.List;

// Esta classe é responsável pela lógica das patentes de ELO.
public class EloRankManager {

    private static final EloRankManager INSTANCE = new EloRankManager();

    private EloRankManager() {}

    public static EloRankManager getInstance() {
        return INSTANCE;
    }

    /**
     * Encontra a patente correta para um determinado valor de ELO.
     * @param elo O ELO do jogador.
     * @return O objeto Rank correspondente, ou null se nenhuma patente for encontrada.
     */
    public Rank getRankForElo(int elo) {
        // Obtém a lista de ranks, que já está ordenada do MAIOR para o MENOR ELO.
        List<Rank> ranks = LegendsArena.getConfigManager().getEloRanks();

        for (Rank rank : ranks) {
            // Como a lista está ordenada, a primeira patente cujo ELO mínimo
            // for menor ou igual ao ELO do jogador é a correta.
            if (elo >= rank.getMinElo()) {
                return rank;
            }
        }

        // Retorna nulo se o ELO for negativo ou se a lista de ranks estiver vazia.
        return null;
    }
}