package denniss17.dsPvptop;

/** A class representing one item in the pvptop */
public class PlayerStats {
	public int killCount;
	public int deathCount;
	public int maxKillstreak;
	public int currentKillstreak;
	
	public String playerName;

	public PlayerStats(String playerName, int killCount, int deathCount, int maxKillstreak, int currentKillstreak) {
		this.killCount = killCount;
		this.deathCount = deathCount;
		this.playerName = playerName;
		this.maxKillstreak = maxKillstreak;
		this.currentKillstreak = currentKillstreak;
	}
	
	public float getKillDeathRate(){
		if(deathCount==0){
			return killCount * 2;
		}else{
			return (float)killCount/(float)deathCount;
		}
	}
}
