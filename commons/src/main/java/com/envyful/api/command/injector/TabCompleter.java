package com.envyful.api.command.injector;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 *
 * An interface for handling when a specific data type needs to have tab completions.
 *
 * @param <A> represents the type of the data which will have tab completions
 * @param <B> represents the player object for the specific platform
 */
public interface TabCompleter<A, B> {

    /**
     *
     * Gets the class of the sender type
     *
     * @return The sender type's class
     */
    Class<B> getSenderClass();

    /**
     *
     * Gets the class that will have tab completions
     *
     * @return The class
     */
    Class<A> getCompletedClass();

    /**
     *
     * Gets the tab completions for the sender with the given data (can be empty)
     *
     * @param sender The sender
     * @param currentData The data already provided by the sender
     * @param completionData The annotation provided on the tab complete
     * @return The tab completions generated
     */
    List<String> getCompletions(B sender, String[] currentData, Annotation... completionData);

}
