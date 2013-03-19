package denniss17.dsPvptop;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Listener;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;

import denniss17.dsPvptop.db.DatabaseConnection;
import denniss17.dsPvptop.VersionChecker;


public class DS_Pvptop extends JavaPlugin{	
	/** The DatabaseConnection used */
	private DatabaseConnection databaseConnection;
	
	/** Mapping from playername to PemissionsAttachment */
	protected Map<String, PermissionAttachment> grantedPermissions;
	
	public static VersionChecker versionChecker;
	
	/** A class representing one item in the pvptop */
	class PlayerStats {
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
				return 0;
			}else{
				return (float)killCount/(float)deathCount;
			}
		}
	}

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
		databaseConnection = new DatabaseConnection(this);
		
		try {
			checkTable();
		} catch (SQLException e) {
			if (e instanceof java.sql.SQLTimeoutException) {
				getLogger().severe("== SQL TIMEOUT ==");
			} else {
				getLogger().severe("== SQL EXCEPTION ==");
			}
			getLogger().severe("The database connection failed. Please check your config.yml. Details:");
			getLogger().severe("Message: " + e.getMessage());
			getLogger().severe("Error code: " + e.getErrorCode());
			getLogger().severe("SQL State: " + e.getSQLState());
			getLogger().severe("Because this plugin depends on the database, it will now be DISABLED");
			this.getServer().getPluginManager().disablePlugin(this);
		}
		
		// Check for newer versions
		if(this.getConfig().getBoolean("general.check_for_updates")){
			versionChecker = new VersionChecker(this);
			versionChecker.activate(this.getConfig().getInt("general.update_check_interval") * 60 * 20);
		}
	}
	
	private void checkTable() throws SQLException{
		if(!databaseConnection.tableExists(this.getConfig().getString("database.table_pvp_top"), "user")){
			String query = "CREATE TABLE IF NOT EXISTS `" + 
					this.getConfig().getString("database.table_pvp_top") + "` (  " +
					"`user` varchar(32) NOT NULL, " +
					"`kills` int(11) NOT NULL, " +
					"`deaths` int(11) NOT NULL, " +
					"`timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
					"PRIMARY KEY (`user`))";
			databaseConnection.executeUpdate(query);
			this.getLogger().info("Database table not found, created...");
		}
	}
	
	public void handlePlayerKill(Entity killer, Player victim) {
		if(killer instanceof Player){			
			PreparedStatement statement;
			String query;
			
			// Update kill count for killer
			query = "INSERT INTO `" + this.getConfig().getString("database.table_pvp_top") +
					"` SET `user`=? `kills`=1 `deaths`=0 ON DUPLICATE KEY UPDATE `kills`=`kills`+1";
			
			try {
				statement = databaseConnection.getConnection().prepareStatement(query);
				statement.setString(1, ((Player)killer).getName().toLowerCase());
				statement.executeUpdate();
				//reloadPermissions((Player)killer);
			} catch (SQLException e) {
				handleSQLException(e);
			}
			
			// Update death count for victim
			query = "INSERT INTO `" + this.getConfig().getString("database.table_pvp_top") +
					"` SET `user`=? `kills`=0 `deaths`=1 ON DUPLICATE KEY UPDATE `deaths`=`deaths`+1";
			
			try {
				statement = databaseConnection.getConnection().prepareStatement(query);
				statement.setString(1, ((Player)victim).getName().toLowerCase());
				statement.executeUpdate();
				//reloadPermissions((Player)victim);
			} catch (SQLException e) {
				handleSQLException(e);
			}
		}else if(killer instanceof Projectile){
			// Recall function with shooter of projectile as killer
			handlePlayerKill(((Projectile)killer).getShooter(), victim);
		}
	}
	
	/** Reload permissions, called when a kill is added */
	/*public void reloadPermissions(Player player){
		if(player==null) return;
		
		try {
			int numberOfKills = getNumberOfKills(player);
			if(getConfig().contains("permission.kills")){
				for(String count: getConfig().getConfigurationSection("permission.kills").getKeys(false)){
					if(numberOfKills >= Integer.parseInt(count)){
						addPermission(player, getConfig().getString("permission.kills." + count));			
					}
				}
			}
		} catch (SQLException e) {
			handleSQLException(e);
		} catch (NumberFormatException e){
			getLogger().warning("Misconfiguring in permission.pvp: NaN");
		}
	}*/
	
	/*public void addPermission(Player player, String permission){
		if(!grantedPermissions.containsKey(player.getName())){
			grantedPermissions.put(player.getName(), player.addAttachment(this, permission, true));
		}
		if(!grantedPermissions.get(player.getName()).getPermissions().containsKey(permission)){
			grantedPermissions.get(player.getName()).setPermission(permission, true);
			getLogger().info("Permission '" + permission + "' added to " + player.getName());
		}
	}*/
	
	public int getNumberOfKills(Player player) throws SQLException{
		
		String query = "SELECT user, kills AS Count FROM `"
				+ getConfig().getString("database.table_pvp_top")
				+ "` WHERE killer='" + player.getName() + "';";
		
		ResultSet resultSet = databaseConnection.executeQuery(query);
		
		if(resultSet.next()){
			int result = resultSet.getInt("Count");
			databaseConnection.close();
			return result;
		}else{
			databaseConnection.close();
			return 0;
		}
	}
	
	
	public PlayerStats[] getDeathtop(int start) throws SQLException {
		PlayerStats[] top = new PlayerStats[10];

		String query = "SELECT user, kills, deaths FROM `"
				+ getConfig().getString("database.table_pvp_top")
				+ "` ORDER BY deaths DESC, kills DESC LIMIT "
				+ start + ", 10;";
		ResultSet result = databaseConnection.executeQuery(query);

		int i = 0;
		while (result.next()) {
			top[i] = new PlayerStats(result.getString("user"), result.getInt("kills"),result.getInt("deaths"));
			i++;
		}

		databaseConnection.close();
		return top;
	}
	
	public PlayerStats[] getKilltop(int start) throws SQLException {
		PlayerStats[] top = new PlayerStats[10];

		String query = "SELECT user, kills, deaths FROM `"
				+ getConfig().getString("database.table_pvp_top")
				+ "` ORDER BY kills DESC, deaths ASC LIMIT "
				+ start + ", 10;";
		ResultSet result = databaseConnection.executeQuery(query);

		int i = 0;
		while (result.next()) {
			top[i] = new PlayerStats(result.getString("user"), result.getInt("kills"),result.getInt("deaths"));
			i++;
		}

		databaseConnection.close();
		return top;
	}
	
	public PlayerStats[] getKillDeathRatetop(int start) throws SQLException {
		PlayerStats[] top = new PlayerStats[10];

		String query = "SELECT user, kills, deaths, 2*kills/((deaths + .5) + ABS(deaths - .5)) AS rate FROM `"
				+ getConfig().getString("database.table_pvp_top")
				+ "` ORDER BY rate DESC LIMIT "
				+ start + ", 10;";
		ResultSet result = databaseConnection.executeQuery(query);
		int i = 0;
		while (result.next()) {
			top[i] = new PlayerStats(result.getString("user"), result.getInt("kills"),result.getInt("deaths"));
			i++;
		}

		databaseConnection.close();
		return top;
	}

	public void sendMessage(CommandSender reciever, String message){
		reciever.sendMessage(ChatStyler.setTotalStyle(message));
	}
	
	public String getDatabaseUrl() {
		return getConfig().getString("database.url");
	}

	public String getDatabaseName() {
		return getConfig().getString("database.database");
	}

	public String getDatabaseUsername() {
		return getConfig().getString("database.user");
	}

	public String getDatabasePassword() {
		return getConfig().getString("database.password");
	}
	
	public void handleSQLException(SQLException e) {
		if (e instanceof java.sql.SQLTimeoutException) {
			getLogger().warning("== SQL TIMEOUT ==");
		} else {
			getLogger().warning("== SQL EXCEPTION ==");
		}
		getLogger().warning("The database connection failed. Please check your config.yml. Details:");
		getLogger().warning("Message: " + e.getMessage());
		getLogger().warning("Error code: " + e.getErrorCode());
		getLogger().warning("SQL State: " + e.getSQLState());
	}
}
