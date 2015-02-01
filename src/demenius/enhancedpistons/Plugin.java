/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/*package demenius.enhancedpistons;

import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minecraft.server.v1_6_R2.EntityFallingBlock;
import net.minecraft.server.v1_6_R2.EntityPlayer;
import net.minecraft.server.v1_6_R2.EntityTypes;
import net.minecraft.server.v1_6_R2.WorldServer;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_6_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

public class Plugin extends JavaPlugin
  implements Listener
{
  public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
  {
    if (cmd.getName().equalsIgnoreCase("se"))
    {
      if ((sender instanceof Player))
      {
        if (!hasPermission((Player)sender, "smoothelevator.admin"))
        {
          sender.sendMessage("You do not have permission to do that!");
          return true;
        }
      }
      if (args.length == 0)
      {
        sender.sendMessage("Usage: /se create <type> <height>");
      }
      if (args.length > 0)
      {
        if ((args[0].equalsIgnoreCase("create")) && (args.length > 2))
        {
          if (!(sender instanceof Player))
          {
            sender.sendMessage("Only Players can issue this command!");
            return true;
          }
          Player player = (Player)sender;
          double wait = 1.0D;
          double waittop = 2.0D;
          double speed = 0.3D;
          if (args.length > 4)
          {
            wait = Double.parseDouble(args[3]);
            waittop = Double.parseDouble(args[4]);
          }
          if (args.length > 5)
            speed = Double.parseDouble(args[5]);
          player.sendMessage("Right click the top left elevator block...");
          requests.put(player.getName(), new Request(Integer.parseInt(args[1]), Integer.parseInt(args[2]), wait, waittop, speed));
          return true;
        }
        if (args[0].equalsIgnoreCase("remove"))
        {
          if (!(sender instanceof Player))
          {
            sender.sendMessage("Only Players can issue this command!");
            return true;
          }
          Player player = (Player)sender;
          requests.put(player.getName(), new Request(2, -1));
          player.sendMessage("Right click one of the elevator switches to remove");
        }
        else if (args[0].equalsIgnoreCase("cancel"))
        {
          Player player = (Player)sender;
          requests.remove(player.getName());
          player.sendMessage("Action Canceled.");
        }
        else if (args[0].equalsIgnoreCase("clean"))
        {
          String world;
          String world;
          if ((sender instanceof Player))
          {
            Player player = (Player)sender;
            player.leaveVehicle();
            world = player.getWorld().getName();
          }
          else
          {
            world = "world";
          }
          if (args.length > 1)
            world = args[0];
          World w = Bukkit.getWorld(world);
          for (Entity e : w.getEntities())
          {
            if ((!(e instanceof FloatingBlock)) && (!(e instanceof EntityFallingBlock)))
              continue;
            e.eject();
            e.remove();
            e.teleport(new Location(e.getWorld(), 0.0D, 0.0D, 0.0D));
          }
        }
      }

      return true;
    }
    return false;
  }

  @EventHandler
  public void onInteract(PlayerInteractEvent event) throws SQLException {
    if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
      return;
    if (requests.containsKey(event.getPlayer().getName()))
    {
      Request r = (Request)requests.get(event.getPlayer().getName());
      Player player = event.getPlayer();
      if (r.height >= 0)
      {
        if (r.stage == 0)
        {
          r.loc0 = event.getClickedBlock().getLocation();
          player.sendMessage("Elevator Corner Set!\n\nRight Click Bottom Right Corner...");
          r.stage += 1;
          event.setCancelled(true);
          return;
        }
        if (r.stage == 1)
        {
          r.loc3 = event.getClickedBlock().getLocation();
          player.sendMessage("Elevator Cuboid Set!\n\nRight Click Top Right Riding Area...");
          r.stage += 1;
          event.setCancelled(true);
          return;
        }
        if (r.stage == 2)
        {
          r.loc4 = event.getClickedBlock().getLocation();
          player.sendMessage("Riding Corner Set!\n\nRight Click Bottom Right Corner...");
          r.stage += 1;
          event.setCancelled(true);
          return;
        }
        if (r.stage == 3)
        {
          r.loc5 = event.getClickedBlock().getLocation();
          player.sendMessage("Riding Cuboid Set!\n\nRight Click Bottom Button...");
          r.stage += 1;
          event.setCancelled(true);
          return;
        }
        if (r.stage == 4)
        {
          r.loc1 = event.getClickedBlock().getLocation();
          player.sendMessage("Button Location Set!\n\nRight Click Top Floor Button...");
          r.stage += 1;
          event.setCancelled(true);
          return;
        }
        if (r.stage == 5)
        {
          r.loc2 = event.getClickedBlock().getLocation();
          player.sendMessage("Button Location Set!");
          Location l = r.loc0;
          ResultSet rs = mysql.query("INSERT OR REPLACE INTO elevators VALUES (" + l.getX() + ", " + l.getY() + ", " + l.getZ() + ", '" + l.getWorld().getName() + "', " + r.loc1.getX() + ", " + r.loc1.getY() + ", " + r.loc1.getZ() + ", " + r.loc2.getX() + ", " + r.loc2.getY() + ", " + r.loc2.getZ() + ", " + r.loc3.getX() + ", " + r.loc3.getY() + ", " + r.loc3.getZ() + ", " + r.loc4.getX() + ", " + r.loc4.getY() + ", " + r.loc4.getZ() + ", " + r.loc5.getX() + ", " + r.loc5.getY() + ", " + r.loc5.getZ() + ", " + r.type + ", " + r.height + ", " + r.wait + ", " + r.waittop + ", " + r.speed + ");");
          rs.close();
          requests.remove(player.getName());
          player.sendMessage("Elevator successfully created!");
          return;
        }
      }
      else if (r.height == -1)
      {
        Location l = event.getClickedBlock().getLocation();
        ResultSet rs = mysql.query("select * from elevators where (x1 = " + l.getX() + " and y1 = " + l.getY() + " and z1 = " + l.getZ() + ") OR ( x2 = " + l.getX() + " and y2 = " + l.getY() + " and z2 = " + l.getZ() + ");");
        if (!rs.next())
        {
          event.getPlayer().sendMessage("This block isn't registered as a switch!");
          return;
        }
        rs.close();
        rs = mysql.query("delete from elevators where (x1 = " + l.getX() + " and y1 = " + l.getY() + " and z1 = " + l.getZ() + ") OR ( x2 = " + l.getX() + " and y2 = " + l.getY() + " and z2 = " + l.getZ() + ");");
        requests.remove(player.getName());
        rs.close();
        player.sendMessage("Elevator successfully removed!");
        return;
      }
    }
    else if (event.getClickedBlock().getType() != Material.AIR)
    {
      Location l = event.getClickedBlock().getLocation();
      ResultSet rs = mysql.query("select * from elevators where (x1 = " + l.getX() + " and y1 = " + l.getY() + " and z1 = " + l.getZ() + ");");
      try
      {
        if (!rs.next())
        {
          rs = mysql.query("select * from elevators where (x2 = " + l.getX() + " and y2 = " + l.getY() + " and z2 = " + l.getZ() + ");");
          if (!rs.next())
            return;
          long wait = ()(rs.getDouble("wait") * 2.0D);
          if (wait < 0L) {
            wait = 20L;
          }
          getServer().getScheduler().scheduleSyncDelayedTask(this, new ElevateDelay(this, event.getPlayer(), rs, false, true), wait);
        }
        else
        {
          long wait = ()(rs.getDouble("wait") * 2.0D);
          if (wait < 0L) {
            wait = 20L;
          }
          getServer().getScheduler().scheduleSyncDelayedTask(this, new ElevateDelay(this, event.getPlayer(), rs, true, true), wait);
          event.setCancelled(true);
        }
      }
      catch (Exception e)
      {
        e.printStackTrace();
        rs.close();
      }
    }
  }

  public FloatingBlock spawnFallingBlock(Location location, Material material, byte data) throws IllegalArgumentException
  {
    Validate.notNull(location, "Location cannot be null");
    Validate.notNull(material, "Material cannot be null");
    Validate.isTrue(material.isBlock(), "Material must be a block");

    double x = location.getBlockX() + 0.5D;
    double y = location.getBlockY() + 1.5D;
    double z = location.getBlockZ() + 0.5D;
    WorldServer world = ((CraftWorld)location.getWorld()).getHandle();

    FloatingBlock entity = new FloatingBlock(world, x, y, z, material.getId(), data);
    entity.c = 1;

    world.addEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
    return entity;
  }

  public void Elevate(Location l, float height, Player player, double speed)
  {
    Location ol = l.add(0.0D, 0.0D, 0.0D);
    Material idd = ol.getBlock().getType();
    byte data = ol.getBlock().getData();
    ol.getBlock().setTypeId(0);
    FloatingBlock f = spawnFallingBlock(l, idd, data);
    if (player != null)
    {
      ((CraftPlayer)player).getHandle().mount(f);
    }

    ElevatorChecker ele = new ElevatorChecker(0, f, height + 1.0F, l, player);
    ele.speed = speed;
    int id = getServer().getScheduler().scheduleSyncRepeatingTask(this, ele, 0L, 1L);
    ele.id = id;
  }

  public void Elevate(Location l, float height, Player[] player)
  {
  }

  public void Elevate(float height, Player player)
  {
    Elevate(player.getLocation().getBlock().getLocation(), height, player, 0.3D);
  }

  public void Elevate(Location l, float height, double speed)
  {
    Location ol = l.add(0.0D, 0.0D, 0.0D);
    Material idd = ol.getBlock().getType();
    byte data = ol.getBlock().getData();
    ol.getBlock().setTypeId(0);
    FloatingBlock f = spawnFallingBlock(l, idd, data);
    ElevatorChecker ele = new ElevatorChecker(0, f, height + 1.0F, l);
    ele.speed = speed;
    System.out.println(f.getBlockId() + ", " + Material.LADDER.getId());
    int id = getServer().getScheduler().scheduleSyncRepeatingTask(this, ele, 0L, 1L);
    ele.id = id;
  }

  public class BlockPlacer implements Runnable {
    int id = 0;
    int data = 0;
    Location l;

    public BlockPlacer(int bID, int data, Location loc) {
      this.id = bID;
      this.data = data;
      this.l = loc;
    }

    public void run()
    {
    }
  }

  class ElevateDelay
    implements Runnable
  {
    Player player;
    ResultSet rs;
    boolean up = false;
    Plugin p;
    boolean spring;

    public void run()
    {
      try
      {
        int height = this.rs.getInt("height");
        if (!this.up)
          height = -height;
        Location l = new Location(Bukkit.getWorld(this.rs.getString("World")), this.rs.getInt("X"), this.rs.getInt("Y"), this.rs.getInt("Z"));
        Location l2 = new Location(Bukkit.getWorld(this.rs.getString("World")), this.rs.getInt("X3"), this.rs.getInt("Y3"), this.rs.getInt("Z3"));
        Location l3 = new Location(Bukkit.getWorld(this.rs.getString("World")), this.rs.getInt("X4"), this.rs.getInt("Y4"), this.rs.getInt("Z4"));
        Location l4 = new Location(Bukkit.getWorld(this.rs.getString("World")), this.rs.getInt("X5"), this.rs.getInt("Y5"), this.rs.getInt("Z5"));
        Location distance = l.clone().subtract(l2);
        double record = 1.38D;
        try
        {
          this.player = null;
          if ((l.getBlock().getType() == Material.AIR) || (l.getBlock().getType() == Material.WATER) || (l.getBlock().getType() == Material.STATIONARY_WATER))
          {
            if (!this.up)
            {
              l.add(0.0D, -height, 0.0D);
              l2.add(0.0D, -height, 0.0D);
              l3.add(0.0D, -height, 0.0D);
              l4.add(0.0D, -height, 0.0D);
            }
            else
            {
              l.add(0.0D, height, 0.0D);
              l2.add(0.0D, height, 0.0D);
              l3.add(0.0D, height, 0.0D);
              l4.add(0.0D, height, 0.0D);
              height = -height;
            }

          }
          else if (!this.up) {
            height = -height;
          }

          if (distance.getX() < 0.0D)
            distance.setX(-distance.getX());
          if (distance.getZ() < 0.0D)
            distance.setZ(-distance.getZ());
          if (distance.getX() > 0.0D) {
            record += distance.getX() + 0.2D;
          }
          List playerList = new ArrayList();
          for (Entity e : l.getChunk().getEntities())
          {
            if (!(e instanceof Player)) {
              continue;
            }
            if (e.getLocation().distance(l) >= record)
              continue;
            if (this.player == null)
              this.player = ((Player)e);
            playerList.add((Player)e);
          }

          for (Entity e : l2.getChunk().getEntities())
          {
            if (!(e instanceof Player))
            {
              continue;
            }

            if (this.player == null)
              this.player = ((Player)e);
            playerList.add((Player)e);
          }

          Location loc1 = l3;
          Location loc2 = l4;

          int startX = Math.min(loc1.getBlockX(), loc2.getBlockX());
          int endX = Math.max(loc1.getBlockX(), loc2.getBlockX());

          int startY = Math.min(loc1.getBlockY(), loc2.getBlockY());
          int endY = Math.max(loc1.getBlockY(), loc2.getBlockY());

          int startZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
          int endZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());

          Iterator i = playerList.iterator();
          Player p = null;
          try
          {
            p = (Player)i.next();
          }
          catch (Exception localException1)
          {
          }

          List mountedLocs = new ArrayList();
          HashMap playerMounts = new HashMap();
          if (p != null)
          {
            for (int x = startX; x <= endX; x++) {
              for (int y = startY; y <= endY; y++) {
                for (int z = startZ; z <= endZ; z++)
                {
                  Location loc = new Location(l.getWorld(), x, y, z);
                  try
                  {
                    System.out.println("ElevatorChecker started!attempt");
                    if ((!p.getLocation().getBlock().getLocation().subtract(0.0D, 1.0D, 0.0D).equals(loc)) || (loc.getBlock().getType() == Material.AIR) || (loc.getBlock().getType() == Material.LADDER) || (loc.getBlock().getType() == Material.WOODEN_DOOR) || (loc.getBlock().getType() == Material.FENCE_GATE) || (loc.getBlock().getType() == Material.IRON_DOOR) || (loc.getBlock().getType() == Material.TORCH)) {
                      continue;
                    }
                    mountedLocs.add(loc);

                    Plugin.this.Elevate(loc, height, p, this.rs.getDouble("speed"));
                    p = (Player)i.next();
                  }
                  catch (Exception e)
                  {
                    e.printStackTrace();
                  }
                }
              }
            }

          }

          loc1 = l;
          loc2 = l2;

          startX = Math.min(loc1.getBlockX(), loc2.getBlockX());
          endX = Math.max(loc1.getBlockX(), loc2.getBlockX());

          startY = Math.min(loc1.getBlockY(), loc2.getBlockY());
          endY = Math.max(loc1.getBlockY(), loc2.getBlockY());

          startZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
          endZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());

          for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
              for (int z = startZ; z <= endZ; z++)
              {
                Location loc = new Location(l.getWorld(), x, y, z);
                try
                {
                  if ((!mountedLocs.contains(loc)) && (loc.getBlock().getType() != Material.AIR) && ((loc.getBlock().getType() == Material.LADDER) || (loc.getBlock().getType() == Material.WOODEN_DOOR) || (loc.getBlock().getType() == Material.FENCE_GATE) || (loc.getBlock().getType() == Material.IRON_DOOR) || (loc.getBlock().getType() == Material.TORCH))) {
                    Plugin.this.Elevate(loc, height, this.rs.getDouble("speed"));
                  }
                }
                catch (Exception localException2)
                {
                }
              }
            }
          }

          for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
              for (int z = startZ; z <= endZ; z++)
              {
                Location loc = new Location(l.getWorld(), x, y, z);
                try
                {
                  if ((!mountedLocs.contains(loc)) && (loc.getBlock().getType() != Material.AIR) && (loc.getBlock().getType() != Material.LADDER) && (loc.getBlock().getType() != Material.WOODEN_DOOR) && (loc.getBlock().getType() != Material.FENCE_GATE) && (loc.getBlock().getType() != Material.IRON_DOOR) && (loc.getBlock().getType() != Material.TORCH)) {
                    Plugin.this.Elevate(loc, height, this.rs.getDouble("speed"));
                  }
                }
                catch (Exception localException3)
                {
                }

              }

            }

          }

        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
        if ((this.spring) && (this.rs.getInt("type") == 0))
        {
          double speed = this.rs.getDouble("speed");
          speed *= 20.0D;
          speed *= height;
          Plugin.this.getServer().getScheduler().scheduleSyncDelayedTask(this.p, new ElevateDelay(Plugin.this, this.p, this.player, this.rs, !this.up, false), ()(this.rs.getDouble("waittop") * 20.0D + speed));
        }
        else {
          this.rs.close();
        }
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }

    public ElevateDelay(Plugin pl, Player p, ResultSet r, boolean up, boolean spring)
    {
      this.player = p;
      this.rs = r;
      this.up = up;
      this.p = pl;
      this.spring = spring;
    }
  }

  public class ElevatorChecker implements Runnable
  {
    public int id = 0;
    FloatingBlock ff;
    float fheight;
    Location fl;
    Player player;
    boolean atTop = false;
    boolean noPlayer = false;
    double speed = 0.3D;

    public ElevatorChecker(int id, FloatingBlock f, float height, Location l)
    {
      this(id, f, height, l, null);
      this.noPlayer = true;
    }

    public ElevatorChecker(int id, FloatingBlock f, float height, Location l, Player p)
    {
      this.id = id;
      this.ff = f;
      this.fheight = height;
      this.fl = l;
      this.player = p;
      if (this.player == null)
        this.noPlayer = true;
    }

    public void run()
    {
      if (this.fheight > 0.0F)
        this.ff.setVelocity(new Vector(0.0D, this.speed, 0.0D));
      else this.ff.setVelocity(new Vector(0.0D, -this.speed, 0.0D));
      boolean ready = this.ff.getLocation().getY() >= this.fl.getY() + this.fheight - 0.2D;
      if (this.fheight < 0.0F) {
        ready = this.ff.getLocation().getY() <= this.fl.getY() + this.fheight + 1.0D;
      }
      if (ready)
      {
        if (this.ff.getBlockId() == Material.LADDER.getId())
        {
          Bukkit.getLogger().log(Level.ALL, "DERP");
          try
          {
            Thread.sleep(1L);
          }
          catch (InterruptedException e)
          {
            e.printStackTrace();
          }
        }
        this.fl = this.fl.add(0.0D, this.fheight - 1.0F, 0.0D);
        this.ff.teleport(this.fl);
        this.atTop = true;
        Block b = this.ff.getLocation().getBlock();
        b.setTypeId(this.ff.getBlockId(), false);
        b.setData(this.ff.getBlockData());
        this.ff.remove();
        if (!this.noPlayer)
        {
          this.fl.add(0.5D, 1.0D, 0.5D);
          this.fl.setYaw(this.player.getLocation().getYaw());
          this.fl.setPitch(this.player.getLocation().getPitch());
          if (this.fheight < 0.0F)
            this.fl.add(0.0D, 1.5D, 0.0D);
          this.player.teleport(this.fl);
        }
        Bukkit.getScheduler().cancelTask(this.id);
      }
    }
  }

  public class Request
  {
    public int type;
    public int height;
    public int stage = 0;
    public double wait;
    public double waittop;
    public double speed;
    public Location loc0;
    public Location loc1;
    public Location loc2;
    public Location loc3;
    public Location loc4;
    public Location loc5;

    public Request(int type, int height)
    {
      this(type, height, 1.0D, 2.0D, 0.3D);
    }

    public Request(int type, int height, double wait, double waittop, double speed)
    {
      this.type = type;
      this.height = height;
      this.wait = wait;
      this.waittop = waittop;
      this.speed = speed;
      System.out.println(speed);
    }
  }
}*/