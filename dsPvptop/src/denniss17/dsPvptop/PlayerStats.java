package denniss17.dsPvptop;

/** A class representing one item in the pvptop */
public class PlayerStats {
	public int killCount;
	public int deathCount;
	public String playerName;

	public PlayerStats(String playerName, int killCount, int deathCount) {
		this.killCount = killCount;
		this.deathCount = deathCount;
		this.playerName = playerName;
	}
	
	public float getKillDeathRate(){
		if(deathCount==0){
			return killCount * 2;
		}else{
			return (float)killCount/(float)deathCount;
		}
	}
}
