package me.nathanfallet.ensilan.tntrun.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import me.nathanfallet.ensilan.tntrun.TNTRun;

public class PlayerJoin implements Listener {

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		TNTRun.getInstance().initPlayer(e.getPlayer());
	}

}
