package com.raishxn.legendsarena.network;

import com.raishxn.legendsarena.legendsarena.Tags;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {

    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Tags.MOD_ID);
    private static int id = 0;

    public static void registerMessages() {
        // Vazio por enquanto. Removemos o registro do S2CTitlePacket.
    }
}