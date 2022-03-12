package me.gleeming.command.paramter.impl;

import me.gleeming.command.paramter.Processor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class OfflinePlayerProcessor extends Processor<OfflinePlayer> {
    public OfflinePlayer process(CommandSender sender, String supplied) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(supplied);

        if(player == null) {
            sender.sendMessage(ChatColor.RED + "A player by the name of '" + supplied + "' cannot be located.");
            return null;
        }

        return player;
    }

    public List<String> tabComplete(CommandSender sender, String supplied) {
        return Arrays.stream(Bukkit.getOfflinePlayers())
                .map(OfflinePlayer::getName)
                .filter(Objects::nonNull)
                .filter(name -> name.toLowerCase().startsWith(supplied.toLowerCase()))
                .collect(Collectors.toList());
    }
}
