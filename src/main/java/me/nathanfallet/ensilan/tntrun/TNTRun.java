package me.nathanfallet.ensilan.tntrun;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import me.nathanfallet.ensilan.core.Core;
import me.nathanfallet.ensilan.core.interfaces.LeaderboardGenerator;
import me.nathanfallet.ensilan.core.interfaces.ScoreboardGenerator;
import me.nathanfallet.ensilan.core.interfaces.WorldProtectionRule;
import me.nathanfallet.ensilan.core.models.EnsilanPlayer;
import me.nathanfallet.ensilan.tntrun.commands.Cmd;
import me.nathanfallet.ensilan.tntrun.events.PlayerCommandPreprocess;
import me.nathanfallet.ensilan.tntrun.events.PlayerJoin;
import me.nathanfallet.ensilan.tntrun.events.PlayerMove;
import me.nathanfallet.ensilan.tntrun.events.PlayerQuit;
import me.nathanfallet.ensilan.tntrun.utils.Game;
import me.nathanfallet.ensilan.tntrun.utils.TNTRunGenerator;
import me.nathanfallet.ensilan.tntrun.utils.TNTRunPlayer;

public class TNTRun extends JavaPlugin {

    public static final int SIZE = 2;
    public static final int DISTANCE = 6;
    public static final int COUNT = 5;
	public static final long SCORE = 10;
	public static final long MONEY = 10;
	private static TNTRun instance;

	public static TNTRun getInstance() {
		return instance;
	}

	private ArrayList<TNTRunPlayer> players = new ArrayList<TNTRunPlayer>();
	private ArrayList<Game> games = new ArrayList<Game>();

	public TNTRunPlayer getPlayer(UUID uuid) {
		for (TNTRunPlayer current : players) {
			if (current.getUuid().equals(uuid)) {
				return current;
			}
		}
		return null;
	}

	public void initPlayer(Player p) {
		players.add(new TNTRunPlayer(p));
	}

	public void uninitPlayer(TNTRunPlayer p) {
		if (players.contains(p)) {
			players.remove(p);
		}
	}

	public ArrayList<Game> getGames() {
		return games;
	}

	public void onEnable() {
		// Set current instance
		instance = this;

		// Check connection
		if (!initDatabase()) {
			return;
		}

		// Configuration stuff
		saveDefaultConfig();
		reloadConfig();

		// Load world
		WorldCreator w = new WorldCreator("TNTRun");
		w.type(WorldType.FLAT);
		w.generator(new TNTRunGenerator());
		w.createWorld();
		Bukkit.getWorld("TNTRun").setDifficulty(Difficulty.PEACEFUL);
		Bukkit.getWorld("TNTRun").setSpawnLocation(-1000, 0, 0);
		Bukkit.getWorld("TNTRun").setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
		Bukkit.getWorld("TNTRun").setTime(0);

		// Init players
		for (Player p : Bukkit.getOnlinePlayers()) {
			initPlayer(p);
		}

		// Load games
		games.clear();
		int i = 1, ga = getConfig().getInt("games-amount");
		while (i <= ga) {
			games.add(new Game(i));
			i++;
		}
		if (games.size() < 1) {
			getLogger().severe("You have to add one game or more to use this plugin !");
			getLogger().severe("Vous devez au moins ajoutez une partie pour faire fonctionner le plugin !");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		for (Game g : games) {
			Core.getInstance().getGames().add(g);
		}

		// Register events
		PluginManager pm = Bukkit.getPluginManager();
		pm.registerEvents(new PlayerJoin(), this);
		pm.registerEvents(new PlayerMove(), this);
		pm.registerEvents(new PlayerQuit(), this);
		pm.registerEvents(new PlayerCommandPreprocess(), this);

		// Register command
		getCommand("tntrun").setExecutor(new Cmd());

        // World protection rule
        Core.getInstance().getWorldProtectionRules().add(new WorldProtectionRule() {
            @Override
            public boolean isAllowedInProtectedLocation(Player player, EnsilanPlayer ep, Location location, Event event) {
				TNTRunPlayer zp = getPlayer(player.getUniqueId());
                return zp.isBuildmode();
            }
            @Override
            public boolean isProtected(Location location) {
                return location.getWorld().getName().equals("TNTRun");
            }
        });

		// Initialize leaderboards
		Core.getInstance().getLeaderboardGenerators().put("tntrun_score", new LeaderboardGenerator() {
			@Override
			public List<String> getLines(int limit) {
				ArrayList<String> lines = new ArrayList<String>();

				try {
					// Fetch data to MySQL Database
					Statement state = Core.getInstance().getConnection().createStatement();
					ResultSet result = state.executeQuery(
							"SELECT name, tntrun_players.score FROM tntrun_players " +
									"INNER JOIN players ON tntrun_players.uuid = players.uuid " +
									"WHERE tntrun_players.score > 0 " +
									"ORDER BY tntrun_players.score DESC " +
									"LIMIT " + limit);

					// Set lines
					while (result.next()) {
						lines.add(
								result.getString("name") +
										ChatColor.GOLD + " - " + ChatColor.YELLOW +
										result.getInt("score") + " points");
					}
					result.close();
					state.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}

				return lines;
			}

			@Override
			public String getTitle() {
				return "Score au TNT Run";
			}
		});
		Core.getInstance().getLeaderboardGenerators().put("tntrun_victories", new LeaderboardGenerator() {
			@Override
			public List<String> getLines(int limit) {
				ArrayList<String> lines = new ArrayList<String>();

				try {
					// Fetch data to MySQL Database
					Statement state = Core.getInstance().getConnection().createStatement();
					ResultSet result = state.executeQuery(
							"SELECT name, tntrun_players.victories FROM tntrun_players " +
									"INNER JOIN players ON tntrun_players.uuid = players.uuid " +
									"WHERE tntrun_players.victories > 0 " +
									"ORDER BY tntrun_players.victories DESC " +
									"LIMIT " + limit);

					// Set lines
					while (result.next()) {
						lines.add(
								result.getString("name") +
										ChatColor.GOLD + " - " + ChatColor.YELLOW +
										result.getInt("victories") + " victoires");
					}
					result.close();
					state.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}

				return lines;
			}

			@Override
			public String getTitle() {
				return "Victoires au TNT Run";
			}
		});

		// Initialize scoreboard
		Core.getInstance().getScoreboardGenerators().add(new ScoreboardGenerator() {
			@Override
			public List<String> generateLines(Player player, EnsilanPlayer ep) {
				ArrayList<String> lines = new ArrayList<String>();
				TNTRunPlayer zp = getPlayer(player.getUniqueId());

				for (Game g : getGames()) {
					if (g.getGameNumber() == zp.getCurrentGame()) {
						lines.add("§c");
						lines.add("§c§lJoueurs :");
						lines.add("§f" + g.getPlayers().size() + "/" + g.getMaxPlayers());
						lines.add("§d");
						lines.add("§d§lStatut :");
						lines.add("§f" + g.getGameDescription());
					}
				}

				return lines;
			}
		});
	}

	public void onDisable() {
		for (Game g : games) {
			for (UUID uuid : g.getAllPlayers()) {
				Player p = Bukkit.getPlayer(uuid);
				p.sendMessage("§cReload du serveur : la partie s'est arrêtée");
				p.teleport(Core.getInstance().getSpawn());
				p.setGameMode(GameMode.SURVIVAL);
				p.getInventory().clear();
				p.updateInventory();
			}
			g.resetArena();
		}
		players.clear();
		games.clear();
	}

	// Initialize database
	private boolean initDatabase() {
		try {
			Statement create = Core.getInstance().getConnection().createStatement();
			create.executeUpdate("CREATE TABLE IF NOT EXISTS `tntrun_players` (" +
					"`uuid` varchar(255) NOT NULL," +
					"`score` bigint NOT NULL DEFAULT '0'," +
					"`victories` bigint NOT NULL DEFAULT '0'," +
					"PRIMARY KEY (`uuid`)" +
					")");
			create.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
    
}
