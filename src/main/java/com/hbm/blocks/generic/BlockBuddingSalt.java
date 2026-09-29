package com.hbm.blocks.generic;

import com.hbm.blocks.BlockBuddingBase;
import com.hbm.blocks.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.world.World;

import java.util.Random;

public class BlockBuddingSalt extends BlockBuddingBase {
	public BlockBuddingSalt() {
		super();
	}

	public BlockBuddingSalt(Material material) {
		super(material);
	}

	@Override
	public float growthChance() {
		return 0.5f;
	}

	@Override
	public Block getFirstStage() {
		return ModBlocks.salt_bud_small;
	}

	@Override
	protected boolean canGrow(World world, int x, int y, int z) {
		return true;
	}
}
