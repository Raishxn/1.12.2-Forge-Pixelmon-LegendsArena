package com.raishxn.legendsarena.ranked;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.battles.rules.BattleRules;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.battle.EnumBattleType;
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
        if (BattleRegistry.getBattle(player) != null) {
            player.sendMessage(new TextComponentString(TextFormatting.RED + "Você não pode entrar na fila enquanto estiver numa batalha."));
            return;
        }

        if (!this.queue.contains(player)) {
            this.queue.add(player);
            player.sendMessage(new TextComponentString(TextFormatting.GREEN + "Você entrou na fila ranqueada!"));
            player.sendMessage(new TextComponentString(TextFormatting.GRAY + "A procurar por um oponente..."));
        } else {
            player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Você já está na fila."));
        }
    }

    public void removePlayerFromQueue(EntityPlayerMP player) {
        if (this.queue.remove(player)) {
            player.sendMessage(new TextComponentString(TextFormatting.RED + "Você saiu da fila ranqueada."));
        } else {
            player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Você não estava na fila."));
        }
    }

    public void onPlayerLogout(EntityPlayerMP player) {
        this.queue.remove(player);
    }

    public void onServerTick() {
        if (this.queue.size() < 2) {
            return;
        }

        EntityPlayerMP player1 = this.queue.poll();
        EntityPlayerMP player2 = this.queue.poll();

        if (player1 == null || player2 == null) {
            if (player1 != null) this.queue.add(player1);
            if (player2 != null) this.queue.add(player2);
            return;
        }

        player1.sendMessage(new TextComponentString(TextFormatting.GOLD + "Partida encontrada contra " + player2.getName() + "! A preparar a batalha..."));
        player2.sendMessage(new TextComponentString(TextFormatting.GOLD + "Partida encontrada contra " + player1.getName() + "! A preparar a batalha..."));

        startRankedBattle(player1, player2);
    }

    private void startRankedBattle(EntityPlayerMP player1, EntityPlayerMP player2) {
        try {
            PlayerPartyStorage party1 = Pixelmon.storageManager.getParty(player1);
            PlayerPartyStorage party2 = Pixelmon.storageManager.getParty(player2);

            if (party1 == null || party2 == null) {
                LegendsArena.LOGGER.error("Falha ao obter party storage para os jogadores");
                return;
            }

            // Obtém o primeiro Pokémon válido de cada jogador
            Pokemon pokemon1 = party1.get(0);
            Pokemon pokemon2 = party2.get(0);

            if (pokemon1 == null || pokemon1.isEgg() || pokemon1.getHealth() <= 0) {
                player1.sendMessage(new TextComponentString(TextFormatting.RED + "O seu primeiro Pokémon não pode batalhar!"));
                player2.sendMessage(new TextComponentString(TextFormatting.RED + player1.getName() + " não tem um Pokémon válido para batalhar."));
                this.queue.add(player2);
                return;
            }

            if (pokemon2 == null || pokemon2.isEgg() || pokemon2.getHealth() <= 0) {
                player2.sendMessage(new TextComponentString(TextFormatting.RED + "O seu primeiro Pokémon não pode batalhar!"));
                player1.sendMessage(new TextComponentString(TextFormatting.RED + player2.getName() + " não tem um Pokémon válido para batalhar."));
                this.queue.add(player1);
                return;
            }

            // Cria a entidade Pokémon para a batalha
            EntityPixelmon pkm1 = pokemon1.getOrSpawnPixelmon(player1.getEntityWorld(), player1.posX, player1.posY, player1.posZ);
            EntityPixelmon pkm2 = pokemon2.getOrSpawnPixelmon(player2.getEntityWorld(), player2.posX, player2.posY, player2.posZ);

            // Cria os participantes da batalha
            PlayerParticipant participant1 = new PlayerParticipant(player1, pkm1);
            PlayerParticipant participant2 = new PlayerParticipant(player2, pkm2);

            BattleParticipant[] team1 = {participant1};
            BattleParticipant[] team2 = {participant2};

            // Define as regras para uma batalha Single 1v1
            BattleRules rules = new BattleRules();
            rules.battleType = EnumBattleType.Single;
            rules.numPokemon = 1;

            // CORREÇÃO: Usar uma solução alternativa para marcar jogadores em batalhas ranqueadas
            // Em vez de tentar acessar o EventHandler via EventBus, vamos usar um método estático
            // Vamos adicionar um método no LegendsArena para acessar o EventHandler
            if (LegendsArena.getEventHandler() != null) {
                LegendsArena.getEventHandler().markPlayerInRankedBattle(player1);
                LegendsArena.getEventHandler().markPlayerInRankedBattle(player2);
            } else {
                LegendsArena.LOGGER.warn("EventHandler não está disponível para marcar batalha ranqueada");
            }

            // Inicia a batalha
            BattleRegistry.startBattle(team1, team2, rules);

            LegendsArena.LOGGER.info("[RANKED] Batalha ranqueada iniciada entre {} e {}", player1.getName(), player2.getName());

        } catch (Exception e) {
            LegendsArena.LOGGER.error("Erro ao iniciar batalha ranqueada", e);
            TextComponentString errorMessage = new TextComponentString(TextFormatting.RED + "Ocorreu um erro ao iniciar a batalha.");
            player1.sendMessage(errorMessage);
            player2.sendMessage(errorMessage);
            if (player1 != null) this.queue.add(player1);
            if (player2 != null) this.queue.add(player2);
        }
    }
}