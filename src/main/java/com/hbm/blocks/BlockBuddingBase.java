package com.hbm.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Random;
public abstract class BlockBuddingBase extends BlockBase {
	protected BlockBuddingBase() {
		super();
		this.setTickRandomly(true);
	}

	protected BlockBuddingBase(Material material) {
		super(material);
		this.setTickRandomly(true);
	}

	public abstract float growthChance();
	public abstract Block getFirstStage();
	protected abstract boolean canGrow(World world, int x, int y, int z);

	@Override
	public void updateTick(World world, int x, int y, int z, Random rand) {
		if (world.isRemote || !canGrow(world, x, y, z)) return;
		if (rand.nextFloat() > growthChance()) return;
		attemptGrowth(world, x, y, z, rand);
	}

	protected boolean attemptGrowth(World world, int x, int y, int z, Random rand) {
		ForgeDirection dir = ForgeDirection.getOrientation(rand.nextInt(6));

		int targetX = x + dir.offsetX;
		int targetY = y + dir.offsetY;
		int targetZ = z + dir.offsetZ;
		Block targetBlock = world.getBlock(targetX, targetY, targetZ);
		if (canNewBudGrow(world, targetX, targetY, targetZ, targetBlock)) {
			return world.setBlock(targetX, targetY, targetZ, getFirstStage(), dir.ordinal(), 3);
		}

		if (targetBlock instanceof BlockBudBase && canBudGrow(world, targetX, targetY, targetZ, targetBlock, dir)) {
			Block next = ((BlockBudBase) targetBlock).getNextStage();
			if (next != null) {
				return world.setBlock(targetX, targetY, targetZ, next, dir.ordinal(), 3);
			}
		}
		return false;
	}

	protected boolean canNewBudGrow(World world, int x, int y, int z, Block block) {
		return block.isAir(world, x, y, z);
	}

	protected boolean canBudGrow(World world, int x, int y, int z, Block block, ForgeDirection dir) {
		return BlockBudBase.getDir(world.getBlockMetadata(x, y, z)) == dir;
	}

	@Override
	protected boolean canSilkHarvest() {
		return false;
	}

	@Override
	public Item getItemDropped(int p_149650_1_, Random p_149650_2_, int p_149650_3_) {
		return null;
	}

	@Override
	public int getMobilityFlag() {
		return 2;
	}
}
