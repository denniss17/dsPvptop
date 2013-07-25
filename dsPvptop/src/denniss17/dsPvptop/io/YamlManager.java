package denniss17.dsPvptop.io;

import java.io.File;
import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;

import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import denniss17.dsPvptop.DS_Pvptop;
import denniss17.dsPvptop.PlayerStats;

public class YamlManager implements IOManager {	
	private FileConfiguration dataConfig = null;
	private File dataConfigFile = null;
	
	private DS_Pvptop plugin;
	
	public YamlManager(DS_Pvptop plugin){
		this.plugin = plugin;
	}

	@Override
	public boolean initialize() {
		return true;
	}

	@Override
	public void reload() {
		reloadDataConfig();
	}

	@Override
	public boolean saveKill(Player player) {
		int current = getDataConfig().getInt("kills." + player.getName());
		getDataConfig().set("kills." + player.getName(), current+1);
		
		current = getDataConfig().getInt("currentstreaks." + player.getName()) + 1;
		getDataConfig().set("currentstreaks." + player.getName(), current);
		
		if(current>getDataConfig().getInt("maxstreaks." + player.getName())){
			getDataConfig().set("maxstreaks." + player.getName(), current);
		}
		
		saveDataConfig();
		
		return true;
	}

	@Override
	public boolean saveDeath(Player player) {
		int current = getDataConfig().getInt("deaths." + player.getName());
		getDataConfig().set("deaths." + player.getName(), current+1);
		
		getDataConfig().set("currentstreaks." + player.getName(), 0);
		
		saveDataConfig();
		
		return true;
	}

	@Override
	public PlayerStats getPlayerStats(Player player) {
		return getPlayerStats(player.getName());
	}
	
	private PlayerStats getPlayerStats(String playername){
		int kills = getDataConfig().getInt("kills." + playername);
		int deaths = getDataConfig().getInt("deaths." + playername);
		int currentstreak = getDataConfig().getInt("currentstreaks." + playername);
		int maxstreak = getDataConfig().getInt("maxstreaks." + playername);
		
		return new PlayerStats(playername, kills, deaths, maxstreak, currentstreak);
	}

	@Override
	public PlayerStats[] getDeathtop(int start) {
		PlayerStats[] result = new PlayerStats[10];
		
		SortedSet<SortedSetEntry> top = new TreeSet<SortedSetEntry>();
		
		if(!getDataConfig().contains("deaths")){
			return result;
		}
		for(String name : getDataConfig().getConfigurationSection("deaths").getKeys(false)){
			top.add(new SortedSetEntry(name, getDataConfig().getInt("deaths." + name), false, getDataConfig().getInt("kills." + name), true));
		}
		
		int count=0;
		int index = 0;
		for(SortedSetEntry entry : top){
			if(count>=start && count<start+10){
				result[index] = getPlayerStats(entry.playername);
				index++;
			}			
			count++;
		}
		
		return result;
	}

	@Override
	public PlayerStats[] getKilltop(int start) {
		PlayerStats[] result = new PlayerStats[10];
		
		SortedSet<SortedSetEntry> top = new TreeSet<SortedSetEntry>();
		
		if(!getDataConfig().contains("kills")){
			return result;
		}
		for(String name : getDataConfig().getConfigurationSection("kills").getKeys(false)){
			top.add(new SortedSetEntry(name, getDataConfig().getInt("kills." + name), true, getDataConfig().getInt("deaths." + name), false));
		}
		
		int count=0;
		int index = 0;
		for(SortedSetEntry entry : top){
			if(count>=start && count<start+10){
				result[index] = getPlayerStats(entry.playername);
				index++;
			}			
			count++;
		}
		
		return result;
	}

	@Override
	public PlayerStats[] getKillstreaktop(int start) {
		PlayerStats[] result = new PlayerStats[10];
		
		SortedSet<SortedSetEntry> top = new TreeSet<SortedSetEntry>();
		
		if(!getDataConfig().contains("maxstreaks")){
			return result;
		}
		for(String name : getDataConfig().getConfigurationSection("maxstreaks").getKeys(false)){
			top.add(new SortedSetEntry(name, getDataConfig().getInt("maxstreaks." + name), true, getDataConfig().getInt("currentstreaks." + name), true));
		}
		
		int count=0;
		int index = 0;
		for(SortedSetEntry entry : top){
			if(count>=start && count<start+10){
				result[index] = getPlayerStats(entry.playername);
				index++;
			}			
			count++;
		}
		
		return result;
	}
	
	@Override
	public PlayerStats[] getCurrentKillstreaktop(int start) {
		PlayerStats[] result = new PlayerStats[10];
		
		SortedSet<SortedSetEntry> top = new TreeSet<SortedSetEntry>();
		
		if(!getDataConfig().contains("currentstreaks")){
			return result;
		}
		for(String name : getDataConfig().getConfigurationSection("currentstreaks").getKeys(false)){
			top.add(new SortedSetEntry(name, getDataConfig().getInt("currentstreaks." + name), true, getDataConfig().getInt("kills." + name), true));
		}
		
		int count=0;
		int index = 0;
		for(SortedSetEntry entry : top){
			if(count>=start && count<start+10){
				result[index] = getPlayerStats(entry.playername);
				index++;
			}			
			count++;
		}
		
		return result;
	}

	@Override
	public PlayerStats[] getKillDeathtop(int start) {
		PlayerStats[] result = new PlayerStats[10];
		
		SortedSet<SortedSetEntry> top = new TreeSet<SortedSetEntry>();
		
		if(!getDataConfig().contains("kills")){
			return result;
		}
		for(String name : getDataConfig().getConfigurationSection("kills").getKeys(false)){
			int deaths = getDataConfig().getInt("deaths." + name);
			float killdeath;
			if(deaths==0){
				killdeath = 2 * getDataConfig().getInt("kills." + name);
			}else{
				killdeath = (float)getDataConfig().getInt("kills." + name) / deaths;
			}
			
			top.add(new SortedSetEntry(name, killdeath, true, getDataConfig().getInt("kills." + name), true));
		}
		
		int count=0;
		int index = 0;
		for(SortedSetEntry entry : top){
			if(count>=start && count<start+10){
				result[index] = getPlayerStats(entry.playername);
				index++;
			}			
			count++;
		}
		
		return result;
	}
	
	
	private void reloadDataConfig() {
		if (dataConfigFile == null) {
			dataConfigFile = new File(plugin.getDataFolder(), "data.yml");
		}
		dataConfig = YamlConfiguration.loadConfiguration(dataConfigFile);
	}

	private void saveDataConfig() {
		if (dataConfig == null || dataConfigFile == null) {
			return;
		}
		try {
			dataConfig.save(dataConfigFile);
		} catch (IOException ex) {
			plugin.getLogger().log(Level.SEVERE, "Could not save config to " + dataConfigFile, ex);
		}
	}

	private MemorySection getDataConfig() {
		if (dataConfig == null) {
			reloadDataConfig();
		}
		return dataConfig;
	}
	
	/**
	 * Custom class for sorting the player in the different tops
	 */
	class SortedSetEntry implements Comparable<SortedSetEntry>{
		public String playername;
		private float value1;
		private float value2;
		private boolean desc1;
		private boolean desc2;
		
		/**
		 * Create a new entry for a SortedSet
		 * @param playername The name of the player
		 * @param value1 The first value to sort on
		 * @param desc1 Whether to sort the first value descending or ascending
		 * @param value2 The second value to sort on
		 * @param desc2 Whether to sort the second value descending or ascending
		 */
		public SortedSetEntry(String playername, float value1, boolean desc1, float value2, boolean desc2){
			this.playername = playername;
			this.value1 = value1;
			this.value2 = value2;
			this.desc1 = desc1;
			this.desc2 = desc2;
		}
		
		
		@Override
		public int compareTo(SortedSetEntry other) {
			if(value1<other.value1){
				return desc1 ? 1 : -1;
			}else if(value1>other.value1){
				return desc1 ? -1 : 1;
			}else{
				if(value2<other.value2){
					return desc2 ? 1 : -1;
				}else if(value2>other.value2){
					return desc2 ? -1 : 1;
				}else{
					return playername.compareTo(other.playername);
				}
			}
		}
		
	}

}
