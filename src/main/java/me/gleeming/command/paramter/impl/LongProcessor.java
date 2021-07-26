package me.gleeming.command.paramter.impl;

import me.gleeming.command.paramter.Processor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class LongProcessor implements Processor {
    public Object process(CommandSender sender, String supplied) {
        try {
            return Long.parseLong(supplied);
        } catch(Exception ex) {
            sender.sendMessage(ChatColor.RED + "The value you entered '" + supplied + "' is an invalid long.");
            return null;
        }
    }
}
