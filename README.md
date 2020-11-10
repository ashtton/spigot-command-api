# spigot-command-api
This is a Spigot Command API made by me, it's extremely useful & easy to use.


**How to use**\
Copy and paste the code at the bottom of the page, into a new class called something like 'ACommand'.\
\
In your onEnable() initialize each command by doing
```java
new ExampleCommand();
```
After initializing each command here, just create the classes and make them extend ACommand.\
\
Here's an example command
```java
public class ExampleCommand extends HCommand {
    // You can now register how ever many subcommands and regular commands you'd like
    
    // This is a basic command
    // The syntax for this would just be /example
    @Command(name = "example")
    public void exampleCommand(Arguments arguments) {
        arguments.getPlayer().sendMessage(ChatColor.GREEN + "Hello!");
    }

    // This is an example of a command with an option for
    // the player to fill in the command argument.
    // The syntax for this command would be /example teleport <player-name>
    @Command(name = "example.teleport.<player>", permission = "command.teleport", aliases = new String[] { tp }, playersOnly = true)
    public void teleportCommand(Arguments arguments) {
        // Now we can get the first argument as there
        // is only one unknown argument in this command.
        
        arguments.getPlayer().teleport(Bukkit.getPlayer(arguments.getArguments().get(0)));
    }

    // You can also have help pages for if the base command is found
    // although a subcommand is not found for it.
    @Help(command = "example")
    public void helpMessage(CommandSender sender) {
        sender.sendMessage("You can use /example teleport <player> or /example");   
    }    
}
```
\
That's all there is to it, you don't need to put your command in your plugin.yml or anything.

**Actual code - put in HCommand.class**
```java
public abstract class HCommand {
    private final Object obj;

    private final HashMap<String, List<CommandHandler>> handlers = new HashMap<>();
    private final HashMap<String, Method> helpMessages = new HashMap<>();
    public HCommand() {
        this.obj = this;

        HashMap<String, org.bukkit.command.Command> commands = new HashMap<>();
        for(Method method : obj.getClass().getMethods()) {
            Command commandAnnotation = method.getAnnotation(Command.class);

            if(commandAnnotation != null) {
                String name = commandAnnotation.name().split("\\.")[0];

                List<CommandHandler> list = handlers.get(name);
                if(list == null) list = new ArrayList<>();
                
                list.add(new CommandHandler(method, commandAnnotation));
                handlers.put(name.toLowerCase(), list);

                if(commands.get(name.toLowerCase()) == null) {
                    commands.put(name.toLowerCase(), new org.bukkit.command.Command(name.toLowerCase()) {
                        public boolean execute(CommandSender sender, String label, String[] args) {
                            Player player = null;
                            if(sender instanceof Player) player = (Player) sender;

                            handle(commandAnnotation.name().split("\\.")[0], new Arguments(player, sender, label, Arrays.asList(args)));

                            return false;
                        }
                    });

                    commands.get(name.toLowerCase()).setAliases(Arrays.asList(commandAnnotation.aliases()));

                    try {
                        Field cmdMap = Core.getInstance().getPlugin().getServer().getClass().getDeclaredField("commandMap");
                        cmdMap.setAccessible(true);

                        ((org.bukkit.command.CommandMap)    cmdMap.get(Core.getInstance().getPlugin().getServer())).register(Core.getInstance().getPlugin().getDescription().getName(), commands.get(name.toLowerCase()));
                    } catch(Exception ex) { ex.printStackTrace(); }
                }
            }
        }

        for(Method method : obj.getClass().getMethods()) {
            Help helpAnnotation = method.getAnnotation(Help.class);
            if(helpAnnotation != null) helpMessages.put(helpAnnotation.command(), method);
        }
    }

    @SneakyThrows
    public void handle(String command, Arguments commandArgs) {
        List<String> arguments = new ArrayList<>(Collections.singletonList(command));
        arguments.addAll(commandArgs.getArguments());

        for(CommandHandler handler : handlers.get(command.toLowerCase())) {
            if(handler.getArguments().size() == arguments.size()) {
                int currentArgument = 0;
                boolean matches = true;
                for(CommandHandler.Argument argument : handler.getArguments()) {
                    if(argument instanceof CommandHandler.Argument.KnownArgument && !arguments.get(currentArgument).equals(argument.getArgument())) matches = false;

                    currentArgument++;
                }

                if(matches) {
                    if(!commandArgs.getSender().hasPermission(handler.getPermission())) {
                        commandArgs.getSender().sendMessage(ChatColor.RED + "No permission.");
                        return;
                    }

                    if(handler.isPlayersOnly() && !(commandArgs.getSender() instanceof Player)) {
                        commandArgs.getSender().sendMessage(ChatColor.RED + "You must be a player to execute this command!");
                        return;
                    }

                    List<String> unknownArguments = new ArrayList<>();
                    currentArgument = 0;
                    for(CommandHandler.Argument argument : handler.getArguments()) {
                        if(argument instanceof CommandHandler.Argument.UnknownArgument) unknownArguments.add(arguments.get(currentArgument));

                        currentArgument++;
                    }

                    handler.getMethod().invoke(obj, new Arguments(commandArgs.getPlayer(), commandArgs.getSender(), commandArgs.getLabel(), unknownArguments));

                    return;
                }
            }
        }

        if(helpMessages.get(command) != null) helpMessages.get(command).invoke(obj, commandArgs.getSender());
        else commandArgs.getSender().sendMessage(ChatColor.RED + "Invalid arguments.");
    }

    public static class CommandHandler {
        @Getter private final Method method;

        @Getter private final String name;
        @Getter private final String permission;
        @Getter private final String[] aliases;
        @Getter private final boolean playersOnly;

        @Getter private final List<Argument> arguments = new ArrayList<>();
        public CommandHandler(Method method, Command command) {
            this.method = method;

            this.name = command.name();
            this.permission = command.permission();
            this.aliases = command.aliases();
            this.playersOnly = command.playerOnly();

            for(String s : name.split("\\.")) {
                if(s.startsWith("<") && s.endsWith(">")) arguments.add(new Argument.UnknownArgument(s.substring(1, s.length() - 2)));
                else arguments.add(new Argument.KnownArgument(s));
            }
        }

        public static abstract class Argument {
            @Getter private final String argument;
            public Argument(String argument) { this.argument = argument; }

            public static class KnownArgument extends Argument { public KnownArgument(String argument) { super(argument); }}
            public static class UnknownArgument extends Argument { public UnknownArgument(String lookingFor) { super(lookingFor); }}
        }
    }

    public static class Arguments {
        @Getter private final Player player;
        @Getter private final CommandSender sender;
        @Getter private final String label;
        @Getter private final List<String> arguments;
        public Arguments(Player player, CommandSender sender, String label, List<String> arguments) {
            this.player = player;
            this.sender = sender;
            this.label = label;
            this.arguments = arguments;
        }
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Command {
        String name();
        String permission() default "";
        String[] aliases() default {};
        boolean playerOnly() default true;
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Help {
        String command();
    }
}
```
