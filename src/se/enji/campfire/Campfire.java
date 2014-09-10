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
	private static boolean treeBurn, treeBurn2;
	
	public void onEnable() {
		config=getConfig();
		addNode("dropCoalWhenTreeBurn",true);
		addNode("fireAboveWoodEnabled",true);
		config.options().copyDefaults(true);
		saveConfig();
		getServer().getPluginManager().registerEvents(this,this);
		treeBurn=getNode("dropCoalWhenTreeBurn");
		treeBurn2=getNode("fireAboveWoodEnabled");
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
	    if (toBurnType == Material.LOG && dropLocation.equals(playerLocation) && !Campfire.treeBurn2) return;
	    dropItFu(getResult(toBurnType),dropLocation,event,block);
	    if (dropLocation == playerLocation) toBeBurned.setType(Material.AIR);
	}
	
	@EventHandler
	public void onBlockPistonExtend(BlockPistonExtendEvent event) {
  		World world=event.getBlock().getWorld();
	    Block block=event.getBlock();
	    BlockFace dir=event.getDirection();
	    int nxv=dir.getModX(),nzv=dir.getModZ();
	    int fvx=block.getX()+nxv,fvz=block.getZ()+nzv;
	    int dvx=block.getX(),dvz=block.getZ();
	    int xv=fvx,zv=fvz,yv=block.getY();
	    if (nxv==1||nxv==-1||nzv==1||nzv==-1) {
	    	dvx=fvx;
	    	dvz=fvz;
	    	if (nxv==1) {
	    		fvx=xv+1;
	    		dvz=fvz+2;
		    	dvx+=2;
	    	} else if (nxv==-1) {
	    		fvx=xv-1;
	    		dvz=fvz-2;
		    	dvx-=2;
	    	}else if (nzv==1) {
	    		dvx=fvx-2;
	    		dvz+=2;
	    		fvz=zv+1;
	    	} else if (nzv==-1) {
	    		dvx=fvx+2;
	    		dvz-=2;
	    		fvz=zv-1;
	    	}
	    }
	    Location dl=new Location(world,dvx,yv,dvz);
	    Location gb=new Location(world,xv,yv,zv);
	    Block burnBlock=world.getBlockAt(xv,yv,zv);
	    if (!blockCanBeUsedWithFire(burnBlock)) return;
	    if (world.getBlockAt(fvx,yv-1,fvz).getType()==Material.FIRE) dropItPi(getResult(burnBlock.getType()),dl,gb,event,block);
	}
	
	@EventHandler
	public void onBlockBurn(BlockBurnEvent event) {
		World world=event.getBlock().getWorld();
	    Block block=event.getBlock();
	    if (!isTree(block.getType())) return;
	    if (Campfire.treeBurn) {
	    	Coal c=new Coal();
			c.setType(CoalType.CHARCOAL);
		    ItemStack coal=c.toItemStack(1);
		    Location dropLocation=new Location(world,block.getX(),block.getY(),block.getZ());
		    event.getBlock().getWorld().dropItemNaturally(dropLocation, coal);
	    }
	}
	
	private boolean getNode(String m) {
		return config.getBoolean(m);
	}
	
	private void addNode(String o, Object p) {
		File configFile = new File("plugins"+File.separator+this.getDescription().getName()+File.separator+"config.yml");
		config.addDefault(o, p);
		if (!configFile.exists()) {
			config.set(o, p);
		}
	}
	
	private ItemStack makeCoal() {
		Coal coal = new Coal();
		coal.setType(CoalType.CHARCOAL);
		return coal.toItemStack(1);
	}
	
	public void dropItFu(Material mat, final Location loc, BlockPlaceEvent evt, Block block) {
		ItemStack itemStack = (mat == Material.COAL) ? makeCoal() : new ItemStack(mat, 1);
	    evt.getPlayer().getWorld().dropItemNaturally(loc, itemStack);
	    Block airBlock = block.getWorld().getBlockAt(loc);
	    airBlock.setType(Material.AIR);
    }
  
    public void dropItPi(Material mat, Location loc, Location locTwo, BlockPistonExtendEvent evt, Block block) {
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