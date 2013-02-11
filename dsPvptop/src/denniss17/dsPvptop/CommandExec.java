package denniss17.dsPvptop;

import java.sql.SQLException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import denniss17.dsPvptop.DS_Pvptop.PvptopItem;

public class CommandExec implements CommandExecutor{

	private DS_Pvptop plugin;
	
	public CommandExec(DS_Pvptop plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandlabel, String[] args) {
		if(cmd.getName().equals("pvptop")){
			return commandPvptop(sender, cmd, args);	
		}
		return false;		
	}
	
	private boolean commandPvptop(CommandSender sender, Command cmd, String[] args){
		int start = 0;
		if(args.length>0){
			try{
				start = Integer.parseInt(args[0]);					
			}catch(NumberFormatException e){
				return false;
			}
		}
		try {
			PvptopItem[] pvptop = plugin.getPvptop(start,10);
			int index = start+1;
			sender.sendMessage(ChatStyler.setMessageColor(plugin.getConfig().getString("pvp_top_header")));
			for(PvptopItem item : pvptop){
				if(item!=null){
					sender.sendMessage(ChatStyler.setMessageColor(
							plugin.getConfig().getString("pvp_top_line")
							.replace("<rank>", String.valueOf(index))
							.replace("<player>", item.playerName)
							.replace("<kills>", String.valueOf(item.killCount))
					));
				}
				index++;
			}
			
		} catch (SQLException e) {
			plugin.handleSQLException(e);
		}
		return true;
	}

}
