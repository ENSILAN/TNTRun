package me.nathanfallet.ensilan.tntrun.events;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import me.nathanfallet.ensilan.tntrun.TNTRun;
import me.nathanfallet.ensilan.tntrun.utils.Game;
import me.nathanfallet.ensilan.tntrun.utils.TNTRunPlayer;

public class PlayerMove implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (
            event.getFrom().getWorld().getName().equals("TNTRun") &&
            (
                event.getFrom().getBlockX() != event.getTo().getBlockX() ||
                event.getFrom().getBlockZ() != event.getTo().getBlockZ()
            )
        ) {
            TNTRunPlayer zp = TNTRun.getInstance().getPlayer(event.getPlayer().getUniqueId());
            for (Game game : TNTRun.getInstance().getGames()) {
                if (game.getGameNumber() == zp.getCurrentGame() && zp.isPlaying()) {
                    // Check for game over
                    if (event.getPlayer().getLocation().getY() <= 0) {
                        zp.setPlaying(false);
                        event.getPlayer().setGameMode(GameMode.SPECTATOR);
                        event.getPlayer().sendMessage("§7Vous avez perdu !");
                        for (UUID uuid : game.getAllPlayers()) {
                            Player p = Bukkit.getPlayer(uuid);
                            p.sendMessage("§e" + event.getPlayer().getName() + "§7 a perdu !");
                        }
                        return;
                    }

                    // Else, break blokcs
                    Block sand = event.getTo().getWorld().getBlockAt(
                        event.getTo().getBlockX(), event.getTo().getBlockY() - 1, event.getTo().getBlockZ()
                    );
                    Block tnt = event.getTo().getWorld().getBlockAt(
                        event.getTo().getBlockX(), event.getTo().getBlockY() - 2, event.getTo().getBlockZ()
                    );
                    if (sand.getType() == Material.SAND && tnt.getType() == Material.TNT) {
                        game.breakBlock(sand);
                        game.breakBlock(tnt);
                    }
                }
            }
        }
    }
    
}
