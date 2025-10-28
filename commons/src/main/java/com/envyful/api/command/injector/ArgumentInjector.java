package com.envyful.api.command.injector;

/**
 *
 * An interface for handling when the parameters of the command are non-regular minecraft classes.
 * For example, passing {@link com.envyful.api.player.EnvyPlayer} as a parameter for a command would require an argument
 * injector to convert the normal sender class to that.
 *
 * @param <A> The converted class type
 * @param <B> The sender (module implementation) class
 */
public interface ArgumentInjector<A, B> {

    /**
     *
     * Gets the class of the type being converted
     *
     * @return The class that is being converted to
     */
    Class<A> getConvertedClass();

    /**
     *
     * If the argument injector requires multiple arguments to instantiate.
     * For example, if you needed a reason as a String (i.e. it goes from argument 1 to the last argument [varying arg
     * length]) then this would be true. This can only be true for the LAST arguments of the command
     *
     * @return True if multiple arguments are supplied, false otherwise
     */
    boolean doesRequireMultipleArgs();

    /**
     *
     * Instantiates the new class from the sender, and the arguments supplied
     *
     * @param sender The sender
     * @param arguments The argument(s) from the command
     * @return The new instantiated converted class
     */
    A instantiateClass(B sender, String... arguments);

}
