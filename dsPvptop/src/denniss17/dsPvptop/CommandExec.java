package denniss17.dsPvptop;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import denniss17.dsPvptop.PlayerStats;

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
			}else if(args[0].equals("me")){
				return commandPvptopMe(sender, cmd, commandlabel, args);
			}else if(args[0].equals("reload")){
				return commandPvptopReload(sender, cmd, commandlabel, args);
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
		plugin.sendMessage(sender, plugin.getConfig().getString("messages.menu_me"));
		if(sender.hasPermission("ds_pvptop.admin")){
			plugin.sendMessage(sender, plugin.getConfig().getString("messages.menu_reload"));
		}
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
		PlayerStats[] top = DS_Pvptop.ioManager.getKillDeathtop(start-1);
		if(top==null){
			// Error occured
			plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_io_failure"));
			return true;
		}
		
		// Send the top to the player
		plugin.sendMessage(sender, plugin.getConfig().getString("messages.killdeath_header"));
		for(PlayerStats playerStats : top){
			if(playerStats!=null){
				plugin.sendMessage(sender, this.parsePvptopLine(plugin.getConfig().getString("messages.killdeath_line"), playerStats, index));
			}
			index++;
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
		PlayerStats[] top = DS_Pvptop.ioManager.getKilltop(start-1);
		if(top==null){
			// Error occured
			plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_io_failure"));
			return true;
		}
		
		
		// Send messages
		plugin.sendMessage(sender, plugin.getConfig().getString("messages.kills_header"));
		for(PlayerStats playerStats : top){
			if(playerStats!=null){
				plugin.sendMessage(sender, this.parsePvptopLine(plugin.getConfig().getString("messages.kills_line"), playerStats, index));
			}
			index++;
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
		PlayerStats[] top = DS_Pvptop.ioManager.getDeathtop(start-1);
		if(top==null){
			// Error occured
			plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_io_failure"));
			return true;
		}
		// Send messages
		plugin.sendMessage(sender, plugin.getConfig().getString("messages.deaths_header"));
		for(PlayerStats playerStats : top){
			if(playerStats!=null){
				plugin.sendMessage(sender, this.parsePvptopLine(plugin.getConfig().getString("messages.deaths_line"), playerStats, index));
			}
			index++;
		}
		return true;
	}
	
	private boolean commandPvptopMe(CommandSender sender, Command cmd, String commandlabel, String[] args){
		if(sender instanceof Player){
			PlayerStats playerStats = DS_Pvptop.ioManager.getPlayerStats((Player)sender);
			if(playerStats!=null){
				plugin.sendMessage(sender, this.parsePvptopLine(plugin.getConfig().getString("messages.me_line"), playerStats, 0));
			}else{
				// Not found in database => Send message with 0 kills and 0 deaths
				plugin.sendMessage(sender, this.parsePvptopLine(plugin.getConfig().getString("messages.me_line"), new PlayerStats(((Player)sender).getName(), 0, 0), 0));
			}
		}else{
			plugin.sendMessage(sender, "&cThe console doesn't have kills or deaths :P");
		}
		return true;		
	}
	
	private boolean commandPvptopReload(CommandSender sender, Command cmd, String commandlabel, String[] args){
		if(sender.hasPermission("ds_pvptop.admin")){
			plugin.reloadConfig();
			for(Player player : plugin.getServer().getOnlinePlayers()){
				plugin.reloadPermissions(player);
			}
			plugin.sendMessage(sender, plugin.getConfig().getString("messages.reloaded"));
		}else{
			plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_no_permission"));
		}
		return true;
	}

}
