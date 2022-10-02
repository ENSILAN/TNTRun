package me.nathanfallet.ensilan.tntrun.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import me.nathanfallet.ensilan.core.Core;
import me.nathanfallet.ensilan.core.models.AbstractGame;
import me.nathanfallet.ensilan.core.models.EnsilanPlayer;
import me.nathanfallet.ensilan.tntrun.TNTRun;

public class Game extends AbstractGame {

	// Properties

	private HashMap<Block, Material> materialHashMap = new HashMap<>();

    // Initializer

	public Game(int gameNumber) {
		super(gameNumber);
	}

	// Methods

	// Countdown before the start of the game. Zero to disable
	@Override
    public int getCountdown() {
		return 30;
	}

    // Number of players required for the game to start
	@Override
    public int getMinPlayers() {
		return 2;
	}

    // Max number of players in the game
	@Override
    public int getMaxPlayers() {
		return 10;
	}

    // Name of the game
	@Override
    public String getGameName() {
		return "TNTRun";
	}
    
    // Handle the start process of the game
	@Override
    public void start() {
		for (UUID uuid : getPlayers()) {
			TNTRunPlayer zp = TNTRun.getInstance().getPlayer(uuid);
			zp.setPlaying(true);
		}
		state = GameState.IN_GAME;
		loadArena();
	}

    // Handle the stop process of the game
	@Override
    public void stop() {
		if (state.equals(GameState.IN_GAME)) {
			state = GameState.FINISHED;
			Player p = Bukkit.getPlayer(getPlayers().get(0));
			if (p != null) {
				Bukkit.broadcastMessage("§e" + p.getName() + "§7 a gagné la partie !");
				p.getInventory().clear();
				p.updateInventory();
				p.setGameMode(GameMode.SPECTATOR);
				p.sendMessage("§aTu as gagné la partie !");

				EnsilanPlayer ep = Core.getInstance().getPlayer(p.getUniqueId());
				TNTRunPlayer zp = TNTRun.getInstance().getPlayer(p.getUniqueId());
				ep.setVictories(ep.getVictories() + 1);
				ep.setScore(ep.getScore() + TNTRun.SCORE);
				ep.setMoney(ep.getMoney() + TNTRun.MONEY);
				zp.setVictories(zp.getVictories() + 1);
				zp.setScore(zp.getScore() + TNTRun.SCORE);
			}
			currentCountValue = 0;
			Bukkit.getScheduler().scheduleSyncDelayedTask(TNTRun.getInstance(), new Runnable() {
				@Override
				public void run() {
					for (UUID uuid : getAllPlayers()) {
						Player player = Bukkit.getPlayer(uuid);
						TNTRunPlayer zp = TNTRun.getInstance().getPlayer(uuid);
						zp.setCurrentGame(0);
						zp.setPlaying(false);
						player.teleport(Core.getInstance().getSpawn());
						player.setGameMode(GameMode.SURVIVAL);
						player.getInventory().clear();
						player.updateInventory();
					}
					state = GameState.WAITING;
                    resetArena();
				}
			}, 100);
		}
	}

    // Called every second
	@Override
    public void mainHandler() {
		int number = getPlayers().size();
		if (number == 0 || number == 1) {
			stop();
		}
	}

    // Get players participating in the game (excluding those who lost)
	@Override
	public ArrayList<UUID> getPlayers() {
		ArrayList<UUID> result = new ArrayList<UUID>();
		for (Player p : Bukkit.getOnlinePlayers()) {
			TNTRunPlayer zp = TNTRun.getInstance().getPlayer(p.getUniqueId());
			if (zp.getCurrentGame() == getGameNumber()
					&& ((!state.equals(GameState.IN_GAME) && !state.equals(GameState.FINISHED)) || zp.isPlaying())
					&& !zp.isBuildmode()) {
				result.add(p.getUniqueId());
			}
		}
		return result;
	}

    // Get all players of the game, even those who lost
	@Override
	public ArrayList<UUID> getAllPlayers() {
		ArrayList<UUID> result = new ArrayList<UUID>();
		for (Player p : Bukkit.getOnlinePlayers()) {
			TNTRunPlayer zp = TNTRun.getInstance().getPlayer(p.getUniqueId());
			if (zp.getCurrentGame() == getGameNumber() && !zp.isBuildmode()) {
				result.add(p.getUniqueId());
			}
		}
		return result;
	}

    // Make a player join this game
	@Override
    public void join(Player player, EnsilanPlayer ep) {
		// Make player part of this game
		TNTRun.getInstance().getPlayer(player.getUniqueId()).setCurrentGame(getGameNumber());
	}

    // Load arena
    public void loadArena() {
        Location l = new Location(
            Bukkit.getWorld("TNTRun"),
            (gameNumber - 1) * TNTRun.SIZE + TNTRun.SIZE * 8,
            65,
            TNTRun.SIZE * 8
        );
        l.setYaw(-90);

        for (UUID uuid : getPlayers()) {
			Player player = Bukkit.getPlayer(uuid);
			TNTRunPlayer zp = TNTRun.getInstance().getPlayer(uuid);
			
			player.teleport(l);
			player.setGameMode(GameMode.SURVIVAL);
			zp.setPlaying(true);
			player.getInventory().clear();
			player.updateInventory();
		}
    }

	// Break block
	public void breakBlock(Block block) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(TNTRun.getInstance(), new Runnable() {
			@Override
			public void run() {
				materialHashMap.put(block, block.getType());
				block.setType(Material.AIR);
			}
		}, 5);
	}

    // Reset arena
    public void resetArena() {
        for (Block block : materialHashMap.keySet()) {
            block.setType(materialHashMap.get(block));
        }
		materialHashMap.clear();
    }
    
}
