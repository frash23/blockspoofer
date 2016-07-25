package com.frash23.blockspoofer;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class FakeBlockCreateListener implements Listener {
	private BlockSpoofer plugin;
	FakeBlockCreateListener(BlockSpoofer p) { plugin = p; }

	@EventHandler
	private void onSignChange(SignChangeEvent e) {
		Player p = e.getPlayer();
		String[] lines = e.getLines();
		if( lines[0].equals("[FakeBlock]") ) {

			if( !p.hasPermission("blockspoofer.admin") ) {

				p.sendMessage(ChatColor.DARK_RED + "You don't have permission to create fake blocks");
				e.setCancelled(true);

			} else {

				String[] idSplit = lines[1].split(":");
				if( (idSplit.length == 1 && idSplit[0].matches("\\d+") )
				||  (idSplit.length == 2 && idSplit[0].matches("\\d+") && idSplit[1].matches("\\d+")) ) {
					p.sendMessage(ChatColor.GREEN + "Created fake block");
				} else {
					e.setCancelled(true);
					p.sendMessage(ChatColor.RED + "Invalid ID / data");
				}

			}
		}
	}
}
