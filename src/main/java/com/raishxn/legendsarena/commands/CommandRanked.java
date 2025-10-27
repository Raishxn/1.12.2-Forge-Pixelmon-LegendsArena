package com.raishxn.legendsarena.commands;

import com.raishxn.legendsarena.capabilities.PlayerDataManager;
import com.raishxn.legendsarena.data.IPlayerData;
import com.raishxn.legendsarena.ranked.Rank;
import com.raishxn.legendsarena.ranked.RankedManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;

public class CommandRanked extends CommandBase {

    @Nonnull
    @Override
    public String getName() {
        return "ranked";
    }

    @Nonnull
    @Override
    public String getUsage(@Nonnull ICommandSender sender) {
        return "/ranked <join|leave|status|debug>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
        if (!(sender instanceof EntityPlayerMP)) {
            sender.sendMessage(new TextComponentString("Este comando só pode ser usado por jogadores."));
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
                RankedManager.getInstance().addPlayerToQueue(player);
                break;
            case "leave":
                RankedManager.getInstance().removePlayerFromQueue(player);
                break;
            case "status":
                IPlayerData data = player.getCapability(PlayerDataManager.PLAYER_DATA_CAPABILITY, null);
                if (data != null) {
                    Rank playerRank = Rank.getRankFromElo(data.getElo());

                    player.sendMessage(new TextComponentString(TextFormatting.AQUA + "--- Status Ranqueado ---"));
                    player.sendMessage(new TextComponentString("Rank: " + TextFormatting.GOLD + playerRank.getDisplayName()));
                    player.sendMessage(new TextComponentString(TextFormatting.WHITE + "ELO: " + data.getElo()));
                    player.sendMessage(new TextComponentString(TextFormatting.GREEN + "Vitórias: " + data.getWins()));
                    player.sendMessage(new TextComponentString(TextFormatting.RED + "Derrotas: " + data.getLosses()));
                    player.sendMessage(new TextComponentString(TextFormatting.AQUA + "------------------------"));

                } else {
                    player.sendMessage(new TextComponentString(TextFormatting.RED + "ERRO: Não foi possível carregar seus dados."));
                }
                break;
            case "debug":
                IPlayerData debugData = player.getCapability(PlayerDataManager.PLAYER_DATA_CAPABILITY, null);
                if (debugData != null) {
                    player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "=== DEBUG INFO ==="));
                    player.sendMessage(new TextComponentString("Capability: " + (debugData != null ? "PRESENT" : "NULL")));
                    player.sendMessage(new TextComponentString("ELO: " + debugData.getElo()));
                    player.sendMessage(new TextComponentString("Wins: " + debugData.getWins()));
                    player.sendMessage(new TextComponentString("Losses: " + debugData.getLosses()));
                    player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "=================="));
                } else {
                    player.sendMessage(new TextComponentString(TextFormatting.RED + "DEBUG: Capability é NULL"));
                }
                break;
            default:
                player.sendMessage(new TextComponentString(TextFormatting.RED + getUsage(sender)));
                break;
        }
    }
}