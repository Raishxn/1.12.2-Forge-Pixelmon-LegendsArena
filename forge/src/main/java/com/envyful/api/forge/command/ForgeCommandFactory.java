package com.envyful.api.forge.command;

import com.envyful.api.command.CommandFactory;
import com.envyful.api.command.annotate.*;
import com.envyful.api.command.annotate.executor.*;
import com.envyful.api.command.exception.CommandLoadException;
import com.envyful.api.command.injector.ArgumentInjector;
import com.envyful.api.command.injector.TabCompleter;
import com.envyful.api.forge.command.command.ForgeCommand;
import com.envyful.api.forge.command.command.ForgeSenderType;
import com.envyful.api.forge.command.command.executor.CommandExecutor;
import com.envyful.api.forge.command.completion.FillerTabCompleter;
import com.envyful.api.forge.command.completion.number.IntegerTabCompleter;
import com.envyful.api.forge.command.completion.player.PlayerTabCompleter;
import com.envyful.api.forge.command.injector.ForgeFunctionInjector;
import com.envyful.api.type.Pair;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.leangen.geantyref.AnnotationFormatException;
import io.leangen.geantyref.TypeFactory;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiFunction;

/**
 *
 * Forge implementation of the {@link CommandFactory} interface
 *
 */
public class ForgeCommandFactory implements CommandFactory<MinecraftServer, ICommandSender> {

    private final List<ArgumentInjector<?, ICommandSender>> registeredInjectors = Lists.newArrayList();
    private final Map<Class<?>, TabCompleter<?, ?>> registeredCompleters = Maps.newConcurrentMap();

    public ForgeCommandFactory() {
        this.registerInjector(EntityPlayerMP.class, (sender, args) -> sender.getServer().getPlayerList().getPlayerByUsername(args[0]));
        this.registerInjector(int.class, (iCommandSender, args) -> {
            try {
                return Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                return null;
            }
        });
        this.registerInjector(String.class, (iCommandSender, args) -> args[0]);
        this.registerInjector(double.class, ((iCommandSender, args) -> {
            try {
                return Double.parseDouble(args[0]);
            } catch (NumberFormatException e) {
                return null;
            }
        }));
        this.registerInjector(long.class, ((iCommandSender, args) -> {
            try {
                return Long.parseLong(args[0]);
            } catch (NumberFormatException e) {
                return null;
            }
        }));
        this.registerCompleter(new IntegerTabCompleter());
        this.registerCompleter(new PlayerTabCompleter());
    }

    @Override
    public boolean registerCommand(MinecraftServer server, Object o) throws CommandLoadException {
        ForgeCommand command = this.createCommand(o.getClass(), o);
        ((CommandHandler) server.getCommandManager()).registerCommand(command);
        return true;
    }

    private ForgeCommand createCommand(Class<?> clazz) throws CommandLoadException {
        return this.createCommand(clazz, null);
    }

    @SuppressWarnings("SuspiciousToArrayCall")
    private ForgeCommand createCommand(Class<?> clazz, Object instance) throws CommandLoadException {
        List<ForgeCommand> subCommands = this.getSubCommands(clazz);
        Command commandData = clazz.getAnnotation(Command.class);

        if (commandData == null) {
            throw new CommandLoadException(clazz.getSimpleName(), "missing @Command annotation on class!");
        }

        String defaultPermission = this.getDefaultPermission(clazz);

        if (instance == null) {
            instance = this.createInstance(clazz);

            if (instance == null) {
                throw new CommandLoadException(clazz.getSimpleName(), "cannot instantiate sub-command as there's no public constructor");
            }
        }

        List<CommandExecutor> subExecutors = Lists.newArrayList();
        BiFunction<ICommandSender, String[], List<String>> tabCompleter = null;

        for (Method declaredMethod : clazz.getDeclaredMethods()) {
            CommandProcessor processorData = declaredMethod.getAnnotation(CommandProcessor.class);

            if (processorData == null) {
                TabCompletions tabCompletions = declaredMethod.getAnnotation(TabCompletions.class);

                if (tabCompletions != null) {
                    if (declaredMethod.getReturnType().equals(List.class) && declaredMethod.getParameterCount() == 3) {
                        Object finalInstance = instance;
                        tabCompleter = (sender, args) -> {
                            try {
                                return (List<String>) declaredMethod.invoke(finalInstance, sender, args);
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                e.printStackTrace();
                            }

                            return Collections.emptyList();
                        };
                    }

                    continue;
                }

                continue;
            }

            String requiredPermission = this.getPermission(declaredMethod);
            List<Pair<ArgumentInjector<?, ICommandSender>, String>> arguments = Lists.newArrayList();
            Class<?>[] parameterTypes = declaredMethod.getParameterTypes();
            Annotation[][] annotations = declaredMethod.getParameterAnnotations();
            ForgeSenderType senderType = null;
            int senderPosition = -1;
            int justArgsPos = -1;
            List<TabCompleter<?, ?>> tabCompleters = Lists.newArrayList();
            List<Annotation[]> extraTabData = Lists.newArrayList();

            for (int i = 0; i < parameterTypes.length; i++) {
                if (parameterTypes[i] == String[].class) {
                    justArgsPos = i;
                    continue;
                }

                if (annotations.length <= i) {
                    continue;
                }

                if (annotations[i][0] instanceof Sender) {
                    senderType = ForgeSenderType.get(parameterTypes[i]);
                    senderPosition = i;
                } else {
                    if (annotations[i][0] instanceof Completable) {
                        tabCompleters.add(this.registeredCompleters.get(((Completable) annotations[i][0]).value()));
                        List<Annotation> data = Lists.newArrayList();

                        for (int x = 1; x < annotations[i].length; x++) {
                            data.add(annotations[i][x]);
                        }

                        extraTabData.add(data.toArray(new Annotation[0]));
                    } else {
                        tabCompleters.add(new FillerTabCompleter());
                        try {
                            extraTabData.add(new Annotation[] { TypeFactory.annotation(
                                    Filler.class,
                                    Maps.newHashMap()
                            ) });
                        } catch (AnnotationFormatException e) {
                            e.printStackTrace();
                        }
                    }

                    Annotation argument = annotations[i][annotations[i].length - 1];
                    String defaultValue = null;

                    if (argument instanceof Argument) {
                        defaultValue = ((Argument) argument).defaultValue();

                        if (defaultValue.isEmpty()) {
                            defaultValue = null;
                        }
                    }

                    arguments.add(Pair.of(this.getInjectorFor(parameterTypes[i]), defaultValue));
                }
            }

            if (senderType == null) {
                throw new CommandLoadException(clazz.getSimpleName(), "Command must have a sender!");
            }

            subExecutors.add(new CommandExecutor(processorData.value(), senderType, senderPosition, instance, declaredMethod,
                    processorData.executeAsync(), justArgsPos, requiredPermission,
                    arguments.toArray(new Pair[0]), tabCompleters, extraTabData));
        }

        Child child = clazz.getAnnotation(Child.class);

        return new ForgeCommand(this, commandData.value(), child != null, commandData.description(), defaultPermission,
                                Arrays.asList(commandData.aliases()), subExecutors, subCommands, tabCompleter
        );
    }

    private String getPermission(Method method) {
        Permissible permissible = method.getAnnotation(Permissible.class);

        if (permissible == null) {
            return "";
        }

        return permissible.value();
    }

    private Object createInstance(Class<?> clazz) {
        if (clazz.getConstructors().length == 0) {
            try {
                return clazz.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
                return null;
            }
        }

        for (Constructor<?> constructor : clazz.getConstructors()) {
            List<Object> objects = Lists.newArrayList();

            for (Class<?> parameterType : constructor.getParameterTypes()) {
                Object o = this.getInjectorFor(parameterType).instantiateClass(null);

                if (o == null) {
                    break;
                }

                objects.add(o);
            }

            if (objects.size() != constructor.getParameterTypes().length) {
                continue;
            }

            try {
                return constructor.newInstance(objects.toArray(new Object[0]));
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private String getDefaultPermission(Class<?> clazz) {
        Permissible permissible = clazz.getAnnotation(Permissible.class);

        if (permissible == null) {
            return "";
        }

        return permissible.value();
    }

    private List<ForgeCommand> getSubCommands(Class<?> clazz) {
        SubCommands subCommands = clazz.getAnnotation(SubCommands.class);

        if (subCommands == null) {
            return Collections.emptyList();
        }

        List<ForgeCommand> commands = Lists.newArrayList();

        for (Class<?> subClazz : subCommands.value()) {
            commands.add(this.createCommand(subClazz));
        }

        return commands;
    }

    private ArgumentInjector<?, ICommandSender> getInjectorFor(Class<?> clazz) {
        for (ArgumentInjector<?, ICommandSender> registeredInjector : this.registeredInjectors) {
            if (registeredInjector.getConvertedClass().isAssignableFrom(clazz)) {
                return registeredInjector;
            }
        }

        return null;
    }

    @Override
    public boolean unregisterCommand(MinecraftServer server, Object o) {
        return false;
    }

    @Override
    public void registerInjector(Class<?> parentClass, boolean multipleArgs, BiFunction<ICommandSender, String[], ?> function) {
        this.registeredInjectors.add(new ForgeFunctionInjector(parentClass, multipleArgs, function));
    }

    @Override
    public void unregisterInjector(Class<?> parentClass) {
        this.registeredInjectors.removeIf(next -> Objects.equals(parentClass, next.getConvertedClass()));
    }

    @Override
    public void registerCompleter(TabCompleter<?, ?> tabCompleter) {
        this.registeredCompleters.put(tabCompleter.getClass(), tabCompleter);
    }
}
