package com.envyful.api.forge.command.injector;

import com.envyful.api.command.injector.ArgumentInjector;
import net.minecraft.command.ICommandSender;

import java.util.function.BiFunction;

/**
 *
 * The forge implementation of the {@link ArgumentInjector} interface
 *
 * @param <T> The type being converted to
 */
public class ForgeFunctionInjector<T> implements ArgumentInjector<T, ICommandSender> {

    private final Class<T> superClass;
    private final boolean multipleArgs;
    private final BiFunction<ICommandSender, String[], T> function;

    public ForgeFunctionInjector(Class<T> superClass, boolean multipleArgs, BiFunction<ICommandSender, String[], T> function) {
        this.superClass = superClass;
        this.multipleArgs = multipleArgs;
        this.function = function;
    }

    @Override
    public Class<T> getConvertedClass() {
        return this.superClass;
    }

    @Override
    public boolean doesRequireMultipleArgs() {
        return this.multipleArgs;
    }

    @Override
    public T instantiateClass(ICommandSender sender, String... args) {
        return this.function.apply(sender, args);
    }
}
