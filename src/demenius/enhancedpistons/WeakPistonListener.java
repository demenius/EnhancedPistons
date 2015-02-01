/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package demenius.enhancedpistons;

import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

/**
 *
 * @author Braydon
 */
public class WeakPistonListener
{

    EnhancedPistons plugin;

    public WeakPistonListener(EnhancedPistons instance)
    {
        this.plugin = instance;
    }

    public void pistonExtended(BlockPistonExtendEvent ev)
    {
        
        List<Block> blocks = ev.getBlocks();
        for (Block block : blocks)
        {
            if (this.plugin.cantMove(block.getType()))
                ev.setCancelled(true);
        }
    }

    public boolean pistonRetracted(BlockPistonRetractEvent ev)
    {
        if(ev.isSticky() && this.plugin.cantMove(ev.getRetractLocation().getBlock().getType()))
        {
            ev.setCancelled(true);
            Block piston = ev.getBlock();
            piston.setData((byte)(piston.getData() - 8));
            
            piston.getRelative(ev.getDirection()).setType(Material.AIR);
            return true;
        }
        return false;
    }
}
