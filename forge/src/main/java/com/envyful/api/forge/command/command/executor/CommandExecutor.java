package com.envyful.api.forge.command.command.executor;

import com.envyful.api.command.injector.ArgumentInjector;
import com.envyful.api.command.injector.TabCompleter;
import com.envyful.api.forge.command.command.ForgeSenderType;
import com.envyful.api.forge.command.completion.FillerTabCompleter;
import com.envyful.api.forge.player.util.UtilPlayer;
import com.envyful.api.type.Pair;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.io.PrintWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * A simple data transfer object for the command methods obtained through reflection.
 * Ensures no laggy reflection during user use-age as the methods, object instances, and required injectors are cached at
 * server start for future use.
 *
 */
public class CommandExecutor {

    private final String identifier;
    private final int senderPosition;
    private final ForgeSenderType sender;
    private final Object commandClass;
    private final Method executor;
    private final boolean executeAsync;
    private final int justArgsPos;
    private final int requiredArgs;
    private final String requiredPermission;
    private final Pair<ArgumentInjector<?, ICommandSender>, String>[] arguments;
    private final List<TabCompleter<?, ?>> tabCompleters;
    private final List<Annotation[]> extraTabData;

    /**
     *
     * Simple constructor taking all required parameters
     *
     * @param identifier The identifier string for the sub-command
     * @param sender The sender type for the sub-command
     * @param senderPosition The position in the parameters of the sender
     * @param commandClass An instance of the command class
     * @param executor The method instance
     * @param executeAsync If the command should be run asynchronously
     * @param requiredPermission The permission required to execute the command
     * @param arguments The injected argument types
     */
    public CommandExecutor(String identifier, ForgeSenderType sender, int senderPosition, Object commandClass, Method executor,
                           boolean executeAsync, int justArgsPos, String requiredPermission,
                           Pair<ArgumentInjector<?, ICommandSender>, String>[] arguments,
                           List<TabCompleter<?, ?>> tabCompleters,
                           List<Annotation[]> extraTabData) {
        this.identifier = identifier;
        this.senderPosition = senderPosition;
        this.sender = sender;
        this.commandClass = commandClass;
        this.executor = executor;
        this.executeAsync = executeAsync;
        this.justArgsPos = justArgsPos;
        this.requiredPermission = requiredPermission;
        this.arguments = arguments;
        this.requiredArgs = this.calculateRequiredArgs();
        this.tabCompleters = tabCompleters;
        this.extraTabData = extraTabData;
    }

    /**
     *
     * Calculates the arguments required based on the injected arguments.
     * If there is a single multiple arg requirement then it will return -1.
     *
     * @return Number of args required for the sub command
     */
    private int calculateRequiredArgs() {
        if (this.justArgsPos != -1) {
            return -1;
        }

        int defaults = 0;

        for (Pair<ArgumentInjector<?, ICommandSender>, String> argument : this.arguments) {
            if (argument != null && argument.getX().doesRequireMultipleArgs()) {
                return -1;
            }

            if (argument != null && argument.getY() != null) {
                ++defaults;
            }
        }

        return this.arguments.length - defaults;
    }

    /**
     *
     * Gets the sub-commands identifying string
     *
     * @return The name of the sub command
     */
    public String getIdentifier() {
        return this.identifier;
    }

    /**
     *
     * Gets the sender type for the sub command
     *
     * @return The sender type
     */
    public ForgeSenderType getSender() {
        return this.sender;
    }

    /**
     *
     * Determines if the command can be executed asynchronously or not. Defaults as true
     *
     * @return If it should be run off thread
     */
    public boolean isExecutedAsync() {
        return this.executeAsync;
    }

    /**
     *
     * Gets the cached number of required arguments for this command
     *
     * @return The number of required args
     */
    public int getRequiredArgs() {
        return this.requiredArgs;
    }

    /**
     *
     * Determines if the command sender specified can execute this command (based on permissions)
     *
     * @param sender The entity attempting to run the command
     * @return If they can execute the command
     */
    public boolean canExecute(ICommandSender sender) {
        if (this.requiredPermission == null || this.requiredPermission.isEmpty()) {
            return true;
        }

        if (!(sender instanceof EntityPlayerMP)) {
            return true;
        }

        return UtilPlayer.hasPermission((EntityPlayerMP) sender, this.requiredPermission);
    }

    /**
     *
     * Attempts to execute the command with the specified sender, and arguments.
     *
     * Will return false if it fails to execute or an error occurs during runtime.
     *
     * @param sender The entity that executed the command
     * @param arguments The arguments that have been passed from the entity
     * @return If the command failed to run
     */
    public boolean execute(ICommandSender sender, String[] arguments) {
        Object[] args = new Object[this.arguments.length + 1 + (justArgsPos != -1 ? 1 : 0)];
        int skipped = 0;

        for (int i = 0; i < this.arguments.length; i++) {
            Pair<ArgumentInjector<?, ICommandSender>, String> argument = this.arguments[i];
            int commandArgsPos = i - skipped;
            int executorPos = i + 1;

            if (argument.getX().doesRequireMultipleArgs()) {
                String[] remainingArgs = Arrays.copyOfRange(arguments, commandArgsPos, arguments.length);

                if (remainingArgs.length == 0 && argument.getY() != null) {
                    remainingArgs = new String[] { argument.getY() };
                }

                args[executorPos] = argument.getX().instantiateClass(sender, remainingArgs);

                if (args[executorPos] == null) {
                    return false;
                }
            } else {
                if (arguments.length <= 0 || arguments.length <= commandArgsPos) {
                    args[executorPos] = argument.getX().instantiateClass(sender, argument.getY());

                    if (args[executorPos] == null) {
                        return false;
                    } else {
                        continue;
                    }
                }

                args[executorPos] = argument.getX().instantiateClass(sender, arguments[commandArgsPos]);

                if (args[executorPos] == null) {
                    if (argument.getY() != null) {
                        args[executorPos] = argument.getX().instantiateClass(sender, argument.getY());

                        if (args[executorPos] == null) {
                            return false;
                        } else {
                            ++skipped;
                        }
                    } else {
                        return false;
                    }
                }
            }
        }

        if (this.sender.getType().equals(sender.getClass())) {
            args[0] = sender;
        } else {
            try {
                args[0] = this.sender.getType().cast(sender);
            } catch (ClassCastException e) {
                FMLCommonHandler.instance().getFMLLogger().info("You cannot use this command from this source (player only).");
                return false;
            }
        }

        if (this.justArgsPos != -1) {
            if (arguments == null) {
                arguments = new String[0];
            }

            args[args.length - 1] = arguments;
        }

        return this.execute(args);
    }

    private boolean execute(Object... args) {
        try {
            this.executor.invoke(this.commandClass, args);
            return true;
        } catch (Exception e) {
            Throwable cause = e.getCause();
            cause.printStackTrace(new PrintWriter(new Writer() {
                @Override
                public void write(char[] cbuf, int off, int len) {
                    FMLCommonHandler.instance().getFMLLogger().error(new String(cbuf));
                }

                @Override
                public void flush() {}

                @Override
                public void close() {}
            }));
        }

        return false;
    }

    public <A> List<String> tabComplete(ICommandSender sender, String[] args) {
        if (this.tabCompleters == null || this.tabCompleters.isEmpty()) {
            return Collections.emptyList();
        }

        int pos = Math.min(Math.max(this.requiredArgs, 0), Math.max(args.length - 1, 0));
        TabCompleter<?, A> completer = (TabCompleter<?, A>) this.tabCompleters.get(Math.min(pos, this.tabCompleters.size()));

        if (completer instanceof FillerTabCompleter) {
            return Collections.emptyList();
        }

        A castSender = completer.getSenderClass().cast(sender);
        List<String> completions = completer.getCompletions(castSender, args, this.extraTabData.get(pos));

        if (this.arguments[pos] != null && this.arguments[pos].getY() != null) {
            if (this.tabCompleters.size() > (pos + 1)) {
                TabCompleter<?, A> tabCompleter = (TabCompleter<?, A>) this.tabCompleters.get(pos + 1);

                if (!(completer instanceof FillerTabCompleter)) {
                    completions.addAll(tabCompleter.getCompletions(castSender, args, this.extraTabData.get(pos + 1)));
                }
            }
        }

        return completions;
    }
}
