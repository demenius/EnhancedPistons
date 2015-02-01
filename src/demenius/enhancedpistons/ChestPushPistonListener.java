/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package demenius.enhancedpistons;

import java.util.HashMap;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Braydon
 */
public class ChestPushPistonListener implements Listener
{

    private EnhancedPistons plugin;
    private HashMap<Block, Long> blocks;

    public ChestPushPistonListener(EnhancedPistons instance)
    {
        this.plugin = instance;
        this.blocks = new HashMap<>();
    }
    
    @EventHandler
    public void blockBroken(BlockBreakEvent ev)
    {
        Block block = ev.getBlock();
        if(this.blocks.containsKey(block))
        {
            this.blocks.remove(block);
        }
    }

    public boolean blockPhysics(BlockPhysicsEvent ev)
    {
        Block piston = ev.getBlock();
        if (piston.getType() == Material.PISTON_BASE
                || piston.getType() == Material.PISTON_STICKY_BASE)
        {
            if (PistonPower.pistonPowered(piston))
            {
                return pushIntoChest(piston);
            }
        }
        return false;
    }

    private boolean pushIntoChest(Block piston)
    {
        byte data = piston.getData();
        if (data > 8)
        {
            return false;
        }
        Block move = piston.getRelative(PistonPower.faces[data]);
        if (move.getTypeId() == Material.AIR.getId() || move.isLiquid())
        {
            return false;
        }
        if (!plugin.canPush(move.getType()))
        {
            return false;
        }
        if (this.blocks.containsKey(move))
        {
            if(!attemptAgain(move))
                return false;
        }

        this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new ScheduledItemInsert(piston, data, move));
        return true;
    }
    
    private boolean attemptAgain(Block block)
    {
        long timeNow = System.currentTimeMillis();
        if(timeNow - this.blocks.get(block) > 250)
        {
            this.blocks.put(block, timeNow);
            return true;
        }
        return false;
    }

    private class ScheduledItemInsert implements Runnable
    {

        private Block piston;
        private byte data;
        private Block move;

        public ScheduledItemInsert(Block piston, byte data, Block move)
        {
            this.piston = piston;
            this.data = data;
            this.move = move;
            blocks.put(move, System.currentTimeMillis());
        }

        @Override
        public void run()
        {
            Block chestBlock = piston.getRelative(PistonPower.faces[data], 2);
            if (chestBlock.getType().getId() != Material.CHEST.getId())
            {
                return;
            }

            Chest chest = (Chest) chestBlock.getState();
            Inventory inv = chest.getInventory();
            HashMap<Integer, ItemStack> stuff = (HashMap<Integer, ItemStack>) inv.all(move.getType());
            boolean in = false;
            if (stuff.size() > 0)
            {
                for (Integer key : stuff.keySet())
                {
                    ItemStack goods = stuff.get(key);
                    if (goods.getAmount() < goods.getMaxStackSize())
                    {
                        goods.setAmount(goods.getAmount() + 1);
                        in = true;
                        break;
                    }
                }
            }
            if (!in)
            {
                int slot = inv.firstEmpty();
                if (slot == -1)
                {
                    return;
                }
                inv.addItem(new ItemStack(move.getType(), 1));
                in = true;
            }
            if (in)
            {
                move.setType(Material.AIR);
                blocks.remove(move);
            }
        }
    }
}
