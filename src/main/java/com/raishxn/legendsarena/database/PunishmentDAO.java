package com.raishxn.legendsarena.database;

import com.raishxn.legendsarena.LegendsArena;
import net.minecraft.entity.player.EntityPlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PunishmentDAO {

    /**
     * Adiciona um novo banimento à base de dados.
     * @param playerUuid O UUID do jogador a ser banido.
     * @param tier O tier do qual o jogador será banido ("all" para todos).
     * @param reason O motivo do banimento.
     * @param durationMillis A duração do banimento em milissegundos. Use 0 para um ban permanente.
     */
    public static void addPunishment(String playerUuid, String tier, String reason, long durationMillis) {
        String sql = "INSERT INTO ranked_punishments (player_uuid, tier_banned, reason, expires_at) VALUES (?, ?, ?, ?);";
        Connection conn = LegendsArena.getDatabaseManager().getConnection();

        // Se a duração for 0, o ban é permanente (expires_at = NULL).
        // Senão, calcula a data de expiração.
        Long expiresAt = (durationMillis == 0) ? null : System.currentTimeMillis() + durationMillis;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerUuid);
            pstmt.setString(2, tier.toLowerCase());
            pstmt.setString(3, reason);

            if (expiresAt == null) {
                pstmt.setNull(4, java.sql.Types.BIGINT);
            } else {
                pstmt.setLong(4, expiresAt);
            }

            pstmt.executeUpdate();
        } catch (SQLException e) {
            LegendsArena.LOGGER.error("Erro ao adicionar punicao para o UUID: " + playerUuid, e);
        }
    }

    /**
     * Remove todos os banimentos de um jogador num determinado tier.
     * @param playerUuid O UUID do jogador.
     * @param tier O tier do qual desbanir ("all" para todos).
     */
    public static void removePunishment(String playerUuid, String tier) {
        String sql = "DELETE FROM ranked_punishments WHERE player_uuid = ? AND tier_banned = ?;";
        Connection conn = LegendsArena.getDatabaseManager().getConnection();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerUuid);
            pstmt.setString(2, tier.toLowerCase());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LegendsArena.LOGGER.error("Erro ao remover punicao para o UUID: " + playerUuid, e);
        }
    }

    /**
     * Verifica se um jogador tem um banimento ativo para um determinado tier.
     * @param player O jogador a ser verificado.
     * @param tier O tier a ser verificado.
     * @return O motivo do banimento se estiver banido, ou null se não estiver.
     */
    public static String getActiveBanReason(EntityPlayer player, String tier) {
        // Esta query procura por um banimento que corresponda ao jogador e ao tier específico,
        // OU por um banimento global ("all").
        // Ela também verifica se a data de expiração é nula (permanente) ou se ainda não passou.
        String sql = "SELECT reason, expires_at FROM ranked_punishments WHERE player_uuid = ? AND (tier_banned = ? OR tier_banned = 'all') AND (expires_at IS NULL OR expires_at > ?);";
        Connection conn = LegendsArena.getDatabaseManager().getConnection();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, player.getUniqueID().toString());
            pstmt.setString(2, tier.toLowerCase());
            pstmt.setLong(3, System.currentTimeMillis());

            ResultSet rs = pstmt.executeQuery();
            // Se encontrar uma linha, significa que há um ban ativo.
            if (rs.next()) {
                return rs.getString("reason"); // Retorna o motivo do ban.
            }
        } catch (SQLException e) {
            LegendsArena.LOGGER.error("Erro ao verificar punicao para o jogador: " + player.getName(), e);
        }

        // Se não encontrar nada, retorna nulo.
        return null;
    }
}