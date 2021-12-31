package me.gleeming.command.node;

import lombok.Getter;
import lombok.SneakyThrows;
import me.gleeming.command.Command;
import me.gleeming.command.CommandHandler;
import me.gleeming.command.bukkit.BukkitCommand;
import me.gleeming.command.paramter.Param;
import me.gleeming.command.paramter.ParamProcessor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CommandNode {
    @Getter private static final List<CommandNode> nodes = new ArrayList<>();
    @Getter private static final HashMap<Class<?>, Object> instances = new HashMap<>();

    // Command information
    @Getter private final ArrayList<String> names = new ArrayList<>();
    @Getter private final String permission;
    @Getter private final String description;
    @Getter private final boolean async;

    // Executor information
    @Getter private final boolean playerOnly;
    @Getter private final boolean consoleOnly;

    // Reflect information
    @Getter private final Object parentClass;
    @Getter private final Method method;

    // Arguments information
    @Getter private final List<ArgumentNode> parameters = new ArrayList<>();

    public CommandNode(Object parentClass, Method method, Command command) {
        // Loads names
        Arrays.stream(command.names()).forEach(name -> names.add(name.toLowerCase()));

        // Retrieve information from annotation
        this.permission = command.permission();
        this.description = command.description();
        this.async = command.async();
        this.playerOnly = command.playerOnly();
        this.consoleOnly = command.consoleOnly();

        // Reflection
        this.parentClass = parentClass;
        this.method = method;

        // Register all of the argument nodes
        Arrays.stream(method.getParameters()).forEach(parameter -> {
            Param param = parameter.getAnnotation(Param.class);
            if(param == null) return;

            parameters.add(new ArgumentNode(param.name(), param.concated(), param.required(), parameter));
        });

        // Register bukkit command if it doesn't exist
        names.forEach(name -> {
            if(!BukkitCommand.getCommands().containsKey(name.split(" ")[0].toLowerCase())) new BukkitCommand(name.split(" ")[0].toLowerCase());
        });

        // Makes it so you can use /plugin:command
        List<String> toAdd = new ArrayList<>();
        names.forEach(name -> toAdd.add(CommandHandler.getPlugin().getName() + ":" + name.toLowerCase()));
        names.addAll(toAdd);

        // Add node to array list
        nodes.add(this);
    }

    /**
     * Gets the probability that a player is talking about this command
     *
     * @param label Label
     * @param args Args
     */
    public int getMatchProbability(String label, String[] args) {
        int level = 0;

        for(String name : names) {
            if(name.startsWith(label)) level+=2;
            if(name.equals(label)) level += 2;
            if(name.split(" ")[0].equals(label)) level++;
            if(name.contains(label)) level++;
            if(label.contains(name)) level++;

            String[] splitName = name.split(" ");
            for(String s : splitName) if(s.toLowerCase().contains(label.toLowerCase())) level += 1;
        }

        if(requiredArgumentsLength() - args.length == 1) level += 3;
        if(args.length == requiredArgumentsLength()) level++;

        return level;
    }

    /**
     * Sends a player the usage message of this command
     */
    public void sendUsageMessage(CommandSender sender) {
        if(consoleOnly && sender instanceof Player) {
            sender.sendMessage(ChatColor.RED + "This command can only be executed by console.");
            return;
        }

        if(playerOnly && sender instanceof ConsoleCommandSender) {
            sender.sendMessage(ChatColor.RED + "You must be a player to execute this command.");
            return;
        }

        if(!permission.equals("") && !sender.hasPermission(permission)) {
            sender.sendMessage(ChatColor.RED + "I'm sorry, although you do not have permission to execute this command.");
            return;
        }

        StringBuilder builder = new StringBuilder(ChatColor.RED + "Usage: /" + names.get(0) + " ");
        parameters.forEach(param -> {
            if(param.isRequired()) builder.append("<").append(param.getName()).append(param.isConcated() ? ".." : "").append(">");
            else builder.append("[").append(param.getName()).append(param.isConcated() ? ".." : "").append("]");
            builder.append(" ");
        });

        // Sends the usage message
        sender.sendMessage(builder.toString());
    }

    /**
     * Checks whether or not a node could execute a command
     *
     * @param label Label
     * @param args Arguments
     */
    public boolean couldExecute(String label, String[] args) {
        boolean containsName = false;
        for(String name : names) {
            StringBuilder actualLabel = new StringBuilder(label);

            for(String arg : args)
                actualLabel.append(" ").append(arg);

            if(name.equalsIgnoreCase(actualLabel.toString().trim().toLowerCase())) {
                containsName = true;
                break;
            }
        }

        // Checks if label even starts with any of the names
        if(!containsName) return false;

        // Checks if there is a concatted argument or a non required as the last argument
        boolean lastConcatted = parameters.size() > 0 && parameters.get(parameters.size() - 1).isConcated();
        boolean lastNonRequired = parameters.size() > 0 && !parameters.get(parameters.size() - 1).isRequired();

        // Checks if the argument length is even with the list
        if(!lastNonRequired) {
            if (args.length != requiredArgumentsLength() && !lastConcatted) return false;
        } else {
            if(args.length < requiredArgumentsLength()) return false;
        }

        // Checks if concatted parameter is ever reached
        if(lastConcatted && args.length < requiredArgumentsLength()) return false;

        // If all tests pass then the command is valid
        return true;
    }

    /**
     * Gets the required arguments length
     * @return Required Length
     */
    public int requiredArgumentsLength() {
        int requiredArgumentsLength = names.get(0).split(" ").length - 1;
        for(ArgumentNode node : parameters) if(node.isRequired()) requiredArgumentsLength++;
        return requiredArgumentsLength;
    }

    /**
     * Executes the command
     *
     * @param sender Sender
     * @param args Arguments
     */
    @SneakyThrows
    public void execute(CommandSender sender, String[] args) {
        // Checks if the player has permission
        if(!permission.equals("") && !sender.hasPermission(permission)) {
            sender.sendMessage(ChatColor.RED + "I'm sorry, although you do not have permission to execute this command.");
            return;
        }

        // Checks if command is console only
        if(sender instanceof ConsoleCommandSender && playerOnly) {
            sender.sendMessage(ChatColor.RED + "You must be a player to execute this command.");
            return;
        }

        // Checks if command is player only
        if(sender instanceof Player && consoleOnly) {
            sender.sendMessage(ChatColor.RED + "This command is only executable by console.");
            return;
        }

        // Calculates the amount of arguments in the name
        int nameArgs = (names.get(0).split(" ").length - 1);

        List<Object> objects = new ArrayList<>(Collections.singletonList(sender));
        for(int i = 0; i < args.length - nameArgs; i++) {
            ArgumentNode node = parameters.get(i);

            // Checks if the node is concatted
            if(node.isConcated()) {
                StringBuilder stringBuilder = new StringBuilder();
                for(int x = i; x < args.length; x++) stringBuilder.append(args[x + nameArgs]).append(" ");
                objects.add(stringBuilder.toString());
                break;
            }

            String suppliedArgument = args[i + nameArgs];
            Object object = new ParamProcessor(node, suppliedArgument, sender).get();

            // If the object is returning null then that means there was a problem parsing
            if(object == null) return;
            objects.add(object);
        }

        int difference = (parameters.size() - requiredArgumentsLength()) - ((args.length - nameArgs) - requiredArgumentsLength());
        for(int i = 0; i < difference; i++) objects.add(null);

        if(async) Bukkit.getScheduler().runTaskAsynchronously(CommandHandler.getPlugin(), () -> {try { method.invoke(parentClass, objects.toArray()); } catch(Exception ex) { ex.printStackTrace(); }});
        else method.invoke(parentClass, objects.toArray());
    }
}