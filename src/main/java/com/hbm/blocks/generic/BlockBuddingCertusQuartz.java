package com.hbm.blocks.generic;

import com.hbm.blocks.BlockBuddingBase;
import com.hbm.blocks.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;

public class BlockBuddingCertusQuartz extends BlockBuddingBase {
	public BlockBuddingCertusQuartz() {
		super();
	}

	public BlockBuddingCertusQuartz(Material material) {
		super(material);
	}

	@Override
	public float growthChance() {
		return 0.5f;
	}

	@Override
	public Block getFirstStage() {
		return ModBlocks.certus_quartz_bud_small;
	}

	@Override
	protected boolean canGrow(World world, int x, int y, int z) {
		return true;
	}
}
