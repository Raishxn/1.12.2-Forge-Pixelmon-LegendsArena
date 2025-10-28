package com.raishxn.legendsarena.events;

import com.pixelmonmod.pixelmon.api.events.battles.BattleEndEvent;
import com.pixelmonmod.pixelmon.api.events.battles.TurnEndEvent;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.battles.controller.BattleControllerBase;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.status.StatusBase;
import com.pixelmonmod.pixelmon.battles.status.StatusPersist;
import com.pixelmonmod.pixelmon.battles.status.Weather;
import com.pixelmonmod.pixelmon.enums.battle.BattleResults;
import com.raishxn.legendsarena.LegendsArena;
import com.raishxn.legendsarena.capabilities.PlayerDataManager;
import com.raishxn.legendsarena.config.rank.Rank;
import com.raishxn.legendsarena.data.IPlayerData;
import com.raishxn.legendsarena.data.IPlayerData.PlayerTierStats;
import com.raishxn.legendsarena.data.PlayerDAO;
import com.raishxn.legendsarena.legendsarena.Tags;
import com.raishxn.legendsarena.network.PacketSyncPlayerData;
import com.raishxn.legendsarena.ranked.EloRankManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class EventHandler {
    private final Map<UUID, String> rankedBattlePlayers = new HashMap<>();
    private final Map<BattleControllerBase, Integer> battleTurnTracker = new HashMap<>();

    public void markPlayerInRankedBattle(EntityPlayerMP player, String tier) {
        rankedBattlePlayers.put(player.getUniqueID(), tier.toLowerCase());
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    // NOVO MÉTODO: Tenta acessar Side Conditions usando Reflection
    private List<Object> getSideConditionsFromParticipant(BattleParticipant participant) {
        if (participant == null) return Collections.emptyList();

        try {
            // 1) Tentar métodos públicos do participante que retornem condições diretamente
            for (Method m : participant.getClass().getMethods()) {
                String name = m.getName().toLowerCase();
                if (name.contains("condition") || name.contains("side") || name.contains("status") || name.contains("controller")) {
                    try {
                        if (List.class.isAssignableFrom(m.getReturnType())) {
                            Object res = m.invoke(participant);
                            if (res instanceof List) return (List<Object>) res;
                        } else {
                            Object controller = m.invoke(participant);
                            if (controller != null) {
                                try {
                                    Method gm = controller.getClass().getMethod("getConditions");
                                    Object res = gm.invoke(controller);
                                    if (res instanceof List) return (List<Object>) res;
                                } catch (NoSuchMethodException ignored) {
                                }
                                try {
                                    Field cf = controller.getClass().getDeclaredField("conditions");
                                    cf.setAccessible(true);
                                    Object res = cf.get(controller);
                                    if (res instanceof List) return (List<Object>) res;
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
            }

            // 2) Percorrer campos declarados na hierarquia da classe do participante
            Class<?> clazz = participant.getClass();
            while (clazz != null && clazz != Object.class) {
                for (Field f : clazz.getDeclaredFields()) {
                    String fname = f.getName().toLowerCase();
                    if (fname.contains("side") || fname.contains("condition") || fname.contains("status") || fname.contains("controller")) {
                        f.setAccessible(true);
                        Object val = f.get(participant);
                        if (val == null) continue;
                        if (val instanceof List) return (List<Object>) val;

                        try {
                            Method gm = val.getClass().getMethod("getConditions");
                            Object res = gm.invoke(val);
                            if (res instanceof List) return (List<Object>) res;
                        } catch (Exception ignored) {
                        }

                        try {
                            Field cf = val.getClass().getDeclaredField("conditions");
                            cf.setAccessible(true);
                            Object res = cf.get(val);
                            if (res instanceof List) return (List<Object>) res;
                        } catch (Exception ignored) {
                        }
                    }
                }
                clazz = clazz.getSuperclass();
            }

            // 3) Tentar encontrar um BattleControllerBase referenciado no participante e inspecionar seus lados
            clazz = participant.getClass();
            while (clazz != null && clazz != Object.class) {
                for (Field f : clazz.getDeclaredFields()) {
                    String fname = f.getName().toLowerCase();
                    if (fname.equals("bcb") || fname.equals("bc") || fname.contains("controller")) {
                        f.setAccessible(true);
                        Object bcObj = f.get(participant);
                        if (bcObj instanceof BattleControllerBase) {
                            BattleControllerBase bcb = (BattleControllerBase) bcObj;

                            // campos diretos do controller
                            for (Field bcf : bcb.getClass().getDeclaredFields()) {
                                bcf.setAccessible(true);
                                Object val = bcf.get(bcb);
                                if (val == null) continue;
                                if (val instanceof List) {
                                    List<Object> list = (List<Object>) val;
                                    for (Object item : list) {
                                        if (item == null) continue;
                                        try {
                                            Method gm = item.getClass().getMethod("getConditions");
                                            Object res = gm.invoke(item);
                                            if (res instanceof List) return (List<Object>) res;
                                        } catch (Exception ignored) {
                                        }
                                    }
                                } else {
                                    try {
                                        Method gm = val.getClass().getMethod("getConditions");
                                        Object res = gm.invoke(val);
                                        if (res instanceof List) return (List<Object>) res;
                                    } catch (Exception ignored) {
                                    }
                                }
                            }
                        }
                    }
                }
                clazz = clazz.getSuperclass();
            }

        } catch (IllegalAccessException e) {
            LegendsArena.LOGGER.error("Erro de acesso ilegal ao campo via Reflection:", e);
        } catch (Exception e) {
            LegendsArena.LOGGER.error("Erro ao acessar Side Conditions via Reflection:", e);
        }

        // Se chegamos aqui, não encontramos side conditions; logamos informações de diagnóstico para inspeção em runtime.
        try {
            Class<?> pc = participant.getClass();
            StringBuilder sb = new StringBuilder();
            sb.append("Participant class: ").append(pc.getName()).append("\nFields:\n");
            for (Field f : pc.getDeclaredFields()) sb.append("  ").append(f.getName()).append(": ").append(f.getType().getName()).append("\n");
            sb.append("Methods:\n");
            for (Method m : pc.getDeclaredMethods()) sb.append("  ").append(m.getName()).append("() -> ").append(m.getReturnType().getName()).append("\n");
            LegendsArena.LOGGER.debug(sb.toString());

            // Procurar por um objeto BattleControllerBase referenciado e logar seus campos
            Class<?> clazz = participant.getClass();
            while (clazz != null && clazz != Object.class) {
                for (Field f : clazz.getDeclaredFields()) {
                    f.setAccessible(true);
                    Object val = f.get(participant);
                    if (val instanceof BattleControllerBase) {
                        BattleControllerBase bcb = (BattleControllerBase) val;
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append("Found BattleControllerBase in field: ").append(f.getName()).append(" (class: ").append(bcb.getClass().getName()).append(")\nFields:\n");
                        for (Field bf : bcb.getClass().getDeclaredFields()) sb2.append("  ").append(bf.getName()).append(": ").append(bf.getType().getName()).append("\n");
                        LegendsArena.LOGGER.debug(sb2.toString());
                    }
                }
                clazz = clazz.getSuperclass();
            }
        } catch (Exception e) {
            LegendsArena.LOGGER.error("Erro ao coletar info debug para side conditions:", e);
        }

        LegendsArena.LOGGER.debug("Não foi possível encontrar side conditions para participant: {} (classe: {})", participant, participant.getClass().getName());
        return Collections.emptyList();
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

    // =========================================================================
    // MÉTODO: ATUALIZAÇÃO DE STATUS DA BATALHA A CADA TURNO
    // =========================================================================
    @SubscribeEvent
    public void onTurnEnd(TurnEndEvent event) {
        BattleControllerBase bc = event.bcb;
        battleTurnTracker.put(bc, bc.battleTurn);

        if (bc.participants.size() != 2 || !(bc.participants.get(0) instanceof PlayerParticipant) || !(bc.participants.get(1) instanceof PlayerParticipant)) {
            return;
        }

        PlayerParticipant p1 = (PlayerParticipant) bc.participants.get(0);
        PlayerParticipant p2 = (PlayerParticipant) bc.participants.get(1);

        if (!rankedBattlePlayers.containsKey(p1.player.getUniqueID()) || !rankedBattlePlayers.containsKey(p2.player.getUniqueID())) {
            return;
        }

        sendBattleStatus(p1, p2, bc);
        sendBattleStatus(p2, p1, bc);
    }

    private void sendBattleStatus(PlayerParticipant player, PlayerParticipant opponent, BattleControllerBase bc) {
        EntityPlayerMP sender = player.player;

        Weather weather = bc.globalStatusController.getWeatherIgnoreAbility();
        String weatherName = "Limpo";
        if (weather != null) {
            weatherName = capitalize(weather.type.name().toLowerCase());
        }

        TextComponentString header = new TextComponentString(
                TextFormatting.AQUA + "Informações da Batalha - " +
                        TextFormatting.YELLOW + "Turno: " + bc.battleTurn +
                        TextFormatting.YELLOW + " (Tempo limite de ação: 20s)" +
                        "\n" +
                        TextFormatting.GREEN + "Clima: " + TextFormatting.WHITE + weatherName +
                        "\n"
        );
        sender.sendMessage(header);

        appendParticipantStatus(sender, player, opponent, "Player", bc);
        appendParticipantStatus(sender, opponent, player, "Adversário", bc);

        sender.sendMessage(new TextComponentString(TextFormatting.AQUA + "--------------------------------------"));
    }

    private void appendParticipantStatus(EntityPlayerMP sender, PlayerParticipant target, PlayerParticipant opposite, String label, BattleControllerBase bc) {
        TextComponentString message = new TextComponentString("");

        PixelmonWrapper activePkmn = target.controlledPokemon.get(0);

        // 2.1. Player Name
        message.appendSibling(new TextComponentString(TextFormatting.GOLD + label + ": " + TextFormatting.WHITE + target.player.getName() +
                (label.equals("Player") ? TextFormatting.YELLOW + " (Você)" : "") + "\n"));

        // 2.2. Team Status (Pokémon vivos/mortos)
        List<String> teamList = target.party.getTeam().stream()
                .map(p -> {
                    if (p == null) return "";
                    TextFormatting color = p.getHealth() > 0 ? TextFormatting.GREEN : TextFormatting.RED;
                    return color + p.getSpecies().getLocalizedName();
                })
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        message.appendSibling(new TextComponentString(TextFormatting.GOLD + "Time: " + TextFormatting.WHITE + String.join(", ", teamList) + "\n"));

        // 2.3. Hazards
        List<Object> sideConditions = getSideConditionsFromParticipant(target);
        String hazardsString;

        if (sideConditions.isEmpty()) {
            hazardsString = TextFormatting.GRAY + "Nenhum";
        } else {
            // Presume que cada item na lista tem um campo 'type' que tem um método 'name()'
            hazardsString = sideConditions.stream()
                    .map(sc -> getHazardName(sc))
                    .collect(Collectors.joining(", "));
            hazardsString = TextFormatting.WHITE + hazardsString;
        }

        message.appendSibling(new TextComponentString(TextFormatting.GOLD + "Hazards: " + hazardsString + "\n"));

        // 2.5. Pokémon Ativo, HP e Status
        int currentHP = activePkmn.getHealth();
        int maxHP = activePkmn.getMaxHealth();
        float hpPercentage = (float)currentHP / maxHP * 100.0f;

        StatusPersist status = activePkmn.pokemon.getStatus();
        String statusDisplay = "";

        if (status != null && status.type != null && status.type.isPrimaryStatus()) {
            statusDisplay = TextFormatting.RED + " (" + status.type.name().toUpperCase() + " badly)";
        }

        message.appendSibling(new TextComponentString(TextFormatting.GOLD + "Pokemon Ativo: " +
                activePkmn.getNickname() + TextFormatting.RESET +
                TextFormatting.RED + " L" + activePkmn.pokemon.getLevel() +
                TextFormatting.YELLOW + " HP: " + currentHP + "/" + maxHP + " (" + String.format("%.2f", hpPercentage) + "%%)" +
                TextFormatting.WHITE + " (Ab: " + activePkmn.getBattleAbility().getLocalizedName() + ")" +
                statusDisplay + "\n"
        ));

        // 2.6. Moves Usados (O último movimento que o Pokémon usou)
        String movesUsed = "";
        Attack lastAttack = activePkmn.attack;

        if (lastAttack != null && lastAttack.getActualMove() != null) {
            movesUsed = TextFormatting.WHITE + lastAttack.getActualMove().getLocalizedName();
        } else {
            movesUsed = TextFormatting.GRAY + "Nenhum";
        }

        List<com.pixelmonmod.pixelmon.battles.attacks.Attack> moveset = activePkmn.pokemon.getMoveset();
        String allMovesPP = moveset.stream()
                .filter(a -> a != null)
                .map(a -> (String)String.format("%s (%d/%d)", a.getActualMove().getLocalizedName(), a.pp, a.getMaxPP()))
                .collect(Collectors.joining(", "));

        movesUsed += TextFormatting.GRAY + " - [" + allMovesPP + "]";

        message.appendSibling(new TextComponentString(TextFormatting.GOLD + "Moves Usados: " + movesUsed + "\n"));

        sender.sendMessage(message);
    }

    @SubscribeEvent
    public void onBattleEnd(BattleEndEvent event) {
        PlayerParticipant winnerParticipant = null;
        PlayerParticipant loserParticipant = null;
        BattleControllerBase bc = null;

        try {
            bc = (BattleControllerBase) event.getClass().getField("bcb").get(event);
        } catch (Exception ignored) {
            try {
                bc = (BattleControllerBase) event.getClass().getField("bc").get(event);
            } catch (Exception ignored2) {
            }
        }


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

            int oldWinnerElo = winnerTierStats.getElo();
            int oldLoserElo = loserTierStats.getElo();

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

            // --- MENSAGEM DE RESUMO DA BATALHA ---
            int totalTurns = 0;
            if (bc != null) {
                totalTurns = battleTurnTracker.getOrDefault(bc, 0);
                battleTurnTracker.remove(bc);
            }

            int estimatedSeconds = totalTurns * 20;
            int minutes = estimatedSeconds / 60;
            int seconds = estimatedSeconds % 60;

            String timeString = String.format("%d min %d seg", minutes, seconds);

            winnerPlayer.sendMessage(new TextComponentString(TextFormatting.GRAY + "Resumo: Duracao Aprox: " + timeString + " (" + totalTurns + " Turnos)"));
            loserPlayer.sendMessage(new TextComponentString(TextFormatting.GRAY + "Resumo: Duracao Aprox: " + timeString + " (" + totalTurns + " Turnos)"));
            // --- FIM DA MENSAGEM DE RESUMO ---

            checkRankChange(winnerPlayer, oldWinnerElo, newWinnerElo);
            checkRankChange(loserPlayer, oldLoserElo, newLoserElo);

            rankedBattlePlayers.remove(winnerPlayer.getUniqueID());
            rankedBattlePlayers.remove(loserPlayer.getUniqueID());
        }
    }

    private void checkRankChange(EntityPlayerMP player, int oldElo, int newElo) {
        EloRankManager rankManager = EloRankManager.getInstance();
        Rank oldRank = rankManager.getRankForElo(oldElo);
        Rank newRank = rankManager.getRankForElo(newElo);

        if (newRank == null || newRank.equals(oldRank)) {
            return;
        }

        if (newRank.getMinElo() > (oldRank != null ? oldRank.getMinElo() : -1)) {
            player.sendMessage(new TextComponentString(TextFormatting.AQUA + "============================="));
            player.sendMessage(new TextComponentString(TextFormatting.GREEN + "       PROMOVIDO!"));
            player.sendMessage(new TextComponentString(TextFormatting.WHITE + "Voce alcancou a patente " + newRank.getDisplayPrefix()));
            player.sendMessage(new TextComponentString(TextFormatting.AQUA + "============================="));
        } else {
            player.sendMessage(new TextComponentString(TextFormatting.RED + "Voce foi rebaixado para a patente " + newRank.getDisplayPrefix()));
        }
    }

    // Extrai um nome legível para um hazard/side condition de vários tipos possíveis
    private String getHazardName(Object sc) {
        if (sc == null) return "Unknown Hazard";
        Class<?> c = sc.getClass();
        try {
            // 1) Enumes
            if (c.isEnum()) {
                return capitalize(sc.toString().toLowerCase());
            }

            // 2) Métodos comuns
            for (String methodName : new String[]{"getName", "getId", "getType", "getDisplayName", "getLocalizedName", "name"}) {
                try {
                    Method m = c.getMethod(methodName);
                    Object res = m.invoke(sc);
                    if (res != null) return capitalize(res.toString().toLowerCase());
                } catch (NoSuchMethodException ignored) {
                }
            }

            // 3) Campos comuns
            for (String fieldName : new String[]{"type", "name", "id", "displayName"}) {
                try {
                    Field f = c.getDeclaredField(fieldName);
                    f.setAccessible(true);
                    Object val = f.get(sc);
                    if (val != null) return capitalize(val.toString().toLowerCase());
                } catch (NoSuchFieldException ignored) {
                }
            }

            // 4) Procurar campo 'type' que seja enum com método name()
            try {
                Field typeField = c.getDeclaredField("type");
                typeField.setAccessible(true);
                Object typeObject = typeField.get(sc);
                if (typeObject != null) return capitalize(typeObject.toString().toLowerCase());
            } catch (Exception ignored) {
            }

            // 5) Fallback para toString
            LegendsArena.LOGGER.debug("Sc hazard detected: class={}, toString={}", c.getName(), sc.toString());
            return capitalize(sc.toString().toLowerCase());
        } catch (Exception e) {
            LegendsArena.LOGGER.error("Erro ao extrair nome do hazard para class {}", c.getName(), e);
            return "Unknown Hazard";
        }
    }
}
