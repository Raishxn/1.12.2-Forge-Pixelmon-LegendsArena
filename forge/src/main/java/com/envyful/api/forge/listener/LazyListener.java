package com.envyful.api.forge.listener;

import net.minecraftforge.common.MinecraftForge;

/**
 *
 * Simple abstract class for registering the listener as soon as it's instantiated
 *
 */
public abstract class LazyListener {

    protected LazyListener() {
        MinecraftForge.EVENT_BUS.register(this);
    }
}
