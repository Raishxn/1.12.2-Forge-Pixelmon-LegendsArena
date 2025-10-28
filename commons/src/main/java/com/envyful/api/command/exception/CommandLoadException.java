package com.envyful.api.command.exception;

/**
 *
 * Exception thrown when a given command fails to load. For easier debugging to console for developers.
 *
 */
public class CommandLoadException extends RuntimeException {

    /**
     *
     * Allows you to pass the class name of the command that failed to load and the reason.
     * The class name thus allowing for significantly easier debugging.
     *
     * @param className The name of the class that had an erroneous value
     * @param reason The details of the error
     */
    public CommandLoadException(String className, String reason) {
        super("Failed to load command " + className + " for reason: " + reason);
    }
}
