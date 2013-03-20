package denniss17.dsPvptop;

import java.sql.SQLException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import denniss17.dsPvptop.DS_Pvptop.PlayerStats;

public class CommandExec implements CommandExecutor{

	private DS_Pvptop plugin;
	
	public CommandExec(DS_Pvptop plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandlabel, String[] args) {
		if(cmd.getName().equals("pvptop")){
			if(args.length==0){
				sendMenu(sender);
				return true;
			}else if(args[0].equals("kd") || args[0].equals("killdeath")){
				return commandPvptopKillDeath(sender, cmd, commandlabel, args);
			}else if(args[0].equals("k") || args[0].equals("kills")){
				return commandPvptopKills(sender, cmd, commandlabel, args);
			}else if(args[0].equals("d") || args[0].equals("deaths")){
				return commandPvptopDeaths(sender, cmd, commandlabel, args);
			}else if(args[0].equals("reload")){
				plugin.reloadConfig();
			}
		}
		return false;		
	}
	
	private String parsePvptopLine(String message, PlayerStats playerstats, int rank){
		return message
				.replace("<rank>", String.valueOf(rank))
				.replace("<player>", playerstats.playerName)
				.replace("<kills>", String.valueOf(playerstats.killCount))
				.replace("<deaths>", String.valueOf(playerstats.deathCount))
				.replace("<killdeath>", String.format("%.2f", playerstats.getKillDeathRate()));
	}
	
	private void sendMenu(CommandSender sender){
		plugin.sendMessage(sender, plugin.getConfig().getString("messages.menu_header"));
		plugin.sendMessage(sender, plugin.getConfig().getString("messages.menu_killdeath"));
		plugin.sendMessage(sender, plugin.getConfig().getString("messages.menu_kills"));
		plugin.sendMessage(sender, plugin.getConfig().getString("messages.menu_deaths"));
	}
	
	private boolean commandPvptopKillDeath(CommandSender sender, Command cmd, String commandlabel, String[] args){
		int start = 1, index = 1;
		// Parse additional argument
		if(args.length>1){
			try{
				start = Integer.parseInt(args[1]);					
			}catch(NumberFormatException e){
			}
		}
		// Get top
		try{
			PlayerStats[] top = plugin.getKillDeathRatetop(start-1);
			// Send messages
			plugin.sendMessage(sender, plugin.getConfig().getString("messages.killdeath_header"));
			for(PlayerStats playerStats : top){
				if(playerStats!=null){
					plugin.sendMessage(sender, this.parsePvptopLine(plugin.getConfig().getString("messages.killdeath_line"), playerStats, index));
				}
				index++;
			}
		} catch (SQLException e) {
			plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_sql_exception"));
			plugin.handleSQLException(e);
		}
		return true;
	}
	
	private boolean commandPvptopKills(CommandSender sender, Command cmd, String commandlabel, String[] args){
		int start = 1, index = 1;
		// Parse additional argument
		if(args.length>1){
			try{
				start = Integer.parseInt(args[1]);					
			}catch(NumberFormatException e){
			}
		}
		// Get top
		try{
			PlayerStats[] top = plugin.getKilltop(start-1);
			// Send messages
			plugin.sendMessage(sender, plugin.getConfig().getString("messages.kills_header"));
			for(PlayerStats playerStats : top){
				if(playerStats!=null){
					plugin.sendMessage(sender, this.parsePvptopLine(plugin.getConfig().getString("messages.kills_line"), playerStats, index));
				}
				index++;
			}
		} catch (SQLException e) {
			plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_sql_exception"));
			plugin.handleSQLException(e);
		}
		return true;
	}
	
	private boolean commandPvptopDeaths(CommandSender sender, Command cmd, String commandlabel, String[] args){
		int start = 1, index = 1;
		// Parse additional argument
		if(args.length>1){
			try{
				start = Integer.parseInt(args[1]);					
			}catch(NumberFormatException e){
			}
		}
		// Get top
		try{
			PlayerStats[] top = plugin.getDeathtop(start-1);
			// Send messages
			plugin.sendMessage(sender, plugin.getConfig().getString("messages.deaths_header"));
			for(PlayerStats playerStats : top){
				if(playerStats!=null){
					plugin.sendMessage(sender, this.parsePvptopLine(plugin.getConfig().getString("messages.deaths_line"), playerStats, index));
				}
				index++;
			}
		} catch (SQLException e) {
			plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_sql_exception"));
			plugin.handleSQLException(e);
		}
		return true;
	}

}
