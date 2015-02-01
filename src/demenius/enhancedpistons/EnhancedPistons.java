/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package demenius.enhancedpistons;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minecraft.server.v1_6_R2.EntityTypes;
import net.minecraft.server.v1_6_R2.WorldServer;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_6_R2.CraftWorld;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Braydon
 */
public class EnhancedPistons extends JavaPlugin
{

    public static final Logger log = Logger.getLogger("Minecraft");
    public static String PLUGIN_NAME = "demenius";
    private ArrayList<Material> strongBlocks = new ArrayList<>();
    private int waitingForStrongBlockClick = -1;
    private ArrayList<Material> weakBlocks = new ArrayList<>();
    private ArrayList<Material> crushBlocks = new ArrayList<>();
    private ArrayList<Material> pushBlocks = new ArrayList<>();
    private ArrayList<Material> pullBlocks = new ArrayList<>();
    private FileConfiguration customConfig = null;
    private File customConfigFile = null;
    
    public boolean changeStrongBlocks(Material mat, int add)
    {
        if(add == 0)
        {
            if(strongBlocks.contains(mat))
                return false;
            else
            {
                ConfigurationSection section = customConfig.getConfigurationSection("StrongPistons");
                if(section.contains("NONE"))
                    section.set("NONE", null);
                section.set(mat.name(), true);
                strongBlocks.add(mat);
                this.saveCustomConfig();
            }
            return true;
        } else if(add == 1)
        {
            if(!strongBlocks.contains(mat))
                return false;
            else
            {
                ConfigurationSection section = customConfig.getConfigurationSection("StrongPistons");
                section.set(mat.name(), null);
                strongBlocks.remove(mat);
                if(strongBlocks.isEmpty())
                    section.set("NONE", true);
                this.saveCustomConfig();
            }
            return true;
        }
        return false;
    }
    
    public int waitingForStrongBlockClick()
    {
        return this.waitingForStrongBlockClick;
    }
    
    public void setWaitingForStrongBlockClick(int var)
    {
        this.waitingForStrongBlockClick = var;
    }

    @Override
    public void onEnable()
    {
        PLUGIN_NAME = getDescription().getName();
        if (this.getCustomConfig() == null)
        {
            this.log(Level.SEVERE, "Config File Null");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        ConfigurationSection ep = customConfig.getConfigurationSection("EnhancedPistons");
        if (ep == null)
        {
            this.log(Level.SEVERE, "EnhancedPistons Key Was Not Found In Config File");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        final boolean strongPistons = ep.getBoolean("StrongPistons");
        final boolean weakPistons = ep.getBoolean("WeakPistons");
        final boolean crusherPistons = ep.getBoolean("CrusherPistons");
        final boolean chestPushPistons = ep.getBoolean("ChestPushPistons");
        final boolean chestPullPistons = ep.getBoolean("ChestPullPistons");
        final boolean veryStickyPistons = false;// ep.getBoolean("VeryStickyPistons");

        if (!strongPistons && !weakPistons && !crusherPistons && !chestPushPistons && !chestPullPistons && !veryStickyPistons)
        {
            this.log(Level.INFO, "No Features Enabled. Disabling Plugin");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        if (chestPullPistons)
        {
            if (this.setupBlocks("PullPistons", this.pullBlocks))
            {
                this.log(Level.INFO, "ChestPullPistons Enabled");
            } else
            {
                this.log(Level.SEVERE, "PullPistons Key Was Not Found In Config File");
            }
        }

        if (strongPistons)
        {
            if (this.setupBlocks("StrongPistons", this.strongBlocks))
            {
                this.log(Level.INFO, "StrongPistons Enabled");
            } else
            {
                this.log(Level.SEVERE, "StrongPistons Key Was Not Found In Config File");
            }
        }


        if (weakPistons)
        {
            if (this.setupBlocks("WeakPistons", this.weakBlocks))
            {
                this.log(Level.INFO, "WeakPistons Enabled");
            } else
            {
                this.log(Level.SEVERE, "WeakPistons Key Was Not Found In Config File");
            }
        }

        if (crusherPistons)
        {
            if (this.setupBlocks("CrusherPistons", this.crushBlocks))
            {
                this.log(Level.INFO, "CrusherPistons Enabled");
            } else
            {
                this.log(Level.SEVERE, "CrusherPistons Key Was Not Found In Config File");
            }
        }

        if (chestPushPistons)
        {
            if (this.setupBlocks("PushPistons", this.pushBlocks))
            {
                this.log(Level.INFO, "ChestPushPistons Enabled");
            } else
            {
                this.log(Level.SEVERE, "PushPistons Key Was Not Found In Config File");
            }
        }

        if (veryStickyPistons)
        {
            this.log(Level.INFO, "VeryStickyPistons Enabled");
        }

        registerEntityType(EntityMovingBlock.class, "FallingBlock", 21);

        getServer().getPluginManager().registerEvents(
                new PistonListener(this, chestPullPistons, chestPushPistons, strongPistons, weakPistons, crusherPistons, veryStickyPistons), this);
        
        getCommand("EnhancedPistons").setExecutor(new EnhancedPistonsCommands(this));
        
        try
        {
            Metrics metrics = new Metrics(this);
            Metrics.Graph graph = metrics.createGraph("Enabled Features");

            graph.addPlotter(createPlotter("Strong Pistons", strongPistons));
            graph.addPlotter(createPlotter("Weak Pistons", weakPistons));
            graph.addPlotter(createPlotter("Crusher Pistons", crusherPistons));
            graph.addPlotter(createPlotter("Chest Push Pistons", chestPushPistons));
            graph.addPlotter(createPlotter("Chest Pull Pistons", chestPullPistons));
            graph.addPlotter(createPlotter("Very Sticky Pistons", veryStickyPistons));

            metrics.start();
        } catch (IOException e)
        {
            this.log(Level.WARNING, "Failed To Load MCStats");
        }
        this.log(Level.INFO, "MCStats Enabled");
        this.log(Level.INFO, "Enabled");
    }

    public static void registerEntityType(Class<?> inClass, String name, int inID)
    {
        try
        {
            Class[] args = new Class[3];
            args[0] = Class.class;
            args[1] = String.class;
            args[2] = Integer.TYPE;

            Method a = EntityTypes.class.getDeclaredMethod("a", args);
            a.setAccessible(true);

            a.invoke(a, new Object[]
                    {
                        inClass, name, Integer.valueOf(inID)
                    });
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
        {
            e.printStackTrace();
        }
    }

    public EntityMovingBlock spawnFallingBlock(Location location, Material material, byte data, BlockFace pistonDir) throws IllegalArgumentException
    {
        Validate.notNull(location, "Location cannot be null");
        Validate.notNull(material, "Material cannot be null");
        Validate.isTrue(material.isBlock(), "Material must be a block");

        double x = location.getBlockX() + 0.5;
        double y = location.getBlockY() + (pistonDir == BlockFace.DOWN ? 0.0 : 0.5);
        double z = location.getBlockZ() + (pistonDir == BlockFace.SOUTH ? 0.5 : 0.5);
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();

        EntityMovingBlock entity = new EntityMovingBlock(world, x, y, z, material.getId(), data);
        

        world.addEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
        return entity;
    }

    private Metrics.Plotter createPlotter(String name, final boolean value)
    {
        return new Metrics.Plotter(name)
        {

            @Override
            public int getValue()
            {
                if (value)
                {
                    return 1;
                }
                return 0;
            }
        };
    }

    private boolean setupBlocks(String sectionName, ArrayList<Material> blocks)
    {
        ConfigurationSection section = customConfig.getConfigurationSection(sectionName);
        if (section == null)
        {
            return customConfig.contains(sectionName);
        }

        Set keys = section.getKeys(false);

        for (Object o : keys)
        {
            String block = (String) o;
            if (block.equals("NONE"))
            {
                break;
            }
            boolean b = section.getBoolean(block);
            if (b)
            {
                Material m = Material.getMaterial(block);
                if (m == null)
                {
                    this.log(Level.WARNING, block + " Is Not A Valid Block");
                    continue;
                }
                blocks.add(Material.getMaterial(block));
            } else
            {
                Material m = Material.getMaterial(block);
                if (m == null)
                {
                    this.log(Level.WARNING, block + " Is Not A Valid Block");
                    continue;
                }
                blocks.remove(Material.getMaterial(block));
            }
        }

        return true;
    }

    @Override
    public void onDisable()
    {
        this.saveCustomConfig();
        this.log(Level.INFO, "Disabled");
    }

    public boolean canMove(Material mat)
    {
        return this.strongBlocks.contains(mat);
    }

    public boolean cantMove(Material mat)
    {
        return this.weakBlocks.contains(mat);
    }

    public boolean canCrush(Material mat)
    {
        return this.crushBlocks.contains(mat);
    }

    public boolean canPush(Material mat)
    {
        return this.pushBlocks.contains(mat);
    }

    public boolean canPull(Material mat)
    {
        return this.pullBlocks.contains(mat);
    }

    public void reloadCustomConfig()
    {
        if (customConfigFile == null)
        {
            customConfigFile = new File(getDataFolder(), "EnhancedPistonsConfig.yml");
            if (!customConfigFile.exists())
            {
                // Look for defaults in the jar
                InputStream defConfigStream = this.getResource("EnhancedPistonsConfig.yml");
                if (defConfigStream != null)
                {
                    customConfig = YamlConfiguration.loadConfiguration(defConfigStream);
                    this.log(Level.INFO, "Default EnhancedPistonsConfig Loaded");
                    this.saveCustomConfig();
                } else
                {
                    this.log(Level.SEVERE, "Config File Cannot Be Loaded. See Help File At");
                    this.log(Level.SEVERE, "http://dev.bukkit.org/server-mods/enhancedpistons/");
                    this.log(Level.SEVERE, "Sorry For The Inconvenience");
                }
            } else
            {
                customConfig = YamlConfiguration.loadConfiguration(customConfigFile);
                this.log(Level.INFO, "EnhancedPistonsConfig Loaded From File");
            }
        }
    }

    public FileConfiguration getCustomConfig()
    {
        if (customConfig == null)
        {
            this.reloadCustomConfig();
        }
        return customConfig;
    }

    public void saveCustomConfig()
    {
        if (customConfig == null || customConfigFile == null)
        {
            return;
        }
        try
        {
            getCustomConfig().save(customConfigFile);
            this.log(Level.INFO, "Config Saved");
        } catch (IOException ex)
        {
            this.log(Level.SEVERE, "Could not save config to " + customConfigFile);
        }
    }

    public static void log(Level level, String info)
    {
        log.log(level, "[" + PLUGIN_NAME + "] " + info);
    }
}
