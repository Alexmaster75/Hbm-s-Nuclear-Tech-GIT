package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ICustomBlockHighlight;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.Mats.MoxerStack;
import com.hbm.items.machine.ItemScraps;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMoxer;

import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;

public class MachineMoxer extends BlockDummyable implements ITooltipProvider {

	public MachineMoxer() {
		super(Material.rock);

		this.bounding.add(AxisAlignedBB.getBoundingBox(-1.5D, 0D, -1.5D, 1.5D, 0.5D, 1.5D));
		this.bounding.add(AxisAlignedBB.getBoundingBox(-1.25D, 0.5D, -1.25D, 1.25D, 1.5D, -1D));
		this.bounding.add(AxisAlignedBB.getBoundingBox(-1.25D, 0.5D, -1.25D, -1D, 1.5D, 1.25D));
		this.bounding.add(AxisAlignedBB.getBoundingBox(-1.25D, 0.5D, 1D, 1.25D, 1.5D, 1.25D));
		this.bounding.add(AxisAlignedBB.getBoundingBox(1D, 0.5D, -1.25D, 1.25D, 1.5D, 1.25D));
		this.maxY = 0.999D; //item bounce prevention
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {

		if(meta >= 12) return new TileEntityMoxer();
		return new TileEntityProxyCombo().inventory();
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {

		if(world.isRemote) {
			return true;
		} else if(!player.isSneaking()) {
			int[] pos = this.findCore(world, x, y, z);

			if(pos == null)
				return false;
			if(player.getHeldItem() != null && player.getHeldItem().getItem() instanceof ItemTool && ((ItemTool) player.getHeldItem().getItem()).getToolClasses(player.getHeldItem()).contains("shovel")) {
				TileEntityMoxer crucible = (TileEntityMoxer) world.getTileEntity(pos[0], pos[1], pos[2]);
				List<MoxerStack> stacks = new ArrayList();
				stacks.addAll(crucible.inpStack);

				for(MoxerStack stack : stacks) {
					ItemStack scrap = ItemScraps.create(new MaterialStack(stack.material, stack.amount));
					if(!player.inventory.addItemStackToInventory(scrap)) {
						EntityItem item = new EntityItem(world, x + hitX, y + hitY, z + hitZ, scrap);
						world.spawnEntityInWorld(item);
					}
				}

				player.inventoryContainer.detectAndSendChanges();
				crucible.inpStack.clear();
				crucible.markDirty();

			} else {
				FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos[0], pos[1], pos[2]);
			}
			return true;
		} else {
			return true;
		}
	}

	@Override
	public int[] getDimensions() {
		return new int[] {1, 0, 1, 1, 1, 1};
	}

	@Override
	public int getOffset() {
		return 1;
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block b, int i) {

		TileEntity te = world.getTileEntity(x, y, z);

		if(te instanceof TileEntityMoxer) {
			TileEntityMoxer crucible = (TileEntityMoxer) te;

			List<MoxerStack> stacks = new ArrayList();
			stacks.addAll(crucible.inpStack);

			for(MoxerStack stack : stacks) {
				ItemStack scrap = ItemScraps.create(new MaterialStack(stack.material, stack.amount));
				EntityItem item = new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, scrap);
				world.spawnEntityInWorld(item);
			}

			crucible.inpStack.clear();
		}

		super.breakBlock(world, x, y, z, b, i);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldDrawHighlight(World world, int x, int y, int z) {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void drawHighlight(DrawBlockHighlightEvent event, World world, int x, int y, int z) {

		int[] pos = this.findCore(world, x, y, z);
		if(pos == null) return;
		TileEntity tile = world.getTileEntity(pos[0], pos[1], pos[2]);
		if(!(tile instanceof TileEntityMoxer)) return;
		TileEntityMoxer crucible = (TileEntityMoxer) tile;

		x = crucible.xCoord;
		y = crucible.yCoord;
		z = crucible.zCoord;

		EntityPlayer player = event.player;
		float interp = event.partialTicks;
		double dX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) interp;
		double dY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) interp;
		double dZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)interp;
		float exp = 0.002F;

		ICustomBlockHighlight.setup();
		for(AxisAlignedBB aabb : this.bounding) event.context.drawOutlinedBoundingBox(aabb.expand(exp, exp, exp).getOffsetBoundingBox(x - dX + 0.5, y - dY, z - dZ + 0.5), -1);
		ICustomBlockHighlight.cleanup();
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		this.addStandardInfo(stack, player, list, ext);
	}
}
