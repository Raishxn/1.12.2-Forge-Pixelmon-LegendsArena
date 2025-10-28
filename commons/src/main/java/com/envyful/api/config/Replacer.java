package com.envyful.api.config;

/**
 *
 * Simple interface for replacing text in a string in an abstract fashion
 *
 */
public interface Replacer {

    /**
     *
     * Updates the text using the replacer logic
     *
     * @param text The original text
     * @return The updated text
     */
    String replaceText(String text);

}
