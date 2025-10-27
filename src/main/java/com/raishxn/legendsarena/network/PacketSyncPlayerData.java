package com.raishxn.legendsarena.network;

import com.raishxn.legendsarena.capabilities.PlayerDataManager;
import com.raishxn.legendsarena.data.IPlayerData;
import com.raishxn.legendsarena.data.IPlayerData.PlayerTierStats;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;

public class PacketSyncPlayerData implements IMessage {

    // Em vez de campos individuais, agora temos um mapa.
    private Map<String, PlayerTierStats> tierStats;

    // Construtor vazio obrigatório
    public PacketSyncPlayerData() {}

    // Construtor que pega todos os dados do jogador
    public PacketSyncPlayerData(IPlayerData data) {
        this.tierStats = data.getAllTierStats();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        // Para enviar um mapa pela rede, a maneira mais fácil e robusta é convertê-lo para NBT.
        NBTTagCompound mainCompound = new NBTTagCompound();
        NBTTagCompound tiersCompound = new NBTTagCompound();

        for (Map.Entry<String, PlayerTierStats> entry : this.tierStats.entrySet()) {
            NBTTagCompound statsCompound = new NBTTagCompound();
            statsCompound.setInteger("elo", entry.getValue().getElo());
            statsCompound.setInteger("wins", entry.getValue().getWins());
            statsCompound.setInteger("losses", entry.getValue().getLosses());
            tiersCompound.setTag(entry.getKey(), statsCompound);
        }
        mainCompound.setTag("rankedTiers", tiersCompound);

        // A classe ByteBufUtils do Forge facilita a escrita de NBTs para a rede.
        ByteBufUtils.writeTag(buf, mainCompound);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        // Lemos o NBT da rede.
        NBTTagCompound mainCompound = ByteBufUtils.readTag(buf);
        this.tierStats = new HashMap<>();

        if (mainCompound != null && mainCompound.hasKey("rankedTiers")) {
            NBTTagCompound tiersCompound = mainCompound.getCompoundTag("rankedTiers");
            for (String tierName : tiersCompound.getKeySet()) {
                NBTTagCompound statsCompound = tiersCompound.getCompoundTag(tierName);
                int elo = statsCompound.getInteger("elo");
                int wins = statsCompound.getInteger("wins");
                int losses = statsCompound.getInteger("losses");
                this.tierStats.put(tierName, new PlayerTierStats(elo, wins, losses));
            }
        }
    }

    // A classe Handler lida com o que acontece quando o pacote chega ao destino.
    public static class Handler implements IMessageHandler<PacketSyncPlayerData, IMessage> {
        @Override
        public IMessage onMessage(PacketSyncPlayerData message, MessageContext ctx) {
            // Garante que o código é executado na thread principal do Minecraft para evitar problemas.
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketSyncPlayerData message, MessageContext ctx) {
            // O código de cliente só pode ser executado no cliente.
            if (ctx.side == Side.CLIENT) {
                handleClientSide(message);
            }
        }

        @SideOnly(Side.CLIENT)
        private void handleClientSide(PacketSyncPlayerData message) {
            // Quando o cliente recebe o pacote, atualizamos os seus dados.
            EntityPlayer player = Minecraft.getMinecraft().player;
            if (player != null) {
                IPlayerData data = player.getCapability(PlayerDataManager.PLAYER_DATA_CAPABILITY, null);
                if (data != null) {
                    data.setAllTierStats(message.tierStats);
                }
            }
        }
    }
}