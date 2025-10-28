package com.envyful.api.command;

import com.envyful.api.command.exception.CommandLoadException;
import com.envyful.api.command.injector.TabCompleter;

import java.util.function.BiFunction;

/**
 *
 * An interface for registering, and unregistering, commands and the injectors for handling those commands.
 *
 * @param <A> The server / command registrar for the platform specific implementation
 * @param <B> The sender type for the platform specific implementation
 */
public interface CommandFactory<A, B> {

    /**
     *
     * Registers the command with the command registrar for that platform. Also handles all reflection methods for
     * determining annotation values, child classes, permissions required, injectors required (etc).
     *
     * @param server The command registrar for that platform
     * @param o The object of the command being registered
     * @return False only if the registry failed
     * @throws CommandLoadException Thrown if something went wrong when loading the command using reflection
     */
    boolean registerCommand(A server, Object o) throws CommandLoadException;

    /**
     *
     * Unregisters the specified command from the platform's command registry
     *
     * @param server The command registrar for the platform
     * @param o The command to be unregistered
     * @return False only if unregistering fails
     */
    boolean unregisterCommand(A server, Object o);

    /**
     *
     * Default method for registering an injector where multiple args defaults to false.
     * By default, uses the {@link CommandFactory#registerInjector(Class, boolean, BiFunction)} method with the
     * multipleArgs flag as false (as this is the most common use-case)
     *
     * @param parentClass The converted class to be registered
     * @param function The function converting the sender and args to the {@param parentClass}
     */
    default void registerInjector(Class<?> parentClass, BiFunction<B, String[], ?> function) {
        this.registerInjector(parentClass, false, function);
    }

    /**
     *
     * Method for registering the injectors converting from the args, and command sender, to the {@param parentClass}
     *
     * @param parentClass The converted class to be registered
     * @param multipleArgs if the command requires multiple arguments to determine the converted data
     * @param function The function converting the sender and args to the {@param parentClass}
     */
    void registerInjector(Class<?> parentClass, boolean multipleArgs, BiFunction<B, String[], ?> function);

    /**
     *
     * Unregisters all injectors with the converted class specified
     *
     * @param parentClass The class for all injectors to be removed
     */
    void unregisterInjector(Class<?> parentClass);

    /**
     *
     * Registers a tab completion method
     *
     * @param tabCompleter The tab completer
     */
    void registerCompleter(TabCompleter<?, ?> tabCompleter);

}
