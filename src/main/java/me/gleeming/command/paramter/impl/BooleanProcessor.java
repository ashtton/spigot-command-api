package me.gleeming.command.paramter.impl;

import me.gleeming.command.paramter.Processor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class BooleanProcessor implements Processor {
    private final Map<String, Boolean> values = new HashMap<>();
    public BooleanProcessor() {
        // Values that mean true
        values.put("true", true);
        values.put("on", true);
        values.put("yes", true);
        values.put("enable", true);

        // Values that mean false
        values.put("false", false);
        values.put("off", false);
        values.put("no", false);
        values.put("disable", false);
    }

    public Object process(CommandSender sender, String supplied) {
        supplied = supplied.toLowerCase();
        if(!values.containsKey(supplied)) {
            sender.sendMessage(ChatColor.RED + "You have entered an invalid value.");
            return null;
        }

        return values.get(supplied);
    }
}
