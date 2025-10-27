package com.raishxn.legendsarena.data;

import com.raishxn.legendsarena.LegendsArena;
import com.raishxn.legendsarena.data.IPlayerData;
import com.raishxn.legendsarena.data.IPlayerData.PlayerTierStats;
import com.raishxn.legendsarena.database.LeaderboardEntry;
import net.minecraft.entity.player.EntityPlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerDAO {

    public static void loadPlayerData(EntityPlayer player, IPlayerData data) {
        String sql = "SELECT tier, elo, wins, losses FROM ranked_stats WHERE uuid = ?;";
        Connection conn = LegendsArena.getDatabaseManager().getConnection();
        Map<String, PlayerTierStats> allStats = new HashMap<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, player.getUniqueID().toString());

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String tier = rs.getString("tier");
                int elo = rs.getInt("elo");
                int wins = rs.getInt("wins");
                int losses = rs.getInt("losses");
                allStats.put(tier.toLowerCase(), new PlayerTierStats(elo, wins, losses));
            }

            data.setAllTierStats(allStats);

            if (allStats.isEmpty()) {
                LegendsArena.LOGGER.info("Nenhum dado ranqueado encontrado para {}, usara valores padrao.", player.getName());
            } else {
                LegendsArena.LOGGER.info("Dados de {} tiers carregados do banco de dados para {}.", allStats.size(), player.getName());
            }

        } catch (SQLException e) {
            LegendsArena.LOGGER.error("Erro ao carregar dados do jogador: " + player.getName(), e);
        }
    }

    public static void savePlayerData(EntityPlayer player, IPlayerData data) {
        String sql = "REPLACE INTO ranked_stats (uuid, tier, elo, wins, losses) VALUES (?, ?, ?, ?, ?);";
        Connection conn = LegendsArena.getDatabaseManager().getConnection();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Map.Entry<String, PlayerTierStats> entry : data.getAllTierStats().entrySet()) {
                String tier = entry.getKey();
                PlayerTierStats stats = entry.getValue();

                pstmt.setString(1, player.getUniqueID().toString());
                pstmt.setString(2, tier);
                pstmt.setInt(3, stats.getElo());
                pstmt.setInt(4, stats.getWins());
                pstmt.setInt(5, stats.getLosses());

                pstmt.addBatch();
            }

            pstmt.executeBatch();

        } catch (SQLException e) {
            LegendsArena.LOGGER.error("Erro ao salvar dados do jogador: " + player.getName(), e);
        }
    }

    public static List<LeaderboardEntry> getLeaderboardForTier(String tier, int limit) {
        String sql = "SELECT uuid, elo, wins, losses FROM ranked_stats WHERE tier = ? ORDER BY elo DESC LIMIT ?;";
        Connection conn = LegendsArena.getDatabaseManager().getConnection();
        List<LeaderboardEntry> leaderboard = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tier.toLowerCase());
            pstmt.setInt(2, limit);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String uuid = rs.getString("uuid");
                int elo = rs.getInt("elo");
                int wins = rs.getInt("wins");
                int losses = rs.getInt("losses");
                String playerName = getPlayerNameFromUUID(uuid);
                leaderboard.add(new LeaderboardEntry(playerName, elo, wins, losses));
            }
        } catch (SQLException e) {
            LegendsArena.LOGGER.error("Erro ao carregar o leaderboard para o tier: " + tier, e);
        }

        return leaderboard;
    }

    private static String getPlayerNameFromUUID(String uuid) {
        net.minecraft.server.MinecraftServer server = net.minecraftforge.fml.common.FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server != null) {
            com.mojang.authlib.GameProfile profile = server.getPlayerProfileCache().getProfileByUUID(UUID.fromString(uuid));
            if (profile != null && profile.getName() != null) {
                return profile.getName();
            }
        }
        return "Desconhecido";
    }

    /**
     * Aplica um "soft reset" de ELO, ZERA as vitórias e derrotas para o tier especificado.
     * A fórmula é: novo_elo = elo_inicial + (elo_atual - elo_inicial) * fator_reset
     * @param tier O tier para o qual o reset será aplicado.
     * @param startingElo O ELO inicial para este tier.
     * @param resetFactor O fator de reset (ex: 0.5 para 50%).
     * @return O número de jogadores afetados.
     */
    public static int performEloSoftReset(String tier, int startingElo, double resetFactor) {
        // CORREÇÃO: Adiciona 'wins = 0, losses = 0' à query
        String sql = "UPDATE ranked_stats SET elo = ? + CAST((elo - ?) * ? AS INT), wins = 0, losses = 0 WHERE tier = ?;";
        Connection conn = LegendsArena.getDatabaseManager().getConnection();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, startingElo);
            pstmt.setInt(2, startingElo);
            pstmt.setDouble(3, resetFactor);
            pstmt.setString(4, tier.toLowerCase());

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            LegendsArena.LOGGER.error("Falha ao executar o soft reset de ELO para o tier: " + tier, e);
            return 0;
        }
    }
}