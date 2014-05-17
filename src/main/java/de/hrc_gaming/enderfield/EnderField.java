package de.hrc_gaming.enderfield;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class EnderField extends JavaPlugin implements Listener {
	private final List<UUID> forceFieldingPlayers = new ArrayList<UUID>();
	private BukkitTask particleTask;
	
	@Override
	public void onEnable()
	{
		Bukkit.getPluginManager().registerEvents(this, this);
		particleTask = Bukkit.getScheduler().runTaskTimer(this, new Runnable(){
			public void run()
			{
				for (UUID playerId : forceFieldingPlayers)
				{
					Player p = Bukkit.getPlayer(playerId);
					if (p != null)
					{
						ParticleEffect.PORTAL.display(Bukkit.getPlayer(playerId).getLocation(), 3.5F, 2.5F, 3.5F, 2, 1000);
					}
				}
			}
		}, 0, 20);
	}
	
	@Override
	public void onDisable()
	{
		particleTask.cancel();
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled=true)
	public void handlePlayerMovementEvent(PlayerMoveEvent event)
	{
		if (forceFieldingPlayers.size() == 0)
		{
			return;
		}
		Player player = event.getPlayer();
		List<Entity> nearEntities = player.getNearbyEntities(3, 3, 3);
		if (!forceFieldingPlayers.contains(player.getUniqueId()))
		{
			for (Entity entity : nearEntities)
			{
				if (entity instanceof Player && forceFieldingPlayers.contains(((Player)entity).getUniqueId()))
				{
					Location lookAt = lookAt(entity.getLocation(), player.getLocation());
					Vector direction = getVector(lookAt);
					entity.getLocation().setPitch(lookAt.getPitch());
					entity.getLocation().setYaw(lookAt.getYaw());
					entity.setVelocity(direction);
					break;
				}
			}
		}
		else
		{
			ParticleEffect.PORTAL.display(player.getLocation(), 3, 3, 3, 0, 1000);
			for (Entity entity : nearEntities)
			{
				if (entity instanceof Player && forceFieldingPlayers.contains(((Player)entity).getUniqueId()))
				{
					Location lookAt = lookAt(player.getLocation(), entity.getLocation());
					Vector direction = getVector(lookAt);
					player.getLocation().setPitch(lookAt.getPitch());
					player.getLocation().setYaw(lookAt.getYaw());
					entity.setVelocity(direction);
				}
			}
		}
	}
	
	private Vector getVector(Location start)
	{
		Location end = move(start, 10.5);
		end.add(0, 2.75, 0);
		Vector velocity = toVector(end).subtract(toVector(start));
		velocity = velocity.normalize().multiply(2);
		return velocity;
	}
	
	private Vector toVector(Location loc)
	{
		return new Vector(loc.getX(), loc.getY(), loc.getZ());
	}
	
	private Location move(Location start, double distance)
	{
		Location ret = start.clone();
		ret.add(ret.getDirection().normalize().multiply(distance).toLocation(start.getWorld()));
		return ret;
	}
	
	//Lookat function of bergerkiller
	//https://forums.bukkit.org/threads/lookat-and-move-functions.26768/
	private Location lookAt(Location loc, Location lookat) {
        //Clone the loc to prevent applied changes to the input loc
        loc = loc.clone();

        // Values of change in distance (make it relative)
        double dx = lookat.getX() - loc.getX();
        double dy = lookat.getY() - loc.getY();
        double dz = lookat.getZ() - loc.getZ();

        // Set yaw
        if (dx != 0) {
            // Set yaw start value based on dx
            if (dx < 0) {
                loc.setYaw((float) (1.5 * Math.PI));
            } else {
                loc.setYaw((float) (0.5 * Math.PI));
            }
            loc.setYaw((float) loc.getYaw() - (float) Math.atan(dz / dx));
        } else if (dz < 0) {
            loc.setYaw((float) Math.PI);
        }

        // Get the distance from dx/dz
        double dxz = Math.sqrt(Math.pow(dx, 2) + Math.pow(dz, 2));

        // Set pitch
        loc.setPitch((float) -Math.atan(dy / dxz));

        // Set values, convert to degrees (invert the yaw since Bukkit uses a different yaw dimension format)
        loc.setYaw(-loc.getYaw() * 180f / (float) Math.PI);
        loc.setPitch(loc.getPitch() * 180f / (float) Math.PI);

        return loc;
    }
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled=false)
	public void handlePlayerInteractEvent(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		if (!player.hasPermission("enderfield.use"))
		{
			return;
		}
		if (!event.hasItem())
		{
			return;
		}
		if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK))
		{
			return;
		}
		if (event.getItem().getType() != Material.ENDER_PEARL)
		{
			return;
		}
		if (forceFieldingPlayers.contains(player.getUniqueId()))
		{
			forceFieldingPlayers.remove(player.getUniqueId());
			player.sendMessage("Schutzschild deaktiviert");
		}
		else
		{
			forceFieldingPlayers.add(player.getUniqueId());
			player.sendMessage("Schutzschild aktiviert");
		}
	}
}
