package com.hbm.blocks.machine;

import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.main.NTMSounds;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityCloner;

import api.hbm.energymk2.IEnergyConnectorBlock;
import api.hbm.fluidmk2.IFluidConnectorBlockMK2;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockCloner extends BlockDummyable implements ITooltipProvider, IEnergyConnectorBlock, IFluidConnectorBlockMK2 {

	public BlockCloner(Material mat) {
		super(mat);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		if(meta >= 12) return new TileEntityCloner();
		if(meta >= extra) return new TileEntityProxyCombo().inventory().power().fluid();
		return null;
	}

	@Override public int[] getDimensions() { return new int[] {2, 0, 0, 1, 0, 1}; }
	@Override public int getOffset() { return 0; }

	@Override
	public boolean canConnect(IBlockAccess world, int x, int y, int z, ForgeDirection dir) {
		return this.canClonerConnect(world, x, y, z, dir);
	}

	@Override
	public boolean canConnect(FluidType type, IBlockAccess world, int x, int y, int z, ForgeDirection dir) {
		return this.canClonerConnect(world, x, y, z, dir);
	}

	private boolean canClonerConnect(IBlockAccess world, int x, int y, int z, ForgeDirection dir) {

		int[] core = this.findCore(world, x, y, z);
		if(core == null || y != core[1]) return false;

		TileEntity te = world.getTileEntity(core[0], core[1], core[2]);
		return te instanceof TileEntityCloner && ((TileEntityCloner) te).canConnect(dir);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		int[] core = this.findCore(world, x, y, z);
		if(core == null) return false;

		TileEntity te = world.getTileEntity(core[0], core[1], core[2]);
		if(!(te instanceof TileEntityCloner)) return false;

		if(player.isSneaking()) {
			ItemStack held = player.getHeldItem();

			if(held != null && held.getItem() instanceof IItemFluidIdentifier) {
				if(!world.isRemote) ((TileEntityCloner) te).setTankType(((IItemFluidIdentifier) held.getItem()).getType(world, core[0], core[1], core[2], held));
				return true;
			}

			if(held == null) {
				if(!world.isRemote) ((TileEntityCloner) te).toggle();
				world.playSoundEffect(core[0] + 0.5D, core[1] + 0.5D, core[2] + 0.5D, NTMSounds.LEVER_STOP, 1F, 1F);
				return true;
			}
		}

		return this.standardOpenBehavior(world, core[0], core[1], core[2], player, 0);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		this.addStandardInfo(stack, player, list, ext);
	}
}
