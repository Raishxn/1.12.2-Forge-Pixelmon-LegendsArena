package com.raishxn.legendsarena.events;

import com.pixelmonmod.pixelmon.api.events.battles.BattleEndEvent;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.enums.battle.BattleResults;
import com.raishxn.legendsarena.LegendsArena;
import com.raishxn.legendsarena.capabilities.PlayerDataManager;
import com.raishxn.legendsarena.legendsarena.Tags;
import com.raishxn.legendsarena.ranked.RankedManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Map;

public class EventHandler {

    public static final ResourceLocation PLAYER_DATA_CAP = new ResourceLocation(Tags.MOD_ID, "player_data");

    @SubscribeEvent
    public void attachCapability(AttachCapabilitiesEvent<Entity> event) {
        if (!(event.getObject() instanceof EntityPlayer)) {
            return;
        }
        event.addCapability(PLAYER_DATA_CAP, new PlayerDataManager());
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            RankedManager.getInstance().onServerTick();
        }
    }

    @SubscribeEvent
    public void onBattleEnd(BattleEndEvent event) {
        if (event.getPlayers().size() != 2) return;

        BattleParticipant winner = null;
        BattleParticipant loser = null;

        for (Map.Entry<BattleParticipant, BattleResults> entry : event.results.entrySet()) {
            if (entry.getValue() == BattleResults.VICTORY) winner = entry.getKey();
            else if (entry.getValue() == BattleResults.DEFEAT) loser = entry.getKey();
        }

        if (winner instanceof PlayerParticipant && loser instanceof PlayerParticipant) {
            EntityPlayer winnerPlayer = ((PlayerParticipant) winner).player;
            EntityPlayer loserPlayer = ((PlayerParticipant) loser).player;

            LegendsArena.LOGGER.info("Batalha Ranqueada Terminada: " + winnerPlayer.getName() + " venceu " + loserPlayer.getName());

            // Lógica de ELO e salvamento foi removida temporariamente
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            RankedManager.getInstance().onPlayerLogout((EntityPlayerMP) event.player);
        }
    }
}