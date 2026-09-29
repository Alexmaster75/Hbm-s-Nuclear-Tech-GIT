package com.hbm.handler.nei;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.recipes.GrowthChamberRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipes;
import net.minecraft.block.Block;

public class GrowthChamberHandler extends NEIGenericRecipeHandler {
	public GrowthChamberHandler() {
		super(ModBlocks.machine_growth_chamber.getLocalizedName(), GrowthChamberRecipes.INSTANCE, ModBlocks.machine_growth_chamber);
	}

	@Override
	public String getRecipeID() {
		return "ntmGrowthChamber";
	}

	@Override
	public int recipiesPerPage() {
		return 1;
	}
}
