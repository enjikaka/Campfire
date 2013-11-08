package se.enji.campfire;

import java.io.File;

import org.bukkit.CoalType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Coal;
import org.bukkit.plugin.java.JavaPlugin;

public final class Campfire extends JavaPlugin implements Listener {
	FileConfiguration config;
	public static boolean treeBurn, treeBurn2;
	
	public void onEnable() {
		config = getConfig();
		addNode("dropCoalWhenTreeBurn", true);
		addNode("fireAboveWoodEnabled", true);
		config.options().copyDefaults(true);
		saveConfig();
		getServer().getPluginManager().registerEvents(this, this);
		treeBurn = getNode("dropCoalWhenTreeBurn");
		treeBurn2 = getNode("fireAboveWoodEnabled");
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
    	World world = event.getPlayer().getWorld();
    	Block block = event.getBlockPlaced();
    	if (!blockCanBeUsedWithFire(block)) {
    		if (block.getType() != Material.FIRE) return;
    	}
    	int xvalue = block.getX(), yvalue = block.getY(), zvalue = block.getZ();
    	int dir = getDirection(event.getPlayer());
    	Location locFu = new Location(world, xvalue + 2, yvalue, zvalue);
	    switch (dir) {
	    	case 360:
	    		locFu.setZ(zvalue - 2);
	    		break;
	    	case 180:
	    		locFu.setZ(zvalue + 2);
	    		break;
	    	case 270:
	    		locFu.setX(xvalue - 2);
	    		break;
	    	case 315:
	    		locFu.setX(xvalue - 1);
	    		locFu.setX(zvalue - 1);
	    		break;
	    	case 45:
	    		locFu.setX(xvalue + 1);
	    		locFu.setX(zvalue - 1);
	    		break;
	    	case 135:
	    		locFu.setX(xvalue + 1);
	    		locFu.setX(zvalue + 1);
	    		break;
	    	case 225:
	    		locFu.setX(xvalue - 1);
	    		locFu.setX(zvalue + 1);
	    		break;
	    }
	    Block toBeBurned = null;
	    Location drop = null;
	    if (block.getType() == Material.FIRE) {
	    	if (blockCanBeUsedWithFire(world.getBlockAt(xvalue, yvalue + 1, zvalue))) {
	    		toBeBurned = world.getBlockAt(xvalue, yvalue + 1, zvalue);
	    		locFu.setY((double)locFu.getY() + 1.0);
	    		drop = locFu;
	    	}
	    	if (blockCanBeUsedWithFire(world.getBlockAt(xvalue, yvalue - 1, zvalue))) {
	    		toBeBurned = world.getBlockAt(xvalue, yvalue - 1, zvalue);
	    		drop = world.getBlockAt(xvalue, yvalue - 1, zvalue).getLocation();
	    	}
	    } else if (world.getBlockAt(xvalue, yvalue - 1, zvalue).getType() == Material.FIRE) {
	    	toBeBurned = block;
	    	drop = locFu;
	    } else return;
	    if (toBeBurned == null) return;
	    switch (toBeBurned.getType().name()) {
			case "GOLD_ORE":
				dropItFu(Material.GOLD_INGOT, drop, event, block);
				break;
			case "IRON_ORE":
				dropItFu(Material.IRON_INGOT, drop, event, block);
				break;
			case "COBBLESTONE":
				dropItFu(Material.STONE, drop, event, block);
				break;
			case "LOG":
				if (drop == locFu) {
					if (Campfire.treeBurn2) dropItFu(Material.COAL, drop, event, block);
				} else {
					dropItFu(Material.COAL, drop, event, block);
				}
				break;
			case "CLAY":
				dropItFu(Material.HARD_CLAY, drop, event, block);
				break;
			case "SAND":
				dropItFu(Material.GLASS, drop, event, block);
				break;
			case "NETHERRACK":
				dropItFu(Material.NETHER_BRICK, drop, event, block);
				break;
			default:
				break;
		}
	    if (drop == locFu) toBeBurned.setType(Material.AIR);
	}
	
	@EventHandler
	public void onBlockPistonExtend(BlockPistonExtendEvent event) {
  		World world = event.getBlock().getWorld();
	    Block block = event.getBlock();
	    BlockFace dir = event.getDirection();
	    int x2value = dir.getModX(), z2value = dir.getModZ();
	    int firevalueX = block.getX();
	    int firevalueZ = block.getZ();
	    int dropvalueX = block.getX();
	    int dropvalueZ = block.getZ();
	    int xvalue = block.getX(), yvalue = block.getY(), zvalue = block.getZ();
	    
	    if (z2value == -1) {
	    	xvalue = block.getX() + x2value;
		    zvalue = block.getZ() + z2value;
		    firevalueZ = zvalue - 1;
		    firevalueX = xvalue;
	    	dropvalueX = firevalueX + 2;
	    	dropvalueZ = firevalueZ;
	    }
	    
	    else if (z2value == 1) {
	    	xvalue = block.getX() + x2value;
		    zvalue = block.getZ() + z2value;
		    firevalueZ = zvalue + 1;
		    firevalueX = xvalue;
	    	dropvalueX = firevalueX - 2;
	    	dropvalueZ = firevalueZ;
	    }
	    
	    else if (x2value == -1) {
	    	xvalue = block.getX() + x2value;
		    zvalue = block.getZ() + z2value;
		    firevalueX = xvalue - 1;
		    firevalueZ = zvalue;
	    	dropvalueX = firevalueX;
	    	dropvalueZ = firevalueZ - 2;
	    }
	    
	    else if (x2value == 1) {
	    	xvalue = block.getX() + x2value;
		    zvalue = block.getZ() + z2value;
		    firevalueZ = zvalue;
		    firevalueX = xvalue + 1;
	    	dropvalueX = firevalueX;
	    	dropvalueZ = firevalueZ + 2;
	    }
	    
	    Location dl = new Location(world, dropvalueX, yvalue, dropvalueZ);
	    Location gb = new Location(world, xvalue, yvalue, zvalue);
	    Material burnBlock = world.getBlockAt(xvalue, yvalue, zvalue).getType();
	    if (world.getBlockAt(firevalueX, yvalue - 1, firevalueZ).getType() == Material.FIRE) {
	    	switch (burnBlock.name()) {
	    		case "COBBLESTONE":
	    			dropItPi(Material.STONE, dl, gb, event, block);
	    			break;
	    		case "SAND":
	    			dropItPi(Material.GLASS, dl, gb, event, block);
	    			break;
	    		case "GOLD_ORE":
	    			dropItPi(Material.GOLD_INGOT, dl, gb, event, block);
	    			break;
	    		case "IRON_ORE":
	    			dropItPi(Material.IRON_INGOT, dl, gb, event, block);
	    			break;
	    		case "LOG":
	    			dropItPi(Material.COAL, dl, gb, event, block);
	    			break;
	    		case "CLAY":
	    			dropItPi(Material.HARD_CLAY, dl, gb, event, block);
	    			break;
	    		case "NETHERRACK":
	    			dropItPi(Material.NETHER_BRICK, dl, gb, event, block);
	    			break;
	    	}
	    }
	}
	
	@EventHandler
	public void onBlockBurn(BlockBurnEvent event) {
		World world = event.getBlock().getWorld();
	    Block block = event.getBlock();
	    if (block.getType() == Material.LOG && Campfire.treeBurn == true) {
		    ItemStack coal = new ItemStack(Material.COAL, 1);
		    Location dropLocation = new Location(world, block.getX(), block.getY(), block.getZ());
		    event.getBlock().getWorld().dropItemNaturally(dropLocation, coal);
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
	
	public void dropItFu(Material m, Location l, BlockPlaceEvent e, Block b) {
		ItemStack i = new ItemStack(m, 1);
		if (m == Material.COAL) {
			Coal c = new Coal();
			c.setType(CoalType.CHARCOAL);
			i = c.toItemStack(1);
		}
	    e.getPlayer().getWorld().dropItemNaturally(l, i);
	    Block f = b.getWorld().getBlockAt(l);
	    f.setType(Material.AIR);
    }
  
    public void dropItPi(Material m, Location l, Location g, BlockPistonExtendEvent e, Block b) {
    	Block burningBlock = b.getWorld().getBlockAt(g); 
	    burningBlock.setType(Material.FIRE);
	    e.getBlock().getWorld().dropItemNaturally(l, new ItemStack(m, 1));
    }
    
    private boolean blockCanBeUsedWithFire(Block b) {
    	String[] supported = {"GOLD_ORE","IRON_ORE","COBBLESTONE","LOG","CLAY","SAND","NETHERRACK"};
    	for (int i = 0; i < supported.length; i++) {
    		if (supported[i] == b.getType().name()) return true;
    	}
    	return false;
    }
    
    private int getDirection(Player player) {
    	float yaw = player.getLocation().getYaw();
	    if (((yaw >= 22.5D) && (yaw < 67.5D)) || ((yaw <= -292.5D) && (yaw > -337.5D))) return 45;
	    if (((yaw >= 67.5D) && (yaw < 112.5D)) || ((yaw <= -247.5D) && (yaw > -292.5D))) return 90;
	    if (((yaw >= 112.5D) && (yaw < 157.5D)) || ((yaw <= -202.5D) && (yaw > -247.5D))) return 135;
	    if (((yaw >= 157.5D) && (yaw < 202.5D)) || ((yaw <= -157.5D) && (yaw > -202.5D))) return 180;
	    if (((yaw >= 202.5D) && (yaw < 247.5D)) || ((yaw <= -112.5D) && (yaw > -157.5D))) return 225;
	    if (((yaw >= 247.5D) && (yaw < 292.5D)) || ((yaw <= -67.5D) && (yaw > -112.5D))) return 270;
	    if (((yaw >= 292.5D) && (yaw < 337.5D)) || ((yaw <= -22.5D) && (yaw > -67.5D))) return 315;
	    if ((yaw >= 337.5D) || (yaw < 22.5D) || (yaw <= -337.5D) || (yaw > -22.5D)) return 360;
	    return 0;
    }
}