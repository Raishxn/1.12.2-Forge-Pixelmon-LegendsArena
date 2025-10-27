package com.raishxn.legendsarena.commands;

import com.raishxn.legendsarena.LegendsArena;
import com.raishxn.legendsarena.capabilities.PlayerDataManager;
import com.raishxn.legendsarena.config.rank.Rank; // Importar Rank
import com.raishxn.legendsarena.data.IPlayerData;
import com.raishxn.legendsarena.data.IPlayerData.PlayerTierStats;
import com.raishxn.legendsarena.data.PlayerDAO;
import com.raishxn.legendsarena.database.LeaderboardEntry;
import com.raishxn.legendsarena.ranked.EloRankManager; // Importar EloRankManager
import com.raishxn.legendsarena.ranked.RankedManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CommandRanked extends CommandBase {

    @Nonnull
    @Override
    public String getName() {
        return "ranked";
    }

    @Nonnull
    @Override
    public String getUsage(@Nonnull ICommandSender sender) {
        return "/ranked <join <tier>|leave|status|top <tier>>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
        if (!(sender instanceof EntityPlayerMP)) {
            sender.sendMessage(new TextComponentString("Este comando so pode ser usado por jogadores."));
            return;
        }
        EntityPlayerMP player = (EntityPlayerMP) sender;

        if (args.length == 0) {
            player.sendMessage(new TextComponentString(TextFormatting.RED + getUsage(sender)));
            return;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "join":
                // ... (código do join, sem alterações)
                if (args.length < 2) {
                    player.sendMessage(new TextComponentString(TextFormatting.RED + "Uso incorreto. Especifique um tier: /ranked join <tier>"));
                    return;
                }
                String tierJoin = args[1];
                RankedManager.getInstance().addPlayerToQueue(player, tierJoin);
                break;
            case "leave":
                // ... (código do leave, sem alterações)
                RankedManager.getInstance().removePlayerFromQueue(player);
                break;
            case "status":
                // --- LÓGICA DE STATUS ATUALIZADA PARA MOSTRAR A PATENTE ---
                IPlayerData data = player.getCapability(PlayerDataManager.PLAYER_DATA_CAPABILITY, null);
                if (data != null) {
                    Map<String, PlayerTierStats> allStats = data.getAllTierStats();
                    if (allStats.isEmpty()) {
                        player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Voce ainda nao jogou nenhuma partida ranqueada."));
                        return;
                    }

                    player.sendMessage(new TextComponentString(TextFormatting.AQUA + "--- Seu Status Ranqueado ---"));
                    for (Map.Entry<String, PlayerTierStats> entry : allStats.entrySet()) {
                        String tierName = entry.getKey().toUpperCase();
                        PlayerTierStats stats = entry.getValue();

                        // Obter a patente correspondente ao ELO do tier
                        Rank rank = EloRankManager.getInstance().getRankForElo(stats.getElo());
                        String rankDisplay = (rank != null) ? rank.getDisplayPrefix() + TextFormatting.WHITE : TextFormatting.GRAY + "Unranked ";

                        player.sendMessage(new TextComponentString(
                                TextFormatting.GOLD + "[" + tierName + "] " +
                                        TextFormatting.WHITE + "Rank: " + rankDisplay +
                                        TextFormatting.WHITE + "| ELO: " + TextFormatting.YELLOW + stats.getElo() +
                                        TextFormatting.WHITE + " | V: " + TextFormatting.GREEN + stats.getWins() +
                                        TextFormatting.WHITE + " | D: " + TextFormatting.RED + stats.getLosses()
                        ));
                    }
                    player.sendMessage(new TextComponentString(TextFormatting.AQUA + "----------------------------"));

                } else {
                    player.sendMessage(new TextComponentString(TextFormatting.RED + "ERRO: Nao foi possivel carregar seus dados."));
                }
                break;

            case "top":
                // ... (código do top, sem alterações)
                if (args.length < 2) {
                    player.sendMessage(new TextComponentString(TextFormatting.RED + "Uso incorreto. Especifique um tier: /ranked top <tier>"));
                    return;
                }
                String tierTop = args[1];
                if (!LegendsArena.getConfigManager().getTiers().contains(tierTop.toLowerCase())) {
                    player.sendMessage(new TextComponentString(TextFormatting.RED + "O tier '" + tierTop + "' nao existe."));
                    return;
                }
                List<LeaderboardEntry> leaderboard = PlayerDAO.getLeaderboardForTier(tierTop, 10);
                if (leaderboard.isEmpty()) {
                    player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Ainda nao ha jogadores classificados no tier " + tierTop.toUpperCase() + "."));
                    return;
                }
                player.sendMessage(new TextComponentString(TextFormatting.GOLD + "--- Top 10 Ranqueado (" + tierTop.toUpperCase() + ") ---"));
                int rankPos = 1;
                for (LeaderboardEntry entry : leaderboard) {
                    PlayerTierStats stats = entry.getStats();
                    player.sendMessage(new TextComponentString(
                            TextFormatting.WHITE + "" + rankPos + ". " +
                                    TextFormatting.AQUA + entry.getPlayerName() + " " +
                                    TextFormatting.GRAY + "- " +
                                    TextFormatting.YELLOW + stats.getElo() + " ELO " +
                                    TextFormatting.GRAY + "(" + stats.getWins() + "V/" + stats.getLosses() + "D)"
                    ));
                    rankPos++;
                }
                player.sendMessage(new TextComponentString(TextFormatting.GOLD + "------------------------------"));
                break;

            default:
                player.sendMessage(new TextComponentString(TextFormatting.RED + getUsage(sender)));
                break;
        }
    }

    @Nonnull
    @Override
    public List<String> getTabCompletions(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "join", "leave", "status", "top");
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("top"))) {
            List<String> tiers = LegendsArena.getConfigManager().getTiers();
            return getListOfStringsMatchingLastWord(args, tiers);
        }
        return Collections.emptyList();
    }
}