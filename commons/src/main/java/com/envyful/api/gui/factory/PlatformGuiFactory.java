package com.envyful.api.gui.factory;

import com.envyful.api.gui.Gui;
import com.envyful.api.gui.close.CloseConsumer;
import com.envyful.api.gui.item.Displayable;
import com.envyful.api.gui.pane.Pane;
import com.envyful.api.gui.pane.TickHandler;
import com.envyful.api.gui.pane.type.PagedPane;

/**
 *
 * Factory interface for handling creating the platform specific builder implementations
 *
 */
public interface PlatformGuiFactory<T> {

    /**
     *
     * Gets the platform's {@link Displayable.Builder<T>} class as a new instance
     *
     * @return The new displayable builder for the given platform
     */
    Displayable.Builder<T> displayableBuilder();

    /**
     *
     * Gets the platform's {@link Pane.Builder} class as a new instance
     *
     * @return The pane builder for the given platform
     */
    Pane.Builder paneBuilder();

    /**
     *
     * Gets the platform's {@link PagedPane.Builder} class as a new instance
     *
     * @return The paged pane builder for the given platform
     */
    PagedPane.Builder pagedPaneBuilder();

    /**
     *
     * Gets the platform's {@link Gui.Builder} class as a new instance
     *
     * @return The gui builder for the given platform
     */
    Gui.Builder guiBuilder();

    /**
     *
     * Creates a tick handler builder instance
     *
     * @return The builder
     */
    TickHandler.Builder tickBuilder();

    /**
     *
     * Gets a close consumer builder instance
     *
     * @return The builder instance
     */
    CloseConsumer.Builder<?, ?> closeConsumerBuilder();

}
