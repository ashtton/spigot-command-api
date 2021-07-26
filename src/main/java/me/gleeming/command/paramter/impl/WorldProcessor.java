package me.gleeming.command.paramter.impl;

import me.gleeming.command.paramter.Processor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

public class WorldProcessor implements Processor {
    @Override
    public Object process(CommandSender sender, String supplied) {
        World world = Bukkit.getWorld(supplied);

        if(world == null) {
            sender.sendMessage(ChatColor.RED + "A world by the name of '" + supplied + "' cannot be found.");
            return null;
        }

        return world;
    }
}
