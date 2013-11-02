package se.enji;

import java.io.File;
import java.util.logging.Logger;

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
import org.bukkit.plugin.java.JavaPlugin;

public class Campfire extends JavaPlugin implements Listener {
	FileConfiguration config;
	Logger log = Logger.getLogger("Minecraft");
	public static boolean treeBurn;
	public static boolean treeBurn2;
	public static int i;
    public static int xvalue = 0;
    public static int yvalue = 0;
    public static int zvalue = 0;
    public static int x2value = 0;
    public static int z2value = 0;
    public static int dropvalueX = 0;
    public static int dropvalueZ = 0;
    public static int firevalueX = 0;
    public static int firevalueZ = 0;
	
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
    	xvalue = block.getX();
    	yvalue = block.getY();
    	zvalue = block.getZ();
    	int dir = getDirection(event.getPlayer());
    	Location locFu = new Location(world, xvalue + 2, yvalue, zvalue);
	    switch (dir) {
	    	case 90:
	    		locFu = new Location(world, xvalue + 2, yvalue, zvalue);
	    		break;
	    	case 360:
	    		locFu = new Location(world, xvalue, yvalue, zvalue - 2);
	    		break;
	    	case 180:
	    		locFu = new Location(world, xvalue, yvalue, zvalue + 2);
	    		break;
	    	case 270:
	    		locFu = new Location(world, xvalue - 2, yvalue, zvalue);
	    		break;
	    	case 315:
	    		locFu = new Location(world, xvalue - 1, yvalue, zvalue - 1);
	    		break;
	    	case 45:
	    		locFu = new Location(world, xvalue + 1, yvalue, zvalue - 1);
	    		break;
	    	case 135:
	    		locFu = new Location(world, xvalue + 1, yvalue, zvalue + 1);
	    		break;
	    	case 225:
	    		locFu = new Location(world, xvalue - 1, yvalue, zvalue + 1);
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
				dropItFu(Material.BRICK, drop, event, block);
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
	    x2value = dir.getModX();
	    z2value = dir.getModZ();
	    yvalue = block.getY();
	    
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
	    			dropItPi(Material.BRICK, dl, gb, event, block);
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
		if (configFile.exists() == false) {
			config.set(o, p);
		}
	}
	
	public void dropItFu(Material m, Location l, BlockPlaceEvent e, Block b) {
	    e.getPlayer().getWorld().dropItemNaturally(l, new ItemStack(m, 1));
	    Block f = b.getWorld().getBlockAt(l);
	    f.setType(Material.AIR);
    }
  
    public void dropItFo(Material m, Location l, BlockPlaceEvent e, Block b) {
	    e.getPlayer().getWorld().dropItemNaturally(l, new ItemStack(m, 1));
	    Block f = b.getWorld().getBlockAt(b.getLocation().getBlockX(), b.getLocation().getBlockY() - 1, b.getLocation().getBlockZ());
	    f.setType(Material.AIR);
    }
  
    public void dropItPi(Material m, Location l, Location g, BlockPistonExtendEvent e, Block b) {
    	Block burningBlock = b.getWorld().getBlockAt(g); 
	    burningBlock.setType(Material.FIRE);
	    e.getBlock().getWorld().dropItemNaturally(l, new ItemStack(m, 1));
    }
    
    private boolean blockCanBeUsedWithFire(Block b) {
    	boolean returnValue = false;
    	switch (b.getType().name()) {
	    	case "GOLD_ORE":
	    		returnValue = true;
				break;
			case "IRON_ORE":
				returnValue = true;
				break;
			case "COBBLESTONE":
				returnValue = true;
				break;
			case "LOG":
				returnValue = true;
				break;
			case "CLAY":
				returnValue = true;
				break;
			case "SAND":
				returnValue = true;
				break;
			case "NETHERRACK":
				returnValue = true;
				break;
			default:
				returnValue = false;
				break;
    	}
    	return returnValue;
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