package me.gleeming.command.paramter.impl;

import me.gleeming.command.paramter.ProcessorComplete;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.stream.Collectors;

public class WorldProcessor implements ProcessorComplete {
    @Override
    public Object process(CommandSender sender, String supplied) {
        World world = Bukkit.getWorld(supplied);

        if(world == null) {
            sender.sendMessage(ChatColor.RED + "A world by the name of '" + supplied + "' cannot be found.");
            return null;
        }

        return world;
    }

    public List<String> tabComplete(CommandSender sender, String supplied) {
        return Bukkit.getWorlds().stream()
                .map(World::getName)
                .filter(name -> name.toLowerCase().startsWith(supplied.toLowerCase()))
                .collect(Collectors.toList());
    }
}
