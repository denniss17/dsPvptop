package denniss17.dsPvptop.io;

import java.util.SortedSet;

public interface IOManager {
	/** Class representing the stats of a player */
	public class PlayerStats implements Comparable{
		public String playername;
		public int killCount;
		public int deathCount;
		
		@Override
		public int compareTo(Object arg0) {
			// TODO Auto-generated method stub
			return 0;
		}
	}
	
	/**
	 * Save a kill
	 * @param killer The name of the killer
	 * @param victim The name of the victim
	 * @param weapon The weapon description
	 * @return
	 */
	public boolean saveKill(String killer, String victim, String weapon);
	
	/** Get the Stats of a single player */
	public PlayerStats getPlayerStats(String playername);
	
	public SortedSet<PlayerStats> getKilltop(int start, int amount);

	
}
