package com.raishxn.legendsarena.network;

import com.raishxn.legendsarena.legendsarena.Tags;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side; // ADICIONE ESTE IMPORT

public class PacketHandler {

    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Tags.MOD_ID);

    public static void registerMessages() {
        // Este método agora registra o nosso pacote.
        int id = 0;
        // Registra o nosso packet, diz qual classe o processa (Handler), e diz que ele é enviado para o CLIENTE (Side.CLIENT)
        INSTANCE.registerMessage(PacketSyncPlayerData.Handler.class, PacketSyncPlayerData.class, id++, Side.CLIENT);
    }
}