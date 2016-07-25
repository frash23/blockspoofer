package com.frash23.blockspoofer;

import com.comphenix.protocol.ProtocolLibrary;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class BlockSpoofer extends JavaPlugin implements Listener {

	private BukkitTask spoofTimer;
	private BlockSpoofPacket blockSpoofPacket;

	@SuppressWarnings("deprecation")
	@Override
	public void onEnable() {

		blockSpoofPacket = new BlockSpoofPacket(this);
		ProtocolLibrary.getProtocolManager().addPacketListener(blockSpoofPacket);
		Bukkit.getPluginManager().registerEvents( new FakeBlockCreateListener(this), this );

		spoofTimer = new SpoofAroundPlayersRunnable(this).runTaskTimer(this, 2, 2);
	}

	@Override
	public void onDisable() {
		ProtocolLibrary.getProtocolManager().removePacketListener(blockSpoofPacket);
		spoofTimer.cancel();
	}

	public boolean isFakeBlock(Block block) {
		return	( block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST )
				&& ( (org.bukkit.block.Sign)block.getState() ).getLines()[0].equals("[FakeBlock]");
	}
}

