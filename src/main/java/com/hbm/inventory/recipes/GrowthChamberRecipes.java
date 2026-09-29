package com.hbm.inventory.recipes;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.inventory.recipes.loader.GenericRecipes;
import com.hbm.items.ModItems;
import net.minecraft.item.ItemStack;

public class GrowthChamberRecipes extends GenericRecipes<GenericRecipe> {
	public static final GrowthChamberRecipes INSTANCE = new GrowthChamberRecipes();

	@Override
	public int inputItemLimit() { return 1; }
	@Override
	public int inputFluidLimit() { return 1; }
	@Override
	public int outputItemLimit() { return 1; }
	@Override
	public int outputFluidLimit() { return 0; }
	@Override
	public String getFileName() { return "hbmGrowthChamber.json"; }
	@Override
	public GenericRecipe instantiateRecipe(String name) { return new GenericRecipe(name); }
	@Override
	public void registerDefaults() {
		this.register(new GenericRecipe("growth.salt.water").setup(500, 200)
			.inputItems(new ComparableStack(ModItems.crystal_salt))
			.inputFluids(new FluidStack(Fluids.WATER, 16_000))
			.outputItems(new ItemStack(ModBlocks.salt_cluster, 1)));
		this.register(new GenericRecipe("growth.salt.brine").setup(500, 200)
			.inputItems(new ComparableStack(ModItems.crystal_salt))
			.inputFluids(new FluidStack(Fluids.BRINE, 1000))
			.outputItems(new ItemStack(ModBlocks.salt_cluster, 3)));
	}
}
