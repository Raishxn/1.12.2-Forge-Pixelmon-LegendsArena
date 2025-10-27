package com.raishxn.legendsarena.config;

import com.raishxn.legendsarena.LegendsArena;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigManager {

    private File configFile;
    private Map<String, Object> configData;

    public ConfigManager(File configDir) {
        // Cria a pasta do seu mod dentro da pasta 'config' do servidor
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
        Map<String, Object> databaseConfig = new LinkedHashMap<>();
        databaseConfig.put("type", "sqlite"); // pode ser 'sqlite' ou 'mysql'
        databaseConfig.put("host", "localhost");
        databaseConfig.put("port", 3306);
        databaseConfig.put("database", "legendsarena");
        databaseConfig.put("username", "user");
        databaseConfig.put("password", "password");

        defaultConfig.put("database", databaseConfig);

        try (FileWriter writer = new FileWriter(configFile)) {
            Yaml yaml = new Yaml();
            yaml.dump(defaultConfig, writer);
        } catch (IOException e) {
            LegendsArena.LOGGER.error("Nao foi possivel criar o arquivo de configuracao padrao.", e);
        }
    }

    public void loadConfig() {
        try (FileInputStream fis = new FileInputStream(configFile)) {
            Yaml yaml = new Yaml();
            this.configData = yaml.load(fis);
            LegendsArena.LOGGER.info("Configuracao carregada com sucesso.");
        } catch (IOException e) {
            LegendsArena.LOGGER.error("Nao foi possivel carregar o arquivo de configuracao.", e);
            this.configData = new LinkedHashMap<>(); // Cria um mapa vazio em caso de erro
        }
    }

    // Método para pegar uma seção inteira da config (vamos usar para o DB)
    public Map<String, Object> getSection(String key) {
        return (Map<String, Object>) configData.getOrDefault(key, new LinkedHashMap<>());
    }
}