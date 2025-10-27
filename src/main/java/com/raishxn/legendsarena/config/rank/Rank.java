package com.raishxn.legendsarena.config.rank;

import java.util.Map;

// Esta classe representa uma única patente (ex: Bronze, Ouro, Divino).
public class Rank {

    private final String name;
    private final int minElo;
    private final String displayPrefix;

    // O construtor pega nos dados lidos do YAML e preenche os campos.
    public Rank(Map<String, Object> data) {
        this.name = (String) data.getOrDefault("name", "Unranked");
        this.minElo = (int) data.getOrDefault("min-elo", 0);
        // O '&' é usado para códigos de cor no Minecraft.
        this.displayPrefix = (String) data.getOrDefault("display-prefix", "&7[Unranked]");
    }

    // Getters para que outras classes possam aceder a esta informação.
    public String getName() {
        return name;
    }

    public int getMinElo() {
        return minElo;
    }

    public String getDisplayPrefix() {
        // Substitui o '&' pelo caractere de formatação do Minecraft.
        return displayPrefix.replace('&', '\u00A7');
    }
}