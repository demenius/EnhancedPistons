package demenius.enhancedpistons;

import org.bukkit.block.BlockFace;
import org.bukkit.event.Listener;

public class VeryStickyPistonsListener implements Listener
{

    EnhancedPistons plugin;
    private BlockFace[] faces = new BlockFace[]
    {
        BlockFace.DOWN, BlockFace.UP, BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH
    };

    public VeryStickyPistonsListener(EnhancedPistons instance)
    {
        this.plugin = instance;
        //this.plugin.log(Level.WARNING, "VeryStickyPistons Is Not Implemented Yet.");
    }
}