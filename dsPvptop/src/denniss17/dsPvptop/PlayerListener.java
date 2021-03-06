package denniss17.dsPvptop;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

	private DS_Pvptop plugin;
	
	public PlayerListener(DS_Pvptop plugin){
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		EntityDamageEvent entityDamageEvent = player.getLastDamageCause();
		
		if(entityDamageEvent instanceof EntityDamageByEntityEvent){
			// Player killed by entity
			Entity killer = ((EntityDamageByEntityEvent)entityDamageEvent).getDamager();
			plugin.handlePlayerKill(killer, (Player)entityDamageEvent.getEntity());
		}else{
			plugin.getLogger().info(player.getName() + " died, but is not killed by a player");
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event){
		plugin.reloadPermissions(event.getPlayer());
		plugin.reloadTopPermissions();
		
		if(event.getPlayer().hasPermission("ds_pvptop.admin")){
			// If there is a new version
			if(DS_Pvptop.versionChecker != null && DS_Pvptop.versionChecker.getLatestVersion() != null && !DS_Pvptop.versionChecker.getLatestVersion().equals(plugin.getDescription().getVersion())){
				// Send message to player with admin permissions
				plugin.sendMessage(event.getPlayer(), plugin.getConfig().getString("messages.update_notification")
						.replace("{version}", DS_Pvptop.versionChecker.getLatestVersion())
						.replace("{current}", plugin.getDescription().getVersion())
						.replace("{website}", plugin.getDescription().getWebsite()));
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent event){
		plugin.grantedPermissions.remove(event.getPlayer());
	}
}
