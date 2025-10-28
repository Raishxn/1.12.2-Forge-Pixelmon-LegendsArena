package com.envyful.api.gui.factory;

import com.envyful.api.gui.Gui;
import com.envyful.api.gui.close.CloseConsumer;
import com.envyful.api.gui.item.Displayable;
import com.envyful.api.gui.pane.Pane;
import com.envyful.api.gui.pane.TickHandler;
import com.envyful.api.gui.pane.type.PagedPane;

/**
 *
 * A static proxy class for an easy way to get new builder instances
 *
 */
public class GuiFactory {

    private static PlatformGuiFactory<?> platformFactory = null;

    /**
     *
     * Gets the platform factory instance
     *
     * @return The platform factory
     */
    public static PlatformGuiFactory<?> getPlatformFactory() {
        return platformFactory;
    }

    /**
     *
     * Sets the platform factory instance (to be done on startup)
     *
     * @param platformFactory The platform factory instance
     */
    public static void setPlatformFactory(PlatformGuiFactory<?> platformFactory) {
        GuiFactory.platformFactory = platformFactory;
    }

    /**
     *
     * Gets a new instance of the playform's displayable with the given item
     *
     * @param t The item provided
     * @param <T> The type for the displayable
     * @return The displayable
     */
    public static <T> Displayable displayable(T t) {
        if (platformFactory == null) {
            throw new RuntimeException("Platform's factory hasn't been set yet!");
        }

        return ((Displayable.Builder<T>) platformFactory.displayableBuilder()).itemStack(t).build();
    }


    /**
     *
     * Gets a new instance of the playform's displayable builder with the given item
     *
     * @param t The item provided
     * @param <T> The type for the displayable
     * @return The builder
     */
    public static <T> Displayable.Builder<T> displayableBuilder(T t) {
        if (platformFactory == null) {
            throw new RuntimeException("Platform's factory hasn't been set yet!");
        }

        return ((Displayable.Builder<T>) platformFactory.displayableBuilder()).itemStack(t);
    }

    /**
     *
     * Gets a new instance of the platform's displayable builder
     *
     * @param unused Used for automatic type detection (So you don't have to do <ItemStack>displayableBuilder [ugly])
     * @return The new displayable builder
     * @param <T> The type for the displayable
     */
    @SuppressWarnings("unchecked")
    public static <T> Displayable.Builder<T> displayableBuilder(Class<T> unused) {
        if (platformFactory == null) {
            throw new RuntimeException("Platform's factory hasn't been set yet!");
        }

        return (Displayable.Builder<T>) platformFactory.displayableBuilder();
    }

    /**
     *
     * Gets a new instance of the platform's pane builder
     *
     * @return The new pane builder
     */
    public static Pane.Builder paneBuilder() {
        if (platformFactory == null) {
            throw new RuntimeException("Platform's factory hasn't been set yet!");
        }

        return platformFactory.paneBuilder();
    }

    /**
     *
     * Gets a new instance of the platform's paged pane builder
     *
     * @return The new paged pane builder
     */
    public static PagedPane.Builder pagedPaneBuilder() {
        if (platformFactory == null) {
            throw new RuntimeException("Platform's factory hasn't been set yet!");
        }

        return platformFactory.pagedPaneBuilder();
    }

    /**
     *
     * Gets a new instance of the platform's GUI builder
     *
     * @return The new GUI builder
     */
    public static Gui.Builder guiBuilder() {
        if (platformFactory == null) {
            throw new RuntimeException("Platform's factory hasn't been set yet!");
        }

        return platformFactory.guiBuilder();
    }

    /**
     *
     * Creates a tick handler builder instance
     *
     * @return The builder
     */
    public static TickHandler.Builder tickBuilder() {
        if (platformFactory == null) {
            throw new RuntimeException("Platform's factory hasn't been set yet!");
        }

        return platformFactory.tickBuilder();
    }

    /**
     *
     * Gets a close consumer builder instance
     *
     * @return The builder instance
     */
    public static CloseConsumer.Builder<?, ?> closeConsumerBuilder() {
        if (platformFactory == null) {
            throw new RuntimeException("Platform's factory hasn't been set yet!");
        }

        return platformFactory.closeConsumerBuilder();
    }
}
