package com.hbm.items.machine;

import java.util.List;
import java.util.Locale;

import com.hbm.items.machine.ItemRBMKRod.EnumDepleteFunc;
import com.hbm.util.BobMathUtil;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class ItemSliceFuel extends ItemFuelRod {
	
	public double reactivity;
	public FunctionSEnum function;
	
	public ItemSliceFuel(int life) {
		super(life);
		this.canRepair = false;
	}
	
	public ItemSliceFuel setFunction(FunctionSEnum function, double reactivity) {
		this.function = function;
		this.reactivity = reactivity;
		return this;
	}
	
	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {
		
		list.add(EnumChatFormatting.YELLOW + "[Kilopower Reactor Slice Fuel]");
		list.add(EnumChatFormatting.DARK_AQUA + "   " + getFunctionDesc() + (int) (10 * getDouble(itemstack, "enrichment")) / 10.0D);
		list.add(EnumChatFormatting.DARK_AQUA + "   Yield of " + BobMathUtil.getShortNumber(lifeTime) + " events");
		list.add(EnumChatFormatting.DARK_PURPLE + "   Xenon poison " + String.format(Locale.US, "%,.1f", getPoison(itemstack)) + "%");
		
		super.addInformation(itemstack, player, list, bool);
	}
	
	public static enum FunctionSEnum {
		LOGARITHM(),
		SQUARE_ROOT(),
		NEGATIVE_QUADRATIC(),
		LINEAR();
		
		private FunctionSEnum() { }
	}
	
	public String getFunctionDesc() {
		switch(this.function) {
		case LOGARITHM: return "f(x) = log10(x + 1) * 0.5 * " + reactivity;
		case SQUARE_ROOT: return "f(x) = sqrt(x) * " + reactivity + " / 10";
		case NEGATIVE_QUADRATIC: return "f(x) = [x - (x² / 10000)] / 100 * " + reactivity;
		case LINEAR: return "f(x) = x / 100 * " + reactivity;
		default: return "x";
		}
	}
	
	public double react(World world, ItemStack stack, double inFlux) {
		double outFlux = inFlux;
		double xenon = getPoison(stack);
		xenon -= xenonBurnFunc(outFlux);
		
		outFlux *= (1D - getPoison(stack)/100D);
		
		xenon += xenonGenFunc(outFlux);
		if(xenon < 0D) xenon = 0D;
		if(xenon > 100D) xenon = 100D;
		setPoison(stack, xenon);
		
		setLifeTime(stack, getLifeTime(stack) + (int) outFlux);
		
		double depletion = 1.0D - getLifeTime(stack) / lifeTime;
		double enrichment = depletion + Math.sin(depletion * Math.PI) / 3.0D;
		
		setDouble(stack, "enrichment", enrichment);
		
		switch(this.function) {
		case LOGARITHM: return (double) (Math.log10(outFlux + 1) * 0.5D * this.reactivity * enrichment);
		case SQUARE_ROOT: return (double) (Math.sqrt(outFlux) * reactivity / 10D * enrichment);
		case NEGATIVE_QUADRATIC: return (double) (Math.max((outFlux - (outFlux * outFlux / 10000D)) / 100D * reactivity, 0) * enrichment);
		case LINEAR: return (double) (outFlux / 100D * reactivity * enrichment);
		default: return 0.0D;
		}
	}
	
	private static void setNBTDefaults(ItemStack stack) {

		stack.stackTagCompound = new NBTTagCompound();
	}
	
	public static void setDouble(ItemStack stack, String key, double yield) {

		if(!stack.hasTagCompound())
			setNBTDefaults(stack);

		stack.stackTagCompound.setDouble(key, yield);
	}

	public static double getDouble(ItemStack stack, String key) {

		if(!stack.hasTagCompound())
			setNBTDefaults(stack);

		return stack.stackTagCompound.getDouble(key);
	}
	
	public static void setPoison(ItemStack stack, double xenon) {
		setDouble(stack, "xenon", xenon);
	}

	public static double getPoison(ItemStack stack) {
		return getDouble(stack, "xenon");
	}
	
	public double xenonGenFunc(double flux) {
		return flux * 0.25D;
	}
	
	public double xenonBurnFunc(double flux) {
		return (flux * flux) / 50D;
	}
}
