package me.gleeming.command.paramter.impl;

import me.gleeming.command.paramter.Processor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class PlayerProcessor extends Processor<Player> {
    public Player process(CommandSender sender, String supplied) {
        Player player = Bukkit.getPlayer(supplied);

        if(player == null) {
            sender.sendMessage(ChatColor.RED + "A player by the name of '" + supplied + "' doesn't exist.");
            return null;
        }

        return player;
    }

    public List<String> tabComplete(CommandSender sender, String supplied) {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(supplied.toLowerCase()))
                .collect(Collectors.toList());
    }
}
