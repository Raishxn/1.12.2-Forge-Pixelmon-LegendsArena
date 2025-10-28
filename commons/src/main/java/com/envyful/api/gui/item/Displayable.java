package com.envyful.api.gui.item;

import com.envyful.api.player.EnvyPlayer;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 *
 * An interface that represents a displayable item in a page / container that can be clicked, and periodically updated.
 *
 */
public interface Displayable {

    /**
     *
     * Called when the displayable entity has been clicked by a player
     *
     * @param player The player that clicked the object
     * @param clickType The type of click that occurred
     */
    void onClick(EnvyPlayer<?> player, ClickType clickType);

    /**
     *
     * Called periodically by the parent page / container to update the item on display (if it's a changing item)
     *
     * @param viewer The player viewing the current page / container for which the update should occur
     */
    void update(EnvyPlayer<?> viewer);

    /**
     *
     * An enum representing the type of click coming from the user
     *
     */
    enum ClickType {

        LEFT,
        SHIFT_LEFT,
        MIDDLE,
        SHIFT_RIGHT,
        RIGHT,

        ;

    }

    /**
     *
     * An interface to build the platform's implementation of the Displayable interface
     *
     * @param <T> The platform's ItemStack class
     */
    interface Builder<T> {

        /**
         *
         * Sets the itemstack for the displayable
         *
         * @param itemStack The item to be displayed
         * @return The builder
         */
        Builder<T> itemStack(T itemStack);

        /**
         *
         * Sets the click handler for the displayable
         *
         * @param clickHandler The consumer for when the displayable is clicked
         * @return The builder
         */
        Builder<T> clickHandler(BiConsumer<EnvyPlayer<?>, ClickType> clickHandler);

        /**
         *
         * Sets the update handler for the displayable
         *
         * @param updateHandler The consumer for when the displayable is updated
         * @return The builder
         */
        Builder<T> updateHandler(Consumer<EnvyPlayer<?>> updateHandler);

        /**
         *
         * Sets the click to be handled asynchronously
         *
         * @return The builder
         */
        default Builder<T> asyncClick() {
            return this.asyncClick(true);
        }

        /**
         *
         * The delay between clicking and the function executing
         *
         * @param tickDelay The delay in ticks
         * @return The builder
         */
        Builder<T> delayTicks(int tickDelay);

        /**
         *
         * Sets the click to be handled asynchronously
         *
         * @return The builder
         */
        Builder<T> asyncClick(boolean async);

        /**
         *
         * Sets the button so it can only be clicked once
         *
         * @return The builder
         */
        default Builder<T> singleClick() {
            return this.singleClick(true);
        }

        /**
         *
         * Sets if the button can only be clicked once
         *
         * @param singleClick True if only once
         * @return The builder
         */
        Builder<T> singleClick(boolean singleClick);

        /**
         *
         * Allowed delay between user's clicks (defaults to 50 millis i.e. 1 tick)
         *
         * @param milliseconds The delay in milliseconds
         * @return The builder
         */
        Builder<T> clickDelay(long milliseconds);

        /**
         *
         * The number of clicks to lock the user out after (defaults to 100)
         *
         * @param clickLockCount The click count to lock the user out after
         * @return The builder
         */
        Builder<T> lockOutClicks(int clickLockCount);

        /**
         *
         * Creates the displayable from the specifications
         *
         * @return The new displayable implementation
         */
        Displayable build();

    }
}
