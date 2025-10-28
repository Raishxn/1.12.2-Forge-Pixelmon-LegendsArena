package com.envyful.api.forge.server;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

/**
 *
 * Static utility class for handling forge server function
 *
 */
public class UtilForgeServer {

    private static final MinecraftServer SERVER = FMLCommonHandler.instance().getMinecraftServerInstance();

    /**
     *
     * Executes the given command from the server
     *
     * Ensure to set the server first
     *
     * @param command The command to execyte
     */
    public static void executeCommand(String command) {
        SERVER.getCommandManager().executeCommand(SERVER, command);
    }
}
