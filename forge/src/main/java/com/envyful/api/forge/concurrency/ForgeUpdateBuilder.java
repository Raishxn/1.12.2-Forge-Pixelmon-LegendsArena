package com.envyful.api.forge.concurrency;

import com.envyful.api.concurrency.UpdateBuilder;
import com.envyful.api.forge.listener.LazyListener;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

/**
 *
 * Forge Implementation of the {@link UpdateBuilder} class
 *
 */
public class ForgeUpdateBuilder extends UpdateBuilder<EntityPlayerMP> {

    private static final BiPredicate<EntityPlayerMP, String> PERM_PREDICATE = (entityPlayerMP, s) -> entityPlayerMP.canUseCommand(4, s);
    private static final BiConsumer<EntityPlayerMP, String> MESSAGE_CONSUMER = (entityPlayerMP, s) -> entityPlayerMP.sendMessage(new TextComponentString(s));

    public static ForgeUpdateBuilder instance() {
        return new ForgeUpdateBuilder();
    }

    private ForgeUpdateBuilder() {}

    @Override
    public void start() {
        super.start();

        new JoinListener();
    }

    private final class JoinListener extends LazyListener {

        @SubscribeEvent
        public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
            if (!ForgeUpdateBuilder.this.upToDate) {
                ForgeUpdateBuilder.this.attemptSendMessage((EntityPlayerMP) event.player, PERM_PREDICATE, MESSAGE_CONSUMER);
            }
        }
    }
}
