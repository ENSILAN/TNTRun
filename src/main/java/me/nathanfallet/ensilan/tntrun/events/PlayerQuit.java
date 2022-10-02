package me.nathanfallet.ensilan.tntrun.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import me.nathanfallet.ensilan.tntrun.TNTRun;
import me.nathanfallet.ensilan.tntrun.utils.TNTRunPlayer;

public class PlayerQuit implements Listener {

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		TNTRunPlayer zp = TNTRun.getInstance().getPlayer(e.getPlayer().getUniqueId());
		if (zp != null) {
			TNTRun.getInstance().uninitPlayer(zp);
		}
	}

}
