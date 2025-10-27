package com.raishxn.legendsarena.config;

import com.raishxn.legendsarena.LegendsArena;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

public class DatabaseManager {

    private Connection connection;
    private final File configDir; // Adicionamos uma variável para guardar o caminho

    // CORREÇÃO: O construtor agora recebe o caminho
    public DatabaseManager(File configDir) {
        this.configDir = configDir;
    }

    public void initialize() {
        Map<String, Object> dbConfig = LegendsArena.getConfigManager().getSection("database");
        String dbType = (String) dbConfig.getOrDefault("type", "sqlite");

        try {
            if ("mysql".equalsIgnoreCase(dbType)) {
                connectMySQL(dbConfig);
            } else {
                connectSQLite();
            }
            createTables();

        } catch (SQLException | ClassNotFoundException e) {
            LegendsArena.LOGGER.error("Falha critica ao conectar ou preparar o banco de dados!", e);
        }
    }

    private void connectMySQL(Map<String, Object> config) throws SQLException, ClassNotFoundException {
        // ... (esta parte não muda)
        Class.forName("com.mysql.jdbc.Driver");
        String host = (String) config.get("host");
        int port = (int) config.get("port");
        String dbName = (String) config.get("database");
        String user = (String) config.get("username");
        String pass = (String) config.get("password");
        String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?autoReconnect=true&useSSL=false";
        this.connection = DriverManager.getConnection(jdbcUrl, user, pass);
        LegendsArena.LOGGER.info("Conectado ao banco de dados MySQL com sucesso!");
    }

    private void connectSQLite() throws SQLException {
        // CORREÇÃO: Usamos o caminho da config para criar o arquivo no sítio certo
        File modConfigDir = new File(this.configDir, "legendsarena");
        if (!modConfigDir.exists()) {
            modConfigDir.mkdirs();
        }
        File dbFile = new File(modConfigDir, "legendsarena.db");

        String jdbcUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();
        this.connection = DriverManager.getConnection(jdbcUrl);
        LegendsArena.LOGGER.info("Conectado ao banco de dados SQLite local em " + dbFile.getAbsolutePath());
    }

    // ... (o resto do arquivo continua igual) ...

    private void createTables() throws SQLException {
        try (Statement statement = this.connection.createStatement()) {
            // Comando SQL para a tabela de estatísticas (sem alterações)
            String createStatsTableSQL = "CREATE TABLE IF NOT EXISTS ranked_stats (" +
                    "uuid VARCHAR(36) NOT NULL," +
                    "tier VARCHAR(32) NOT NULL," +
                    "elo INT DEFAULT 1000," +
                    "wins INT DEFAULT 0," +
                    "losses INT DEFAULT 0," +
                    "PRIMARY KEY (uuid, tier)" +
                    ");";
            statement.execute(createStatsTableSQL);
            LegendsArena.LOGGER.info("Tabela 'ranked_stats' verificada/criada com sucesso.");

            // --- NOVO COMANDO SQL PARA A TABELA DE PUNIÇÕES ---
            String createPunishmentsTableSQL = "CREATE TABLE IF NOT EXISTS ranked_punishments (" +
                    "punishment_id INTEGER PRIMARY KEY AUTOINCREMENT," + // Um ID único para cada punição
                    "player_uuid VARCHAR(36) NOT NULL," + // UUID do jogador punido
                    "tier_banned VARCHAR(32) NOT NULL," + // O tier do ban ('all' para todos)
                    "reason TEXT," + // O motivo do banimento
                    "expires_at BIGINT" + // A data de expiração em milissegundos (nulo para permanente)
                    ");";
            statement.execute(createPunishmentsTableSQL);
            LegendsArena.LOGGER.info("Tabela 'ranked_punishments' verificada/criada com sucesso.");
        }
    }

    public void closeConnection() {
        try {
            if (this.connection != null && !this.connection.isClosed()) {
                this.connection.close();
                LegendsArena.LOGGER.info("Conexao com o banco de dados fechada.");
            }
        } catch (SQLException e) {
            LegendsArena.LOGGER.error("Erro ao fechar a conexao com o banco de dados.", e);
        }
    }

    public Connection getConnection() {
        return this.connection;
    }
}