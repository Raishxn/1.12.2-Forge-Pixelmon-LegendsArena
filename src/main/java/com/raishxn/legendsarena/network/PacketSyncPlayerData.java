package com.raishxn.legendsarena.network;

import com.raishxn.legendsarena.capabilities.PlayerDataManager;
import com.raishxn.legendsarena.data.IPlayerData;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketSyncPlayerData implements IMessage {

    private int elo;
    private int wins;
    private int losses;

    // Construtor vazio obrigatório para o Forge
    public PacketSyncPlayerData() {}

    // Construtor para facilitar a criação do pacote com os dados
    public PacketSyncPlayerData(IPlayerData data) {
        this.elo = data.getElo();
        this.wins = data.getWins();
        this.losses = data.getLosses();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.elo = buf.readInt();
        this.wins = buf.readInt();
        this.losses = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.elo);
        buf.writeInt(this.wins);
        buf.writeInt(this.losses); // <--- estava faltando
    }

    // A classe Handler agora é segura para o servidor
    public static class Handler implements IMessageHandler<PacketSyncPlayerData, IMessage> {
        @Override
        public IMessage onMessage(PacketSyncPlayerData message, MessageContext ctx) {
            // Este código é executado na thread de rede.
            // Agendamos uma tarefa para ser executada na thread principal do jogo para evitar problemas de concorrência.
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        // Este método auxiliar é chamado na thread principal.
        private void handle(PacketSyncPlayerData message, MessageContext ctx) {
            // Verificamos explicitamente se estamos no cliente antes de chamar o código de cliente.
            if (ctx.side == Side.CLIENT) {
                handleClientSide(message);
            }
        }

        /**
         * Este método contém TODO o código que só pode ser executado no cliente.
         * A anotação @SideOnly(Side.CLIENT) faz com que o Forge remova completamente este método
         * da versão do mod que corre no servidor, evitando assim o crash.
         */
        @SideOnly(Side.CLIENT)
        private void handleClientSide(PacketSyncPlayerData message) {
            EntityPlayer player = Minecraft.getMinecraft().player;
            if (player != null) {
                IPlayerData data = player.getCapability(PlayerDataManager.PLAYER_DATA_CAPABILITY, null);
                if (data != null) {
                    // Atualiza os dados do lado do cliente com os valores recebidos do servidor
                    data.setElo(message.elo);
                    data.setWins(message.wins);
                    data.setLosses(message.losses);
                }
            }
        }
    }
}