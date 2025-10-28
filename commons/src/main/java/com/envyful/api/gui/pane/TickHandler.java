package com.envyful.api.gui.pane;

import java.util.function.Consumer;

/**
 *
 * A tick handler for a pane
 *
 * This will be attempted to be called every Minecraft tick, and then depending on the repeat and initial delay for the
 * tick handler it will then attempt to update the pane using the given consumer
 *
 */
public interface TickHandler {

    /**
     *
     * Ticks the pane
     *
     * @param pane The pane being ticked
     */
    void tick(Pane pane);

    interface Builder {

        /**
         *
         * Setting the tick to handle async
         *
         * @return The builder
         */
        Builder async();

        /**
         *
         * Setting the tick to handle sync
         *
         * @return The builder
         */
        Builder sync();

        /**
         *
         * Setting the initial delay in ticks (delay before first time the consumer is called)
         *
         * @param ticks The delay in ticks
         * @return The builder
         */
        Builder initialDelay(int ticks);

        /**
         *
         * Setting the repeat delay in ticks (the delay between the times the consumer is called)
         *
         * @param ticks The repeat delay in ticks
         * @return The builder
         */
        Builder repeatDelay(int ticks);

        /**
         *
         * Sets the handler for when the pane is ticked
         *
         * @param tickHandler The handler
         * @return The builder
         */
        Builder handler(Consumer<Pane> tickHandler);

        /**
         *
         * Builds the tick handler
         *
         * @return The new tick handler
         */
        TickHandler build();

    }
}
