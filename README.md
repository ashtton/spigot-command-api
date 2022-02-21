# spigot-command-api
This is a Spigot Command API made by me, it's extremely useful & easy to use.
### Features
* Creates usage messages for you
* Automatically parses parameters
* Easily register all your commands
* No need for commands in plugin.yml  
* Makes it easier than ever to create commands
### Parsing
At the moment, this command api will parse the following values for you.\
You can also create custom processors which there is an example of at the bottom of this page.
* **Numbers:** Integer, Long, Double
* **Players:** Player, OfflinePlayer
* **Misc:** World, Boolean, Duration
### Command Example
This example shows you the basics of using the api
```java
// Package: me.gleeming.plugin
public class MainClass extends JavaPlugin {
    public void onEnable() {
        // You initialize all your commands using 
        // the file path to them like this
        CommandHandler.registerCommands("me.gleeming.plugin.commands", this);
        
        // You can also initialize commands using
        // this method, although the above one will
        // register all your commands at once which
        // is not only faster but also cleaner
        CommandHandler.registerCommands(Commands.class, this);
    }
}

// Package: me.gleeming.plugin.commands
public class Commands {
    // All you have to do now is teleport the player
    // The messages like player not found, usage, player only, etc..
    // are handled automatically without the need to worry
    @Command(names = {"teleport"}, permission = "command.teleport", playerOnly = true)
    public void teleportCommand(Player player, @Param(name = "player") Player target) {
        player.teleport(target);
    }
    
    // Concated = true makes it so the rest of the command is 
    // put together automatically making the usage be
    // Command: /msg <player> <reason..>
    @Command(names = {"message", "msg", "tell"}, playerOnly = true)
    public void messageCommand(Player player, @Param(name = "player") Player target, @Param(name = "message", concated = true) String message) {
        target.sendMessage("Player has messaged you " + message);
    }
    
    // You can also make certain things not required like this
    @Command(names = {"eat"}, permission = "command.eat")
    public void eatCommand(CommandSender sender, @Param(name = "target", required = false)) {
        if(target != null) {
            target.setFoodLevel(20);
        } else {
            sender.setFoodLevel(20);
        }
    }
}

// Package: me.gleeming.faction
public class FactionCommands {
    @Command(names = {"f create", "faction create"}, playerOnly = true)
    public void factionCreateCommand(Player player, @Param(name = "name") String name) {
        // Faction create logic here
    }
    
    // You can also create help messages
    // by catching different commands.
    // In this example, if a command could
    // not be executed and starts with f
    // then this message would be displayed
    @Help(names = {"f", "faction"})
    public void factionHelp(CommandSender sender) {
        // Faction help message here
        // Remember: If no help messages are found, a custom
        // usage message will be automatically generated
    }
}
```
### Custom Processor Example
This example shows you how you can make a custom processor to parse your custom objects or parse more objects that you feel should have processors that don't already.
```java
// Package: me.gleeming.plugin
public class MainClass extends JavaPlugin {
    public void onEnable() {
        // You initialize all your processors
        // in your on enable like this:
        
        // Make sure you do this before registering your commands
        ParamProcessor.getProcessors().put(CustomEnum.class, new CustomEnumProcessor());
    }
}

// Package: me.gleeming.plugin.objects
public enum CustomEnum {
    BANANA, HAHA;
}

// Package: me.gleeming.plugin.processors
public class CustomEnum implements Processor {
    public Object process(CommandSender sender, String supplied) {
        try {
            return CustomEnum.valueOf(supplied);
        } catch(Exception ex) {
            sender.sendMessage(ChatColor.RED + "You have entered an invalid value.");
            return null;
        }
    }
}
```
