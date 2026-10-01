package com.hbm.module.machine;

import api.hbm.energymk2.IEnergyHandlerMK2;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.recipes.GrowthChamberRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import net.minecraft.item.ItemStack;

import java.util.Random;

public class ModuleMachineGrowthChamber extends ModuleMachineBase {
	private static final Random rand = new Random();

	public ModuleMachineGrowthChamber(int index, IEnergyHandlerMK2 battery, ItemStack[] slots) {
		super(index, battery, slots);
		this.inputSlots = new int[1];
		this.outputSlots = new int[1];
		this.inputTanks = new FluidTank[1];
		this.outputTanks = FluidTank.EMPTY_ARRAY;
	}

	@Override
	public void process(GenericRecipe recipe, double speed, double power) {
		if(this.restrictedMode) speed *= 0.25; // RoR controlled machines have a speed penalty

		this.battery.setPower(this.battery.getPower() - (power == 1 ? recipe.power : (long) (recipe.power * power)));
		double step = Math.min(speed / (recipe.duration + rand.nextInt(recipe.duration)), 1D); // can't do more than one recipe per tick, might look into that later
		this.progress += step;

		if(this.progress >= 1D) {
			consumeInput(recipe);
			produceItem(recipe);

			if(this.canProcess(recipe, speed, power))  this.progress -= 1D;
			else this.progress = 0D;
		}
	}

	@Override
	public GrowthChamberRecipes getRecipeSet() {
		return GrowthChamberRecipes.INSTANCE;
	}

	public ModuleMachineGrowthChamber itemInput(int from) { for(int i = 0; i < inputSlots.length; i++) inputSlots[i] = from + i; return this; }
	public ModuleMachineGrowthChamber itemOutput(int a) { outputSlots[0] = a; return this; }
	public ModuleMachineGrowthChamber fluidInput(FluidTank a) { inputTanks[0] = a; return this; }
}
