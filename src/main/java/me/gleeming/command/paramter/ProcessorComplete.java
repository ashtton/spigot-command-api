package me.gleeming.command.paramter;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface ProcessorComplete extends Processor {

    /**
     * Processes the tab completion
     */
    List<String> tabComplete(CommandSender sender, String supplied);

}
