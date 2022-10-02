package me.nathanfallet.ensilan.tntrun.utils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.entity.Player;

import me.nathanfallet.ensilan.core.Core;

public class TNTRunPlayer {

    // Local data

	private UUID uuid;
	private int currentGame;
	private boolean buildmode;
	private boolean playing;

	// Cached data

	private Long score;
	private Long victories;

	// Methods

	public TNTRunPlayer(Player p) {
		uuid = p.getUniqueId();
		setCurrentGame(0);
		setBuildmode(false);
		setPlaying(false);

		// Update player data
        try {
            // Database fetch/update/insert
            PreparedStatement fetch = Core.getInstance().getConnection()
                .prepareStatement("SELECT score, victories FROM tntrun_players WHERE uuid = ?");
            fetch.setString(1, uuid.toString());
			ResultSet result = fetch.executeQuery();
            if (result.next()) {
                // Save data to cache
                score = result.getLong("score");
                victories = result.getLong("victories");
            } else {
                // Save data to cache
                score = 0L;
				victories = 0L;

                // Insert
                PreparedStatement insert = Core.getInstance().getConnection()
                    .prepareStatement("INSERT INTO tntrun_players (uuid) VALUES(?)");
                insert.setString(1, uuid.toString());
                insert.executeUpdate();
                insert.close();
            }
            result.close();
            fetch.close();
        } catch (Exception e) {
            // Error, disconnect player
            e.printStackTrace();
            p.kickPlayer("Erreur lors de la vérification de votre identité dans la base de données !");
        }
	}

	public UUID getUuid() {
		return uuid;
	}

	public int getCurrentGame() {
		return currentGame;
	}

	public void setCurrentGame(int currentGame) {
		this.currentGame = currentGame;
	}

	public boolean isBuildmode() {
		return buildmode;
	}

	public void setBuildmode(boolean buildmode) {
		this.buildmode = buildmode;
	}

	public boolean isPlaying() {
		return playing;
	}

	public void setPlaying(boolean playing) {
		this.playing = playing;
	}

	public Long getScore() {
		try {
			PreparedStatement state = Core.getInstance().getConnection().prepareStatement("SELECT score FROM tntrun_players WHERE uuid = ?");
			state.setString(1, uuid.toString());
			ResultSet result = state.executeQuery();
			result.next();
			score = result.getLong("score");
			result.close();
			state.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return score;
	}
	
	public void setScore(Long newScore) {
		try {
			PreparedStatement state = Core.getInstance().getConnection().prepareStatement("UPDATE tntrun_players SET score = ? WHERE uuid = ?");
			state.setDouble(1, newScore);
			state.setString(2, uuid.toString());
			state.executeUpdate();
			state.close();
			score = newScore;
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public Long getVictories() {
		try {
			PreparedStatement state = Core.getInstance().getConnection().prepareStatement("SELECT victories FROM tntrun_players WHERE uuid = ?");
			state.setString(1, uuid.toString());
			ResultSet result = state.executeQuery();
			result.next();
			victories = result.getLong("victories");
			result.close();
			state.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return victories;
	}
	
	public void setVictories(Long newVictories) {
		try {
			PreparedStatement state = Core.getInstance().getConnection().prepareStatement("UPDATE tntrun_players SET victories = ? WHERE uuid = ?");
			state.setDouble(1, newVictories);
			state.setString(2, uuid.toString());
			state.executeUpdate();
			state.close();
			victories = newVictories;
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
    
}
