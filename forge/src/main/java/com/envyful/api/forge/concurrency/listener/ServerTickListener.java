package com.envyful.api.forge.concurrency.listener;

import com.google.common.collect.Sets;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Iterator;
import java.util.Set;

/**
 *
 * Simple listener class for running tasks on the minecraft thread.
 *
 */
public class ServerTickListener {

    private final Set<Runnable> tasks = Sets.newConcurrentHashSet();

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        Iterator<Runnable> iterator = tasks.iterator();

        while (iterator.hasNext()) {
            Runnable next = iterator.next();
            next.run();
            iterator.remove();
        }
    }

    public void addTask(Runnable runnable) {
        this.tasks.add(runnable);
    }

    public boolean hasTask(Runnable runnable) {
        return tasks.contains(runnable);
    }
}
