package com.raishxn.legendsarena.capabilities;

import com.raishxn.legendsarena.data.IPlayerData;
import net.minecraft.nbt.NBTBase;
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
    public void readNBT(Capability<IPlayerData> capability, IPlayerData instance, EnumFacing side, NBTBase nbt) {
        // A correção está aqui.
        if (nbt instanceof NBTTagCompound) {
            NBTTagCompound compound = (NBTTagCompound) nbt;

            // Apenas tentamos ler um valor SE ele existir no arquivo salvo.
            if (compound.hasKey("elo")) {
                instance.setElo(compound.getInteger("elo"));
            }
            if (compound.hasKey("wins")) {
                instance.setWins(compound.getInteger("wins"));
            }
            if (compound.hasKey("losses")) {
                instance.setLosses(compound.getInteger("losses"));
            }
        }
    }
}