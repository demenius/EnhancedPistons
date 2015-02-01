/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package demenius.enhancedpistons;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

/**
 *
 * @author Braydon
 */
public class PistonPower
{
    public static EnhancedPistons plugin;
    
    public static BlockFace[] faces = new BlockFace[]
    {
        BlockFace.DOWN, BlockFace.UP, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST
    };
    
    public static void setPlugin(EnhancedPistons p)
    {
        plugin = p;
    }
    
    public static boolean pistonPowered(Block piston)
    {
        return pistonPoweredAboveDiag(piston) || aboveBlockPowered(piston) || pistonNormalPowered(piston);
    }
    
    private static boolean pistonNormalPowered(Block piston)
    {
        byte data = piston.getData();
        if (data < 8)
        {
            Block b = piston.getRelative(faces[data]);
            int id = b.getType().getId();
            if (id == Material.REDSTONE_TORCH_ON.getId() || (id == Material.REDSTONE_WIRE.getId() && b.getData() > 0) || id == Material.DIODE_BLOCK_ON.getId())
            {
                return checkForOtherPower(piston);
            }
            if (piston.getBlockPower() > 0)
            {
                return true;
            }
        }
        return false;
    }

    private static boolean checkForOtherPower(Block piston)
    {
        byte data = piston.getData();
        for (int i = 0; i < faces.length; i++)
        {
            if (i != data)
            {
                if (piston.getBlockPower(faces[i]) > 0 || piston.getRelative(faces[i]).getBlockPower(faces[i]) > 0)
                {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean pistonPoweredAboveDiag(Block piston)
    {
        Block above = piston.getRelative(BlockFace.UP);
        int id = above.getType().getId();
        if (id == Material.AIR.getId() || id == Material.PISTON_BASE.getId() || id == Material.PISTON_STICKY_BASE.getId())
        {
            for (int i = 2; i < faces.length; i++)
            {
                Block b = above.getRelative(faces[i]);
                if (b.getType().getId() == Material.REDSTONE_TORCH_ON.getId())
                {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean aboveBlockPowered(Block piston)
    {
        if (piston.getRelative(BlockFace.UP).getBlockPower() > 0)
        {
            return true;
        }
        return false;
    }
    
    public static byte oppositeData(byte data)
    {
        if (data % 2 == 0)
        {
            return (byte)(data + 1);
        } else
        {
            return (byte) (data - 1);
        }
    }
}
