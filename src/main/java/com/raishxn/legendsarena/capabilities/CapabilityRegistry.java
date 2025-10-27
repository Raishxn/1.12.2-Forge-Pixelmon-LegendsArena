package com.raishxn.legendsarena.capabilities;

import com.raishxn.legendsarena.data.IPlayerData;
import com.raishxn.legendsarena.data.PlayerData;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class CapabilityRegistry {

    public static void register() {
        CapabilityManager.INSTANCE.register(IPlayerData.class, new PlayerStorage(), PlayerData::new);
    }
}