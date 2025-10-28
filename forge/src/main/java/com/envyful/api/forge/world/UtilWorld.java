package com.envyful.api.forge.world;

import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

/**
 *
 * Static utility class for world methods
 *
 */
public class UtilWorld {

    /**
     *
     * Finds a world represented by the given name.
     * Returns null if not found
     *
     * @param name The name of the world to be found
     * @return The world found
     */
    public static World findWorld(String name) {
        for (WorldServer world : FMLCommonHandler.instance().getMinecraftServerInstance().worlds) {
            if (world.getWorldInfo().getWorldName().equalsIgnoreCase(name)) {
                return world;
            }
        }

        return null;
    }

    /**
     *
     * Obtains the name of the world and abstracts the impl away from the platform.
     *
     * @param world The world
     * @return The name of the world
     */
    public static String getName(World world) {
        return world.getWorldInfo().getWorldName();
    }
}
