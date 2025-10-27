package com.raishxn.legendsarena.config;

import com.raishxn.legendsarena.LegendsArena;
import com.raishxn.legendsarena.config.rank.Rank;
import com.raishxn.legendsarena.config.tier.TierConfig;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {

    private File configFile;
    private Map<String, Object> configData;
    private final Map<String, TierConfig> tierConfigs = new HashMap<>();
    private final List<Rank> eloRanks = new ArrayList<>();
    private int currentSeason = 1;
    private double eloResetFactor = 0.5;

    public ConfigManager(File configDir) {
        File modConfigDir = new File(configDir, "legendsarena");
        if (!modConfigDir.exists()) {
            modConfigDir.mkdirs();
        }
        this.configFile = new File(modConfigDir, "config.yml");
    }

    public void initialize() {
        if (!configFile.exists()) {
            LegendsArena.LOGGER.info("Arquivo de configuracao nao encontrado, criando um novo...");
            createDefaultConfig();
        }
        loadConfig();
    }

    private void createDefaultConfig() {
        Map<String, Object> defaultConfig = new LinkedHashMap<>();

        // Secção da Base de Dados
        Map<String, Object> databaseConfig = new LinkedHashMap<>();
        databaseConfig.put("type", "sqlite");
        databaseConfig.put("host", "localhost");
        databaseConfig.put("port", 3306);
        databaseConfig.put("database", "legendsarena");
        databaseConfig.put("username", "user");
        databaseConfig.put("password", "password");
        defaultConfig.put("database", databaseConfig);

        // Secção dos Tiers Ranqueados
        Map<String, Object> rankedTiers = new LinkedHashMap<>();

        Map<String, Object> ouConfig = new LinkedHashMap<>();
        ouConfig.put("level-cap", 100);
        ouConfig.put("starting-elo", 1200);
        List<String> ouBannedPokemon = new ArrayList<>();
        ouBannedPokemon.add("Mewtwo");
        ouConfig.put("banned-pokemon", ouBannedPokemon);
        List<String> ouBannedAbilities = new ArrayList<>();
        ouBannedAbilities.add("Moody");
        ouConfig.put("banned-abilities", ouBannedAbilities);
        List<String> ouBannedItems = new ArrayList<>();
        ouBannedItems.add("King's Rock");
        ouConfig.put("banned-held-items", ouBannedItems);
        // --- NOVO CAMPO ---
        List<String> ouBannedMoves = new ArrayList<>();
        ouBannedMoves.add("Tackle"); // Exemplo do seu teste anterior
        ouConfig.put("banned-moves", ouBannedMoves);

        rankedTiers.put("ou", ouConfig);
        defaultConfig.put("ranked-tiers", rankedTiers);

        // Secção de Ranks
        List<Map<String, Object>> ranks = new ArrayList<>();
        ranks.add(createRank("Bronze", 0, "&7[Bronze]"));
        ranks.add(createRank("Prata", 1200, "&f[Prata]"));
        ranks.add(createRank("Ouro", 1500, "&6[Ouro]"));
        defaultConfig.put("elo-ranks", ranks);

        // Secção de Temporadas
        Map<String, Object> seasonSettings = new LinkedHashMap<>();
        seasonSettings.put("current-season", 1);
        seasonSettings.put("start-date", "2025-10-27");
        seasonSettings.put("end-date", "2026-01-27");
        seasonSettings.put("elo-reset-factor", 0.5);
        defaultConfig.put("season-settings", seasonSettings);

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setIndent(2);
        Yaml yaml = new Yaml(options);

        try (FileWriter writer = new FileWriter(configFile)) {
            yaml.dump(defaultConfig, writer);
        } catch (IOException e) {
            LegendsArena.LOGGER.error("Nao foi possivel criar o arquivo de configuracao padrao.", e);
        }
    }

    private Map<String, Object> createRank(String name, int minElo, String prefix) {
        Map<String, Object> rank = new LinkedHashMap<>();
        rank.put("name", name);
        rank.put("min-elo", minElo);
        rank.put("display-prefix", prefix);
        return rank;
    }

    public void loadConfig() {
        try (FileInputStream fis = new FileInputStream(configFile)) {
            Yaml yaml = new Yaml();
            this.configData = yaml.load(fis);

            this.tierConfigs.clear();
            Map<String, Object> tiersData = (Map<String, Object>) configData.get("ranked-tiers");
            if (tiersData != null) {
                for (Map.Entry<String, Object> entry : tiersData.entrySet()) {
                    String tierName = entry.getKey().toLowerCase();
                    this.tierConfigs.put(tierName, new TierConfig((Map<String, Object>) entry.getValue()));
                }
            }

            this.eloRanks.clear();
            List<Map<String, Object>> ranksData = (List<Map<String, Object>>) configData.get("elo-ranks");
            if (ranksData != null) {
                for (Map<String, Object> rankData : ranksData) {
                    this.eloRanks.add(new Rank(rankData));
                }
                this.eloRanks.sort(Comparator.comparingInt(Rank::getMinElo).reversed());
            }

            Map<String, Object> seasonSettings = (Map<String, Object>) configData.get("season-settings");
            if (seasonSettings != null) {
                this.currentSeason = (int) seasonSettings.getOrDefault("current-season", 1);
                this.eloResetFactor = (double) seasonSettings.getOrDefault("elo-reset-factor", 0.5);
            }

            LegendsArena.LOGGER.info("Configuracao carregada com sucesso.");
        } catch (IOException | ClassCastException e) {
            LegendsArena.LOGGER.error("Nao foi possivel carregar ou interpretar o arquivo de configuracao.", e);
        }
    }

    public Map<String, Object> getSection(String key) {
        return (Map<String, Object>) configData.getOrDefault(key, new LinkedHashMap<>());
    }

    public TierConfig getTierConfig(String tierName) {
        return this.tierConfigs.get(tierName.toLowerCase());
    }

    public List<String> getTiers() {
        return new ArrayList<>(this.tierConfigs.keySet());
    }

    public List<Rank> getEloRanks() {
        return this.eloRanks;
    }

    public int getCurrentSeason() {
        return this.currentSeason;
    }

    public double getEloResetFactor() {
        return this.eloResetFactor;
    }

    public void setValue(String section, String key, Object value) {
        if (this.configData == null) {
            this.configData = new LinkedHashMap<>();
        }
        Map<String, Object> sectionData = (Map<String, Object>) this.configData.computeIfAbsent(section, k -> new LinkedHashMap<>());
        sectionData.put(key, value);
    }

    public void saveConfig() {
        if (this.configData == null) {
            return;
        }
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setIndent(2);
        Yaml yaml = new Yaml(options);
        try (FileWriter writer = new FileWriter(configFile)) {
            yaml.dump(this.configData, writer);
        } catch (IOException e) {
            LegendsArena.LOGGER.error("Nao foi possivel salvar as alteracoes no arquivo de configuracao.", e);
        }
    }
}