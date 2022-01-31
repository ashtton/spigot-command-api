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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
     * Gets the tab completions for the param processor
     * @return Tab Completions
     */
    public List<String> getTabComplete() {
        if(!loaded) loadProcessors();

        Processor processor = processors.get(node.getParameter().getType());
        if(processor == null) return new ArrayList<>();

        if(processor instanceof ProcessorComplete)
            return ((ProcessorComplete) processor).tabComplete(sender, supplied);

        return new ArrayList<>();
    }

    /**
     * Creates a new processor
     * @param type Type
     * @param processor Processor
     */
    public static void createProcessor(Class<?> type, Processor processor) {
        processors.put(type, processor);
    }

    /**
     * Loads the processors
     */
    public static void loadProcessors() {
        loaded = true;

        processors.put(int.class, new IntegerProcessor());
        processors.put(long.class, new LongProcessor());
        processors.put(double.class, new DoubleProcessor());
        processors.put(boolean.class, new BooleanProcessor());

        processors.put(Integer.class, processors.get(int.class));
        processors.put(Long.class, processors.get(long.class));
        processors.put(Double.class, processors.get(double.class));
        processors.put(Boolean.class, processors.get(boolean.class));

        processors.put(Player.class, new PlayerProcessor());
        processors.put(OfflinePlayer.class, new OfflinePlayerProcessor());
        processors.put(World.class, new WorldProcessor());
        processors.put(Duration.class, new DurationProcessor());
        processors.put(GameMode.class, new GamemodeProcessor());
    }
}
