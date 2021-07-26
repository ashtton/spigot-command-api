package me.gleeming.command.help;

import lombok.Getter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class HelpNode {
    @Getter private static final List<HelpNode> nodes = new ArrayList<>();

    @Getter private final Object parentClass;
    @Getter private final String[] names;
    @Getter private final Method method;
    public HelpNode(Object parentClass, String[] names, Method method) {
        this.parentClass = parentClass;
        this.names = names;
        this.method = method;

        nodes.add(this);
    }
}
