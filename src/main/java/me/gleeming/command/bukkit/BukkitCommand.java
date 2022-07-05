package me.gleeming.command.bukkit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.SneakyThrows;
import me.gleeming.command.CommandHandler;
import me.gleeming.command.help.HelpNode;
import me.gleeming.command.node.ArgumentNode;
import me.gleeming.command.node.CommandNode;
import me.gleeming.command.paramter.ParamProcessor;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class BukkitCommand extends Command {
    @Getter private static final HashMap<String, BukkitCommand> commands = new HashMap<>();

    @SneakyThrows
    public BukkitCommand(String root) {
        super(root);
        commands.put(root.toLowerCase(), this);

        Field commandMap = CommandHandler.getPlugin().getServer().getClass().getDeclaredField("commandMap");
        commandMap.setAccessible(true);
        ((org.bukkit.command.CommandMap) commandMap.get(CommandHandler.getPlugin().getServer())).register(CommandHandler.getPlugin().getName(), this);
    }

    @SneakyThrows
    public boolean execute(CommandSender sender, String label, String[] args) {
        List<CommandNode> sortedNodes = CommandNode.getNodes().stream()
                .sorted(Comparator.comparingInt(node -> node.getMatchProbability(sender, label, args, false)))
                .collect(Collectors.toList());

        CommandNode node = sortedNodes.get(sortedNodes.size() - 1);
        if(node.getMatchProbability(sender, label, args, false) < 90) {
            if(node.getHelpNodes().size() == 0) {
                node.sendUsageMessage(sender);
                return false;
            }

            HelpNode helpNode = node.getHelpNodes().get(0);

            if(!helpNode.getPermission().isEmpty() && !sender.hasPermission(helpNode.getPermission())) {
                sender.sendMessage(ChatColor.RED + "I'm sorry, although you do not have permission to execute this command.");
                return false;
            }

            helpNode.getMethod().invoke(helpNode.getParentClass(), sender);
            return false;
        }

        node.execute(sender, args);
        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String label, String[] args) throws IllegalArgumentException {
        try {
            List<CommandNode> sortedNodes = CommandNode.getNodes().stream()
                    .sorted(Comparator.comparingInt(node -> node.getMatchProbability(sender, label, args, true)))
                    .collect(Collectors.toList());

            CommandNode node = sortedNodes.get(sortedNodes.size() - 1);
            if(node.getMatchProbability(sender, label, args, true) >= 50) {

                int extraLength = node.getNames().get(0).split(" ").length - 1;
                int arg = (args.length - extraLength) - 1;

                if(arg < 0 || node.getParameters().size() < arg + 1)
                    return new ArrayList<>();

                ArgumentNode argumentNode = node.getParameters().get(arg);
                return new ParamProcessor(argumentNode, args[args.length - 1], sender).getTabComplete();
            }

            return sortedNodes.stream()
                    .filter(sortedNode -> sortedNode.getPermission().isEmpty() || sender.hasPermission(sortedNode.getPermission()))
                    .map(sortedNode -> sortedNode.getNames().stream()
                            .map(name -> name.split(" "))
                            .filter(splitName -> splitName[0].equalsIgnoreCase(label))
                            .filter(splitName -> splitName.length > args.length)
                            .map(splitName -> splitName[args.length])
                            .collect(Collectors.toList()))
                    .flatMap(List::stream)
                    .filter(name -> name.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                    .collect(Collectors.toList());
        } catch(Exception exception) {
            exception.printStackTrace();
            return new ArrayList<>();
        }
    }
}
