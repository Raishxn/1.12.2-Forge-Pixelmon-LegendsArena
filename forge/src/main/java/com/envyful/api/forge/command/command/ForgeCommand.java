package com.envyful.api.forge.command.command;

import com.envyful.api.concurrency.UtilConcurrency;
import com.envyful.api.forge.command.ForgeCommandFactory;
import com.envyful.api.forge.command.command.executor.CommandExecutor;
import com.envyful.api.forge.concurrency.UtilForgeConcurrency;
import com.envyful.api.forge.player.util.UtilPlayer;
import com.google.common.collect.Lists;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 *
 * The forge command implementation for registering to the server. Handles checking all sub commands
 * and running all commands from the forge implementation.
 *
 */
@MethodsReturnNonnullByDefault
public class ForgeCommand extends CommandBase {

    private static final ITextComponent NO_PERMISSION = new TextComponentString("§c§l(!) §cNo permission!");

    private final ForgeCommandFactory commandFactory;
    private final String name;
    private final boolean child;
    private final List<ITextComponent> description;
    private final String basePermission;
    private final List<String> aliases;
    private final List<CommandExecutor> executors;
    private final List<ForgeCommand> subCommands;
    private final BiFunction<ICommandSender, String[], List<String>> tabCompleter;

    public ForgeCommand(ForgeCommandFactory commandFactory, String name, boolean child, String[] description,
                        String basePermission,
                        List<String> aliases, List<CommandExecutor> executors, List<ForgeCommand> subCommands,
                        BiFunction<ICommandSender, String[], List<String>> tabCompleter) {
        this.commandFactory = commandFactory;
        this.name = name;
        this.child = child;
        this.description = this.initializeDescription(description);
        this.basePermission = basePermission;
        this.aliases = aliases;
        this.executors = executors;
        this.subCommands = subCommands;
        this.tabCompleter = tabCompleter;
    }

    private List<ITextComponent> initializeDescription(String[] description) {
        List<ITextComponent> newDescription = Lists.newArrayList();

        for (String s : description) {
            newDescription.add(new TextComponentString(s));
        }

        if (!this.child) {
            newDescription.add(new TextComponentString(""));
            TextComponentString textComponent = new TextComponentString("§eFor further support visit the §nEnvyWare§e discord: ");
            textComponent.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.envyware.co.uk"));
            TextComponentString textComponent2 = new TextComponentString("§e§nhttps://discord.envyware.co.uk§e");
            textComponent2.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.envyware.co.uk"));
            newDescription.add(textComponent);
            newDescription.add(textComponent2);
        }
        
        return newDescription;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "";
    }

    @Override
    public List<String> getAliases() {
        return this.aliases;
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        if (this.basePermission.isEmpty() || !(sender instanceof EntityPlayerMP)) {
            return true;
        }

        return UtilPlayer.hasPermission((EntityPlayerMP) sender, this.basePermission);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        UtilConcurrency.runAsync(() -> this.executeSync(server, sender, args));
    }

    public void executeSync(MinecraftServer server, ICommandSender sender, String[] args) {
        if (!this.checkPermission(server, sender)) {
            sender.sendMessage(NO_PERMISSION);
            return;
        }

        if (args.length > 0) {
            for (ForgeCommand subCommand : this.subCommands) {
                if (this.fitsCommand(args[0], subCommand)) {
                    subCommand.executeSync(server, sender, Arrays.copyOfRange(args, 1, args.length));
                    return;
                }
            }
        }

        for (CommandExecutor executor : this.executors) {
            if (executor.getIdentifier().isEmpty()) {
                if (this.attemptRun(executor, sender, args)) {
                    return;
                }
            }

            if (args.length == 0) {
                continue;
            }

            if (!executor.getIdentifier().equalsIgnoreCase(args[0]) || (executor.getIdentifier().isEmpty() && !args[0].isEmpty())) {
                continue;
            }

            if (this.attemptRun(executor, sender, args)) {
                return;
            }
        }

        for (ITextComponent iTextComponent : this.description) {
            sender.sendMessage(iTextComponent);
        }
    }

    private boolean attemptRun(CommandExecutor executor, ICommandSender sender, String[] args) {
        if (!executor.canExecute(sender)) {
            sender.sendMessage(NO_PERMISSION);
            return true;
        }

        if (executor.getRequiredArgs() == -1 || executor.getRequiredArgs() == 0) {
            if (!executor.isExecutedAsync()) {
                UtilForgeConcurrency.runSync(() -> executor.execute(sender, args));
                return true;
            }

            return executor.execute(sender, args);
        }

        if (executor.getRequiredArgs() <= (args.length + 1)) {
            if (!executor.isExecutedAsync()) {
                UtilForgeConcurrency.runSync(() -> {
                    if (executor.execute(sender, args)) {
                        return;
                    }

                    for (ITextComponent iTextComponent : this.description) {
                        sender.sendMessage(iTextComponent);
                    }
                });
                return true;
            }

            return executor.execute(sender, args);
        }

        return false;
    }

    private boolean fitsCommand(String arg, ForgeCommand subCommand) {
        if (subCommand.getName().equalsIgnoreCase(arg)) {
            return true;
        }

        for (String alias : subCommand.getAliases()) {
            if (alias.equalsIgnoreCase(arg)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
        if (this.tabCompleter != null) {
            return this.tabCompleter.apply(sender, args);
        }

        if (!this.executors.isEmpty()) {
            for (CommandExecutor executor : this.executors) {
                if (executor.getIdentifier().isEmpty()) {
                    List<String> values = executor.tabComplete(sender, args);

                    if (!values.isEmpty()) {
                        if (args.length == 0) {
                            return values;
                        }

                        return this.getMatching(args[0], values);
                    }
                }

                if (args.length == 0) {
                    continue;
                }

                if (!executor.getIdentifier().equalsIgnoreCase(args[0]) || (executor.getIdentifier().isEmpty() && !args[0].isEmpty())) {
                    continue;
                }

                List<String> values = executor.tabComplete(sender, Arrays.copyOfRange(args, 1, args.length));

                if (!values.isEmpty()) {
                    return values;
                }
            }
        }

        if (this.subCommands.size() == 0) {
            if (args.length == 0) {
                return this.getAllPlayers();
            } else {
                return this.getPlayers(sender, args[0]);
            }
        }

        if (args.length == 0) {
            List<String> values = this.getAccessibleSubCommands(sender);

            if (!values.isEmpty()) {
                return values;
            }
        }

        if (args.length == 1) {
            List<String> values = this.getMatching(args[0], this.getAccessibleSubCommands(sender));

            if (!values.isEmpty()) {
                return values;
            }
        }

        for (ForgeCommand subCommand : this.subCommands) {
            if (args[0].equalsIgnoreCase(subCommand.getName()) || subCommand.getAliases().contains(args[0])) {
                String[] argCopy = Arrays.copyOfRange(args, 1, args.length);
                List<String> values = subCommand.getTabCompletions(server, sender, argCopy, pos);

                if (!values.isEmpty()) {
                    return this.getMatching(argCopy[0], values);
                }
            }
        }

        return Collections.emptyList();
    }

    protected List<String> getPlayers(ICommandSender sender, String name) {
        if (name.isEmpty()) {
            return this.getAllPlayers();
        }

        return this.getMatching(name, this.getAllPlayers());
    }

    protected List<String> getAllPlayers() {
        List<EntityPlayerMP> players = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers();
        return players.stream().map(EntityPlayerMP::getName).collect(Collectors.toList());
    }

    protected List<String> getAccessibleSubCommands(ICommandSender sender) {
        List<String> subCommands = Lists.newArrayList();

        for (ForgeCommand subCommand : this.subCommands) {
            if (!(sender instanceof EntityPlayerMP)
                    || subCommand.checkPermission(FMLCommonHandler.instance().getMinecraftServerInstance(), sender)) {
                subCommands.add(subCommand.name);
                subCommands.addAll(subCommand.aliases);
            }
        }

        return subCommands;
    }

    protected List<String> getMatching(String arg, List<String> potential) {
        List<String> args = Lists.newArrayList();

        for (String s : potential) {
            if (s.toLowerCase().startsWith(arg.toLowerCase())) {
                args.add(s);
            }
        }

        return args;
    }
}
