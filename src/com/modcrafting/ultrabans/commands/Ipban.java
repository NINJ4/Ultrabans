package com.modcrafting.ultrabans.commands;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.UltraBan;

public class Ipban implements CommandExecutor{
	public static final Logger log = Logger.getLogger("Minecraft");
	UltraBan plugin;
	String permission = "ultraban.ipban";
	public Ipban(UltraBan ultraBan) {
		this.plugin = ultraBan;
	}
		
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
    	YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		boolean auth = false;
		boolean broadcast = true;
		Player player = null;
		String admin = config.getString("defAdminName", "server");
		String reason = config.getString("defReason", "not sure");
		if (sender instanceof Player){
			player = (Player)sender;
			if(player.hasPermission(permission) || player.isOp()) auth = true;
			admin = player.getName();
		}else{
			auth = true;
		}
		if (!auth){
			sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
			return true;
		}
		if (args.length < 1) return false;

		String p = args[0];
		
		//Enhanced Variables
		if(args.length > 1){
			if(args[1].equalsIgnoreCase("-s")){
				broadcast = false;
				reason = plugin.util.combineSplit(2, args, " ");
			}else{
				if(args[1].equalsIgnoreCase("-a")){
					admin = config.getString("defAdminName", "server");
					reason = plugin.util.combineSplit(2, args, " ");
				}else{
					reason = plugin.util.combineSplit(1, args, " ");
				}
			}
		}
		//Validate IP format
		if(plugin.util.validIP(p)){
			plugin.bannedIPs.add(p);
			String pname = plugin.db.getName(p);
			if (pname != null){
				plugin.db.addPlayer(pname, reason, admin, 0, 1);
				plugin.bannedPlayers.add(pname);
			}else{
				plugin.db.setAddress(p, p);
				plugin.db.addPlayer(p, reason, admin, 0, 1);
			}
			String banMsgBroadcast = config.getString("messages.banMsgBroadcast", "%victim% was banned by %admin%. Reason: %reason%");
			if(banMsgBroadcast.contains(plugin.regexAdmin)) banMsgBroadcast = banMsgBroadcast.replaceAll(plugin.regexAdmin, admin);
			if(banMsgBroadcast.contains(plugin.regexReason)) banMsgBroadcast = banMsgBroadcast.replaceAll(plugin.regexReason, reason);
			if(banMsgBroadcast.contains(plugin.regexVictim)) banMsgBroadcast = banMsgBroadcast.replaceAll(plugin.regexVictim, p.toLowerCase());
			if(banMsgBroadcast != null){
				if(broadcast){
					plugin.getServer().broadcastMessage(plugin.util.formatMessage(banMsgBroadcast));
				}else{
					sender.sendMessage(ChatColor.ITALIC + "Silent: " + plugin.util.formatMessage(banMsgBroadcast));
				}
			}
			log.log(Level.INFO, "[UltraBan] " + admin + " banned ip " + p + ".");
			return true;
		}
		
		if(plugin.autoComplete) p = plugin.util.expandName(p);
		
		//Player Checks
		Player victim = plugin.getServer().getPlayer(p); 
		String victimip = null;
		if(victim == null){
			victim = plugin.getServer().getOfflinePlayer(p).getPlayer();
			if(victim != null){
				if(victim.hasPermission( "ultraban.override.ipban")){
					sender.sendMessage(ChatColor.RED + "Your ipban has been denied!");
					return true;
				}
			}else{
				victimip = plugin.db.getAddress(p);
				if(victimip == null){
					sender.sendMessage(ChatColor.GRAY + "IP address not found by Ultrabans for " + p);
					sender.sendMessage(ChatColor.GRAY + "Processed as a normal ban for " + p);
					plugin.db.addPlayer(p.toLowerCase(), reason, admin, 0, 0);
					plugin.bannedPlayers.add(p.toLowerCase());
					log.log(Level.INFO, "[UltraBan] " + admin + " banned player " + p + ".");
					return true;
				}
			}
		}else{
			if(victim.getName() == admin){
				sender.sendMessage(ChatColor.RED + "You cannot ipban yourself!");
				return true;
			}
			if(victim.hasPermission( "ultraban.override.ipban")){
				sender.sendMessage(ChatColor.RED + "Your ipban has been denied! Player Notified!");
				victim.sendMessage(ChatColor.RED + "Player: " + admin + " Attempted to ipban you!");
				return true;
			}
			victimip = plugin.db.getAddress(victim.getName().toLowerCase());
		}
		
		
		if(plugin.bannedIPs.contains(victimip)){
			sender.sendMessage(ChatColor.BLUE + victim.getName() +  ChatColor.GRAY + " is already banned for " + plugin.db.getBanReason(p.toLowerCase()));
			return true;
		}
		
		if(victimip == null){
			sender.sendMessage(ChatColor.GRAY + "IP address not found by Ultrabans for " + p.toLowerCase());
			sender.sendMessage(ChatColor.GRAY + "Processed as a normal ban for " + p.toLowerCase());
			plugin.db.addPlayer(victim.getName(), reason, admin, 0, 0);
			log.log(Level.INFO, "[UltraBan] " + admin + " banned player " + p.toLowerCase() + ".");
			
			String banMsgVictim = config.getString("messages.banMsgVictim", "You have been banned by %admin%. Reason: %reason%");
			if(banMsgVictim.contains(plugin.regexAdmin)) banMsgVictim = banMsgVictim.replaceAll(plugin.regexAdmin, admin);
			if(banMsgVictim.contains(plugin.regexReason)) banMsgVictim = banMsgVictim.replaceAll(plugin.regexReason, reason);
			victim.kickPlayer(plugin.util.formatMessage(banMsgVictim));
			return true;
		}

		String banMsgVictim = config.getString("messages.banMsgVictim", "You have been banned by %admin%. Reason: %reason%");
		if(banMsgVictim.contains(plugin.regexAdmin)) banMsgVictim = banMsgVictim.replaceAll(plugin.regexAdmin, admin);
		if(banMsgVictim.contains(plugin.regexReason)) banMsgVictim = banMsgVictim.replaceAll(plugin.regexReason, reason);
		
		String banMsgBroadcast = config.getString("messages.banMsgBroadcast", "%victim% was banned by %admin%. Reason: %reason%");
		if(banMsgBroadcast.contains(plugin.regexAdmin)) banMsgBroadcast = banMsgBroadcast.replaceAll(plugin.regexAdmin, admin);
		if(banMsgBroadcast.contains(plugin.regexReason)) banMsgBroadcast = banMsgBroadcast.replaceAll(plugin.regexReason, reason);
		if(banMsgBroadcast.contains(plugin.regexVictim)) banMsgBroadcast = banMsgBroadcast.replaceAll(plugin.regexVictim, p.toLowerCase());

		plugin.bannedPlayers.add(p.toLowerCase());
		plugin.bannedIPs.add(victimip);
		plugin.db.addPlayer(p.toLowerCase(), reason, admin, 0, 1);
		victim.kickPlayer(plugin.util.formatMessage(banMsgVictim));
		if(banMsgBroadcast != null){
			if(broadcast){
				plugin.getServer().broadcastMessage(plugin.util.formatMessage(banMsgBroadcast));
			}else{
				sender.sendMessage(ChatColor.ITALIC + "Silent: " + plugin.util.formatMessage(banMsgBroadcast));
			}
		}
		log.log(Level.INFO, "[UltraBan] " + admin + " banned player " + p.toLowerCase() + ".");
		return true;
	}	
	
	
}
