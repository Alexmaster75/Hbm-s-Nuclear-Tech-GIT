package com.hbm.blocks.generic;

import com.hbm.blocks.BlockEnumMulti;
import com.hbm.blocks.BlockEnums.EnumCrystalBlockType;
import com.hbm.util.EnumUtil;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public class BlockCrystalBlock extends BlockEnumMulti {
// bro uses three block IDs
	public final int offset;
	public final int count;

	public BlockCrystalBlock(int offset, int count) {
		super(Material.rock, EnumCrystalBlockType.class, true, true);
		this.offset = offset;
		this.count = count;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return icons[Math.min(meta + offset, EnumCrystalBlockType.values().length - 1)];
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		Enum num = EnumUtil.grabEnumSafely(this.theEnum, stack.getItemDamage() + offset);
		return getUnlocalizedMultiName(num);
	}

	@Override
	public int getSubCount() {
		return count;
	}
}
