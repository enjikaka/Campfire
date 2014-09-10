package se.enji.campfire;

import java.io.File;

import org.bukkit.CoalType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Coal;
import org.bukkit.plugin.java.JavaPlugin;

public final class Campfire extends JavaPlugin implements Listener {
	private FileConfiguration config;
	private static boolean dropCoalWhenTreeBurn, fireAboveWood;
	
	public void onEnable() {
		config = getConfig();
		addNode("dropCoalWhenTreeBurn", true);
		addNode("fireAboveWoodEnabled", true);
		config.options().copyDefaults(true);
		saveConfig();
		getServer().getPluginManager().registerEvents(this,this);
		dropCoalWhenTreeBurn = getNode("dropCoalWhenTreeBurn");
		fireAboveWood = getNode("fireAboveWoodEnabled");
	}

	private ItemStack makeCoal() {
		Coal coal = new Coal();
		coal.setType(CoalType.CHARCOAL);
		return coal.toItemStack(1);
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
    	Block block = event.getBlockPlaced();
    	if (!blockCanBeUsedWithFire(block) && block.getType() != Material.FIRE) return;
    	int blockX = block.getX(), 
    		blockY = block.getY(), 
    		blockZ = block.getZ();
    	World world = event.getPlayer().getWorld();
    	Location playerLocation = event.getPlayer().getLocation();
	    Block toBeBurned = null;
	    Location dropLocation = null;
	    if (block.getType() == Material.FIRE) {
	    	if (blockCanBeUsedWithFire(world.getBlockAt(blockX, blockY + 1, blockZ))) {
	    		toBeBurned = world.getBlockAt(blockX, blockY + 1, blockZ);
	    		playerLocation.setY((double) playerLocation.getY() + 1.0);
	    		dropLocation = playerLocation;
	    	}
	    	if (blockCanBeUsedWithFire(world. getBlockAt(blockX, blockY - 1, blockZ))) {
	    		toBeBurned = world.getBlockAt(blockX, blockY - 1, blockZ);
	    		if (toBeBurned.getType() == Material.NETHERRACK && block.getType() == Material.FIRE) return;
	    		dropLocation = world.getBlockAt(blockX, blockY - 1, blockZ).getLocation();
	    	}
	    } else if (world.getBlockAt(blockX, blockY - 1, blockZ).getType() == Material.FIRE) {
	    	toBeBurned = block;
	    	dropLocation = playerLocation;
	    } else return;
	    if (toBeBurned == null) return;
	    Material toBurnType = toBeBurned.getType();
	    if (toBurnType == Material.LOG && dropLocation.equals(playerLocation) && !Campfire.fireAboveWood) return;
	    dropNormal(getResult(toBurnType),dropLocation,event,block);
	    if (dropLocation == playerLocation) toBeBurned.setType(Material.AIR);
	}
	
	@EventHandler
	public void onBlockPistonExtend(BlockPistonExtendEvent evt) {
  		World world = evt.getBlock().getWorld();
	    Block block = evt.getBlock();
	    BlockFace dir = evt.getDirection();
	    int modX = dir.getModX(),
	    	modZ = dir.getModZ();
	    int xModX = block.getX() + modX,
	    	zModZ = block.getZ() + modZ;
	    int blockX = block.getX(), 
	    	blockZ = block.getZ();
	    int posX = xModX,
	    	posZ = zModZ,
	    	posY = block.getY();
	    if (modX == 1 || modX == -1 || modZ == 1 || modZ == -1) {
	    	blockX = xModX;
	    	blockZ = zModZ;
	    	if (modX == 1) {
	    		xModX = posX + 1;
	    		blockZ = zModZ + 2;
		    	blockX += 2;
	    	} else if (modX==-1) {
	    		xModX = posX - 1;
	    		blockZ = zModZ - 2;
		    	blockX -= 2;
	    	} else if (modZ == 1) {
	    		blockX = xModX - 2;
	    		blockZ += 2;
	    		zModZ =posZ + 1;
	    	} else if (modZ ==- 1) {
	    		blockX = xModX + 2;
	    		blockZ -= 2;
	    		zModZ = posZ - 1;
	    	}
	    }
	    Block burnBlock = world.getBlockAt(posX, posY, posZ);
	    if (!blockCanBeUsedWithFire(burnBlock)) return;
	    if (world.getBlockAt(xModX, posY - 1, zModZ).getType() == Material.FIRE) dropPiston(getResult(burnBlock.getType()), new Location(world, blockX, posY, blockZ), new Location(world, posX, posY, posZ), evt, block);
	}
	
	@EventHandler
	public void onBlockBurn(BlockBurnEvent evt) {
		World world = evt.getBlock().getWorld();
	    Block block = evt.getBlock();
	    if (!isTree(block.getType())) return;
	    if (Campfire.dropCoalWhenTreeBurn) {
		    evt.getBlock().getWorld().dropItemNaturally(new Location(world, block.getX(), block.getY(), block.getZ()), makeCoal());
	    }
	}
	
	private boolean getNode(String m) {
		return config.getBoolean(m);
	}
	
	private void addNode(String o, Object p) {
		File configFile = new File("plugins" + File.separator + this.getDescription().getName() + File.separator + "config.yml");
		config.addDefault(o, p);
		if (!configFile.exists()) {
			config.set(o, p);
		}
	}
	
	public void dropNormal(Material mat, final Location loc, BlockPlaceEvent evt, Block block) {
		ItemStack itemStack = (mat == Material.COAL) ? makeCoal() : new ItemStack(mat, 1);
	    evt.getPlayer().getWorld().dropItemNaturally(loc, itemStack);
	    Block airBlock = block.getWorld().getBlockAt(loc);
	    airBlock.setType(Material.AIR);
    }
  
    public void dropPiston(Material mat, Location loc, Location locTwo, BlockPistonExtendEvent evt, Block block) {
    	ItemStack itemStack = (mat == Material.COAL) ? makeCoal() : new ItemStack(mat, 1);
    	final Block burningBlock = block.getWorld().getBlockAt(locTwo); 
	    burningBlock.setType(Material.FIRE);
	    evt.getBlock().getWorld().dropItemNaturally(loc, itemStack);
    }
    
    private Material getResult(Material material) {
    	Material returnMaterial;
    	switch (material) {
    		case GOLD_ORE:
    			returnMaterial = Material.GOLD_INGOT;
    			break;
			case IRON_ORE:
				returnMaterial = Material.IRON_INGOT;
				break;
			case COBBLESTONE:
				returnMaterial = Material.STONE;
				break;
			case CLAY:
				returnMaterial = Material.HARD_CLAY;
				break;
			case SAND:
				returnMaterial = Material.GLASS;
				break;
			case NETHERRACK:
				returnMaterial = Material.NETHER_BRICK;
				break;
			default:
				returnMaterial = Material.COAL;
				break;
		}
    	return returnMaterial;
    }
    
    private boolean blockCanBeUsedWithFire(Block b) {
    	String[] supported= {"GOLD_ORE", "IRON_ORE", "COBBLESTONE", "LOG", "LOG_2", "CLAY", "SAND", "NETHERRACK"};
    	for (int i = 0; i < supported.length; i++) {
    		if (supported[i] == b.getType().name()) {
    			return true;
    		}
    	}
    	return false;
    }
    
    private boolean isTree(Material material) {
    	return (material == Material.LOG || material == Material.LOG_2) ? true : false;
    }
}