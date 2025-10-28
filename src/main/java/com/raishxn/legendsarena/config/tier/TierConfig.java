package com.raishxn.legendsarena.config.tier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TierConfig {

    private final int levelCap;
    private final List<String> bannedPokemon;
    private final List<String> bannedAbilities;
    private final List<String> bannedHeldItems;
    private final int startingElo;
    private final List<String> bannedMoves;
    // --- NOVO CAMPO ---
    private final List<String> bannedForms;

    public TierConfig(Map<String, Object> data) {
        this.levelCap = (int) data.getOrDefault("level-cap", 100);
        this.startingElo = (int) data.getOrDefault("starting-elo", 1000);

        this.bannedPokemon = loadStringList(data, "banned-pokemon");
        this.bannedAbilities = loadStringList(data, "banned-abilities");
        this.bannedHeldItems = loadStringList(data, "banned-held-items");
        this.bannedMoves = loadStringList(data, "banned-moves");
        // --- CARREGAR NOVO CAMPO ---
        this.bannedForms = loadStringList(data, "banned-forms");
    }

    private List<String> loadStringList(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value instanceof List) {
            return (List<String>) value;
        }
        return Collections.emptyList();
    }

    // Getters
    public int getLevelCap() { return levelCap; }
    public int getStartingElo() { return startingElo; }
    public List<String> getBannedPokemon() { return new ArrayList<>(bannedPokemon); }
    public List<String> getBannedAbilities() { return new ArrayList<>(bannedAbilities); }
    public List<String> getBannedHeldItems() { return new ArrayList<>(bannedHeldItems); }
    public List<String> getBannedMoves() { return new ArrayList<>(bannedMoves); }
    // --- NOVO GETTER ---
    public List<String> getBannedForms() { return new ArrayList<>(bannedForms); }
}