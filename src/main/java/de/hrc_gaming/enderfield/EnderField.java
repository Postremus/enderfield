package de.hrc_gaming.enderfield;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import de.slikey.effectlib.Effect;
import de.slikey.effectlib.EffectLib;
import de.slikey.effectlib.EffectManager;
import de.slikey.effectlib.EffectType;
import de.slikey.effectlib.effect.ShieldEntityEffect;
import de.slikey.effectlib.util.ParticleEffect;

public class EnderField extends JavaPlugin implements Listener {
	private Map<UUID, Effect> forceFieldingPlayers;
	private EffectManager effectManager;
	
	@Override
	public void onEnable()
	{
		Bukkit.getPluginManager().registerEvents(this, this);
		EffectLib lib = EffectLib.instance();
        effectManager = new EffectManager(lib);
        forceFieldingPlayers = new HashMap<UUID, Effect>();
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
		if (!forceFieldingPlayers.containsKey(player.getUniqueId()))
		{
			for (Entity entity : nearEntities)
			{
				if (entity instanceof Player && forceFieldingPlayers.containsKey(((Player)entity).getUniqueId()))
				{
					Location lookAt = lookAt(entity.getLocation(), player.getLocation());
					Vector direction = getVector(lookAt);
					player.getLocation().setPitch(lookAt.getPitch());
					player.getLocation().setYaw(lookAt.getYaw());
					player.setVelocity(direction);
					break;
				}
			}
		}
		else
		{
			for (Entity entity : nearEntities)
			{
				if (entity instanceof Player && forceFieldingPlayers.containsKey(((Player)entity).getUniqueId()))
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
		if (forceFieldingPlayers.containsKey(player.getUniqueId()))
		{
			forceFieldingPlayers.remove(player.getUniqueId()).cancel(false);
			player.sendMessage(ChatColor.GREEN+"Schutzschild deaktiviert");
		}
		else
		{
			ShieldEntityEffect effect = new ShieldEntityEffect(effectManager, player);
			effect.radius = 3;
			effect.type = EffectType.REPEATING;
			effect.particle = ParticleEffect.PORTAL;
			effect.period = 1;
			effect.iterations = -1;
			effect.start();
			forceFieldingPlayers.put(player.getUniqueId(), effect);
			player.sendMessage(ChatColor.GREEN+"Schutzschild aktiviert");
			player.openInventory(Bukkit.getServer().createInventory(null, InventoryType.HOPPER, "Title for hopper"));
		}
	}
}
