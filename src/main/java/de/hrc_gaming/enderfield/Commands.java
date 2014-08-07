package de.hrc_gaming.enderfield;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import de.pro_crafting.commandframework.Command;
import de.pro_crafting.commandframework.CommandArgs;
import de.slikey.effectlib.EffectType;
import de.slikey.effectlib.effect.ShieldEntityEffect;
import de.slikey.effectlib.util.ParticleEffect;

public class Commands {
	private EnderField plugin;

	public Commands(EnderField plugin) {
		this.plugin = plugin;
	}

	@Command(name = "schutzschild", description = "Aktiviert / Deaktiviert dein Schutzschild.", usage = "/schutzschild", permission = "lobby.schutzschild")
	public void arena(CommandArgs args) {
		if (args.getPlayer() == null) {
			args.getSender().sendMessage(ChatColor.GREEN + "Du bist kein Spieler.");
			return;
		}
		Player player = args.getPlayer();
		if (plugin.forceFieldingPlayers.containsKey(player.getUniqueId())) {
			plugin.forceFieldingPlayers.remove(player.getUniqueId()).cancel(
					false);
			player.sendMessage(ChatColor.GREEN + "Schutzschild deaktiviert.");
		} else {
			ShieldEntityEffect effect = new ShieldEntityEffect(
					plugin.effectManager, player);
			effect.radius = 3;
			effect.type = EffectType.REPEATING;
			effect.particle = ParticleEffect.PORTAL;
			effect.period = 1;
			effect.iterations = -1;
			effect.start();
			plugin.forceFieldingPlayers.put(player.getUniqueId(), effect);
			player.sendMessage(ChatColor.GREEN + "Schutzschild aktiviert.");
		}
	}
}
