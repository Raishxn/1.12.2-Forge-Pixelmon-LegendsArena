package com.raishxn.legendsarena.events;

import com.pixelmonmod.pixelmon.api.events.battles.BattleEndEvent;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.enums.battle.BattleResults;
import com.raishxn.legendsarena.LegendsArena;
import com.raishxn.legendsarena.capabilities.PlayerDataManager;
import com.raishxn.legendsarena.data.FileDataManager;
import com.raishxn.legendsarena.data.IPlayerData;
import com.raishxn.legendsarena.legendsarena.Tags;
import com.raishxn.legendsarena.network.PacketHandler;
import com.raishxn.legendsarena.network.PacketSyncPlayerData;
import com.raishxn.legendsarena.ranked.Rank;
import com.raishxn.legendsarena.ranked.RankedManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EventHandler {

    public static final ResourceLocation PLAYER_DATA_CAP = new ResourceLocation(Tags.MOD_ID, "player_data");
    private final Map<UUID, Long> rankedBattlePlayers = new ConcurrentHashMap<>();

    @SubscribeEvent
    public void attachCapability(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityPlayer) {
            event.addCapability(PLAYER_DATA_CAP, new PlayerDataManager());
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
    public void onPlayerLogin(PlayerLoggedInEvent event) {
        if (!event.player.world.isRemote && event.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.player;
            IPlayerData data = player.getCapability(PlayerDataManager.PLAYER_DATA_CAPABILITY, null);
            if (data != null) {
                FileDataManager.loadData(player, data);
                PacketHandler.INSTANCE.sendTo(new PacketSyncPlayerData(data), player);
                LegendsArena.LOGGER.info("[LOGIN] Dados carregados para " + player.getName() + " - ELO: " + data.getElo());
            } else {
                LegendsArena.LOGGER.error("[LOGIN] Capability é NULL para " + player.getName());
            }
        }
    }

    @SubscribeEvent
    public void onPlayerSave(PlayerEvent.SaveToFile event) {
        if (event.getEntityPlayer() instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.getEntityPlayer();
            IPlayerData data = player.getCapability(PlayerDataManager.PLAYER_DATA_CAPABILITY, null);
            if (data != null) {
                FileDataManager.saveData(player, data);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerLoggedOutEvent event) {
        if (!event.player.world.isRemote && event.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.player;
            IPlayerData data = player.getCapability(PlayerDataManager.PLAYER_DATA_CAPABILITY, null);
            if (data != null) {
                FileDataManager.saveData(player, data);
            }
            RankedManager.getInstance().onPlayerLogout(player);
            rankedBattlePlayers.remove(player.getUniqueID());
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            RankedManager.getInstance().onServerTick();

            // Limpar jogadores antigos do mapa de batalhas ranqueadas
            long currentTime = System.currentTimeMillis();
            rankedBattlePlayers.entrySet().removeIf(entry ->
                    currentTime - entry.getValue() > 300000); // 5 minutos
        }
    }

    // Método para marcar jogadores em batalha ranqueada
    public void markPlayerInRankedBattle(EntityPlayerMP player) {
        rankedBattlePlayers.put(player.getUniqueID(), System.currentTimeMillis());
        LegendsArena.LOGGER.info("[RANKED] Jogador {} marcado para batalha ranqueada", player.getName());
    }

    public boolean isPlayerInRankedBattle(EntityPlayerMP player) {
        return rankedBattlePlayers.containsKey(player.getUniqueID());
    }

    private int getKFactor(IPlayerData playerData) {
        Rank rank = Rank.getRankFromElo(playerData.getElo());
        if (rank.ordinal() >= Rank.OURO.ordinal()) {
            return 16;
        }
        return 32;
    }

    @SubscribeEvent
    public void onBattleEnd(BattleEndEvent event) {
        LegendsArena.LOGGER.info("[BATTLE] Evento BattleEndEvent disparado");

        // CORREÇÃO: event.getPlayers() retorna List<EntityPlayerMP>, não List<BattleParticipant>
        // Verificar se é uma batalha ranqueada
        boolean isRankedBattle = false;
        for (EntityPlayerMP player : event.getPlayers()) {
            if (isPlayerInRankedBattle(player)) {
                isRankedBattle = true;
                break;
            }
        }

        if (!isRankedBattle) {
            LegendsArena.LOGGER.info("[BATTLE] Batalha não é ranqueada, ignorando");
            return;
        }

        if (event.getPlayers().size() != 2) {
            LegendsArena.LOGGER.info("[BATTLE] Batalha não tem 2 jogadores, ignorando");
            return;
        }

        BattleParticipant winner = null;
        BattleParticipant loser = null;

        for (Map.Entry<BattleParticipant, BattleResults> entry : event.results.entrySet()) {
            if (entry.getValue() == BattleResults.VICTORY) winner = entry.getKey();
            else if (entry.getValue() == BattleResults.DEFEAT) loser = entry.getKey();
        }

        if (winner instanceof PlayerParticipant && loser instanceof PlayerParticipant) {
            EntityPlayerMP winnerPlayer = ((PlayerParticipant) winner).player;
            EntityPlayerMP loserPlayer = ((PlayerParticipant) loser).player;

            LegendsArena.LOGGER.info("[BATTLE] Processando batalha ranqueada: {} vs {}", winnerPlayer.getName(), loserPlayer.getName());

            IPlayerData winnerData = winnerPlayer.getCapability(PlayerDataManager.PLAYER_DATA_CAPABILITY, null);
            IPlayerData loserData = loserPlayer.getCapability(PlayerDataManager.PLAYER_DATA_CAPABILITY, null);

            if (winnerData == null || loserData == null) {
                LegendsArena.LOGGER.error("[BATTLE] ERRO: Capability nula para um dos jogadores");
                return;
            }

            int winnerElo = winnerData.getElo();
            int loserElo = loserData.getElo();

            LegendsArena.LOGGER.info("[BATTLE] ELO antes - Vencedor: {}, Perdedor: {}", winnerElo, loserElo);

            // Cálculo do ELO
            int winnerKFactor = getKFactor(winnerData);
            int loserKFactor = getKFactor(loserData);

            double expectedWinner = 1.0 / (1.0 + Math.pow(10.0, (double) (loserElo - winnerElo) / 400.0));
            double expectedLoser = 1.0 - expectedWinner;

            int newWinnerElo = (int) Math.round(winnerElo + winnerKFactor * (1.0 - expectedWinner));
            int newLoserElo = (int) Math.round(loserElo + loserKFactor * (0.0 - expectedLoser));
            int eloChangeWinner = newWinnerElo - winnerElo;
            int eloChangeLoser = newLoserElo - loserElo;

            // Atualizar dados
            winnerData.setElo(newWinnerElo);
            winnerData.setWins(winnerData.getWins() + 1);
            loserData.setElo(newLoserElo);
            loserData.setLosses(loserData.getLosses() + 1);

            LegendsArena.LOGGER.info("[BATTLE] ELO depois - Vencedor: {}, Perdedor: {}", newWinnerElo, newLoserElo);

            // Sincronizar com clientes
            PacketHandler.INSTANCE.sendTo(new PacketSyncPlayerData(winnerData), winnerPlayer);
            PacketHandler.INSTANCE.sendTo(new PacketSyncPlayerData(loserData), loserPlayer);

            // Salvar dados
            FileDataManager.saveData(winnerPlayer, winnerData);
            FileDataManager.saveData(loserPlayer, loserData);

            // Mensagens para jogadores
            winnerPlayer.sendMessage(new TextComponentString(TextFormatting.GOLD + "Você VENCEU a partida ranqueada!"));
            winnerPlayer.sendMessage(new TextComponentString(TextFormatting.GREEN + " +" + eloChangeWinner + " ELO (Novo ELO: " + newWinnerElo + ")"));
            loserPlayer.sendMessage(new TextComponentString(TextFormatting.RED + "Você PERDEU a partida ranqueada."));
            loserPlayer.sendMessage(new TextComponentString(TextFormatting.RED + " " + eloChangeLoser + " ELO (Novo ELO: " + newLoserElo + ")"));

            // Remover dos jogadores em batalha ranqueada
            rankedBattlePlayers.remove(winnerPlayer.getUniqueID());
            rankedBattlePlayers.remove(loserPlayer.getUniqueID());

            LegendsArena.LOGGER.info("[BATTLE] Batalha ranqueada processada com sucesso");

        } else {
            LegendsArena.LOGGER.warn("[BATTLE] Não foi possível determinar vencedor/perdedor");
        }
    }
}