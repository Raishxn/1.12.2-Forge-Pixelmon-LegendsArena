package com.envyful.api.player.save;

/**
 *
 * Used for custom save handling of specific variables
 *
 * @param <A> The initial type
 */
public interface VariableSaveHandler<A> {

    /**
     *
     * Used for converting from {@link A} to String
     *
     * @param t The data being converted
     * @return The converted data
     */
    String convertCasted(A t);

    /**
     *
     * Used for reverting the conversion process from String to {@link A}
     *
     * @param b The data being reverted
     * @return The inverted data
     */
    A invert(String b);

    /**
     *
     * Converts and casts {@param o} to {@link A}
     *
     * @param o The object
     * @return The converted String
     */
    default String convert(Object o) {
        return this.convert((A) o);
    }
}
