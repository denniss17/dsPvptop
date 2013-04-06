package denniss17.dsPvptop;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Listener;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;

import denniss17.dsPvptop.io.DatabaseManager;
import denniss17.dsPvptop.io.IOManager;
import denniss17.dsPvptop.VersionChecker;


public class DS_Pvptop extends JavaPlugin{	
	
	/** Mapping from playername to PemissionsAttachment */
	protected Map<String, PermissionAttachment> grantedPermissions;
	
	public static VersionChecker versionChecker;
	
	public static IOManager ioManager;

	/** Enable the plugin */
	public void onEnable(){
		// Register listeners
		Listener playerlistener = new PlayerListener(this);
		this.getServer().getPluginManager().registerEvents(playerlistener, this);
		
		// Set the command executors
		CommandExec commandExec = new CommandExec(this);
		this.getCommand("pvptop").setExecutor(commandExec);
		
		// Save the config to the file
		getConfig().options().copyDefaults(true);
		saveConfig();
		
		grantedPermissions = new HashMap<String, PermissionAttachment>();
		
		if(getConfig().getString("general.save_method").equals("database")){
			ioManager = new DatabaseManager(this);
		}else{
			
		}
		if(!ioManager.initialize()){
			getLogger().severe("Unable to initialize IO. Please see earlier errors");
			getLogger().severe("As a result, this plugin will be disabled!");
			this.getServer().getPluginManager().disablePlugin(this);
		}
		
		// Check for newer versions
		if(this.getConfig().getBoolean("general.check_for_updates")){
			versionChecker = new VersionChecker(this);
			versionChecker.activate(this.getConfig().getInt("general.update_check_interval") * 60 * 20);
		}
	}
	
	/**
	 * Handle a kill (add it to the database)
	 * @param killer The killer
	 * @param victim The victim
	 */
	public void handlePlayerKill(Entity killer, Player victim) {
		if(killer instanceof Player){
			ioManager.saveKill((Player)killer);
			ioManager.saveDeath(victim);
			
			reloadPermissions((Player)killer);
			reloadPermissions(victim);
		}else if(killer instanceof Projectile){
			// Recall function with shooter of projectile as killer
			handlePlayerKill(((Projectile)killer).getShooter(), victim);
		}
	}
	
	public void checkPermission(Player player, float value, String condition, String permission){
		float c;
		try{
			// komma fix
			condition = condition.replace(',', '.');
			if(condition.charAt(0) == '<'){
				c = Float.parseFloat(condition.substring(1));
				
				if(value < c){
					addPermission(player, permission);			
				}else{
					removePermission(player, permission);
				}
			}else{
				c = condition.charAt(0) == '>' ? Float.parseFloat(condition.substring(1)) : Float.parseFloat(condition);
				
				if(value > c){
					addPermission(player, permission);			
				}else{
					removePermission(player, permission);
				}
				
			}
		} catch (NumberFormatException e){
			getLogger().warning("Misconfiguring in permission.* (config.yml): '" + condition + "' is not a number");
		}
	}
	
	/**
	 * Reload the permissions of this player
	 * (Called when a kill is added or a player joins)
	 * @param player The player to reload the perms for
	 */
	public void reloadPermissions(Player player){
		if(player==null) return;
		
		PlayerStats playerStats = ioManager.getPlayerStats(player);
		// An error occured. Stop here
		if(playerStats==null) return;
		float killdeath = playerStats.getKillDeathRate();
		
		getLogger().info(playerStats.playerName + " d:" +  playerStats.deathCount + " k:" +   playerStats.killCount + " kd:" +   killdeath);
		
		if(getConfig().contains("permission.kills")){
			for(String condition: getConfig().getConfigurationSection("permission.kills").getKeys(false)){
				checkPermission(player, playerStats.killCount,  condition, getConfig().getString("permission.kills." + condition));
			}
		}
		
		if(getConfig().contains("permission.deaths")){
			for(String condition: getConfig().getConfigurationSection("permission.deaths").getKeys(false)){
				checkPermission(player, playerStats.killCount,  condition, getConfig().getString("permission.deaths." + condition));
			}
		}
		
		if(getConfig().contains("permission.killdeath")){
			for(String condition: getConfig().getConfigurationSection("permission.killdeath").getKeys(false)){
				checkPermission(player, playerStats.killCount,  condition, getConfig().getString("permission.killdeath." + condition));
			}
		}
	}
	
	public void addPermission(Player player, String permission){
		if(!grantedPermissions.containsKey(player.getName())){
			grantedPermissions.put(player.getName(), player.addAttachment(this, permission, true));
		}
		if(!grantedPermissions.get(player.getName()).getPermissions().containsKey(permission)){
			grantedPermissions.get(player.getName()).setPermission(permission, true);
			getLogger().info("Permission '" + permission + "' added to " + player.getName());
		}
	}
	
	public void removePermission(Player player, String permission){
		if(grantedPermissions.containsKey(player.getName())){
			grantedPermissions.get(player.getName()).unsetPermission(permission);
		}
	}

	public void sendMessage(CommandSender reciever, String message){
		reciever.sendMessage(ChatStyler.setTotalStyle(message));
	}
	
	/*public void handleSQLException(SQLException e) {
		if (e instanceof java.sql.SQLTimeoutException) {
			getLogger().warning("== SQL TIMEOUT ==");
		} else {
			getLogger().warning("== SQL EXCEPTION ==");
		}
		getLogger().warning("The database connection failed. Please check your config.yml. Details:");
		getLogger().warning("Message: " + e.getMessage());
		getLogger().warning("Error code: " + e.getErrorCode());
		getLogger().warning("SQL State: " + e.getSQLState());
	}*/
}
