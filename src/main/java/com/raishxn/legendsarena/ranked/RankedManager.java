package com.raishxn.legendsarena.ranked;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.battles.rules.BattleRules;
import com.pixelmonmod.pixelmon.enums.battle.EnumBattleType;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import com.raishxn.legendsarena.LegendsArena;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import java.util.List;
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
            player.sendMessage(new TextComponentString(TextFormatting.RED + "Voce ja esta em uma batalha."));
            return;
        }
        if (this.queue.contains(player)) {
            player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Voce ja esta na fila."));
            return;
        }

        PlayerPartyStorage storage = Pixelmon.storageManager.getParty(player);
        List<Pokemon> team = storage.getTeam();

        // --- CORREÇÃO AQUI ---
        // Em vez de p.isFainted(), verificamos se a vida (HP) é maior que 0
        long ablePokemon = team.stream().filter(p -> p != null && p.getHealth() > 0 && !p.isEgg()).count();

        if (team.size() != 6 || ablePokemon != 6) {
            player.sendMessage(new TextComponentString(TextFormatting.RED + "Voce precisa de uma equipa completa de 6 Pokemon saudaveis para entrar na fila ranqueada."));
            return;
        }

        this.queue.add(player);
        player.sendMessage(new TextComponentString(TextFormatting.GREEN + "Voce entrou na fila ranqueada. Posicao: " + this.queue.size() + "/2"));
        LegendsArena.LOGGER.info("[RANKED] {} entrou na fila. Tamanho da fila: {}", player.getName(), this.queue.size());

        if (this.queue.size() >= 2) {
            startMatch();
        }
    }

    public void removePlayerFromQueue(EntityPlayerMP player) {
        if (this.queue.remove(player)) {
            player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Voce saiu da fila ranqueada."));
            LegendsArena.LOGGER.info("[RANKED] {} saiu da fila.", player.getName());
        }
    }

    private void startMatch() {
        EntityPlayerMP player1 = this.queue.poll();
        EntityPlayerMP player2 = this.queue.poll();

        if (player1 == null || player2 == null) {
            if (player1 != null) this.queue.add(player1);
            if (player2 != null) this.queue.add(player2);
            return;
        }

        try {
            player1.sendMessage(new TextComponentString(TextFormatting.GOLD + "Partida ranqueada encontrada contra " + player2.getName() + "!"));
            player2.sendMessage(new TextComponentString(TextFormatting.GOLD + "Partida ranqueada encontrada contra " + player1.getName() + "!"));

            PlayerPartyStorage storage1 = Pixelmon.storageManager.getParty(player1);
            PlayerPartyStorage storage2 = Pixelmon.storageManager.getParty(player2);

            List<Pokemon> team1 = storage1.getTeam();
            List<Pokemon> team2 = storage2.getTeam();

            PlayerParticipant participant1 = new PlayerParticipant(player1, team1, 1);
            PlayerParticipant participant2 = new PlayerParticipant(player2, team2, 1);

            BattleParticipant[] teamParticipants1 = {participant1};
            BattleParticipant[] teamParticipants2 = {participant2};

            BattleRules rules = new BattleRules();
            rules.battleType = EnumBattleType.Single;
            rules.numPokemon = 1;

            if (LegendsArena.getEventHandler() != null) {
                LegendsArena.getEventHandler().markPlayerInRankedBattle(player1);
                LegendsArena.getEventHandler().markPlayerInRankedBattle(player2);
            }

            BattleRegistry.startBattle(teamParticipants1, teamParticipants2, rules);

            LegendsArena.LOGGER.info("[RANKED] Batalha ranqueada iniciada entre {} e {}", player1.getName(), player2.getName());

        } catch (Exception e) {
            LegendsArena.LOGGER.error("Erro ao iniciar batalha ranqueada", e);
            TextComponentString errorMessage = new TextComponentString(TextFormatting.RED + "Ocorreu um erro ao iniciar a batalha.");
            if (player1 != null) {
                player1.sendMessage(errorMessage);
                this.queue.add(player1);
            }
            if (player2 != null) {
                player2.sendMessage(errorMessage);
                this.queue.add(player2);
            }
        }
    }
}