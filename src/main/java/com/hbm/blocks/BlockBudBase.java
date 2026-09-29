package com.hbm.blocks;

import com.hbm.items.ModItems;
import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Random;

public abstract class BlockBudBase extends BlockBase {
	public static int renderID = RenderingRegistry.getNextAvailableRenderId();
	protected final int stage;

	protected final float halfWidth;
	protected final float height;
	protected Block nextStage;

	public BlockBudBase(Material material, int stage, float halfWidth, float height) {
		super(material);
		this.stage = stage;
		this.halfWidth = halfWidth;
		this.height = height;
	}

	public static int getRenderID() {
		return renderID;
	}

	public BlockBudBase setNextStage(Block next) {
		this.nextStage = next;
		return this;
	}

	public int getStage() {
		return stage;
	}

	public static ForgeDirection getDir(int meta) {
		return ForgeDirection.getOrientation(meta);
	}

	public Block getNextStage() {
		return nextStage;
	}

	@Override
	public int getRenderType() {
		return renderID;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public int onBlockPlaced(World world, int x, int y, int z, int side, float fx, float fy, float fz, int meta) {
		return side;
	}

	@Override
	public boolean canPlaceBlockOnSide(World world, int x, int y, int z, int side) {
		return canBlockStay(world, x, y, z, ForgeDirection.getOrientation(side));
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
		int meta = world.getBlockMetadata(x, y, z);
		if (!canBlockStay(world, x, y, z, getDir(meta))) {
			this.dropBlockAsItem(world, x, y, z, meta, 0);
			world.setBlockToAir(x, y, z);
		}
	}

	public boolean canBlockStay(World world, int x, int y, int z, ForgeDirection dir) {
		return world.isSideSolid(x - dir.offsetX, y - dir.offsetY, z - dir.offsetZ, dir);
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess access, int x, int y, int z) {
		setBoundsForDir(getDir(access.getBlockMetadata(x, y, z)));
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		setBoundsForDir(getDir(world.getBlockMetadata(x, y, z)));
		return super.getCollisionBoundingBoxFromPool(world, x, y, z);
	}

	@Override
	public void setBlockBoundsForItemRender() {
		setBoundsForDir(ForgeDirection.UP);
	}

	protected void setBoundsForDir(ForgeDirection dir) {
		this.setBlockBounds(
			min(dir.offsetX), min(dir.offsetY), min(dir.offsetZ),
			max(dir.offsetX), max(dir.offsetY), max(dir.offsetZ));
	}

	private float min(int offset) {
		return offset == 1 ? 0 : offset == -1 ? 1 - height : 0.5f - halfWidth;
	}

	private float max(int offset) {
		return offset == 1 ? height : offset == -1 ? 1 : 0.5f + halfWidth;
	}

	@Override
	public int getMobilityFlag() {
		return 1;
	}

	@Override
	protected boolean canSilkHarvest() {
		return true;
	}
	@Override
	public abstract Item getItemDropped(int meta, Random rand, int fortune);
	@Override
	public abstract int quantityDropped(Random rand);
}
