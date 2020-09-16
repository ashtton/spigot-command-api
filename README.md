# SpigotCommandAPI

This is a Spigot Command API made by me, it's extremely useful & easy to use.


**How to use**\
Copy and paste the code at the bottom of the page, into a new class called something like 'ACommand'.\
\
In your onEnable() initialize each command by doing
```java
new CommandClassName();
```
After initializing each command here, just create the classes and make them extend ACommand.\
\
Here's an example command
```java
public class ExampleCommand extends ACommand {
    public ExampleCommand() { super("example", "command.grant", "alias1", "alias2"); }

    public void consoleExecute(String label, String[] args) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Console msg");
    }

    public void playerExecute(Player player, String label, String[] args) {
        player.sendMessage(ChatColor.RED + "player msg");
    }
}
```
\
That's all there is to it, you don't need to put your command in your plugin.yml or anything.

**Actual code - put in ACommand.class**
```java
import net.md_5.bungee.api.plugin.*;
import com.qbasic.project.shade.com.google.common.io.*;
import net.md_5.bungee.config.*;
import java.io.*;

public class Config {
    private Configuration config;
    private File file;
    
    public Config(String name, Plugin plugin) {
        this.file = new File(plugin.getDataFolder(), name);
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdir();
    
        if (!this.file.exists()) {
            try {
                this.file.createNewFile();
                try (final InputStream is = plugin.getResourceAsStream(name);
                     final OutputStream os = new FileOutputStream(this.file)) {
                    ByteStreams.copy(is, os);
                }
            }
            catch (Exception e) { e.printStackTrace(); }
        }
        
        try { this.config = ConfigurationProvider.getProvider((Class)YamlConfiguration.class).load(this.file); }
        catch (Exception e) { e.printStackTrace(); }
    }
    
    public void save() {
        try { ConfigurationProvider.getProvider((Class)YamlConfiguration.class).save(this.config, this.file); }
        catch (Exception e) { e.printStackTrace(); }
    }
    
    public Configuration getConfig() { return this.config; }
}
```
