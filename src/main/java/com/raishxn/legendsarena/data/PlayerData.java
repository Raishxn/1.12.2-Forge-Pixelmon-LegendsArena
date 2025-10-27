package com.raishxn.legendsarena.data;

import com.raishxn.legendsarena.LegendsArena;
import com.raishxn.legendsarena.config.tier.TierConfig;

import java.util.HashMap;
import java.util.Map;

public class PlayerData implements IPlayerData {

    private Map<String, PlayerTierStats> tierStats = new HashMap<>();

    @Override
    public PlayerTierStats getStatsForTier(String tier) {
        // --- LÓGICA ATUALIZADA ---
        // Se o jogador ainda não tem estatísticas para este tier,
        // vamos criá-las usando o ELO inicial da configuração.
        return tierStats.computeIfAbsent(tier.toLowerCase(), tierName -> {
            TierConfig config = LegendsArena.getConfigManager().getTierConfig(tierName);
            // Se a configuração existir, usa o ELO inicial dela. Senão, usa 1000 como fallback.
            int startingElo = (config != null) ? config.getStartingElo() : 1000;
            return new PlayerTierStats(startingElo, 0, 0);
        });
    }

    @Override
    public Map<String, PlayerTierStats> getAllTierStats() {
        return this.tierStats;
    }

    @Override
    public void setAllTierStats(Map<String, PlayerTierStats> stats) {
        this.tierStats.clear();
        this.tierStats.putAll(stats);
    }
}