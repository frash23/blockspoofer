package com.frash23.blockspoofer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class SpoofAroundPlayersRunnable extends BukkitRunnable {
	private BlockSpoofer plugin;
	SpoofAroundPlayersRunnable(BlockSpoofer p) { plugin = p; }

	@Override
	public void run() {
		Bukkit.getOnlinePlayers().forEach(this::spoofAroundPlayer);
	}

	@SuppressWarnings("deprecation")
	private void spoofAroundPlayer(Player p) {
		if( p.hasPermission("blockspoofer.exempt") ) return;

		int rad = 5;
		Location pLoc = p.getLocation();
		Block pBlock = pLoc.getBlock();

		for(int x=-rad+1; x<rad; x++) for(int y=-rad+1; y<rad+1; y++) for(int z=-rad+1; z<rad; z++) {
			Block probe = pBlock.getRelative(x, y+2, z);
			if( plugin.isFakeBlock(probe) ) {
				String customPerm = ( (org.bukkit.block.Sign)probe.getState() ).getLine(3);
				if( customPerm.matches("[a-z]+") ) {
					if( p.hasPermission("blockspoofer."+ customPerm) ) p.sendBlockChange(probe.getLocation(), 0, (byte)0);
				} else p.sendBlockChange(probe.getLocation(), 0, (byte)0);
			}
		}
	}
}

