package com.hbm.module.machine;

import api.hbm.energymk2.IEnergyHandlerMK2;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.recipes.GrowthChamberRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.inventory.recipes.loader.GenericRecipes;
import com.hbm.util.BobMathUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Random;

public class ModuleMachineGrowthChamber extends ModuleMachineBase {
	private static final Random rand = new Random();

	public int cycleDuration = 0;
	public int ticks = 0;
	public int remaining = 0;

	public ModuleMachineGrowthChamber(int index, IEnergyHandlerMK2 battery, ItemStack[] slots) {
		super(index, battery, slots);
		this.inputSlots = new int[1];
		this.outputSlots = new int[1];
		this.inputTanks = new FluidTank[1];
		this.outputTanks = FluidTank.EMPTY_ARRAY;
	}

	@Override
	public GrowthChamberRecipes getRecipeSet() {
		return GrowthChamberRecipes.INSTANCE;
	}

	public boolean isRunning() {
		return ticks > 0;
	}

	private long getCost(GenericRecipe recipe, double power) {
		return power == 1 ? recipe.power : (long) (recipe.power * power);
	}

	@Override
	public boolean canProcess(GenericRecipe recipe, double speed, double power) {
		if (recipe == null) return false;
		if (battery.getPower() < getCost(recipe, power)) return false;
		if (!isRunning() && !hasInput(recipe)) return false;
		return canFitOutput(recipe);
	}

	@Override
	public void process(GenericRecipe recipe, double speed, double power) {
		battery.setPower(battery.getPower() - recipe.power);

		if (!isRunning()) {
			consumeInput(recipe);
			remaining = recipe.outputItem[0].getSingle().stackSize;
			int rolled = recipe.duration + rand.nextInt(recipe.duration + 1);
			cycleDuration = Math.max(remaining, (int) (rolled / speed));
		}

		ticks++;
		int ticksLeft = cycleDuration - ticks + 1;
		if (remaining > 0 && rand.nextInt(ticksLeft) < remaining) {
			produceItem(recipe);
			remaining--;
		}

		this.progress = (double) ticks / cycleDuration;
		if (ticks >= cycleDuration) { ticks = 0; remaining = 0; }
		this.markDirty = true;
	}

	public ModuleMachineGrowthChamber itemInput(int from) { for(int i = 0; i < inputSlots.length; i++) inputSlots[i] = from + i; return this; }
	public ModuleMachineGrowthChamber itemOutput(int a) { outputSlots[0] = a; return this; }
	public ModuleMachineGrowthChamber fluidInput(FluidTank a) { inputTanks[0] = a; return this; }

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setInteger("cycleDuration" + index, cycleDuration);
		nbt.setInteger("ticks" + index, ticks);
		nbt.setInteger("remaining" + index, remaining);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.cycleDuration = nbt.getInteger("cycleDuration" + index);
		this.ticks = nbt.getInteger("ticks" + index);
		this.remaining = nbt.getInteger("remaining" + index);
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeInt(ticks);
		buf.writeInt(cycleDuration);
		buf.writeInt(remaining);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.ticks = buf.readInt();
		this.cycleDuration = buf.readInt();
		this.remaining = buf.readInt();
	}
}
