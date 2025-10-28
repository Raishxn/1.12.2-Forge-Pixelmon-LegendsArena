package com.raishxn.legendsarena.commands;

import com.raishxn.legendsarena.LegendsArena;
import com.raishxn.legendsarena.capabilities.PlayerDataManager; // Adicionado para o reset de temporada
import com.raishxn.legendsarena.data.IPlayerData; // Adicionado para o reset de temporada
import com.raishxn.legendsarena.config.tier.TierConfig;
import com.raishxn.legendsarena.data.PlayerDAO;
import com.raishxn.legendsarena.database.PunishmentDAO;
import com.raishxn.legendsarena.network.PacketHandler; // Adicionado para sincronização de rede
import com.raishxn.legendsarena.network.PacketSyncPlayerData; // Adicionado para sincronização de rede
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.player.EntityPlayerMP; // Adicionado para a lista de jogadores online
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CommandRankedAdmin extends CommandBase {

    @Nonnull
    @Override
    public String getName() {
        return "rankedadmin";
    }

    @Nonnull
    @Override
    public String getUsage(@Nonnull ICommandSender sender) {
        return "/rankedadmin <ban|unban|season|reload> ...";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
        if (args.length < 1) {
            throw new CommandException(getUsage(sender));
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "ban":
                handleBan(server, sender, args);
                break;
            case "unban":
                handleUnban(server, sender, args);
                break;
            case "season":
                handleSeason(server, sender, args);
                break;
            case "reload":
                LegendsArena.getConfigManager().loadConfig();
                notifyCommandListener(sender, this, TextFormatting.GREEN + "Configuracao ranqueada recarregada com sucesso!");
                break;
            default:
                throw new CommandException(getUsage(sender));
        }
    }

    private void handleBan(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 3) throw new CommandException("/rankedadmin ban <jogador> <tier|all> [duração] [motivo]");

        String playerName = args[1];
        String tier = args[2].toLowerCase();

        com.mojang.authlib.GameProfile gameProfile = server.getPlayerProfileCache().getGameProfileForUsername(playerName);
        if (gameProfile == null) throw new PlayerNotFoundException("Jogador '" + playerName + "' não encontrado.");

        String playerUuid = gameProfile.getId().toString();
        long durationMillis = (args.length > 3) ? parseDuration(args[3]) : 0;
        String reason = (args.length > 4) ? String.join(" ", Arrays.copyOfRange(args, 4, args.length)) : "Motivo não especificado.";

        PunishmentDAO.addPunishment(playerUuid, tier, reason, durationMillis);
        notifyCommandListener(sender, this, "Jogador " + playerName + " foi banido das ranqueadas no tier '" + tier + "'.");
    }

    private void handleUnban(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 3) throw new CommandException("/rankedadmin unban <jogador> <tier|all>");

        String playerName = args[1];
        String tier = args[2].toLowerCase();

        com.mojang.authlib.GameProfile gameProfile = server.getPlayerProfileCache().getGameProfileForUsername(playerName);
        if (gameProfile == null) throw new PlayerNotFoundException("Jogador '" + playerName + "' não encontrado.");

        String playerUuid = gameProfile.getId().toString();
        PunishmentDAO.removePunishment(playerUuid, tier);
        notifyCommandListener(sender, this, "Removidos os banimentos de " + playerName + " no tier '" + tier + "'.");
    }

    private void handleSeason(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) throw new CommandException("/rankedadmin season <start|end>");

        String seasonSubCommand = args[1].toLowerCase();

        if (args.length < 3) throw new CommandException("Especifique um tier: /rankedadmin season " + seasonSubCommand + " <tier|all>");

        String tierTarget = args[2].toLowerCase();
        List<String> tiersToReset = new ArrayList<>();

        if ("all".equals(tierTarget)) {
            tiersToReset.addAll(LegendsArena.getConfigManager().getTiers());
        } else if (LegendsArena.getConfigManager().getTiers().contains(tierTarget)) {
            tiersToReset.add(tierTarget);
        } else {
            throw new CommandException("O tier '" + tierTarget + "' nao e valido. Use 'all' ou um tier existente.");
        }

        if (tiersToReset.isEmpty()) {
            sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Nenhum tier para processar. Verifique o config.yml."));
            return;
        }

        switch (seasonSubCommand) {
            case "start":
                sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "A iniciar SOFT RESET de ELO para nova temporada..."));

                double resetFactor = LegendsArena.getConfigManager().getEloResetFactor();
                int totalPlayersAffected = 0;

                for (String tier : tiersToReset) {
                    TierConfig tierConfig = LegendsArena.getConfigManager().getTierConfig(tier);
                    if (tierConfig != null) {
                        int startingElo = tierConfig.getStartingElo();
                        // Chama o método atualizado que zera WINS/LOSSES
                        int playersAffected = PlayerDAO.performEloSoftReset(tier, startingElo, resetFactor);
                        totalPlayersAffected += playersAffected;
                        sender.sendMessage(new TextComponentString(TextFormatting.GRAY + " - Tier '" + tier.toUpperCase() + "': " + playersAffected + " jogadores reiniciados (ELO, WINS, LOSSES)."));
                    }
                }

                // --- FIX PARA O ERRO 3: Recarregar e Sincronizar dados para jogadores online ---
                for (EntityPlayerMP onlinePlayer : server.getPlayerList().getPlayers()) {
                    IPlayerData onlineData = onlinePlayer.getCapability(PlayerDataManager.PLAYER_DATA_CAPABILITY, null);
                    if (onlineData != null) {
                        // 1. Força a recarga do dado do DB para a memória do servidor
                        PlayerDAO.loadPlayerData(onlinePlayer, onlineData);

                        // 2. Sincroniza o novo dado com o cliente
                        PacketHandler.INSTANCE.sendTo(new PacketSyncPlayerData(onlineData), onlinePlayer);
                    }
                }
                // --- FIM DO FIX ---

                // Se o reset for em 'all', incrementamos o número da temporada.
                if ("all".equals(tierTarget)) {
                    int currentSeason = LegendsArena.getConfigManager().getCurrentSeason();
                    int newSeason = currentSeason + 1;
                    LegendsArena.getConfigManager().setValue("season-settings", "current-season", newSeason);
                    LegendsArena.getConfigManager().saveConfig();

                    sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "RESET GLOBAL CONCLUIDO. Temporada " + newSeason + " comecou!"));
                } else {
                    sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Reset para o tier " + tierTarget.toUpperCase() + " concluido!"));
                }

                break;
            case "end":
                // --- FIX PARA O ERRO 1: Substitui a exceção por uma mensagem de placeholder ---
                notifyCommandListener(sender, this, TextFormatting.YELLOW + "FIM DE TEMPORADA para o tier '" + tierTarget.toUpperCase() + "' processado. A logica de arquivamento de dados (se for o caso) deve ser adicionada aqui.");
                break;
            default:
                throw new CommandException("Subcomando de temporada desconhecido. Use: start <tier|all> ou end <tier|all>.");
        }
    }

    private long parseDuration(String durationStr) {
        if (durationStr.equalsIgnoreCase("perm")) return 0;
        try {
            char unit = durationStr.charAt(durationStr.length() - 1);
            long value = Long.parseLong(durationStr.substring(0, durationStr.length() - 1));
            switch (Character.toLowerCase(unit)) {
                case 'd': return TimeUnit.DAYS.toMillis(value);
                case 'h': return TimeUnit.HOURS.toMillis(value);
                case 'm': return TimeUnit.MINUTES.toMillis(value);
                case 's': return TimeUnit.SECONDS.toMillis(value);
            }
        } catch (Exception e) {}
        return 0;
    }

    @Nonnull
    @Override
    public List<String> getTabCompletions(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "ban", "unban", "season", "reload");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("season")) {
            return getListOfStringsMatchingLastWord(args, "start", "end");
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("season")) {
            List<String> tiers = new ArrayList<>(LegendsArena.getConfigManager().getTiers());
            tiers.add("all");
            return getListOfStringsMatchingLastWord(args, tiers);
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("ban") || args[0].equalsIgnoreCase("unban")) {
                return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
            }
        }
        if (args.length == 3 && (args[0].equalsIgnoreCase("ban") || args[0].equalsIgnoreCase("unban"))) {
            List<String> tiers = new ArrayList<>(LegendsArena.getConfigManager().getTiers());
            tiers.add("all");
            return getListOfStringsMatchingLastWord(args, tiers);
        }
        return Collections.emptyList();
    }
}