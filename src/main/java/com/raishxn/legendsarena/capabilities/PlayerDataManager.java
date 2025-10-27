package com.raishxn.legendsarena.capabilities;

import com.raishxn.legendsarena.data.IPlayerData;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerDataManager implements ICapabilitySerializable<NBTBase> {

    @CapabilityInject(IPlayerData.class)
    public static final Capability<IPlayerData> PLAYER_DATA_CAPABILITY = null;

    private IPlayerData instance;

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == PLAYER_DATA_CAPABILITY;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == PLAYER_DATA_CAPABILITY) {
            if (instance == null) {
                instance = PLAYER_DATA_CAPABILITY.getDefaultInstance();
            }
            return PLAYER_DATA_CAPABILITY.cast(this.instance);
        }
        return null;
    }

    @Override
    public NBTBase serializeNBT() {
        // Garante que a instância não seja nula ao salvar
        if (this.instance == null) {
            this.instance = PLAYER_DATA_CAPABILITY.getDefaultInstance();
        }
        return PLAYER_DATA_CAPABILITY.getStorage().writeNBT(PLAYER_DATA_CAPABILITY, this.instance, null);
    }

    @Override
    public void deserializeNBT(NBTBase nbt) {
        // Garante que a instância não seja nula ao carregar
        if (this.instance == null) {
            this.instance = PLAYER_DATA_CAPABILITY.getDefaultInstance();
        }
        PLAYER_DATA_CAPABILITY.getStorage().readNBT(PLAYER_DATA_CAPABILITY, this.instance, null, nbt);
    }
}