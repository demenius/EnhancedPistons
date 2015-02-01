/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package demenius.enhancedpistons;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockPhysicsEvent;

/**
 *
 * @author Braydon
 */
public class CrusherPistonListener
{
    EnhancedPistons plugin;

    public CrusherPistonListener(EnhancedPistons instance)
    {
        this.plugin = instance;
    }
    
    public boolean blockPhysics(BlockPhysicsEvent ev)
    {
        Block piston = ev.getBlock();
        if (piston.getType() == Material.PISTON_BASE
                || piston.getType() == Material.PISTON_STICKY_BASE)
        {
            if (PistonPower.pistonPowered(piston))
            {
                return crushBlock(piston);
            }
        }
        return false;
    }
    
    public boolean crushBlock(Block piston)
    {
        byte data1 = piston.getData();
        if(data1 > 8)
            return false;
        
        Block crush = piston.getRelative(PistonPower.faces[data1]);
        boolean canCrush = this.plugin.canCrush(crush.getType());
        
        
        Block piston2 = piston.getRelative(PistonPower.faces[data1], 3);
        byte data = piston2.getData();
        
        if(data > 8 && canCrush)
        {
            data -= 8;
            data = PistonPower.oppositeData(data);
            if(data == data1)
            {
                crush.breakNaturally();
                return true;
            }
        }
        return false;
    }
}
