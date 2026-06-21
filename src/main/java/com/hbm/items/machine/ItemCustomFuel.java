package com.hbm.items.machine;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.util.BobMathUtil;
import com.hbm.util.EnumUtil;
import com.hbm.util.i18n.I18nUtil;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;

public class ItemCustomFuel extends Item {

	protected IIcon iconBG;
	public static enum EnumCustomFuel {

		U235(	1.00D,	0.00D),
		U238(	1.25D,   -1.00D),
		PU239(	1.50D,	0.00D),
		NP237(	1.75D,	0.00D),
		SB326(	2.00D,	0.20D),
		SB327(	1.25D,	0.50D),
		;

		public double reactivity;
		public double selfInfluence;

		private EnumCustomFuel(double react, double self) {
			this.reactivity = react;
			this.selfInfluence = self;
		}
	}

	public static HashMap<NTMMaterial, ItemCustomFuel.EnumCustomFuel> materialMap = new HashMap();

	public static void init() {
		if(!materialMap.isEmpty()) return;
		materialMap.put(Mats.MAT_U235, ItemCustomFuel.EnumCustomFuel.U235);
		materialMap.put(Mats.MAT_U238, ItemCustomFuel.EnumCustomFuel.U238);
		materialMap.put(Mats.MAT_PU239, ItemCustomFuel.EnumCustomFuel.PU239);
		materialMap.put(Mats.MAT_NEPTUNIUM, ItemCustomFuel.EnumCustomFuel.NP237);
		materialMap.put(Mats.MAT_SCHRABIDIUM, ItemCustomFuel.EnumCustomFuel.SB326);
		materialMap.put(Mats.MAT_SOLINIUM, ItemCustomFuel.EnumCustomFuel.SB327);
	}

	public ItemCustomFuel() {
		this.setMaxStackSize(64);
	}

	public static enum CustomFluxFuncEnum {
		LOGARITHM(),
		SQUARE_ROOT(),
		NEGATIVE_QUADRATIC(),
		LINEAR(),
		PASSIVE();

		private CustomFluxFuncEnum() { }
	}

	// very hacky way of doing things, should work as long as the sizes are the same..
	public static ItemStack setup(List<Mats.MaterialStack> stack) {
		return setup(new ItemStack(ModItems.custom_fuel), stack);
	}

	public static ItemStack setup(ItemStack stack, List<Mats.MaterialStack> mats) {
		if(!stack.hasTagCompound()) stack.stackTagCompound = new NBTTagCompound();
		int i = 0;
		for (Mats.MaterialStack mat : mats) {
			for (NTMMaterial material : materialMap.keySet()) {
				if (mat.material == material) {
					stack.stackTagCompound.setByte("type" + i, (byte) materialMap.get(material).ordinal());
					i++;
					break;
				}
			}
		}
		return stack;
	}


}
