package me.gleeming.command.paramter.impl;

import me.gleeming.command.duration.Duration;
import me.gleeming.command.paramter.Processor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class DurationProcessor implements Processor {
    public Object process(CommandSender sender, String supplied) {
        long duration = parseDuration(supplied);

        if(duration == -1) {
            sender.sendMessage(ChatColor.RED + "You have entered an invalid duration.");
            return null;
        }

        return new Duration(supplied.toLowerCase(), duration);
    }

    /**
     * Get duration from string
     *
     * @param toParse String to parse
     * @return Duration
     */
    public long parseDuration(String toParse) {
        try {
            toParse = toParse.toUpperCase();
            long value = Long.parseLong(toParse.substring(0, toParse.length() - 1));

            if(toParse.endsWith("S")) value = value * 1000;
            else if(toParse.endsWith("M")) value = value * 1000 * 60;
            else if(toParse.endsWith("H")) value = value * 1000 * 60 * 60;
            else if(toParse.endsWith("D")) value = value * 1000 * 60 * 60 * 12;
            else return -1;

            return value;
        } catch(Exception ignored) { return -1; }
    }
}
