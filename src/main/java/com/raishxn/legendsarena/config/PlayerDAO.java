package com.raishxn.legendsarena.database;

import com.raishxn.legendsarena.LegendsArena;
import com.raishxn.legendsarena.data.IPlayerData;
import net.minecraft.entity.player.EntityPlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class PlayerDAO {

    // Carrega os dados do jogador do banco de dados para a "capability"
    public static void loadPlayerData(EntityPlayer player, IPlayerData data) {
        String sql = "SELECT elo, wins, losses FROM player_data WHERE uuid = ?;";
        Connection conn = LegendsArena.getDatabaseManager().getConnection();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, player.getUniqueID().toString());

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                // Se o jogador foi encontrado no DB, carregamos os dados
                data.setElo(rs.getInt("elo"));
                data.setWins(rs.getInt("wins"));
                data.setLosses(rs.getInt("losses"));
                LegendsArena.LOGGER.info("Dados de {} carregados do banco de dados.", player.getName());
            } else {
                // Se não foi encontrado, é um novo jogador. Criamos uma entrada para ele.
                LegendsArena.LOGGER.info("Nenhum dado encontrado para {}, criando novo registro...", player.getName());
                createPlayer(player);
                // Os dados da capability já estarão com os valores padrão (1000, 0, 0), então não precisamos fazer nada.
            }
        } catch (SQLException e) {
            LegendsArena.LOGGER.error("Erro ao carregar dados do jogador: " + player.getName(), e);
        }
    }

    // Salva os dados da "capability" do jogador no banco de dados
    public static void savePlayerData(EntityPlayer player, IPlayerData data) {
        // "INSERT OR REPLACE" (SQLite) ou "INSERT ... ON DUPLICATE KEY UPDATE" (MySQL) são mais eficientes.
        // Para simplicidade e compatibilidade, vamos usar uma query que funciona em ambos.
        String sql = "UPDATE player_data SET player_name = ?, elo = ?, wins = ?, losses = ? WHERE uuid = ?;";
        Connection conn = LegendsArena.getDatabaseManager().getConnection();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, player.getName());
            pstmt.setInt(2, data.getElo());
            pstmt.setInt(3, data.getWins());
            pstmt.setInt(4, data.getLosses());
            pstmt.setString(5, player.getUniqueID().toString());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            LegendsArena.LOGGER.error("Erro ao salvar dados do jogador: " + player.getName(), e);
        }
    }

    // Cria um novo registro para o jogador com valores padrão
    private static void createPlayer(EntityPlayer player) {
        String sql = "INSERT INTO player_data (uuid, player_name) VALUES (?, ?);";
        Connection conn = LegendsArena.getDatabaseManager().getConnection();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, player.getUniqueID().toString());
            pstmt.setString(2, player.getName());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LegendsArena.LOGGER.error("Erro ao criar registro para o novo jogador: " + player.getName(), e);
        }
    }
}