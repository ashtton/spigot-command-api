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
                .sorted(Comparator.comparingInt(node -> node.getMatchProbability(sender, label, args)))
                .collect(Collectors.toList());

        CommandNode node = sortedNodes.get(sortedNodes.size() - 1);
        if(node.getMatchProbability(sender, label, args) < 90) {
            if(node.getHelpNodes().size() == 0) {
                node.sendUsageMessage(sender);
                return false;
            }

            HelpNode helpNode = node.getHelpNodes().get(0);
            helpNode.getMethod().invoke(helpNode.getParentClass(), sender);
            return false;
        }

        node.execute(sender, args);
        return false;
    }
}
