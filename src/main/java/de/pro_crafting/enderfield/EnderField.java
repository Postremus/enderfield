package de.pro_crafting.enderfield;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import de.pro_crafting.commandframework.CommandFramework;
import de.slikey.effectlib.Effect;
import de.slikey.effectlib.EffectLib;
import de.slikey.effectlib.EffectManager;

public class EnderField extends JavaPlugin implements Listener {
	Map<String, Effect> forceFieldingPlayers;
	EffectManager effectManager;
	CommandFramework cmd;
	
	@Override
	public void onEnable()
	{
		EffectLib lib = EffectLib.instance();
        effectManager = new EffectManager(lib);
        forceFieldingPlayers = new HashMap<String, Effect>();
        cmd = new CommandFramework(this);
        cmd.registerCommands(new Commands(this));
		Bukkit.getPluginManager().registerEvents(this, this);
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
		if (!forceFieldingPlayers.containsKey(player.getUniqueId().toString()))
		{
			for (Entity entity : nearEntities)
			{
				if (entity instanceof Player && forceFieldingPlayers.containsKey(((Player)entity).getUniqueId().toString()))
				{
					Location lookAt = lookAt(entity.getLocation(), player.getLocation());
					Vector direction = getVector(lookAt);
					player.getLocation().setPitch(lookAt.getPitch());
					player.getLocation().setYaw(lookAt.getYaw());
					player.setVelocity(direction);
				}
			}
		}
		else
		{
			for (Entity entity : nearEntities)
			{
				if (entity instanceof Player)
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
	
	@EventHandler
	public void playerQuitHandler(PlayerQuitEvent event) {
		Effect eff = forceFieldingPlayers.remove(event.getPlayer().getUniqueId().toString());
		if (eff != null) {
			eff.cancel(false);
		}
	}
	
	@Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        return this.cmd.handleCommand(sender, label, command, args);
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
}
