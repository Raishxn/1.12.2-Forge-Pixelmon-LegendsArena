package com.envyful.api.forge.chat;

import com.google.common.collect.Lists;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentString;

import java.util.List;
import java.util.function.Function;

/**
 *
 * Simple builder for displaying pages to a user
 *
 * @param <T> The type to be paginated
 */
public class PageBuilder<T> {

    private String mainColor = "§e";
    private String offColor = "§e";
    private int pageSize = 5;
    private List<T> values = Lists.newArrayList();
    private Function<T, String> converter = null;

    /**
     *
     * Gets a new instance of the page builder for the given type
     *
     * @param clazz The clazz of the type for easier use
     * @param <A> The type being paginated
     * @return The new page builder
     */
    public static <A> PageBuilder<A> instance(Class<A> clazz) {
        return new PageBuilder<>();
    }

    /**
     *
     * Sets the number of elements on each page
     *
     * @param pageSize The size of the page
     * @return The builder
     */
    public PageBuilder<T> pageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    /**
     *
     * Adds values to the list of all values in the pages
     *
     * @param values The values added
     * @return The builder
     */
    public PageBuilder<T> values(List<T> values) {
        this.values.addAll(values);
        return this;
    }

    /**
     *
     * Sets the converter to change the type into a string for displaying
     *
     * @param converter The converter
     * @return The builder
     */
    public PageBuilder<T> display(Function<T, String> converter) {
        this.converter = converter;
        return this;
    }

    /**
     *
     * Sets the main colour for the formatting
     *
     * @param mainColor The main colour
     * @return The builder
     */
    public PageBuilder<T> mainColor(String mainColor) {
        this.mainColor = mainColor;
        return this;
    }

    /**
     *
     * Sets the off colour for the formatting
     *
     * @param offColor The off colour
     * @return The builder
     */
    public PageBuilder<T> offColor(String offColor) {
        this.offColor = offColor;
        return this;
    }

    /**
     *
     * Sends the given page to the user
     *
     * @param sender The person to send the page to
     * @param page The page to send
     */
    public void send(ICommandSender sender, int page) {
        for (int i = (page * this.pageSize); i < ((page + 1) * this.pageSize); ++i) {
            if (i >= this.values.size()) {
                break;
            }

            sender.sendMessage(new TextComponentString(this.mainColor + (i + 1) + ". " + this.offColor + this.converter.apply(this.values.get(i))));
        }
    }
}
