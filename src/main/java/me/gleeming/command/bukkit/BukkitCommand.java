package me.gleeming.command.bukkit;

import lombok.Getter;
import lombok.SneakyThrows;
import me.gleeming.command.CommandHandler;
import me.gleeming.command.help.HelpNode;
import me.gleeming.command.node.CommandNode;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BukkitCommand extends Command {
    @Getter private static final HashMap<String, BukkitCommand> commands = new HashMap<>();

    @SneakyThrows
    public BukkitCommand(String root) {
        super(root);
        commands.put(root.toLowerCase(), this);

        // Registers the command with bukkit
        Field commandMap = CommandHandler.getPlugin().getServer().getClass().getDeclaredField("commandMap");
        commandMap.setAccessible(true);
        ((org.bukkit.command.CommandMap) commandMap.get(CommandHandler.getPlugin().getServer())).register(CommandHandler.getPlugin().getName(), this);
    }

    @SneakyThrows
    public boolean execute(CommandSender sender, String label, String[] args) {
        List<CommandNode> couldExecute = new ArrayList<>();
        CommandNode.getNodes().forEach(node -> {
            if(node.couldExecute(label.toLowerCase(), args)) couldExecute.add(node);
        });

        if(couldExecute.size() == 0) {
            HelpNode helpNode = null;
            int lastSize = 0;
            for(HelpNode node : HelpNode.getNodes()) {
                for(String name : node.getNames()) {
                    if(label.toLowerCase().equals(name)) {
                        helpNode = node;
                        lastSize = 100;
                        break;
                    }

                    String[] split = name.split(" ");
                    for(String s : split) {
                        if(s.contains(label.toLowerCase())) if(lastSize < split.length) { helpNode = node; lastSize = split.length; }
                    }
                }
            }

            if(helpNode != null) {
                helpNode.getMethod().invoke(helpNode.getParentClass(), sender);
                return false;
            }

            CommandNode highestProbabilityNode = null;
            int highestProbability = 0;
            for(CommandNode commandNode : CommandNode.getNodes()) {
                int probability = commandNode.getMatchProbability(label, args);
                if(probability > highestProbability) {
                    highestProbability = probability;
                    highestProbabilityNode = commandNode;
                }
            }

            if(highestProbabilityNode == null) {
                sender.sendMessage(ChatColor.RED + "You have entered an invalid set of command arguments. We were unable to find a usage message to display to you.");
                return false;
            }

            highestProbabilityNode.sendUsageMessage(sender);
            return false;
        }

        if(couldExecute.size() == 1) {
            couldExecute.get(0).execute(sender, args);
            return false;
        }

        List<CommandNode> notConcat = new ArrayList<>();
        for(CommandNode node : couldExecute)
            if(node.getParameters().size() < 1 || !node.getParameters().get(node.getParameters().size() - 1).isConcated())
                notConcat.add(node);

        if (notConcat.size() == 0) {
            int toExec = 0;
            for (int i = 0; i < couldExecute.size(); i++) {
                for (String l : couldExecute.get(i).getNames()) {
                    if (l+"" == label+"") {
                        toExec = i;
                    }
                }
            }
            couldExecute.get(toExec).execute(sender, args);
            return false;
        }

        for(CommandNode node : notConcat) {
            for(String name : node.getNames()) {
                if(name.split(" ")[0].equalsIgnoreCase(label)) {
                    node.execute(sender, args);
                    return false;
                }
            }
        }

        notConcat.get(0).execute(sender, args);
        return false;
    }
}
