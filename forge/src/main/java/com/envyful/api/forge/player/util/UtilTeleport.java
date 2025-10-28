package com.envyful.api.forge.player.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.fml.common.FMLCommonHandler;

/**
 *
 * Static utility class for teleporting players
 *
 */
public class UtilTeleport {

    private static final PlayerList PLAYER_LIST = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();

    /**
     *
     * Teleports the player to the given position in the world
     *
     * @param player The player
     * @param world The world
     * @param pos The x, y, z coords
     * @param pitch The pitch
     * @param yaw The yaw
     */
    public static void teleportPlayer(EntityPlayerMP player, World world, Vec3d pos, float pitch, float yaw) {
        if (player.getServerWorld().provider.getDimension() != world.provider.getDimension()) {
            PLAYER_LIST.transferPlayerToDimension(player, world.provider.getDimension(), new PlayerTeleporter(new BlockPos(pos)));
        }

        player.connection.setPlayerLocation(pos.x, pos.y, pos.z, yaw, pitch);
    }

    private static class PlayerTeleporter implements ITeleporter {

        private final BlockPos targetPos;

        private PlayerTeleporter(BlockPos targetPos) {
            this.targetPos = targetPos;
        }

        @Override
        public void placeEntity(World world, Entity entity, float yaw) {
            entity.moveToBlockPosAndAngles(targetPos, yaw, entity.rotationPitch);
        }
    }
}
