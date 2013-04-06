package denniss17.dsPvptop.io;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.plugin.java.JavaPlugin;

/** 
 * Generic Database class for executing queries
 * Loads settings from config of the plugin given to the constructor
 * @author Denniss17
 * @version 1.0.0
 */
public class DatabaseConnection{
	private Connection connection;
	private JavaPlugin plugin;
	
	private String url;
	private String database;
	private String user;
	private String password;
	
	/**
	 * Create a new DatabaseConnection
	 * @param plugin The plugin to which this connection belongs
	 */
	public DatabaseConnection(JavaPlugin plugin){
		this.plugin = plugin;
		this.url = plugin.getConfig().getString("database.url");
		this.database = plugin.getConfig().getString("database.database");
		this.user = plugin.getConfig().getString("database.user");
		this.password = plugin.getConfig().getString("database.password");
		this.connection = null;
	}
	
	/**
	 * Get the connection of this object. Can be used to create PreparedStatement
	 * @return The connection of this object
	 */
	public Connection getConnection(){
		return this.connection;
	}
	
	/**
	 * Connect to the database
	 * @throws SQLException If one of the options is not set or the connection could not be instantiated
	 * @ensure this.getConnection() != null on success
	 */
	public void connect() throws SQLException{		
		if(url==null||user==null||password==null||database==null){
			throw new SQLException("One of the settings is not set!");
		}
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			throw new SQLException("Driver not found");
		}
		this.connection = DriverManager.getConnection(url + database, user, password);
	}
	
	/**
	 * Close the connection to the database
	 * @throws SQLException If the closing failed
	 */
	public void close() throws SQLException{
		if(!this.connection.isClosed()){
			this.connection.close();
		}
	}
	
	/**
	 * Check if the given table exists by checking if the column could be found
	 * @param table The table to check
	 * @param column A column in this table
	 * @return True if the table exists
	 * @throws SQLException If opening of closing of the connection failed
	 */
	public boolean tableExists(String table, String column) throws SQLException{
		boolean exists = false;
		
		this.connect();

        try {
            this.executeQuery("SELECT " + column + " FROM " + table);
            exists = true;
        } catch (SQLException e) {
            exists = false;
        }
        
        this.close();

        return exists;
	}
	
	/**
	 * Execute a query like UPDATE/INSERT/DELETE which returns a counter or nothing
	 * @param query The query to execute
	 * @return either (1) the row count for SQL Data Manipulation Language (DML) statements or (2) 0 for SQL statements that return nothing
	 * @throws SQLException (Could also be SQLTimeoutException)
	 */
	public int executeUpdate(String query) throws SQLException{		
		this.connect();
		
		Statement statement = this.connection.createStatement();
		int result = statement.executeUpdate(query);
		
		this.close();
		
		return result;
	}
	
	/**
	 * Execute an async query like UPDATE/INSERT/DELETE which returns a counter or nothing
	 * @param query The query to execute
	 * @throws SQLException (Could also be SQLTimeoutException)
	 */
	public void executeAsyncUpdate(String query){
		AsyncDBTask task = new AsyncDBTask(this, query);
		this.plugin.getServer().getScheduler().runTaskAsynchronously(plugin, task);
	}
	
	/**
	 * Execute the given PreparedStatement asynchroniously
	 * @param The PreparedStatement
	 * @throws SQLException (Could also be SQLTimeoutException)
	 */
	public void executeAsyncUpdate(PreparedStatement statement){
		AsyncDBTask task = new AsyncDBTask(this, statement);
		this.plugin.getServer().getScheduler().runTaskAsynchronously(plugin, task);
	}
	
	/**
	 * The Runnable which executes an async update
	 * @author Denniss17
	 */
	class AsyncDBTask implements Runnable{
		private DatabaseConnection databaseConnection;
		private String query;
		private PreparedStatement statement;
		
		public AsyncDBTask(DatabaseConnection databaseConnection, String query){
			this.databaseConnection = databaseConnection;
			this.query = query;
		}
		
		public AsyncDBTask(DatabaseConnection databaseConnection, PreparedStatement statement){
			this.databaseConnection = databaseConnection;
			this.statement = statement;
		}
		
		@Override
		public void run() {
			try {
				if(statement!=null){
					statement.executeUpdate();
				}else{
						databaseConnection.executeUpdate(query);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Execute a query like SELECT which returns a result.<br>
	 * DatabaseConnection.close() MUST BE CALLED afterwarts
	 * @param query The query to execute
	 * @return A ResultSet Object with the result in it.
	 * @throws SQLException (Could also be SQLTimeoutException)
	 */
	public ResultSet executeQuery(String query) throws SQLException{
		
		this.connect();
		
		Statement statement = this.connection.createStatement();
		ResultSet rs = statement.executeQuery(query);
		
		// Close can't be called here, this would make the ResultSet empty
		
		return rs;
	}

	
}
