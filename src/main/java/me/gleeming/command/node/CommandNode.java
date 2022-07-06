package me.gleeming.command.node;

import lombok.Getter;
import lombok.SneakyThrows;
import me.gleeming.command.Command;
import me.gleeming.command.CommandHandler;
import me.gleeming.command.bukkit.BukkitCommand;
import me.gleeming.command.help.HelpNode;
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

@Getter
public class CommandNode {
    @Getter private static final List<CommandNode> nodes = new ArrayList<>();
    @Getter private static final HashMap<Class<?>, Object> instances = new HashMap<>();

    // Command information
    private final ArrayList<String> names = new ArrayList<>();
    private final String permission;
    private final String description;
    private final boolean async;

    // Executor information
    private final boolean playerOnly;
    private final boolean consoleOnly;

    // Reflect information
    private final Object parentClass;
    private final Method method;

    // Arguments information
    private final List<ArgumentNode> parameters = new ArrayList<>();

    // The help nodes associated with this node
    private final List<HelpNode> helpNodes = new ArrayList<>();

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

        // Register all the argument nodes
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
     * Gets the probably that a player is referring to
     * this command whenever executing a command
     * @param args Arguments
     * @return Match Probability
     */
    public int getMatchProbability(CommandSender sender, String label, String[] args, boolean tabbed) {
        AtomicInteger probability = new AtomicInteger(0);

        this.names.forEach(name -> {
            StringBuilder nameLabel = new StringBuilder(label).append(" ");
            String[] splitName = name.split(" ");
            int nameLength = splitName.length;

            for(int i = 1; i < nameLength; i++)
                if(args.length>= i) nameLabel.append(args[i - 1]).append(" ");

            if(name.equalsIgnoreCase(nameLabel.toString().trim())) {
                int requiredParameters = (int) this.parameters.stream()
                        .filter(ArgumentNode::isRequired)
                        .count();

                int actualLength = args.length - (nameLength - 1);

                if(requiredParameters == actualLength || parameters.size() == actualLength) {
                    probability.addAndGet(125);
                    return;
                }

                if(this.parameters.size() > 0) {
                    ArgumentNode lastArgument = this.parameters.get(this.parameters.size() - 1);
                    if (lastArgument.isConcated() && actualLength > requiredParameters) {
                        probability.addAndGet(125);
                        return;
                    }
                }

                if(!tabbed || splitName.length > 1 || parameters.size() > 0)
                    probability.addAndGet(50);

                if(actualLength > requiredParameters)
                    probability.addAndGet(15);

                if(sender instanceof Player && consoleOnly)
                    probability.addAndGet(-15);

                if(!(sender instanceof Player) && playerOnly)
                    probability.addAndGet(-15);

                if(!permission.equals("") && !sender.hasPermission(permission))
                    probability.addAndGet(-15);

                return;
            }

            String[] labelSplit = nameLabel.toString().split(" ");
            for(int i = 0; i < nameLength && i < labelSplit.length; i++)
                if(splitName[i].equalsIgnoreCase(labelSplit[i]))
                    probability.addAndGet(5);
        });

        return probability.get();
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
            if(parameters.size() < i + 1) break;
            ArgumentNode node = parameters.get(i);

            // Checks if the node is concatted
            if(node.isConcated()) {
                StringBuilder stringBuilder = new StringBuilder();
                for(int x = i; x < args.length; x++) {
                    if(args.length - 1 < x + nameArgs) continue;
                    stringBuilder.append(args[x + nameArgs]).append(" ");
                }
                objects.add(stringBuilder.substring(0, stringBuilder.toString().length() - 1));
                break;
            }

            String suppliedArgument = args[i + nameArgs];
            Object object = new ParamProcessor(node, suppliedArgument, sender).get();

            // If the object is returning null then that means there was a problem parsing
            if(object == null) return;
            objects.add(object);
        }

        if(args.length < requiredArgumentsLength()) {
            sendUsageMessage(sender);
            return;
        }

        int difference = (parameters.size() - requiredArgumentsLength()) - ((args.length - nameArgs) - requiredArgumentsLength());
        for(int i = 0; i < difference; i++) objects.add(null);

        if(async) {
            Bukkit.getScheduler().runTaskAsynchronously(CommandHandler.getPlugin(), () -> {
                try { method.invoke(parentClass, objects.toArray()); } catch(Exception exception) { exception.printStackTrace(); }
            });
            return;
        }

        method.invoke(parentClass, objects.toArray());
    }
}