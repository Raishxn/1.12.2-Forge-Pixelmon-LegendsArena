package com.envyful.api.gui.pane.type;

import com.envyful.api.gui.pane.Pane;

/**
 *
 * The paged pane represents a series of panes that follow a default format, and display a series of items in a paged
 * format
 *
 */
public interface PagedPane extends Pane {

    /**
     *
     * Gets the default format pane for all pages
     *
     * @return The default format
     */
    Pane getDefaultPane();

    /**
     *
     * Gets the pane at the specified page.
     * Will return the default pane if no items exist for this page
     *
     * @param page The page number (starting from 0)
     * @return The page at that number
     */
    Pane getPane(int page);

    /**
     *
     * Sets the pane for the specified page number
     * Will overwrite any that already exist
     *
     * @param page The page number to set
     * @param pane The pane to set for that page
     */
    void setPane(int page, Pane pane);

    /**
     *
     * PagedPane builder interface
     *
     */
    interface Builder extends Pane.Builder {

        /**
         *
         * Sets the next page's pane
         *
         * @param pane The next page
         * @return The builder
         */
        PagedPane.Builder setNextPage(Pane pane);

        /**
         *
         * Sets the previous page's pane
         *
         * @param pane The previous page
         * @return The builder
         */
        PagedPane.Builder setPreviousPage(Pane pane);

        /**
         *
         * Builds the pane on the specifications provided
         *
         * @return The new pane
         */
        @Override
        PagedPane build();
    }
}
