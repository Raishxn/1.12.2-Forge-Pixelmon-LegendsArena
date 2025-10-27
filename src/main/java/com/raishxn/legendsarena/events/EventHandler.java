package com.raishxn.legendsarena.events;

import com.pixelmonmod.pixelmon.api.events.battles.BattleEndEvent;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.enums.battle.BattleResults;
import com.raishxn.legendsarena.LegendsArena;
import com.raishxn.legendsarena.capabilities.PlayerDataManager;
import com.raishxn.legendsarena.config.rank.Rank; // Importar Rank
import com.raishxn.legendsarena.data.IPlayerData;
import com.raishxn.legendsarena.data.IPlayerData.PlayerTierStats;

import com.raishxn.legendsarena.data.PlayerDAO;
import com.raishxn.legendsarena.legendsarena.Tags;
import com.raishxn.legendsarena.network.PacketHandler;
import com.raishxn.legendsarena.network.PacketSyncPlayerData;
import com.raishxn.legendsarena.ranked.EloRankManager; // Importar EloRankManager
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EventHandler {
    private final Map<UUID, String> rankedBattlePlayers = new HashMap<>();

    public void markPlayerInRankedBattle(EntityPlayerMP player, String tier) {
        rankedBattlePlayers.put(player.getUniqueID(), tier.toLowerCase());
    }

    @SubscribeEvent
    public void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityPlayer) {
            event.addCapability(new ResourceLocation(Tags.MOD_ID, "player_data"), new PlayerDataManager());
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            IPlayerData oldData = event.getOriginal().getCapability(PlayerDataManager.PLAYER_DATA_CAPABILITY, null);
            IPlayerData newData = event.getEntityPlayer().getCapability(PlayerDataManager.PLAYER_DATA_CAPABILITY, null);
            if (oldData != null && newData != null) {
                newData.setAllTierStats(oldData.getAllTierStats());
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLoad(PlayerEvent.LoadFromFile event) {
        EntityPlayer player = event.getEntityPlayer();
        IPlayerData data = player.getCapability(PlayerDataManager.PLAYER_DATA_CAPABILITY, null);
        if (data != null) {
            PlayerDAO.loadPlayerData(player, data);
        }
    }

    @SubscribeEvent
    public void onPlayerSave(PlayerEvent.SaveToFile event) {
        EntityPlayer player = event.getEntityPlayer();
        IPlayerData data = player.getCapability(PlayerDataManager.PLAYER_DATA_CAPABILITY, null);
        if (data != null) {
            PlayerDAO.savePlayerData(player, data);
        }
    }

    @SubscribeEvent
    public void onBattleEnd(BattleEndEvent event) {
        PlayerParticipant winnerParticipant = null;
        PlayerParticipant loserParticipant = null;

        for (Map.Entry<BattleParticipant, BattleResults> entry : event.results.entrySet()) {
            if (entry.getKey() instanceof PlayerParticipant) {
                if (entry.getValue() == BattleResults.VICTORY) {
                    winnerParticipant = (PlayerParticipant) entry.getKey();
                } else {
                    loserParticipant = (PlayerParticipant) entry.getKey();
                }
            }
        }

        if (winnerParticipant != null && loserParticipant != null) {
            EntityPlayerMP winnerPlayer = winnerParticipant.player;
            EntityPlayerMP loserPlayer = loserParticipant.player;

            String tier = rankedBattlePlayers.get(winnerPlayer.getUniqueID());
            if (tier == null || !rankedBattlePlayers.containsKey(loserPlayer.getUniqueID())) {
                return;
            }

            IPlayerData winnerData = winnerPlayer.getCapability(PlayerDataManager.PLAYER_DATA_CAPABILITY, null);
            IPlayerData loserData = loserPlayer.getCapability(PlayerDataManager.PLAYER_DATA_CAPABILITY, null);
            if (winnerData == null || loserData == null) return;

            PlayerTierStats winnerTierStats = winnerData.getStatsForTier(tier);
            PlayerTierStats loserTierStats = loserData.getStatsForTier(tier);

            // --- LÓGICA DE PROMOÇÃO/DESPROMOÇÃO ---
            // 1. Guardar o ELO antigo antes de o alterar
            int oldWinnerElo = winnerTierStats.getElo();
            int oldLoserElo = loserTierStats.getElo();
            // --- FIM ---

            int K = 32;
            double expectedWinner = 1.0 / (1.0 + Math.pow(10.0, (oldLoserElo - oldWinnerElo) / 400.0));
            double expectedLoser = 1.0 / (1.0 + Math.pow(10.0, (oldWinnerElo - oldLoserElo) / 400.0));

            int eloChangeWinner = (int) Math.round(K * (1 - expectedWinner));
            int eloChangeLoser = (int) Math.round(K * (0 - expectedLoser));

            int newWinnerElo = oldWinnerElo + eloChangeWinner;
            int newLoserElo = oldLoserElo + eloChangeLoser;

            winnerTierStats.setElo(newWinnerElo);
            winnerTierStats.setWins(winnerTierStats.getWins() + 1);
            loserTierStats.setElo(Math.max(0, newLoserElo));
            loserTierStats.setLosses(loserTierStats.getLosses() + 1);

            PlayerDAO.savePlayerData(winnerPlayer, winnerData);
            PlayerDAO.savePlayerData(loserPlayer, loserData);

            String tierUpper = tier.toUpperCase();
            winnerPlayer.sendMessage(new TextComponentString(TextFormatting.GOLD + "Voce VENCEU a partida ranqueada ("+tierUpper+")!"));
            winnerPlayer.sendMessage(new TextComponentString(TextFormatting.GREEN + " +" + eloChangeWinner + " ELO (Novo ELO: " + newWinnerElo + ")"));
            loserPlayer.sendMessage(new TextComponentString(TextFormatting.RED + "Voce PERDEU a partida ranqueada ("+tierUpper+")."));
            loserPlayer.sendMessage(new TextComponentString(TextFormatting.RED + " " + eloChangeLoser + " ELO (Novo ELO: " + newLoserElo + ")"));

            // --- LÓGICA DE PROMOÇÃO/DESPROMOÇÃO ---
            // 2. Verificar se a patente mudou para o vencedor e o perdedor
            checkRankChange(winnerPlayer, oldWinnerElo, newWinnerElo);
            checkRankChange(loserPlayer, oldLoserElo, newLoserElo);
            // --- FIM ---

            rankedBattlePlayers.remove(winnerPlayer.getUniqueID());
            rankedBattlePlayers.remove(loserPlayer.getUniqueID());
        }
    }

    // --- NOVO MÉTODO AUXILIAR ---
    /**
     * Compara a patente do ELO antigo com a do novo e envia uma mensagem se houver mudança.
     */
    private void checkRankChange(EntityPlayerMP player, int oldElo, int newElo) {
        EloRankManager rankManager = EloRankManager.getInstance();
        Rank oldRank = rankManager.getRankForElo(oldElo);
        Rank newRank = rankManager.getRankForElo(newElo);

        // Se a nova patente for nula (improvável) ou a mesma que a antiga, não faz nada.
        if (newRank == null || newRank.equals(oldRank)) {
            return;
        }

        // Compara o ELO mínimo para determinar se foi uma promoção ou despromoção.
        if (newRank.getMinElo() > (oldRank != null ? oldRank.getMinElo() : -1)) {
            // Promoção!
            player.sendMessage(new TextComponentString(TextFormatting.AQUA + "============================="));
            player.sendMessage(new TextComponentString(TextFormatting.GREEN + "       PROMOVIDO!"));
            player.sendMessage(new TextComponentString(TextFormatting.WHITE + "Voce alcancou a patente " + newRank.getDisplayPrefix()));
            player.sendMessage(new TextComponentString(TextFormatting.AQUA + "============================="));
        } else {
            // Despromoção.
            player.sendMessage(new TextComponentString(TextFormatting.RED + "Voce foi despromovido para a patente " + newRank.getDisplayPrefix()));
        }
    }
}