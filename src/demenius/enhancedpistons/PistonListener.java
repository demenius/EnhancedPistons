/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package demenius.enhancedpistons;

import java.util.HashMap;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 *
 * @author Braydon
 */
public class PistonListener implements Listener
{

    private boolean chestPullPistons;
    private boolean chestPushPistons;
    private boolean strongPistons;
    private boolean weakPistons;
    private boolean crusherPistons;
    private boolean veryStickyPistons;
    private ChestPullPistonListener chestPullPistonsListener;
    private ChestPushPistonListener chestPushPistonListener;
    private CrusherPistonListener crusherPistonListener;
    private StrongPistonListener strongPistonListener;
    private WeakPistonListener weakPistonListener;
    private VeryStickyPistonsListener veryStickyPistonsListener;
    private HashMap<Block, Long> physicsBlocks;
    private HashMap<Block, Long> retractBlocks;
    private EnhancedPistons plugin;

    public PistonListener(EnhancedPistons plugin, boolean cpp, boolean cup, boolean sp, boolean wp, boolean cp, boolean vsp)
    {
        chestPullPistons = cpp;
        chestPushPistons = cup;
        strongPistons = sp;
        weakPistons = wp;
        crusherPistons = cp;
        veryStickyPistons = vsp;

        this.plugin = plugin;

        chestPullPistonsListener = new ChestPullPistonListener(plugin);
        chestPushPistonListener = new ChestPushPistonListener(plugin);
        crusherPistonListener = new CrusherPistonListener(plugin);
        strongPistonListener = new StrongPistonListener(plugin);
        weakPistonListener = new WeakPistonListener(plugin);
        veryStickyPistonsListener = new VeryStickyPistonsListener(plugin);

        this.physicsBlocks = new HashMap<>();
        this.retractBlocks = new HashMap<>();
    }

    private boolean attemptAgain(HashMap<Block, Long> map, Block block)
    {
        long timeNow = System.currentTimeMillis();
        if (timeNow - map.get(block) > 50)
        {
            map.remove(block);
            return true;
        }
        return false;
    }

    @EventHandler
    public void playerEvent(PlayerInteractEvent ev)
    {
        Block block = ev.getClickedBlock();
        Player cs = ev.getPlayer();
        if (block == null)
        {
            return;
        }
        int var = plugin.waitingForStrongBlockClick();
        if (var != -1)
        {
            plugin.setWaitingForStrongBlockClick(-1);
            Material mat = block.getType();
            if (plugin.changeStrongBlocks(mat, var))
            {
                if(var == 0)
                    cs.sendMessage(ChatColor.GREEN + mat.toString() + " added to StrongPiston allow list");
                else
                    cs.sendMessage(ChatColor.GREEN + mat.toString() + " removed from StrongPiston allow list");
            } else
            {
                cs.sendMessage(ChatColor.RED + mat.toString() + " is already in the list");
            }

        }
    }

    @EventHandler
    public void BlockPhysics(BlockPhysicsEvent ev)
    {
        if (this.physicsBlocks.containsKey(ev.getBlock()))
        {
            if (!attemptAgain(this.physicsBlocks, ev.getBlock()))
            {
                return;
            }
        }
        this.physicsBlocks.put(ev.getBlock(), System.currentTimeMillis());


        if (this.chestPushPistons)
        {
            if (this.chestPushPistonListener.blockPhysics(ev))
            {
                return;
            }
        }
        if (this.crusherPistons)
        {
            if (this.crusherPistonListener.blockPhysics(ev))
            {
                return;
            }
        }
        if (this.strongPistons)
        {
            this.strongPistonListener.blockPhysics(ev);
        }
    }

    @EventHandler
    public void BlockPlaced(BlockPlaceEvent ev)
    {
        if (this.strongPistons)
        {
            this.strongPistonListener.pistonPlaced(ev);
        }
    }

    @EventHandler
    public void PistonRetracted(BlockPistonRetractEvent ev)
    {
        if (this.retractBlocks.containsKey(ev.getBlock()))
        {
            if (!attemptAgain(this.retractBlocks, ev.getBlock()))
            {
                return;
            }
        }
        this.retractBlocks.put(ev.getBlock(), System.currentTimeMillis());
        if (this.chestPullPistons)
        {
            if (this.chestPullPistonsListener.PistonRetract(ev))
            {
                return;
            }
        }
        if (this.weakPistons)
        {
            if (this.weakPistonListener.pistonRetracted(ev))
            {
                return;
            }
        }
        if (this.strongPistons)
        {
            this.strongPistonListener.pistonRetract(ev);
        }
    }

    @EventHandler
    public void blockBroken(BlockBreakEvent ev)
    {
        if (this.chestPushPistons)
        {
            this.chestPushPistonListener.blockBroken(ev);
        }
    }

    @EventHandler
    public void PistonExtended(BlockPistonExtendEvent ev)
    {
        if (this.weakPistons)
        {
            this.weakPistonListener.pistonExtended(ev);
        }
    }
}
