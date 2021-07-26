package me.gleeming.command.paramter;

import org.bukkit.command.CommandSender;

public interface Processor {
    /**
     * Process the object
     */
    Object process(CommandSender sender, String supplied);
}
