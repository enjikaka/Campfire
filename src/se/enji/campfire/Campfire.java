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
    	if (!blockCanBeUsedWithFire(block)&&block.getType()!=Material.FIRE) return;
    	int xv=block.getX(),yv=block.getY(),zv=block.getZ();
    	World world=event.getPlayer().getWorld();
    	Location locFu=event.getPlayer().getLocation();
	    Block toBeBurned=null;
	    Location drop=null;
	    if (block.getType() == Material.FIRE) {
	    	if (blockCanBeUsedWithFire(world.getBlockAt(xv,yv+1,zv))) {
	    		toBeBurned = world.getBlockAt(xv,yv+1,zv);
	    		locFu.setY((double)locFu.getY()+1.0);
	    		drop=locFu;
	    	}
	    	if (blockCanBeUsedWithFire(world.getBlockAt(xv,yv-1,zv))) {
	    		toBeBurned=world.getBlockAt(xv,yv-1,zv);
	    		if (toBeBurned.getType()==Material.NETHERRACK&&block.getType()==Material.FIRE) return;
	    		drop=world.getBlockAt(xv,yv-1,zv).getLocation();
	    	}
	    } else if (world.getBlockAt(xv,yv-1,zv).getType()==Material.FIRE) {
	    	toBeBurned=block;
	    	drop=locFu;
	    } else return;
	    if (toBeBurned==null) return;
	    Material mtbb=toBeBurned.getType();
	    if (mtbb==Material.LOG&&drop.equals(locFu)&&!Campfire.treeBurn2) return;
	    dropItFu(getResult(mtbb),drop,event,block);
	    if (drop==locFu) toBeBurned.setType(Material.AIR);
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
	
	public void dropItFu(Material m, final Location l, BlockPlaceEvent e, Block b) { // NOPMD by Jeremy on 2014-01-16 19:29
		ItemStack i=new ItemStack(m, 1);
		if (m==Material.COAL) {
			Coal c=new Coal();
			c.setType(CoalType.CHARCOAL);
			i=c.toItemStack(1);
		}
	    e.getPlayer().getWorld().dropItemNaturally(l, i);
	    Block f=b.getWorld().getBlockAt(l);
	    f.setType(Material.AIR);
    }
  
    public void dropItPi(Material m, Location l, Location g, BlockPistonExtendEvent e, Block b) {
    	ItemStack i = new ItemStack(m, 1);
    	if (m==Material.COAL) {
			Coal c=new Coal();
			c.setType(CoalType.CHARCOAL);
			i=c.toItemStack(1);
		}
    	final Block burningBlock = b.getWorld().getBlockAt(g); 
	    burningBlock.setType(Material.FIRE);
	    e.getBlock().getWorld().dropItemNaturally(l, i);
    }
    
    private Material getResult(Material m) {
    	Material rvl;
    	switch (m) {
    		case GOLD_ORE:rvl=Material.GOLD_INGOT;break;
			case IRON_ORE:rvl=Material.IRON_INGOT;break;
			case COBBLESTONE:rvl=Material.STONE;break;
			case CLAY:rvl=Material.HARD_CLAY;break;
			case SAND:rvl=Material.GLASS;break;
			case NETHERRACK:rvl=Material.NETHER_BRICK;break;
			default:rvl=Material.COAL;break;
		}
    	return rvl;
    }
    
    private boolean blockCanBeUsedWithFire(Block b) {
    	String[] supported={"GOLD_ORE","IRON_ORE","COBBLESTONE","LOG","LOG_2","CLAY","SAND","NETHERRACK"};
    	for (int i=0;i<supported.length;i++) if (supported[i]==b.getType().name()) return true;
    	return false;
    }
    
    private boolean isTree(Material m) {
    	if (m==Material.LOG||m==Material.LOG_2) return true;
    	return false;
    }
}