package me.gleeming.command.paramter;

import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.command.CommandSender;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class Processor<T> {

    private final Class<?> type;


    @SneakyThrows
    public Processor() {
        // can cause ClassNotFoundException with Loader based plugins
        Type type = ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        this.type = Class.forName(type.getTypeName());
        ParamProcessor.createProcessor(this);
    }

    // This constructor makes it easier for Loader based plugins to create processors
    // ensures that the Processor constructor has a reference to the type, and doesn't need to use reflection to determine the class.
    @SneakyThrows
    public Processor(Class<?> type) {
        this.type = type;
        ParamProcessor.createProcessor(this);
    }

    /**
     * Process the object
     */
    public abstract T process(CommandSender sender, String supplied);

    /**
     * Processes the tab completion
     */
    public List<String> tabComplete(CommandSender sender, String supplied) {
        return new ArrayList<>();
    }

}
