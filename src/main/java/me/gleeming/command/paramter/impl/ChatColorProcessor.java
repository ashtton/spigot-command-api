package me.gleeming.command.paramter.impl;

import me.gleeming.command.paramter.ProcessorComplete;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ChatColorProcessor implements ProcessorComplete {

    public Object process(CommandSender sender, String supplied) {
        try { return ChatColor.valueOf(supplied); }
        catch(Exception exception) {
            sender.sendMessage(ChatColor.RED + "A color by the name of '" + supplied + "' doesn't exist.");
            return null;
        }
    }

    public List<String> tabComplete(CommandSender sender, String supplied) {
        return Arrays.stream(ChatColor.values())
                .map(ChatColor::name)
                .filter(name -> name.toLowerCase().startsWith(supplied.toLowerCase()))
                .collect(Collectors.toList());
    }
}
