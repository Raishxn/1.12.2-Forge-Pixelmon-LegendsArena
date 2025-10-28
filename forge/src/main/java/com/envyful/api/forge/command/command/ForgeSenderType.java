package com.envyful.api.forge.command.command;

import com.google.common.collect.Maps;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.Map;

/**
 *
 * Enum for identifying what type of executor a command has from the class of the sender
 *
 */
public enum ForgeSenderType {

    CONSOLE(ICommandSender.class),
    PLAYER(EntityPlayerMP.class, EntityPlayer.class),

    ;

    private static final Map<Class<?>, ForgeSenderType> SENDERS = Maps.newHashMap();

    static {
        for (ForgeSenderType value : values()) {
            for (Class<?> clazz : value.clazz) {
                SENDERS.put(clazz, value);
            }
        }
    }

    private final Class<?>[] clazz;

    ForgeSenderType(Class<?>... clazz) {
        this.clazz = clazz;
    }

    public Class<?> getType() {
        return this.clazz[0];
    }

    public static ForgeSenderType get(Class<?> clazz) {
        return SENDERS.get(clazz);
    }
}
