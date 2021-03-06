package com.modcrafting.ultrabans.commands;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.UltraBan;

public class Empty implements CommandExecutor{
	public static final Logger log = Logger.getLogger("Minecraft");
	UltraBan plugin;
	String permission = "ultraban.empty";
	public Empty(UltraBan ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
    	YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
    	boolean auth = false;
		Player player = null;
		String admin = config.getString("defAdminName", "server");
		if (sender instanceof Player){
			player = (Player)sender;
			if(player.hasPermission(permission) || player.isOp()) auth = true;
			admin = player.getName();
		}else{
			auth = true;
		}
		if(!auth){
			sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
			return true;
		}
		if (args.length < 1) return false;
		String p = args[0];
		if(plugin.autoComplete) p = plugin.util.expandName(p); 
		
		Player victim = plugin.getServer().getPlayer(p);
		if (victim == null){
			sender.sendMessage(ChatColor.GRAY + "Player must be online!");
			return true;
		}
		String idoit = victim.getName();
		String emptyMsgAll = config.getString("messages.emptyMsgBroadcast");
		if(emptyMsgAll.contains(plugin.regexAdmin))emptyMsgAll = emptyMsgAll.replaceAll(plugin.regexAdmin, admin);
		if(emptyMsgAll.contains(plugin.regexVictim)) emptyMsgAll = emptyMsgAll.replaceAll(plugin.regexVictim, idoit);
		if(emptyMsgAll != null) sender.sendMessage(plugin.util.formatMessage(emptyMsgAll));
		
		String emptyMsg = config.getString("messages.emptyMsgVictim");
		if(emptyMsg.contains(plugin.regexAdmin)) emptyMsg = emptyMsg.replaceAll(plugin.regexAdmin, admin);
		if(emptyMsg.contains(plugin.regexVictim)) emptyMsg = emptyMsg.replaceAll(plugin.regexVictim, idoit);
		if(emptyMsg != null) victim.sendMessage(plugin.util.formatMessage(emptyMsg));
		
		victim.getInventory().clear();
		return true;		
	}

}
