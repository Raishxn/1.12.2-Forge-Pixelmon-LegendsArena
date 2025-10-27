package com.raishxn.legendsarena.capabilities;

import com.raishxn.legendsarena.data.IPlayerData;
import net.minecraft.nbt.NBTBase; // Importação alterada
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class PlayerStorage implements Capability.IStorage<IPlayerData> {
    @Nullable
    @Override
    public NBTBase writeNBT(Capability<IPlayerData> capability, IPlayerData instance, EnumFacing side) {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("elo", instance.getElo());
        compound.setInteger("wins", instance.getWins());
        compound.setInteger("losses", instance.getLosses());
        return compound;
    }

    @Override
    // A mudança principal está aqui: trocamos NBTTagCompound por NBTBase
    public void readNBT(Capability<IPlayerData> capability, IPlayerData instance, EnumFacing side, NBTBase nbt) {
        // Agora nós verificamos se o NBT recebido é do tipo que esperamos e o convertemos
        if (nbt instanceof NBTTagCompound) {
            NBTTagCompound compound = (NBTTagCompound) nbt;
            instance.setElo(compound.getInteger("elo"));
            instance.setWins(compound.getInteger("wins"));
            instance.setLosses(compound.getInteger("losses"));
        }
    }
}