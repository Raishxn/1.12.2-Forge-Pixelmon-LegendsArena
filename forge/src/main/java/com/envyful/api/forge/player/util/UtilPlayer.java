package com.envyful.api.forge.player.util;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.server.permission.PermissionAPI;

import java.util.UUID;

/**
 *
 * Static utility class for handling getting online & offline players
 *
 */
public class UtilPlayer {

    public static boolean hasPermission(EntityPlayerMP player, String permission) {
        return (PermissionAPI.hasPermission(player, permission) || player.canUseCommand(4, permission) || isOP(player));
    }

    public static boolean isOP(EntityPlayerMP player) {
        return FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getOppedPlayers()
                .getEntry(player.getGameProfile()) != null;
    }

    /**
     *
     * Forces the player to run a command
     *
     * @param player The player running the command
     * @param command The command
     */
    public static void runCommand(EntityPlayerMP player, String command) {
        FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager()
                .executeCommand(player, command);
    }

    /**
     *
     * Gets the online player with the given name.
     * Returns null if not online
     *
     * @param name The name of the player
     * @return The online player
     */
    public static EntityPlayerMP findByName(String name) {
        return FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(name);
    }

    /**
     *
     * Gets the online player with the given {@link UUID}.
     * Returns null if not online
     *
     * @param uuid The uuid of the player
     * @return The online player
     */
    public static EntityPlayerMP getOnlinePlayer(UUID uuid) {
        return FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(uuid);
    }
}
