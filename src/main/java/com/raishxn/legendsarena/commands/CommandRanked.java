package com.raishxn.legendsarena.commands;

import com.raishxn.legendsarena.capabilities.PlayerDataManager;
import com.raishxn.legendsarena.data.IPlayerData;
import com.raishxn.legendsarena.ranked.RankedManager; // Importação adicionada
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
        return "/ranked <join|leave|status>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0; // Qualquer jogador pode usar
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
                // Lógica agora chama o RankedManager
                RankedManager.getInstance().addPlayerToQueue(player);
                break;
            case "leave":
                // Lógica agora chama o RankedManager
                RankedManager.getInstance().removePlayerFromQueue(player);
                break;
            case "status":
                IPlayerData data = player.getCapability(PlayerDataManager.PLAYER_DATA_CAPABILITY, null);
                if (data != null) {
                    player.sendMessage(new TextComponentString(TextFormatting.AQUA + "--- Status Ranqueado ---"));
                    player.sendMessage(new TextComponentString("ELO: " + data.getElo()));
                    player.sendMessage(new TextComponentString("Vitórias: " + data.getWins()));
                    player.sendMessage(new TextComponentString("Derrotas: " + data.getLosses()));
                } else {
                    player.sendMessage(new TextComponentString(TextFormatting.RED + "Não foi possível carregar seus dados."));
                }
                break;
            default:
                player.sendMessage(new TextComponentString(TextFormatting.RED + getUsage(sender)));
                break;
        }
    }
}