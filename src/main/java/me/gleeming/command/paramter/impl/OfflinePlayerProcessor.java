package me.gleeming.command.paramter.impl;

import me.gleeming.command.paramter.Processor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class OfflinePlayerProcessor implements Processor {
    public Object process(CommandSender sender, String supplied) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(supplied);

        if(player == null) {
            sender.sendMessage(ChatColor.RED + "A player by the name of '" + supplied + "' cannot be located.");
            return null;
        }

        return player;
    }
}
