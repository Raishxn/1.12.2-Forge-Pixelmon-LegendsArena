package com.envyful.api.gui.close;

import com.envyful.api.player.EnvyPlayer;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 *
 * Represents a close consumer handler
 *
 * @param <A> The envy player type
 * @param <B> The player type
 */
public interface CloseConsumer<A extends EnvyPlayer<B>, B> {

    /**
     *
     * Handles when the inventory is closed
     *
     * @param a The player
     */
    void handle(A a);

    interface Builder<A extends EnvyPlayer<B>, B> {

        /**
         *
         * Sets the delay in ticks of the close consumer
         *
         * @param delayTicks The delay in ticks until execution
         * @return The builder
         */
        Builder<A, B> delayTicks(int delayTicks);

        /**
         *
         * Sets the predicate for the close consumer to execute
         *
         * @param predicate The predicate
         * @return The builder
         */
        Builder<A, B> predicate(Predicate<A> predicate);

        /**
         *
         * The close consumer
         *
         * @param player The player handler
         * @return The builder
         */
        Builder<A, B> handler(Consumer<A> player);

        /**
         *
         * Sets the close consumer to handle the task asynchronously
         *
         * @return The builder
         */
        default Builder<A, B> async() {
            return this.async(true);
        }

        /**
         *
         * Sets if the close consumer should handle the task synchronously, or asyncrhonously
         *
         * @param async If the task is async
         * @return The builder
         */
        Builder<A, B> async(boolean async);

        /**
         *
         * Builds the close consumer
         *
         * @return The close consumer
         */
        CloseConsumer<A, B> build();

    }
}
