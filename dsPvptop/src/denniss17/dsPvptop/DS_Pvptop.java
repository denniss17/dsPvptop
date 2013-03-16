package denniss17.dsPvptop;

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


public class DS_Pvptop extends JavaPlugin{	
	/** The DatabaseConnection used */
	private DatabaseConnection databaseConnection;
	
	/** Mapping from playername to PemissionsAttachment */
	protected Map<String, PermissionAttachment> grantedPermissions;
	
	/** A class representing one item in the pvptop */
	class PvptopItem {
		public int killCount;
		public String playerName;

		public PvptopItem(String playerName, int killCount) {
			this.killCount = killCount;
			this.playerName = playerName;
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
	}
	
	private void checkTable() throws SQLException{
		if(!databaseConnection.tableExists(this.getConfig().getString("database.table_pvp_top"), "id")){
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
	
	private void handlePlayerKill(Entity killer, Player victim, Projectile weapon) {
		if(killer instanceof Player){
			// Weapon name
			String weaponName;
			if(weapon!=null){
				weaponName = weapon.getType().name();
			}else{
				weaponName = ((Player)killer).getItemInHand().getType().name();
			}
			
			// Query
			String query = "INSERT INTO `" + 
					this.getConfig().getString("database.table_pvp_top") +
					"` ( `id` , `killer` , `victim`, `weapon` , `timestamp`) VALUES ( NULL, '" + 
						((Player)killer).getName().toLowerCase() + "', '" + 
						victim.getName().toLowerCase() + "', '" +
						weaponName +
						"', CURRENT_TIMESTAMP);";
			try {
				databaseConnection.executeUpdate(query);
				reloadPermissions((Player)killer);
			} catch (SQLException e) {
				handleSQLException(e);
			}
		}else if(killer instanceof Projectile){
			// Recall function with shooter of projectile as killer
			handlePlayerKill(((Projectile)killer).getShooter(), victim, (Projectile)killer);
		}
	}
	
	public void handlePlayerKill(Entity killer, Player victim){
		this.handlePlayerKill(killer, victim, null);
	}
	
	/** Reload permissions, called when a kill is added */
	public void reloadPermissions(Player player){
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
	
	public int getNumberOfKills(Player player) throws SQLException{
		
		String query = "SELECT killer, COUNT(victim) AS Count FROM `"
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
	
	public PvptopItem[] getDeathtop(int start, int amount) throws SQLException {
		PvptopItem[] top = new PvptopItem[amount];

		String query = "SELECT victim, COUNT(killer) as count FROM `"
				+ getConfig().getString("database.table_pvp_top")
				+ "` GROUP BY victim ORDER BY COUNT(killer) DESC LIMIT "
				+ start + ", " + amount + ";";

		ResultSet result = databaseConnection.executeQuery(query);

		int i = 0;
		while (result.next()) {
			top[i] = new PvptopItem(result.getString("victim"),
					result.getInt("count"));
			i++;
		}

		databaseConnection.close();

		return top;
	}
	
	public PvptopItem[] getKilltop(int start, int amount) throws SQLException {
		PvptopItem[] top = new PvptopItem[amount];

		String query = "SELECT killer, COUNT(victim) as count FROM `"
				+ getConfig().getString("database.table_pvp_top")
				+ "` GROUP BY killer ORDER BY COUNT(victim) DESC LIMIT "
				+ start + ", " + amount + ";";

		ResultSet result = databaseConnection.executeQuery(query);

		int i = 0;
		while (result.next()) {
			top[i] = new PvptopItem(result.getString("killer"),
					result.getInt("count"));
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
