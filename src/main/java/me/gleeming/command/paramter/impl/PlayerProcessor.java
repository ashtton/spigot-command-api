package me.gleeming.command.paramter.impl;

import me.gleeming.command.paramter.Processor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlayerProcessor implements Processor {
    public Object process(CommandSender sender, String supplied) {
        Player player = Bukkit.getPlayer(supplied);

        if(player == null) {
            sender.sendMessage(ChatColor.RED + "A player by the name of '" + supplied + "' doesn't exist.");
            return null;
        }

        return player;
    }
}
