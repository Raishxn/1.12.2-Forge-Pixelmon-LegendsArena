package com.envyful.api.config;

import java.util.Collection;

/**
 *
 * Static utility for converting text using a collection of {@link Replacer}s
 *
 */
public class UtilReplacer {

    /**
     *
     * Converts original text into converted text using a {@link Collection} of {@link Replacer}s
     *
     * @param original The original text
     * @param replacers The replacers
     * @return The updated text
     */
    public static String getReplacedText(String original, Collection<Replacer> replacers) {
        return getReplacedText(original, replacers.toArray(new Replacer[0]));
    }

    /**
     *
     * Converts original text into converted text using an array of {@link Replacer}s
     *
     * @param original The original text
     * @param replacers The replacers
     * @return The updated text
     */
    public static String getReplacedText(String original, Replacer... replacers) {
        if (original == null) {
            return original;
        }

        for (Replacer replacer : replacers) {
            original = replacer.replaceText(original);
        }

        return original;
    }
}
