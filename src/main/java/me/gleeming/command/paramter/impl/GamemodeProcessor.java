package me.gleeming.command.paramter.impl;

import me.gleeming.command.paramter.Processor;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;

public class GamemodeProcessor implements Processor {
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

        sender.sendMessage(ChatColor.RED + "The value you entered '" + supplied + "' is not a valid gamemode.");
        return null;
    }
}
