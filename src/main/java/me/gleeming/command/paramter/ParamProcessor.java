package me.gleeming.command.paramter;

import lombok.Data;
import lombok.Getter;
import me.gleeming.command.duration.Duration;
import me.gleeming.command.node.ArgumentNode;
import me.gleeming.command.paramter.impl.*;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;

@Data
public class ParamProcessor {
    @Getter private static final HashMap<Class<?>, Processor> processors = new HashMap<>();
    private static boolean loaded = false;

    private final ArgumentNode node;
    private final String supplied;
    private final CommandSender sender;

    /**
     * Processes the param into an object
     * @return Processed Object
     */
    public Object get() {
        if(!loaded) loadProcessors();

        Processor processor = processors.get(node.getParameter().getType());
        if(processor == null) return supplied;

        return processor.process(sender, supplied);
    }

    /**
     * Loads the processors
     */
    public static void loadProcessors() {
        loaded = true;

        processors.put(Integer.class, new IntegerProcessor());
        processors.put(Long.class, new LongProcessor());
        processors.put(Double.class, new DoubleProcessor());
        processors.put(Player.class, new PlayerProcessor());
        processors.put(OfflinePlayer.class, new OfflinePlayerProcessor());
        processors.put(World.class, new WorldProcessor());
        processors.put(Boolean.class, new BooleanProcessor());
        processors.put(Duration.class, new DurationProcessor());
        processors.put(GameMode.class, new GamemodeProcessor());
    }
}
