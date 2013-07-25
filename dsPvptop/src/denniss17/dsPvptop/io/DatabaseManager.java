package denniss17.dsPvptop.io;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.entity.Player;

import denniss17.dsPvptop.DS_Pvptop;
import denniss17.dsPvptop.PlayerStats;

public class DatabaseManager implements IOManager {
	private DS_Pvptop plugin;
	private DatabaseConnection databaseConnection;
	
	public DatabaseManager(DS_Pvptop plugin){
		this.plugin = plugin;
		this.databaseConnection = new DatabaseConnection(plugin);
	}
	
	@Override
	public boolean initialize() {
		try {
			checkTable();
			return true;
		} catch (SQLException e) {
			handleSQLException(e);
			return false;
		}
	}
	
	@Override
	public void reload() {
		// Nothing		
	}

	/**
	 * Check if the table exists
	 * @throws SQLException
	 */
	private void checkTable() throws SQLException{
		if(!databaseConnection.tableExists(plugin.getConfig().getString("database.table_pvp_top"), "user")){
			String query = "CREATE TABLE IF NOT EXISTS `" + 
					plugin.getConfig().getString("database.table_pvp_top") + "` (  " +
					"`user` varchar(32) NOT NULL, " +
					"`kills` int(11) NOT NULL, " +
					"`deaths` int(11) NOT NULL, " +
					"`currentstreak` int(11) NOT NULL DEFAULT '0'," +
					"`maxstreak` int(11) NOT NULL DEFAULT '0'," +
					"`timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
					"PRIMARY KEY (`user`))";
			databaseConnection.executeUpdate(query);
			plugin.getLogger().info("Database table not found, created...");
		}
		
		// Check for new table layout
		if(!databaseConnection.tableExists(plugin.getConfig().getString("database.table_pvp_top"), "currentstreak")){
			// ALTER TABLE `dspvptoptest` ADD `currentstreak` INT NOT NULL DEFAULT '0' AFTER `deaths` ,
			// ADD `maxstreak` INT NOT NULL DEFAULT '0' AFTER `currentstreak` 
			String query = "ALTER TABLE `" + 
					plugin.getConfig().getString("database.table_pvp_top") + "` " +
					"ADD `currentstreak` int(11) NOT NULL DEFAULT '0' AFTER `deaths` , " +
					"ADD `maxstreak` int(11) NOT NULL DEFAULT '0' AFTER `currentstreak`;";
			databaseConnection.executeUpdate(query);
			plugin.getLogger().info("Database table updated to new version.");
		}
	}

	@Override
	public boolean saveKill(Player player) {
		PreparedStatement statement;
		String query;
		
		// Update kill count for killer
		query = "INSERT INTO `" + plugin.getConfig().getString("database.table_pvp_top") +
				"`(`kills`,`deaths`,`user`,`currentstreak`, `maxstreak`) VALUES(1,0,?,1,1) ON DUPLICATE KEY UPDATE `kills`=`kills`+1, `currentstreak`=`currentstreak`+1, `maxstreak`=((`maxstreak`+`currentstreak`)+ABS(`maxstreak`-`currentstreak`))/2";
		
		try {
			databaseConnection.connect();
			statement = databaseConnection.getConnection().prepareStatement(query);
			statement.setString(1, (player).getName().toLowerCase());
			statement.executeUpdate();
			return true;
		} catch (SQLException e) {
			handleSQLException(e);
			return false;
		}
	}

	@Override
	public boolean saveDeath(Player player) {
		PreparedStatement statement;
		String query;
		
		// Update death count for victim
		query = "INSERT INTO `" + plugin.getConfig().getString("database.table_pvp_top") +
				"`(`kills`,`deaths`,`user`, `currentstreak`) VALUES(0,1,?,0) ON DUPLICATE KEY UPDATE `deaths`=`deaths`+1, `currentstreak`=0";
		
		try {
			statement = databaseConnection.getConnection().prepareStatement(query);
			statement.setString(1, player.getName().toLowerCase());
			statement.executeUpdate();
			return true;
		} catch (SQLException e) {
			handleSQLException(e);
			return false;
		}
	}
	
	@Override
	public PlayerStats getPlayerStats(Player player){
		PreparedStatement statement;
		String query;
		PlayerStats result;
		
		// Update kill count for killer
		query = "SELECT * FROM `" + plugin.getConfig().getString("database.table_pvp_top") +
				"` WHERE `user`=?;";
		
		try{
			databaseConnection.connect();
			statement = databaseConnection.getConnection().prepareStatement(query);
			statement.setString(1, player.getName().toLowerCase());
			ResultSet resultset = statement.executeQuery();
			
			if(resultset.next()){
				result =  new PlayerStats(player.getName(), resultset.getInt("kills"), resultset.getInt("deaths"), resultset.getInt("maxstreak"), resultset.getInt("currentstreak"));
			}else{
				result =  new PlayerStats(player.getName(), 0, 0, 0, 0);
			}
			
			databaseConnection.close();
			return result;
		}catch(SQLException e){
			handleSQLException(e);
			return null;
		}
	}

	@Override
	public PlayerStats[] getDeathtop(int start) {
		PlayerStats[] top = new PlayerStats[10];

		String query = "SELECT * FROM `"
				+ plugin.getConfig().getString("database.table_pvp_top")
				+ "` ORDER BY deaths ASC, kills DESC LIMIT "
				+ start + ", 10;";
		
		try{
			ResultSet result = databaseConnection.executeQuery(query);
	
			int i = 0;
			while (result.next()) {
				top[i] = new PlayerStats(result.getString("user"), result.getInt("kills"),result.getInt("deaths"), result.getInt("maxstreak"), result.getInt("currentstreak"));
				i++;
			}
	
			databaseConnection.close();
			return top;
		}catch(SQLException e){
			handleSQLException(e);
			return null;
		}
	}

	@Override
	public PlayerStats[] getKilltop(int start) {
		PlayerStats[] top = new PlayerStats[10];

		String query = "SELECT * FROM `"
				+ plugin.getConfig().getString("database.table_pvp_top")
				+ "` ORDER BY kills DESC, deaths ASC LIMIT "
				+ start + ", 10;";
		
		try{
			ResultSet result = databaseConnection.executeQuery(query);
	
			int i = 0;
			while (result.next()) {
				top[i] = new PlayerStats(result.getString("user"), result.getInt("kills"),result.getInt("deaths"), result.getInt("maxstreak"), result.getInt("currentstreak"));
				i++;
			}
	
			databaseConnection.close();
			return top;
		}catch(SQLException e){
			handleSQLException(e);
			return null;
		}
	}

	@Override
	public PlayerStats[] getKillstreaktop(int start) {
		PlayerStats[] top = new PlayerStats[10];

		String query = "SELECT * FROM `"
				+ plugin.getConfig().getString("database.table_pvp_top")
				+ "` ORDER BY `maxstreak` DESC, `kills` DESC LIMIT "
				+ start + ", 10;";
		
		try{
			ResultSet result = databaseConnection.executeQuery(query);
	
			int i = 0;
			while (result.next()) {
				top[i] = new PlayerStats(result.getString("user"), result.getInt("kills"),result.getInt("deaths"), result.getInt("maxstreak"), result.getInt("currentstreak"));
				i++;
			}
	
			databaseConnection.close();
			return top;
		}catch(SQLException e){
			handleSQLException(e);
			return null;
		}
	}
	
	@Override
	public PlayerStats[] getCurrentKillstreaktop(int start) {
		PlayerStats[] top = new PlayerStats[10];

		String query = "SELECT * FROM `"
				+ plugin.getConfig().getString("database.table_pvp_top")
				+ "` ORDER BY `currentstreak` DESC, `kills` DESC LIMIT "
				+ start + ", 10;";
		
		try{
			ResultSet result = databaseConnection.executeQuery(query);
	
			int i = 0;
			while (result.next()) {
				top[i] = new PlayerStats(result.getString("user"), result.getInt("kills"),result.getInt("deaths"), result.getInt("maxstreak"), result.getInt("currentstreak"));
				i++;
			}
	
			databaseConnection.close();
			return top;
		}catch(SQLException e){
			handleSQLException(e);
			return null;
		}
	}

	@Override
	public PlayerStats[] getKillDeathtop(int start) {
		PlayerStats[] top = new PlayerStats[10];

		String query = "SELECT * FROM `"
				+ plugin.getConfig().getString("database.table_pvp_top")
				+ "` ORDER BY (2*kills/((deaths + .5) + ABS(deaths - .5))) DESC LIMIT "
				+ start + ", 10;";
		try{
			ResultSet result = databaseConnection.executeQuery(query);
			int i = 0;
			while (result.next()) {
				top[i] = new PlayerStats(result.getString("user"), result.getInt("kills"),result.getInt("deaths"), result.getInt("maxstreak"), result.getInt("currentstreak"));
				i++;
			}
	
			databaseConnection.close();
			return top;
		}catch(SQLException e){
			handleSQLException(e);
			return null;
		}
	}

	private void handleSQLException(SQLException e) {
		if (e instanceof java.sql.SQLTimeoutException) {
			plugin.getLogger().warning("== SQL TIMEOUT ==");
		} else {
			plugin.getLogger().warning("== SQL EXCEPTION ==");
		}
		plugin.getLogger().warning("The database connection failed. Please check your config.yml. Details:");
		plugin.getLogger().warning("Message: " + e.getMessage());
		plugin.getLogger().warning("Error code: " + e.getErrorCode());
		plugin.getLogger().warning("SQL State: " + e.getSQLState());
	}

}
