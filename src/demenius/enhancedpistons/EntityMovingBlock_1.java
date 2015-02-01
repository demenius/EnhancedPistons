package demenius.enhancedpistons;

import java.lang.reflect.Field;
import java.util.logging.Level;
import net.minecraft.server.v1_6_R2.AxisAlignedBB;
import net.minecraft.server.v1_6_R2.Block;
import net.minecraft.server.v1_6_R2.BlockPistonMoving;
import net.minecraft.server.v1_6_R2.Entity;
import net.minecraft.server.v1_6_R2.EntityFallingBlock;
import net.minecraft.server.v1_6_R2.MathHelper;
import net.minecraft.server.v1_6_R2.World;
import net.minecraft.server.v1_6_R2.WorldServer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_6_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.Vector;

public class EntityMovingBlock_1 extends EntityFallingBlock
{

    boolean ignoreGravity = true;
    private float brightness = 0.0F;

    public Material getMaterial()
    {
        return Material.getMaterial(this.id);
    }

    public int getBlockId()
    {
        return this.id;
    }

    public byte getBlockData()
    {
        return (byte) this.data;
    }

    public boolean getDropItem()
    {
        return false;
    }

    public boolean isOnGround()
    {
        return true;
    }

    public void setPassenger(Entity e)
    {
        this.passenger = e;
    }

    public void setPassenger(Player p)
    {
        this.passenger = ((CraftPlayer) p).getHandle();
    }

    public EntityMovingBlock_1(World paramWorld)
    {
        super(paramWorld);
        this.dropItem = false;
    }

    public EntityMovingBlock_1(World paramWorld, double paramDouble1, double paramDouble2, double paramDouble3, int paramInt)
    {
        super(paramWorld, paramDouble1, paramDouble2, paramDouble3, paramInt);
        setBrightness(Block.lightEmission[this.id]);
        try
        {
            setFinalStatic(getClass().getField("boundingBox"), AxisAlignedBB.a(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D));
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static void setFinalStatic(Field field, Object newValue) throws Exception
    {
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.set(field, Integer.valueOf(field.getModifiers() & 0xFFFFFFEF));

        field.set(null, newValue);
    }

    public EntityMovingBlock_1(World paramWorld, double paramDouble1, double paramDouble2, double paramDouble3, int paramInt, int paramInt2)
    {
        super(paramWorld, paramDouble1, paramDouble2, paramDouble3, paramInt, paramInt2);
        setBrightness(Block.lightEmission[this.id]);
    }

    @Override
    public void l_()
    {
        this.lastX = this.locX;
        this.lastY = this.locY;
        this.lastZ = this.locZ;
        
        EnhancedPistons.log(Level.INFO, "C: " + c);
        EnhancedPistons.log(Level.INFO, "X: " + this.lastX + " Y: " + this.lastY + " Z: " + this.lastZ);
        EnhancedPistons.log(Level.INFO, "MX: " + this.motX + " MY: " + this.motY + " MZ: " + this.motZ);

        move(this.motX, this.motY, this.motZ);
        
        EnhancedPistons.log(Level.INFO, "Static: " + this.world.isStatic);
        EnhancedPistons.log(Level.INFO, "OnGround: " + this.onGround);

        if (!this.world.isStatic)
        {
            int i = MathHelper.floor(this.locX);
            int j = MathHelper.floor(this.locY);
            int k = MathHelper.floor(this.locZ);

            if (this.c == 1)
            {
                if ((this.c == 1) && (this.world.getTypeId(i, j, k) == this.id))
                {
                    this.world.setTypeIdUpdate(i, j, k, 0);
                } else if (!this.ignoreGravity)
                {
                    die();
                }
            }

            if (this.onGround)
            {
                this.motX *= 0.699999988079071D;
                this.motZ *= 0.699999988079071D;
                if (!this.ignoreGravity)
                {
                    this.motY *= -0.5D;
                }

                if ((this.world.getTypeId(i, j, k) != Block.PISTON_MOVING.id) && (!this.ignoreGravity))
                {
                    die();
                } else if (((this.c > 100) && (!this.world.isStatic) && ((j < 1) || (j > 256))) || ((this.c > 600)
                        && (!this.ignoreGravity)))
                {
                    if (this.dropItem)
                    {
                        b(this.id, 1);
                    }
                    die();
                }
            }
        }
    }

    public Vector getVelocity()
    {
        return new Vector(this.motX, this.motY, this.motZ);
    }

    public void setVelocity(Vector vel)
    {
        this.motX = vel.getX();
        this.motY = vel.getY();
        this.motZ = vel.getZ();
        this.velocityChanged = true;
    }

    public CraftWorld getWorld()
    {
        return ((WorldServer) this.world).getWorld();
    }

    public boolean teleport(Location location)
    {
        return teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    public boolean teleport(Location location, PlayerTeleportEvent.TeleportCause cause)
    {
        this.world = ((CraftWorld) location.getWorld()).getHandle();
        setLocation(location.getX(), location.getY(), location.getZ(),
                location.getYaw(), location.getPitch());

        return true;
    }

    public void setYaw(float yaw)
    {
        this.yaw = yaw;
    }

    public float getYaw()
    {
        return this.yaw;
    }

    public void setPitch(float pitch)
    {
        this.pitch = pitch;
    }

    public float getPitch()
    {
        return this.pitch;
    }

    public Location getLocation()
    {
        return new Location(getWorld(), this.locX, this.locY, this.locZ);
    }

    public void setLocation(Location l)
    {
        this.locX = l.getX();
        this.locY = l.getY();
        this.locZ = l.getZ();
    }

    public void remove()
    {
        die();
    }

    public float c(float f)
    {
        return this.brightness;
    }

    public void setBrightness(float brightness)
    {
        this.brightness = brightness;
    }

    public float getBrightness()
    {
        return this.brightness;
    }
}