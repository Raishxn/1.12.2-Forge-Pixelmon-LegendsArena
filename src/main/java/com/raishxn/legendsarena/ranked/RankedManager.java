package com.raishxn.legendsarena.ranked;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.battles.rules.BattleRules;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import com.raishxn.legendsarena.LegendsArena;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RankedManager {
    private static final RankedManager INSTANCE = new RankedManager();
    private final Queue<EntityPlayerMP> queue = new ConcurrentLinkedQueue<>();

    private RankedManager() {}

    public static RankedManager getInstance() {
        return INSTANCE;
    }

    public void addPlayerToQueue(EntityPlayerMP player) {
        if (queue.stream().anyMatch(p -> p.getUniqueID().equals(player.getUniqueID()))) {
            player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Você já está na fila."));
            return;
        }
        if (BattleRegistry.getBattle(player) != null) {
            player.sendMessage(new TextComponentString(TextFormatting.RED + "Você não pode entrar na fila enquanto estiver em uma batalha."));
            return;
        }
        PlayerPartyStorage party = Pixelmon.storageManager.getParty(player);
        if (party.countAblePokemon() == 0) {
            player.sendMessage(new TextComponentString(TextFormatting.RED + "Você precisa de pelo menos um Pokémon no seu time para entrar na fila."));
            return;
        }
        queue.add(player);
        player.sendMessage(new TextComponentString(TextFormatting.GREEN + "Você entrou na fila para uma partida ranqueada!"));
    }

    public void removePlayerFromQueue(EntityPlayerMP player) {
        boolean removed = queue.removeIf(p -> p.getUniqueID().equals(player.getUniqueID()));
        if (removed) {
            player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Você saiu da fila."));
        } else {
            player.sendMessage(new TextComponentString(TextFormatting.RED + "Você não estava em nenhuma fila."));
        }
    }

    public void onPlayerLogout(EntityPlayerMP player) {
        this.removePlayerFromQueue(player);
    }

    public void onServerTick() {
        if (queue.size() >= 2) {
            EntityPlayerMP player1 = queue.poll();
            EntityPlayerMP player2 = queue.poll();

            if (player1 == null || player2 == null) return;

            // Lógica de iniciar a batalha imediatamente
            try {
                PlayerPartyStorage party1 = Pixelmon.storageManager.getParty(player1);
                PlayerPartyStorage party2 = Pixelmon.storageManager.getParty(player2);
                Pokemon pokemon1 = party1.get(0);
                Pokemon pokemon2 = party2.get(0);

                if (pokemon1 == null || pokemon1.isEgg() || pokemon1.getHealth() <= 0 ||
                        pokemon2 == null || pokemon2.isEgg() || pokemon2.getHealth() <= 0) {
                    player1.sendMessage(new TextComponentString(TextFormatting.RED + "Não foi possível iniciar a batalha. Verifique seu time."));
                    player2.sendMessage(new TextComponentString(TextFormatting.RED + "Não foi possível iniciar a batalha. Verifique seu time."));
                    // Devolve os jogadores para a fila se a batalha falhar
                    if (player1 != null) queue.add(player1);
                    if (player2 != null) queue.add(player2);
                    return;
                }

                EntityPixelmon pkm1 = pokemon1.getOrSpawnPixelmon(player1.getEntityWorld(), player1.posX, player1.posY, player1.posZ);
                EntityPixelmon pkm2 = pokemon2.getOrSpawnPixelmon(player2.getEntityWorld(), player2.posX, player2.posY, player2.posZ);

                PlayerParticipant participant1 = new PlayerParticipant(player1, pkm1);
                PlayerParticipant participant2 = new PlayerParticipant(player2, pkm2);

                BattleRules rules = new BattleRules();
                BattleRegistry.startBattle(new BattleParticipant[]{participant1}, new BattleParticipant[]{participant2}, rules);

            } catch (Exception e) {
                LegendsArena.LOGGER.error("Falha ao iniciar batalha ranqueada entre " + player1.getName() + " e " + player2.getName(), e);
                player1.sendMessage(new TextComponentString(TextFormatting.RED + "Ocorreu um erro inesperado ao iniciar a batalha."));
                player2.sendMessage(new TextComponentString(TextFormatting.RED + "Ocorreu um erro inesperado ao iniciar a batalha."));
            }
        }
    }
}