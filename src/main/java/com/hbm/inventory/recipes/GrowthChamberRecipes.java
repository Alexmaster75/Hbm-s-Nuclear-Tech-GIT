package com.hbm.inventory.recipes;

import com.hbm.blocks.BlockEnums.EnumCrystalBlockType;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockNTMFlower.EnumFlowerType;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.OreDictManager.DictFrame;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.inventory.recipes.loader.GenericRecipes;
import com.hbm.items.ItemEnums.EnumPlantType;
import com.hbm.items.ModItems;
import com.hbm.util.Compat;
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
		// Salt
		this.register(new GenericRecipe("growth.salt.salt_water").setup(500, 200)
			.inputItems(new ComparableStack(ModItems.salt_shard))
			.inputFluids(new FluidStack(Fluids.SALT_WATER, 4_000))
			.outputItems(new ItemStack(ModBlocks.salt_cluster, 1)));
		this.register(new GenericRecipe("growth.salt.brine").setup(500, 200)
			.inputItems(new ComparableStack(ModItems.salt_shard))
			.inputFluids(new FluidStack(Fluids.BRINE, 1000))
			.outputItems(new ItemStack(ModBlocks.salt_cluster, 3)));
		this.register(new GenericRecipe("growth.salt.water").setup(250, 100)
			.inputItems(new ComparableStack(ModItems.salt_shard))
			.inputFluids(new FluidStack(Fluids.WATER, 16000))
			.outputItems(new ItemStack(ModBlocks.salt_bud_large, 1)));
		this.register(new GenericRecipe("growth.budding_salt").setup(1000, 100)
			.inputItems(new ComparableStack(ModBlocks.block_crystal_3, 8, EnumCrystalBlockType.SALT.ordinal() - 32))
			.inputFluids(new FluidStack(Fluids.BRINE, 16000, 2))
			.outputItems(new ItemStack(ModBlocks.budding_salt)));

		// Certus Quartz
		if (Compat.isModLoaded(Compat.MOD_AE2)) {
			this.register(new GenericRecipe("growth.certus_quartz.water")
				.inputItems(new ComparableStack(ModItems.powder_certus_quartz))
				.inputFluids(new FluidStack(Fluids.WATER, 8000))
				.outputItems(new ItemStack(ModBlocks.certus_quartz_bud_large)));
			this.register(new GenericRecipe("growth.certus_quartz.heavywater")
				.inputItems(new ComparableStack(ModItems.powder_certus_quartz))
				.inputFluids(new FluidStack(Fluids.HEAVYWATER, 8000))
				.outputItems(new ItemStack(ModBlocks.certus_quartz_cluster)));
			this.register(new GenericRecipe("growth.budding_certus_quartz")
				.inputItems(new ComparableStack(ModBlocks.block_crystal_3, 8, EnumCrystalBlockType.CERTUS.ordinal() - 32))
				.inputFluids(new FluidStack(Fluids.HEAVYWATER, 16000, 4))
				.outputItems(new ItemStack(ModBlocks.budding_certus_quartz)));
		}

		//Plants
		this.register(new GenericRecipe("growth.hemp").setup(100, 50)
			.inputItems(new ComparableStack(ModBlocks.plant_flower, 1, EnumFlowerType.WEED))
			.inputFluids(new FluidStack(Fluids.WATER, 1000))
			.outputItems(new ItemStack(ModBlocks.plant_flower, 3, EnumFlowerType.WEED.ordinal())));
		this.register(new GenericRecipe("growth.nightshade").setup(100, 50)
			.inputItems(new ComparableStack(ModBlocks.plant_flower, 1, EnumFlowerType.NIGHTSHADE))
			.inputFluids(new FluidStack(Fluids.WATER, 1000))
			.outputItems(new ItemStack(ModBlocks.plant_flower, 3, EnumFlowerType.NIGHTSHADE.ordinal())));
		this.register(new GenericRecipe("growth.foxglove").setup(100, 50)
			.inputItems(new ComparableStack(ModBlocks.plant_flower, 1, EnumFlowerType.FOXGLOVE))
			.inputFluids(new FluidStack(Fluids.WATER, 1000))
			.outputItems(new ItemStack(ModBlocks.plant_flower, 3, EnumFlowerType.FOXGLOVE.ordinal())));
		this.register(new GenericRecipe("growth.tabacco").setup(100, 50)
			.inputItems(new ComparableStack(ModBlocks.plant_flower, 1, EnumFlowerType.TOBACCO))
			.inputFluids(new FluidStack(Fluids.WATER, 1000))
			.outputItems(new ItemStack(ModBlocks.plant_flower, 3, EnumFlowerType.TOBACCO.ordinal())));
		this.register(new GenericRecipe("growth.strawberry").setup(100, 50)
			.inputItems(new ComparableStack(ModBlocks.plant_flower, 1, EnumFlowerType.STRAWBERRY))
			.inputFluids(new FluidStack(Fluids.WATER, 1000))
			.outputItems(new ItemStack(ModBlocks.plant_flower, 3, EnumFlowerType.STRAWBERRY.ordinal())));
		this.register(new GenericRecipe("growth.mint").setup(100, 50)
			.inputItems(new ComparableStack(ModBlocks.plant_flower, 1, EnumFlowerType.MINT))
			.inputFluids(new FluidStack(Fluids.WATER, 1000))
			.outputItems(new ItemStack(ModBlocks.plant_flower, 3, EnumFlowerType.MINT.ordinal())));
		this.register(new GenericRecipe("growth.cd0").setup(100, 50)
			.inputItems(new ComparableStack(ModBlocks.plant_flower, 1, EnumFlowerType.CD0))
			.inputFluids(new FluidStack(Fluids.WATER, 1000))
			.outputItems(DictFrame.fromOne(ModBlocks.plant_flower, EnumFlowerType.CD0, 3)));
		this.register(new GenericRecipe("growth.mustard_willow").setup(100, 50)
			.inputItems(new ComparableStack(ModBlocks.plant_flower, 1, EnumFlowerType.CD0))
			.inputFluids(new FluidStack(Fluids.OIL, 500))
			.outputItems(DictFrame.fromOne(ModItems.plant_item, EnumPlantType.MUSTARDWILLOW, 1)));
	}
}
