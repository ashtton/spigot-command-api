package me.gleeming.command.paramter.impl;

import me.gleeming.command.paramter.ProcessorComplete;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GamemodeProcessor implements ProcessorComplete {
    public Object process(CommandSender sender, String supplied) {
        if(supplied.equalsIgnoreCase("creative") || supplied.equalsIgnoreCase("c") || supplied.equals("1")) {
            return GameMode.CREATIVE;
        }

        if(supplied.equalsIgnoreCase("survival") || supplied.equalsIgnoreCase("s") || supplied.equals("0")) {
            return GameMode.SURVIVAL;
        }

        if(supplied.equalsIgnoreCase("adventure") || supplied.equalsIgnoreCase("a") || supplied.equals("2")) {
            return GameMode.ADVENTURE;
        }

        if(supplied.equalsIgnoreCase("spectator") || supplied.equalsIgnoreCase("sp") || supplied.equals("3")) {
            return GameMode.SPECTATOR;
        }

        sender.sendMessage(ChatColor.RED + "The value you entered '" + supplied + "' is not a valid gamemode.");
        return null;
    }

    public List<String> tabComplete(CommandSender sender, String supplied) {
        return Arrays.stream(GameMode.values())
                .map(GameMode::name)
                .map(String::toLowerCase)
                .filter(name -> name.startsWith(supplied.toLowerCase()))
                .collect(Collectors.toList());
    }
}
