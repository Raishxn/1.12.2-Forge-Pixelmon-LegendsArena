package com.envyful.api.type;

import java.util.Optional;

/**
 *
 * Static utility class for parsing types
 *
 */
public class UtilParse {

    /**
     *
     * Parses the arg to an integer
     *
     * @param arg The arg to parse
     * @return The potential parsed integer
     */
    public static Optional<Integer> parseInteger(String arg) {
        try {
            return Optional.of(Integer.parseInt(arg));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     *
     * Parses the arg to a long
     *
     * @param arg The arg to parse
     * @return The potential parsed long
     */
    public static Optional<Long> parseLong(String arg) {
        try {
            return Optional.of(Long.parseLong(arg));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     *
     * Parses the arg to an double
     *
     * @param arg The arg to parse
     * @return The potential parsed double
     */
    public static Optional<Double> parseDouble(String arg) {
        try {
            return Optional.of(Double.parseDouble(arg));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
