package denniss17.dsPvptop.io;

import org.bukkit.entity.Player;

import denniss17.dsPvptop.PlayerStats;

public interface IOManager {
	/** 
	 * Initialize this manager (for example check if table exists)
	 * @return True if initialization succeeded, false otherwise
	 */
	public boolean initialize();
	
	/**
	 * Save a kill by this player
	 * @param player The killer
	 * @return true on success
	 */
	public boolean saveKill(Player player);
	
	/**
	 * Save a death of this player
	 * @param player The victim
	 * @return true on success
	 */
	public boolean saveDeath(Player player);
	
	/**
	 * Get the stats of this player
	 * @param player The player to get the stats from
	 * @return PlayerStats object or null on failure
	 */
	public PlayerStats getPlayerStats(Player player);
	
	/**
	 * Get the 10 players with highest deathcount, starting at start rank.
	 * @param start The rank where to start
	 * @return An array or null on failure
	 */
	public PlayerStats[] getDeathtop(int start);
	
	/**
	 * Get the 10 players with highest killcount, starting at start.
	 * @param start
	 * @return An array or null on failure
	 */
	public PlayerStats[] getKilltop(int start);
	
	/**
	 * Get the 10 players with highest killstreak, starting at start.
	 * @param start
	 * @return An array or null on failure
	 */
	public PlayerStats[] getKillstreaktop(int start);
	
	/**
	 * Get the 10 players with highest kill/death rate, starting at start.
	 * @param start
	 * @return An array or null on failure
	 */
	public PlayerStats[] getKillDeathtop(int start);
}
