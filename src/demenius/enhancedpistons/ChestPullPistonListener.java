/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package demenius.enhancedpistons;

import java.util.HashMap;
import java.util.logging.Level;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Braydon
 */
public class ChestPullPistonListener
{

    EnhancedPistons plugin;
    private HashMap<Block, Long> blocks;

    public ChestPullPistonListener(EnhancedPistons instance)
    {
        this.plugin = instance;
        this.blocks = new HashMap<>();
    }

    public boolean PistonRetract(BlockPistonRetractEvent ev)
    {
        Block piston = ev.getBlock();
        if (piston.getType().getId() != Material.PISTON_STICKY_BASE.getId())
        {
            return false;
        }
        byte data = (byte) (piston.getData() - 8);
        Block chestBlock = piston.getRelative(PistonPower.faces[data], 2);
        if (chestBlock.getType().getId() != Material.CHEST.getId())
        {
            return false;
        }
        Chest chest = (Chest) chestBlock.getState();
        Inventory inv = chest.getInventory();
        ItemStack[] stacks = inv.getContents();
        for (int i = 0; i < stacks.length; i++)
        {
            ItemStack stack = stacks[i];
            if (stack == null)
            {
                continue;
            }
            if (this.plugin.canPull(stack.getType()))
            {
                Block block = piston.getRelative(PistonPower.faces[data]);
                if (blocks.containsKey(block))
                {
                    if (!attemptAgain(block))
                    {
                        return false;
                    }
                }
                Material mat = stack.getType();
                stack.setAmount(stack.getAmount()-1);
                if(stack.getAmount() == 0)
                {
                    stacks[i] = null;
                    inv.setContents(stacks);
                }
                if (mat.isBlock())
                {
                    this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new ScheduledBlockMove(block, mat.getId()));
                    return true;
                } else
                {
                    piston.getWorld().dropItem(block.getLocation(), new ItemStack(mat));
                    return true;
                }
            }
        }
        return false;
    }

    private boolean attemptAgain(Block block)
    {
        long timeNow = System.currentTimeMillis();
        if (timeNow - this.blocks.get(block) > 250)
        {
            this.blocks.put(block, timeNow);
            return true;
        }
        return false;
    }

    private class ScheduledBlockMove implements Runnable
    {

        private Block block;
        private int materialID;
        private int ran;

        public ScheduledBlockMove(Block block, int matID)
        {
            this.block = block;
            this.materialID = matID;
            this.ran = 0;
            blocks.put(block, System.currentTimeMillis());
        }

        @Override
        public void run()
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    while (block.getTypeId() != Material.AIR.getId())
                    {
                    }
                    while (block.getTypeId() != materialID)
                    {
                        block.setTypeId(materialID);
                    }
                    blocks.remove(block);
                }
            }).start();
        }
    }
}
