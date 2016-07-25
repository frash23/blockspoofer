package com.frash23.blockspoofer;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.BlockPosition;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import static java.lang.Integer.parseInt;

public class BlockSpoofPacket extends PacketAdapter {
	private BlockSpoofer plugin;
	private String version;
	private Method getNmsBlockMethod;
	private Method nmsBlockToIBlockDataMethod;
	private HashMap<Integer, Object> iBlockDataCache = new HashMap<>();

	@SuppressWarnings("deprecation")
	BlockSpoofPacket(BlockSpoofer p) {
		super(p, ListenerPriority.HIGH, PacketType.Play.Server.BLOCK_CHANGE);
		plugin = p;
		version = Bukkit.getServer().getClass().getPackage().getName().replace(".",  ",").split(",")[3];

		try {
			Class<?> CraftMagicNumbersClass = Class.forName("org.bukkit.craftbukkit." + version + ".util.CraftMagicNumbers");
			Class<?> BlockClass = Class.forName("net.minecraft.server." + version + ".Block");
			getNmsBlockMethod = CraftMagicNumbersClass.getMethod("getBlock", int.class);
			nmsBlockToIBlockDataMethod = BlockClass.getDeclaredMethod("fromLegacyData", int.class);

			getNmsBlockMethod.setAccessible(true);
			nmsBlockToIBlockDataMethod.setAccessible(true);

		} catch(ClassNotFoundException | NoSuchMethodException e)  { throw new RuntimeException("Failed to reflect ID -> Block methods", e); }
	}

	Object getIBlockData(int id) { return getIBlockData(id, 0);}
	Object getIBlockData(int id, int data) {
		int idData = (id << 8) + data;
		if( iBlockDataCache.containsKey(idData) ) return iBlockDataCache.get(idData);
		else try {
			Object block = getNmsBlockMethod.invoke(null, id);
			Object iBlockData = nmsBlockToIBlockDataMethod.invoke(block, data);

			iBlockDataCache.put(idData, iBlockData);
			return iBlockData;
		} catch(InvocationTargetException | IllegalAccessException e)  { throw new RuntimeException("Failed get IBlockData from ID:data", e); }
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onPacketSending(PacketEvent e) {
		PacketContainer packet = e.getPacket();
		PacketType packetType = e.getPacketType();
		Player p = e.getPlayer();
		World world = p.getWorld();
		StructureModifier mod = packet.getModifier();

		if( p.hasPermission("blockspoofer.exempt") ) return;

		BlockPosition bLoc = packet.getBlockPositionModifier().readSafely(0);
		Location loc = new Location(world, bLoc.getX(), bLoc.getY(), bLoc.getZ() );
		Block sourceBlock = world.getBlockAt(loc);
		if( plugin.isFakeBlock(sourceBlock) ) {
			String[] lines = ( (org.bukkit.block.Sign)sourceBlock.getState() ).getLines();
			String[] idSplit = lines[1].split(":");
			String customPerm = lines[3];
			int spoofId = parseInt(idSplit[0]);
			int spoofData = idSplit.length > 1? parseInt(idSplit[1]) : 0;

			int spoofRange = 3;
			Location blockLoc = sourceBlock.getLocation();
			Location playerLoc = p.getLocation().add(-.5, .5, -.5);

			boolean hasPerm = !customPerm.matches("[a-z]+") || p.hasPermission("blockspoofer." + customPerm);

			if( blockLoc.distanceSquared(playerLoc) < spoofRange && hasPerm ) mod.write( 1, getIBlockData(0) );
			else mod.write( 1, getIBlockData(spoofId, spoofData) );
		}
	}

}

