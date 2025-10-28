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
import com.raishxn.legendsarena.config.tier.TierConfig;
import com.raishxn.legendsarena.database.PunishmentDAO;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Timer;
import java.util.TimerTask;

public class RankedManager {
    private static final RankedManager INSTANCE = new RankedManager();
    private final Map<String, Queue<EntityPlayerMP>> rankedQueues = new HashMap<>();

    private RankedManager() {
        List<String> tiers = LegendsArena.getConfigManager().getTiers();
        for (String tier : tiers) {
            rankedQueues.put(tier.toLowerCase(), new ConcurrentLinkedQueue<>());
            LegendsArena.LOGGER.info("[RankedManager] Fila para o tier '{}' inicializada.", tier);
        }
    }

    public static RankedManager getInstance() {
        return INSTANCE;
    }

    // --- MÉTODO AUXILIAR PARA NORMALIZAR NOMES ---
    // Remove caracteres que não sejam letras/números e converte para minúsculas
    private String normalizeName(String name) {
        if (name == null) return "";
        return name.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
    }
    // ----------------------------------------------------

    public void addPlayerToQueue(EntityPlayerMP player, String tier) {
        String tierLower = tier.toLowerCase();

        String banReason = PunishmentDAO.getActiveBanReason(player, tierLower);
        if (banReason != null) {
            player.sendMessage(new TextComponentString(TextFormatting.RED + "Voce esta banido de entrar nesta fila."));
            player.sendMessage(new TextComponentString(TextFormatting.RED + "Motivo: " + banReason));
            return;
        }

        Queue<EntityPlayerMP> queue = rankedQueues.get(tierLower);

        if (queue == null) {
            player.sendMessage(new TextComponentString(TextFormatting.RED + "O tier '" + tier + "' nao existe."));
            return;
        }
        if (BattleRegistry.getBattle(player) != null) {
            player.sendMessage(new TextComponentString(TextFormatting.RED + "Voce ja esta em uma batalha."));
            return;
        }
        for (Queue<EntityPlayerMP> q : rankedQueues.values()) {
            if (q.contains(player)) {
                player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Voce ja esta em uma fila."));
                return;
            }
        }

        TierConfig tierConfig = LegendsArena.getConfigManager().getTierConfig(tierLower);
        if (tierConfig == null) {
            player.sendMessage(new TextComponentString(TextFormatting.RED + "Erro interno: Nao foi possivel encontrar as regras para o tier " + tier + "."));
            return;
        }

        PlayerPartyStorage storage = Pixelmon.storageManager.getParty(player);
        List<Pokemon> team = storage.getTeam();

        long ablePokemon = team.stream().filter(p -> p != null && p.getHealth() > 0 && !p.isEgg()).count();
        if (team.size() != 6 || ablePokemon != 6) {
            player.sendMessage(new TextComponentString(TextFormatting.RED + "Voce precisa de uma equipa completa de 6 Pokemon saudaveis para entrar na fila ranqueada."));
            return;
        }

        // --- VALIDAÇÃO COMPLETA DE REGRAS ---
        for (Pokemon pokemon : team) {
            String pkmnDisplayName = pokemon.getDisplayName();

            // 1. Nível
            if (pokemon.getLevel() > tierConfig.getLevelCap()) {
                player.sendMessage(new TextComponentString(TextFormatting.RED + "Equipa invalida! O Pokemon " + pkmnDisplayName + " (Nivel " + pokemon.getLevel() + ") excede o limite de nivel de " + tierConfig.getLevelCap() + "."));
                return;
            }

            // 2. Pokémon Banido
            String normalizedPokemonName = normalizeName(pokemon.getSpecies().getPokemonName());
            for (String bannedPokemonName : tierConfig.getBannedPokemon()) {
                if (normalizedPokemonName.equals(normalizeName(bannedPokemonName))) {
                    player.sendMessage(new TextComponentString(TextFormatting.RED + "Equipa invalida! O Pokemon " + pkmnDisplayName + " esta banido neste formato."));
                    return;
                }
            }

            // 3. Habilidade Banida
            String normalizedAbilityName = normalizeName(pokemon.getAbility().getLocalizedName());
            for (String bannedAbilityName : tierConfig.getBannedAbilities()) {
                if (normalizedAbilityName.equals(normalizeName(bannedAbilityName))) {
                    player.sendMessage(new TextComponentString(TextFormatting.RED + "Equipa invalida! O Pokemon " + pkmnDisplayName + " tem a habilidade banida '" + pokemon.getAbility().getLocalizedName() + "'."));
                    return;
                }
            }

            // 4. Item Banido
            ItemStack heldItem = pokemon.getHeldItem();
            if (!heldItem.isEmpty()) {
                String normalizedItemName = normalizeName(heldItem.getDisplayName());
                for (String bannedItemName : tierConfig.getBannedHeldItems()) {
                    if (normalizedItemName.equals(normalizeName(bannedItemName))) {
                        player.sendMessage(new TextComponentString(TextFormatting.RED + "Equipa invalida! O Pokemon " + pkmnDisplayName + " esta a segurar um item banido: '" + heldItem.getDisplayName() + "'."));
                        return;
                    }
                }
            }

            // 5. Ataque Banido
            for (String bannedMoveName : tierConfig.getBannedMoves()) {
                String normalizedBannedMove = normalizeName(bannedMoveName);
                for (int i = 0; i < pokemon.getMoveset().size(); i++) {
                    com.pixelmonmod.pixelmon.battles.attacks.Attack attack = pokemon.getMoveset().get(i);
                    if (attack != null) {
                        String normalizedMoveName = normalizeName(attack.getActualMove().getLocalizedName());
                        if (normalizedMoveName.equals(normalizedBannedMove)) {
                            player.sendMessage(new TextComponentString(TextFormatting.RED + "Equipa invalida! O Pokemon " + pkmnDisplayName + " tem o ataque banido '" + attack.getActualMove().getLocalizedName() + "'."));
                            return;
                        }
                    }
                }
            }

            // 6. FORMA BANIDA
            if (pokemon.getForm() > 0 && pokemon.getFormEnum() != null) {
                String formIdentifier = pokemon.getFormEnum().toString();
                String normalizedFormIdentifier = normalizeName(formIdentifier);

                for (String bannedFormName : tierConfig.getBannedForms()) {
                    String normalizedBannedName = normalizeName(bannedFormName);

                    if (normalizedFormIdentifier.equals(normalizedBannedName)) {
                        player.sendMessage(new TextComponentString(TextFormatting.RED + "Equipa invalida! O Pokemon " + pkmnDisplayName + " está usando a forma banida: " + formIdentifier + "."));
                        return;
                    }

                    String fullPokemonFormName = pokemon.getSpecies().name + "-" + formIdentifier;
                    if (normalizeName(fullPokemonFormName).equals(normalizedBannedName)) {
                        player.sendMessage(new TextComponentString(TextFormatting.RED + "Equipa invalida! O Pokemon " + pkmnDisplayName + " está usando a forma banida: " + fullPokemonFormName + "."));
                        return;
                    }
                }
            }
        }
        // --- FIM DA VALIDAÇÃO ---

        queue.add(player);
        player.sendMessage(new TextComponentString(TextFormatting.GREEN + "Voce entrou na fila do tier " + tierUpper(tier) + ". Posicao: " + queue.size() + "/2"));
        LegendsArena.LOGGER.info("[RANKED] {} entrou na fila do tier {}. Tamanho da fila: {}", player.getName(), tierLower, queue.size());

        if (queue.size() >= 2) {
            startMatch(tierLower);
        }
    }

    public void removePlayerFromQueue(EntityPlayerMP player) {
        for (Map.Entry<String, Queue<EntityPlayerMP>> entry : rankedQueues.entrySet()) {
            if (entry.getValue().remove(player)) {
                player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Voce saiu da fila do tier " + tierUpper(entry.getKey()) + "."));
                LegendsArena.LOGGER.info("[RANKED] {} saiu da fila do tier {}.", player.getName(), entry.getKey());
                return;
            }
        }
    }

    private void startMatch(String tier) {
        Queue<EntityPlayerMP> queue = rankedQueues.get(tier);
        if (queue == null) return;

        EntityPlayerMP player1 = queue.poll();
        EntityPlayerMP player2 = queue.poll();

        if (player1 == null || player2 == null) {
            if (player1 != null) queue.add(player1);
            if (player2 != null) queue.add(player2);
            return;
        }

        // --- INÍCIO DA LÓGICA DE CONTADOR ---
        player1.sendMessage(new TextComponentString(TextFormatting.GOLD + "Partida ("+tierUpper(tier)+") encontrada contra " + player2.getName() + "! Preparando a batalha..."));
        player2.sendMessage(new TextComponentString(TextFormatting.GOLD + "Partida ("+tierUpper(tier)+") encontrada contra " + player1.getName() + "! Preparando a batalha..."));

        Timer timer = new Timer();
        final int[] countdown = {5};

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (countdown[0] > 0) {
                    TextComponentString msg = new TextComponentString(TextFormatting.YELLOW + "A batalha comeca em: " + TextFormatting.AQUA + countdown[0] + TextFormatting.YELLOW + " segundos!");
                    player1.sendMessage(msg);
                    player2.sendMessage(msg);
                    countdown[0]--;
                } else {
                    timer.cancel();

                    // --- INÍCIO DA BATALHA REAL (Executado no final do timer) ---
                    try {
                        PlayerPartyStorage storage1 = Pixelmon.storageManager.getParty(player1);
                        PlayerPartyStorage storage2 = Pixelmon.storageManager.getParty(player2);

                        List<Pokemon> team1 = storage1.getTeam();
                        List<Pokemon> team2 = storage2.getTeam();

                        // REVERSÃO PARA O BACKUP (COM numControlledPokemon = 1)
                        // Confiamos que esta configuração funciona para seleção inicial em seu ambiente.
                        PlayerParticipant participant1 = new PlayerParticipant(player1, team1, 1);
                        PlayerParticipant participant2 = new PlayerParticipant(player2, team2, 1);

                        BattleParticipant[] teamParticipants1 = {participant1};
                        BattleParticipant[] teamParticipants2 = {participant2};

                        BattleRules rules = new BattleRules();
                        rules.battleType = EnumBattleType.Single;
                        rules.numPokemon = 1; // Batalha 1v1

                        if (LegendsArena.getEventHandler() != null) {
                            LegendsArena.getEventHandler().markPlayerInRankedBattle(player1, tier);
                            LegendsArena.getEventHandler().markPlayerInRankedBattle(player2, tier);
                        }

                        // Dispara o início da batalha
                        BattleRegistry.startBattle(teamParticipants1, teamParticipants2, rules);

                        // NOTA: Seleção forçada (getNextPokemon) removida para seguir a sua lógica.

                        LegendsArena.LOGGER.info("[RANKED] Batalha ranqueada ({}) iniciada entre {} e {}", tier, player1.getName(), player2.getName());

                    } catch (Exception e) {
                        LegendsArena.LOGGER.error("Erro ao iniciar batalha ranqueada no tier " + tier, e);
                        TextComponentString errorMessage = new TextComponentString(TextFormatting.RED + "Ocorreu um erro ao iniciar a batalha.");
                        if (player1 != null) {
                            player1.sendMessage(errorMessage);
                            queue.add(player1);
                        }
                        if (player2 != null) {
                            player2.sendMessage(errorMessage);
                            queue.add(player2);
                        }
                    }
                }
            }
        }, 0, 1000);
    }

    private String tierUpper(String tier) {
        return tier.toUpperCase();
    }
}