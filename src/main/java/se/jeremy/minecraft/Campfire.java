package se.jeremy.minecraft;

import org.bukkit.Bukkit;
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
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.material.Coal;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Iterator;

public final class Campfire extends JavaPlugin implements Listener {
	private FileConfiguration config;
	private static boolean dropCoalWhenTreeBurn, fireAboveWood;

	public void onEnable() {
		config = getConfig();
		config.options().copyDefaults(true);

		getServer().getPluginManager().registerEvents(this, this);

		dropCoalWhenTreeBurn = config.getBoolean("dropCoalWhenTreeBurn");
		fireAboveWood = config.getBoolean("fireAboveWoodEnabled");

		saveConfig();
	}

	private ItemStack charcoal() {
		Coal coal = new Coal();
		coal.setType(CoalType.CHARCOAL);

		return coal.toItemStack(1);
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		Block block = event.getBlockPlaced();

		if (!blockCanBeUsedWithFire(block) && block.getType() != Material.FIRE) {
			return;
		}

		int blockX = block.getX(), blockY = block.getY(), blockZ = block.getZ();

		Player player = event.getPlayer();
		World world = player.getWorld();
		Location playerLocation = player.getLocation();

		Block toBeBurned = null;
		Location dropLocation = null;

		if (block.getType() == Material.FIRE) {
			if (blockCanBeUsedWithFire(world.getBlockAt(blockX, blockY + 1, blockZ))) {
				toBeBurned = world.getBlockAt(blockX, blockY + 1, blockZ);
				playerLocation.setY(playerLocation.getY() + 1.0);
				dropLocation = playerLocation;
			}

			if (blockCanBeUsedWithFire(world.getBlockAt(blockX, blockY - 1, blockZ))) {
				toBeBurned = world.getBlockAt(blockX, blockY - 1, blockZ);

				if (toBeBurned.getType() == Material.NETHERRACK && block.getType() == Material.FIRE) {
					return;
				}

				dropLocation = world.getBlockAt(blockX, blockY - 1, blockZ).getLocation();
			}
		} else if (world.getBlockAt(blockX, blockY - 1, blockZ).getType() == Material.FIRE) {
			toBeBurned = block;
			dropLocation = playerLocation;
		} else {
			return;
		}

		if (toBeBurned == null) {
			return;
		}

		Material toBurnType = toBeBurned.getType();

		if (isTree(toBurnType) && dropLocation == playerLocation && !Campfire.fireAboveWood) {
			return;
		}

		dropNormal(blockResultWhenSmelted(toBeBurned, player), dropLocation, event, block);

		if (dropLocation == playerLocation) {
			toBeBurned.setType(Material.AIR);
		}
	}

	@EventHandler
	public void onBlockPistonExtend(BlockPistonExtendEvent event) {
		World world = event.getBlock().getWorld();

		Block block = event.getBlock();
		BlockFace dir = event.getDirection();

		int modX = dir.getModX(), modZ = dir.getModZ();
		int xModX = block.getX() + modX, zModZ = block.getZ() + modZ;

		int blockX = block.getX(), blockZ = block.getZ();

		int posX = xModX, posZ = zModZ, posY = block.getY();

		if (modX == 1 || modX == -1 || modZ == 1 || modZ == -1) {
			blockX = xModX;
			blockZ = zModZ;

			if (modX == 1) {
				xModX = posX + 1;
				blockZ = zModZ + 2;
				blockX += 2;
			} else if (modX == -1) {
				xModX = posX - 1;
				blockZ = zModZ - 2;
				blockX -= 2;
			} else if (modZ == 1) {
				blockX = xModX - 2;
				blockZ += 2;
				zModZ = posZ + 1;
			} else {
				blockX = xModX + 2;
				blockZ -= 2;
				zModZ = posZ - 1;
			}
		}

		Block burnBlock = world.getBlockAt(posX, posY, posZ);

		if (!blockCanBeUsedWithFire(burnBlock)) {
			return;
		}

		if (world.getBlockAt(xModX, posY - 1, zModZ).getType() == Material.FIRE) {
			ItemStack stack = blockResultWhenSmelted(burnBlock, null);
			Location dropLocation = new Location(world, blockX, posY, blockZ);
			Location fireLocation = new Location(world, posX, posY, posZ);

			dropPiston(stack, dropLocation, fireLocation, event, block);
		}
	}

	@EventHandler
	public void onBlockBurn(BlockBurnEvent evt) {
		World world = evt.getBlock().getWorld();
		Block block = evt.getBlock();

		if (!isTree(block.getType())) {
			return;
		}

		if (Campfire.dropCoalWhenTreeBurn) {
			Location dropLocation = new Location(world, block.getX(), block.getY(), block.getZ());
			evt.getBlock().getWorld().dropItemNaturally(dropLocation, charcoal());
		}
	}

	public void dropNormal(ItemStack stack, final Location loc, BlockPlaceEvent evt, Block block) {
		evt.getPlayer().getWorld().dropItemNaturally(loc, stack);

		Block airBlock = block.getWorld().getBlockAt(loc);
		airBlock.setType(Material.AIR);
	}

	public void dropPiston(ItemStack stack, Location loc, Location locTwo, BlockPistonExtendEvent evt, Block block) {
		final Block burningBlock = block.getWorld().getBlockAt(locTwo);
		burningBlock.setType(Material.FIRE);

		evt.getBlock().getWorld().dropItemNaturally(loc, stack);
	}

	private ItemStack blockResultWhenSmelted(Block b, Player player) {
		ItemStack result = null;
		Iterator<Recipe> iter = Bukkit.recipeIterator();

		while (iter.hasNext()) {
			Recipe recipe = iter.next();
			if (!(recipe instanceof FurnaceRecipe))
				continue;
			if (player != null && ((FurnaceRecipe) recipe).getInput().getType() != player.getInventory().getItemInMainHand().getType())
				continue;
			result = recipe.getResult();
			break;
		}

		return result;
	}

	private boolean blockCanBeUsedWithFire(Block b) {
		ItemStack result = blockResultWhenSmelted(b, null);

		return result != null;
	}

	private boolean isTree(Material material) {
		return material.name().endsWith("LOG");
	}
}
