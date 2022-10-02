package me.nathanfallet.ensilan.tntrun.events;

import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import me.nathanfallet.ensilan.core.models.AbstractGame.GameState;
import me.nathanfallet.ensilan.tntrun.TNTRun;
import me.nathanfallet.ensilan.tntrun.utils.Game;
import me.nathanfallet.ensilan.tntrun.utils.TNTRunPlayer;

public class PlayerCommandPreprocess implements Listener {

	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
		TNTRunPlayer zp = TNTRun.getInstance().getPlayer(e.getPlayer().getUniqueId());
		if (zp.getCurrentGame() != 0) {
			for (Game g : TNTRun.getInstance().getGames()) {
				if (g.getGameNumber() == zp.getCurrentGame() && g.getState().equals(GameState.IN_GAME)) {
					for (UUID uuid : g.getAllPlayers()) {
						if (e.getPlayer().getUniqueId().equals(uuid)) {
							if (!e.getMessage().equalsIgnoreCase("/tntrun leave")) {
								e.setCancelled(true);
								e.getPlayer().sendMessage(
										"§cVous ne pouvez utiliser que la commande &4/tntrun leave &cpendant une partie !");
							}
							return;
						}
					}
				}
			}
		}
	}

}
