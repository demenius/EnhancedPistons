/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package demenius.enhancedpistons;

import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.block.*;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 *
 * @author Braydon
 */
public class StrongPistonListener
{

    EnhancedPistons plugin;

    public StrongPistonListener(EnhancedPistons instance)
    {
        this.plugin = instance;
    }

    public void pistonPlaced(BlockPlaceEvent ev)
    {
        if (ev.getBlock().getType() == Material.PISTON_BASE
                || ev.getBlock().getType() == Material.PISTON_STICKY_BASE)
        {
            this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new ScheduledPiston(ev.getBlock()));
        }
    }

    /**
     * **********************************************************************
     */
    public void blockPhysics(BlockPhysicsEvent ev)
    {
        Block piston = ev.getBlock();
        if (piston.getType() == Material.PISTON_BASE
                || piston.getType() == Material.PISTON_STICKY_BASE)
        {
            if (PistonPower.pistonPowered(piston))
            {
                changeMovingBlock(piston, piston.getData(), false);
            }
        }
    }

    /**
     * **********************************************************************
     */
    public void pistonRetract(BlockPistonRetractEvent ev)
    {
        if (ev.getBlock().getType() == Material.PISTON_STICKY_BASE)
        {
            byte data = (byte) (ev.getBlock().getData() - 8);
            changeMovingBlock(ev.getBlock(), data, true);
        }
    }

    private boolean changeMovingBlock(Block piston, byte data, boolean retract)
    {
        if (data > 5)
        {
            return false;
        }
        Block move = piston.getRelative(PistonPower.faces[data], retract ? 2 : 1);
        int typeId = move.getTypeId();
        if (!this.plugin.canMove(Material.getMaterial(typeId)))
        {
            return false;
        }
        
        Block next = piston.getRelative(PistonPower.faces[data], 2);

        if (!retract && !(next.isLiquid() || next.isEmpty()))
        {
            if(!changeMovingBlock(move, data, retract))
                return false;
        }

        if (retract)
        {
            data = PistonPower.oppositeData(data);
        }

        if (typeId == Material.CHEST.getId())
        {
            moveChest(piston, move, data, retract);
        } else if (isStorage(typeId))
        {
            moveStorage(piston, move, data, retract);
        } else if (typeId == Material.NOTE_BLOCK.getId())
        {
            moveNoteBlock(piston, move, data, retract);
        } else if (typeId == Material.WALL_SIGN.getId() || typeId == Material.SIGN_POST.getId())
        {
            moveSignBlock(piston, move, data, retract);
        } else
        {
            move2(move, data, retract).runTaskTimer(plugin, 0, 1);
        }
        return true;
    }

    private Block moveBlock(Block piston, Block blockToMove, byte pistonData, boolean retract)
    {
        byte old_data = blockToMove.getData();
        Material type = blockToMove.getType();
        Block block = blockToMove.getRelative(PistonPower.faces[pistonData]);
        if (retract)
        {
            piston.setData(PistonPower.oppositeData(pistonData));
            blockToMove.setType(Material.AIR);
        } else
        {
            piston.setData((byte) (pistonData + 8));
            blockToMove.setType(Material.PISTON_EXTENSION);
            blockToMove.setData(piston.getType() == Material.PISTON_BASE ? pistonData : (byte) (pistonData + 8));
        }

        block.setType(type);
        block.setData(old_data);
        return block;
    }

    private EntityMovingBlockChecker move2(Block blockToMove, final byte pistonData, final boolean retract)
    {
        EntityMovingBlock movingBlock;
        Material blockMaterial = blockToMove.getType();
        byte blockData = blockToMove.getData();
        try
        {
            if ((!blockMaterial.equals(Material.AIR)))
            {
                blockToMove.setType(Material.AIR);
                Location block_loc = blockToMove.getLocation();
                Location to_loc = blockToMove.getRelative(PistonPower.faces[pistonData]).getLocation();
                byte pistonDirData = retract ? PistonPower.oppositeData(pistonData) : pistonData;
                
                BlockFace pistonDirection = PistonPower.faces[pistonDirData];
                BlockFace blockDirection = PistonPower.faces[pistonData];
                
                movingBlock = plugin.spawnFallingBlock(block_loc, blockMaterial, blockData, blockDirection);
                EntityMovingBlockChecker checker = new EntityMovingBlockChecker(movingBlock, block_loc, to_loc, pistonDirection);

                    checker.speed = getVector(blockDirection);
                if(pistonDirection == BlockFace.DOWN && blockDirection == BlockFace.DOWN)
                    checker.speed.setY(0.3F);
                return checker;
            }
        } catch (IllegalArgumentException | IllegalStateException e)
        {
        }
        return null;
    }

    private Vector getVector(BlockFace Direction)
    {
        return getVector(0.5F, Direction);
    }

    private Vector getVector(float power, BlockFace Direction)
    {
        float x = 0.0F;
        float y = 0.00F;
        float z = 0.0F;

        switch (Direction)
        {
            case DOWN:
                y = -power + 0.1F;
                break;
            case UP:
                y = power;
                break;
            case NORTH:
                z = -power;
                break;
            case SOUTH:
                z = power;
                break;
            case WEST:
                x = -power;
                break;
            case EAST:
                x = power;
                break;
        }

        return new Vector(x, y, z);
    }

    private void moveChest(Block piston, Block blockToMove, byte pistonData, boolean retract)
    {
        Chest chest = ((Chest) blockToMove.getState());
        final ItemStack[] stack = chest.getBlockInventory().getContents();
        chest.getBlockInventory().setContents(new ItemStack[]
                {
                });

        EntityMovingBlockChecker checker = move2(blockToMove, pistonData, retract);
        checker.itemStack = stack;
        checker.runTaskTimer(plugin, 0, 1);
    }

    private void moveStorage(Block piston, Block blockToMove, byte pistonData, boolean retract)
    {
        InventoryHolder storage = ((InventoryHolder) blockToMove.getState());

        Integer time = null;
        if (blockToMove.getTypeId() == Material.BREWING_STAND.getId())
        {
            time = ((BrewingStand) blockToMove.getState()).getBrewingTime();
        }
        ItemStack[] stack = storage.getInventory().getContents();
        storage.getInventory().setContents(new ItemStack[]
                {
                });

        Block block = moveBlock(piston, blockToMove, pistonData, retract);
        InventoryHolder newStorage = ((InventoryHolder) blockToMove.getState());
        newStorage.getInventory().setContents(stack);

        if (time != null)
        {
            BrewingStand stand = (BrewingStand) block.getState();
            stand.update(true);
            stand.setBrewingTime(time);
            stand.update(true);
        }
    }

    private void moveNoteBlock(Block piston, Block blockToMove, byte pistonData, boolean retract)
    {
        NoteBlock noteBlock = ((NoteBlock) blockToMove.getState());
        Note note = noteBlock.getNote();

        Block block = moveBlock(piston, blockToMove, pistonData, retract);
        NoteBlock newNoteBlock = (NoteBlock) block.getState();
        newNoteBlock.setNote(note);
        newNoteBlock.update(true);
    }

    private void moveSignBlock(Block piston, Block blockToMove, byte pistonData, boolean retract)
    {
        Sign sign = ((Sign) blockToMove.getState());
        String[] lines = sign.getLines();

        Block block = moveBlock(piston, blockToMove, pistonData, retract);

        Sign newSign = (Sign) block.getState();
        for (int i = 0; i < lines.length; i++)
        {
            newSign.setLine(i, lines[i]);
        }
        newSign.update(true);
    }

    private boolean isStorage(int typeId)
    {
        return typeId == Material.BREWING_STAND.getId()
                || typeId == Material.DISPENSER.getId()
                || typeId == Material.FURNACE.getId()
                || typeId == Material.BURNING_FURNACE.getId();


    }

    private class ScheduledPiston implements Runnable
    {

        private Block block;

        public ScheduledPiston(Block b)
        {
            this.block = b;
        }

        @Override
        public void run()
        {
            plugin.getServer().getPluginManager().callEvent(new BlockPhysicsEvent(block.getWorld().getBlockAt(block.getLocation()), 34));
        }
    }

    public class EntityMovingBlockChecker extends BukkitRunnable
    {

        public ItemStack[] itemStack = null;
        
        public int id = 0;
        EntityMovingBlock movingBlock;
        Location fromLocation;
        Location toLocation;
        BlockFace pistonDirection;
        Vector speed = null;
        
        

        public EntityMovingBlockChecker(EntityMovingBlock m, Location from, Location to, BlockFace pistonDir)
        {
            this.movingBlock = m;
            this.fromLocation = from;
            this.toLocation = to;
            this.pistonDirection = pistonDir;
        }

        @Override
        public void run()
        {
            if(speed != null)
                this.movingBlock.setVelocity(speed);
            
            if (atX() && atY() && atZ())
            {
                this.movingBlock.teleport(this.toLocation);
                Block b = this.movingBlock.getLocation().getBlock();
                b.setTypeId(this.movingBlock.getBlockId(), false);
                b.setData(this.movingBlock.getBlockData());
                if(itemStack != null)
                {
                    Chest chest = ((Chest) b.getState());
                    chest.getBlockInventory().setContents(itemStack);
                }
                this.movingBlock.remove();
                this.cancel();
            }
        }
        
        private boolean atY()
        {
            if(this.fromLocation.getY() == this.toLocation.getY())
            {
                this.movingBlock.getLocation().setY(this.toLocation.getY() + 0.5);
                return true;
            }
            else if(this.fromLocation.getY() > this.toLocation.getY()) // Going Down
            {
                return this.movingBlock.getLocation().getY() <= this.toLocation.getY() + 0.5;
            } else // Going Up
            {    
                return this.movingBlock.getLocation().getY() >= this.toLocation.getY() + 0.5;
            }
        }
        
        private boolean atX()
        {
            if(this.fromLocation.getX() == this.toLocation.getX())
                return true;
            else if(this.fromLocation.getX() > this.toLocation.getX()) // Going WEST
            {
                return this.movingBlock.getLocation().getX() <= this.toLocation.getX() + 0.5;
            } else // Going EAST
            {    
                return this.movingBlock.getLocation().getX() >= this.toLocation.getX() + 0.5;
            }
        }
        
        private boolean atZ()
        {
            if(this.fromLocation.getZ() == this.toLocation.getZ())
                return true;
            else if(this.fromLocation.getZ() > this.toLocation.getZ()) // Going NORTH
            {
                return this.movingBlock.getLocation().getZ() <= this.toLocation.getZ() + 0.5;
            } else // Going SOUTH
            {
                return this.movingBlock.getLocation().getZ() >= this.toLocation.getZ() + 0.5;
            }
        }
    }
}
