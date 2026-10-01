package com.hbm.blocks.generic;

import com.hbm.blocks.BlockBudBase;
import com.hbm.items.ModItems;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;

import java.util.Random;

public class BlockBudSalt extends BlockBudBase {
	public BlockBudSalt(int stage, float halfWidth, float height) {
		super(Material.rock, stage, halfWidth, height);
	}

	@Override
	public Item getItemDropped(int meta, Random rand, int fortune) {
		return stage >= 2 ? ModItems.salt_shard : null;
	}

	@Override
	public int quantityDropped(Random rand) {
		return stage == 2 ? 1 : 2 + rand.nextInt(3);
	}
}
