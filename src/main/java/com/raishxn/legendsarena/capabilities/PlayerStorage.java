package com.raishxn.legendsarena.capabilities;

import com.raishxn.legendsarena.data.IPlayerData;
import com.raishxn.legendsarena.data.IPlayerData.PlayerTierStats;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class PlayerStorage implements Capability.IStorage<IPlayerData> {

    @Nullable
    @Override
    public NBTBase writeNBT(Capability<IPlayerData> capability, IPlayerData instance, EnumFacing side) {
        // Cria o NBT principal
        NBTTagCompound mainCompound = new NBTTagCompound();
        // Cria um NBT para guardar todos os tiers
        NBTTagCompound tiersCompound = new NBTTagCompound();

        // Percorre o mapa de estatísticas do jogador
        for (Map.Entry<String, PlayerTierStats> entry : instance.getAllTierStats().entrySet()) {
            String tierName = entry.getKey();
            PlayerTierStats stats = entry.getValue();

            // Para cada tier, cria um NBT com as suas estatísticas
            NBTTagCompound statsCompound = new NBTTagCompound();
            statsCompound.setInteger("elo", stats.getElo());
            statsCompound.setInteger("wins", stats.getWins());
            statsCompound.setInteger("losses", stats.getLosses());

            // Adiciona o NBT do tier ao NBT de tiers, usando o nome do tier como chave
            tiersCompound.setTag(tierName, statsCompound);
        }

        // Adiciona o NBT de tiers ao NBT principal
        mainCompound.setTag("rankedTiers", tiersCompound);
        return mainCompound;
    }

    @Override
    public void readNBT(Capability<IPlayerData> capability, IPlayerData instance, EnumFacing side, NBTBase nbt) {
        if (!(nbt instanceof NBTTagCompound)) {
            return;
        }

        NBTTagCompound mainCompound = (NBTTagCompound) nbt;
        // Verifica se existe a nossa tag de tiers
        if (mainCompound.hasKey("rankedTiers")) {
            NBTTagCompound tiersCompound = mainCompound.getCompoundTag("rankedTiers");
            Map<String, PlayerTierStats> allStats = new HashMap<>();

            // Percorre todas as chaves (nomes dos tiers) no NBT de tiers
            for (String tierName : tiersCompound.getKeySet()) {
                NBTTagCompound statsCompound = tiersCompound.getCompoundTag(tierName);

                // Lê as estatísticas de cada tier
                int elo = statsCompound.getInteger("elo");
                int wins = statsCompound.getInteger("wins");
                int losses = statsCompound.getInteger("losses");

                // Cria o objeto de estatísticas e adiciona ao nosso mapa
                allStats.put(tierName, new PlayerTierStats(elo, wins, losses));
            }

            // Define o mapa completo de estatísticas na capability do jogador
            instance.setAllTierStats(allStats);
        }
    }
}