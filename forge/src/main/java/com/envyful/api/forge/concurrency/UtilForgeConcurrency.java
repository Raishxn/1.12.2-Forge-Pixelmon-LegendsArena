package com.envyful.api.forge.concurrency;

import com.envyful.api.forge.concurrency.listener.ServerTickListener;
import net.minecraftforge.common.MinecraftForge;

import java.util.function.Predicate;

/**
 *
 * Forge static utility class for handling concurrency methods specific to forge.
 *
 */
public class UtilForgeConcurrency {

    static final ServerTickListener TICK_LISTENER = new ServerTickListener();

    static {
        MinecraftForge.EVENT_BUS.register(TICK_LISTENER);
    }

    /**
     *
     * Passes runnable task to be run on the main minecraft thread
     *
     * @param runnable The runnable to be run on the main thread
     */
    public static void runSync(Runnable runnable) {
        TICK_LISTENER.addTask(runnable);
    }


    /**
     *
     * Passes runnable task to be run on the main minecraft thread {@param delay} ticks later
     *
     * @param runnable The runnable to be run on the main thread
     * @param delay the delay in ticks
     */
    public static void runLater(Runnable runnable, int delay) {
        TICK_LISTENER.addTask(() -> processRunLater(runnable, delay));
    }

    private static void processRunLater(Runnable runnable, int delay) {
        if (delay >= 0) {
            int finalDelay = delay - 1;
            TICK_LISTENER.addTask(() -> processRunLater(runnable, finalDelay));
            return;
        }

        TICK_LISTENER.addTask(runnable);
    }

    /**
     *
     * Executes the runnable task the tick after the predicate returns true
     *
     * @param predicate The predicate to use
     * @param runnable The runnable to execute
     */
    public static void runLaterWhenTrue(Predicate<Runnable> predicate, int delay, Runnable runnable) {
        runLater(() -> attemptRun(predicate, runnable), delay);
    }

    /**
     *
     * Executes the runnable task the tick after the predicate returns true
     *
     * @param predicate The predicate to use
     * @param runnable The runnable to execute
     */
    public static void runWhenTrue(Predicate<Runnable> predicate, Runnable runnable) {
        TICK_LISTENER.addTask(() -> attemptRun(predicate, runnable));
    }

    private static void attemptRun(Predicate<Runnable> predicate, Runnable runnable) {
        if (!predicate.test(runnable)) {
            TICK_LISTENER.addTask(() -> attemptRun(predicate, runnable));
            return;
        }

        TICK_LISTENER.addTask(runnable);
    }
}
