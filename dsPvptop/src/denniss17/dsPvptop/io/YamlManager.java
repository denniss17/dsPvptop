package denniss17.dsPvptop.io;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PlayerStats[] getKilltop(int start) {
		PlayerStats[] result = new PlayerStats[10];
		
		SortedMap<Integer, String> top = new TreeMap<Integer, String>();
		for(String name : getDataConfig().getConfigurationSection("kills").getKeys(false)){
			top.put(getDataConfig().getInt("kills." + name), name);
		}
		
		int count=0;
		int index = 0;
		for(Entry<Integer, String> entry : top.entrySet()){
			if(count>=start && count<start+10){
				result[index] = getPlayerStats(entry.getValue());
				index++;
			}			
			count++;
		}
		
		return result;
	}

	@Override
	public PlayerStats[] getKillstreaktop(int start) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PlayerStats[] getKillDeathtop(int start) {
		// TODO Auto-generated method stub
		return null;
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

}
