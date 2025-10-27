package com.raishxn.legendsarena.events;

import com.pixelmonmod.pixelmon.api.events.battles.BattleEndEvent;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.enums.battle.BattleResults;
import com.raishxn.legendsarena.LegendsArena;
import com.raishxn.legendsarena.capabilities.PlayerDataManager;
import com.raishxn.legendsarena.data.IPlayerData;
import com.raishxn.legendsarena.database.PlayerDAO;
import com.raishxn.legendsarena.legendsarena.Tags;
import com.raishxn.legendsarena.network.PacketHandler;
import com.raishxn.legendsarena.network.PacketSyncPlayerData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashSet;
import java.util.Map; // Adicionado para usar o Map.Entry
import java.util.Set;
import java.util.UUID;

public class EventHandler {
    private final Set<UUID> rankedBattlePlayers = new HashSet<>();

    public void markPlayerInRankedBattle(EntityPlayerMP player) {
        rankedBattlePlayers.add(player.getUniqueID());
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
                newData.setElo(oldData.getElo());
                newData.setWins(oldData.getWins());
                newData.setLosses(oldData.getLosses());
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

        // CORREÇÃO: Iterar sobre o mapa de resultados do evento
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

            if (!rankedBattlePlayers.contains(winnerPlayer.getUniqueID()) || !rankedBattlePlayers.contains(loserPlayer.getUniqueID())) {
                return;
            }

            IPlayerData winnerData = winnerPlayer.getCapability(PlayerDataManager.PLAYER_DATA_CAPABILITY, null);
            IPlayerData loserData = loserPlayer.getCapability(PlayerDataManager.PLAYER_DATA_CAPABILITY, null);

            if (winnerData == null || loserData == null) {
                LegendsArena.LOGGER.error("[BATTLE] Dados de jogador nulos para vencedor ou perdedor.");
                return;
            }

            int K = 32;
            double winnerElo = winnerData.getElo();
            double loserElo = loserData.getElo();
            double expectedWinner = 1.0 / (1.0 + Math.pow(10.0, (loserElo - winnerElo) / 400.0));
            double expectedLoser = 1.0 / (1.0 + Math.pow(10.0, (winnerElo - loserElo) / 400.0));
            int eloChangeWinner = (int) Math.round(K * (1 - expectedWinner));
            int eloChangeLoser = (int) Math.round(K * (0 - expectedLoser));
            int newWinnerElo = (int) Math.round(winnerElo + eloChangeWinner);
            int newLoserElo = (int) Math.round(loserElo + eloChangeLoser);

            winnerData.setElo(newWinnerElo);
            winnerData.setWins(winnerData.getWins() + 1);
            loserData.setElo(Math.max(0, newLoserElo));
            loserData.setLosses(loserData.getLosses() + 1);

            PacketHandler.INSTANCE.sendTo(new PacketSyncPlayerData(winnerData), winnerPlayer);
            PacketHandler.INSTANCE.sendTo(new PacketSyncPlayerData(loserData), loserPlayer);

            PlayerDAO.savePlayerData(winnerPlayer, winnerData);
            PlayerDAO.savePlayerData(loserPlayer, loserData);

            winnerPlayer.sendMessage(new TextComponentString(TextFormatting.GOLD + "Voce VENCEU a partida ranqueada!"));
            winnerPlayer.sendMessage(new TextComponentString(TextFormatting.GREEN + " +" + eloChangeWinner + " ELO (Novo ELO: " + newWinnerElo + ")"));
            loserPlayer.sendMessage(new TextComponentString(TextFormatting.RED + "Voce PERDEU a partida ranqueada."));
            loserPlayer.sendMessage(new TextComponentString(TextFormatting.RED + " " + eloChangeLoser + " ELO (Novo ELO: " + newLoserElo + ")"));

            rankedBattlePlayers.remove(winnerPlayer.getUniqueID());
            rankedBattlePlayers.remove(loserPlayer.getUniqueID());
        }
    }
}