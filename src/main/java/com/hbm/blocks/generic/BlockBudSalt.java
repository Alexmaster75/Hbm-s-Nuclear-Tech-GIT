package com.hbm.blocks.generic;

import com.hbm.blocks.BlockBudBase;
import com.hbm.items.ModItems;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;

import java.util.Random;

public class BlockBudSalt extends BlockBudBase {
	public BlockBudSalt(Material material, int stage, float halfWidth, float height) {
		super(material, stage, halfWidth, height);
	}

	@Override
	public Item getItemDropped(int meta, Random rand, int fortune) {
		return stage >= 2 ? ModItems.crystal_salt : null;
	}

	@Override
	public int quantityDropped(Random rand) {
		return stage == 2 ? 1 : 2 + rand.nextInt(3);
	}
}
