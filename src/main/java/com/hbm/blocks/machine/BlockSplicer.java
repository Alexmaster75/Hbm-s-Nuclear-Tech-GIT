package com.hbm.blocks.machine;

import com.hbm.tileentity.machine.TileEntityMachineSplicer;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockSplicer extends BlockMachineBase {

	public BlockSplicer() {
		super(Material.iron, 0);
		this.rotatable = true;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityMachineSplicer();
	}
}
