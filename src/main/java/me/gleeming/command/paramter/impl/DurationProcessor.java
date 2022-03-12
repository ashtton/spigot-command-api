package me.gleeming.command.paramter.impl;

import me.gleeming.command.duration.Duration;
import me.gleeming.command.paramter.Processor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DurationProcessor extends Processor<Duration> {

    private final List<String> defaults = Arrays.asList(
            "perm", "5m", "30m", "1h", "2h",
            "1d", "2d", "3d", "4d", "5d", "30d"
    );

    public Duration process(CommandSender sender, String supplied) {
        long duration = parseDuration(supplied);

        if(duration == 0) {
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
    public static long parseDuration(String toParse) {
        try {
            toParse = toParse.toUpperCase();
            if(toParse.equals("FOREVER") || toParse.equals("EVER") || toParse.equals("NEVER") || toParse.equals("PERM") || toParse.equals("PERMANENT"))
                return -1;

            long value = Long.parseLong(toParse.substring(0, toParse.length() - 1));

            if(toParse.endsWith("S")) value = value * 1000;
            else if(toParse.endsWith("M")) value = value * 1000 * 60;
            else if(toParse.endsWith("H")) value = value * 1000 * 60 * 60;
            else if(toParse.endsWith("D")) value = value * 1000 * 60 * 60 * 24;
            else return 0;

            return value;
        } catch(Exception ignored) { return 0; }
    }

    public List<String> tabComplete(CommandSender sender, String supplied) {
        return defaults.stream()
                .filter(name -> name.toLowerCase().startsWith(supplied.toLowerCase()))
                .collect(Collectors.toList());
    }
}
