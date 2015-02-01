/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package demenius.enhancedpistons;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Braydon
 */
public class EnhancedPistonsCommands implements CommandExecutor
{

    private EnhancedPistons plugin;

    public EnhancedPistonsCommands(EnhancedPistons p)
    {
        super();
        this.plugin = p;
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String commandLabel, String[] args)
    {
        if (cmnd.getName().equalsIgnoreCase("EnhancedPistons"))
        {
            if (args.length < 2)
            {
                return false;
            }
            if (args[0].equalsIgnoreCase("-s"))
            {
                String msg = "";
                String msg2 = "";
                int add = 0;
                if (args[1].equalsIgnoreCase("-a"))
                {
                    msg = " added to StrongPiston allow list";
                    msg2 = "Click block to add to StrongPiston allow list";
                } else if (args[1].equalsIgnoreCase("-r"))
                {
                    msg = " removed from StrongPiston allow list";
                    msg2 = "Click block to remove from StrongPiston allow list";
                    add = 1;
                }

                if (args.length > 2)
                {
                    Material mat = Material.getMaterial(args[2].toUpperCase());
                    if (mat == null)
                    {
                        cs.sendMessage(ChatColor.RED + mat.toString() + " is not a valid block");
                    } else
                    {
                        if (plugin.changeStrongBlocks(mat, add))
                        {
                            cs.sendMessage(ChatColor.GREEN + mat.toString() + msg);
                        } else
                        {
                            cs.sendMessage(ChatColor.RED + mat.toString() + " is already in the list");
                        }
                    }
                } else
                {
                    plugin.setWaitingForStrongBlockClick(add);
                    cs.sendMessage(ChatColor.RED + msg2);
                }
            }
            return true;
        }
        return false;
    }
}
