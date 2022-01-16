package me.gleeming.command.help;

import lombok.Data;
import java.lang.reflect.Method;

@Data
public class HelpNode {
    private final Object parentClass;
    private final String[] names;
    private final Method method;
}
