package com.hbm.items.tool;

import com.hbm.items.ISatChip;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemSatChip;
import com.hbm.saveddata.satellites.Satellite;

import com.hbm.saveddata.satellites.SatelliteHorizons;
import com.hbm.util.ChatBuilder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class ItemHorizonSpawn extends ItemSatChip {

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {

		if(!world.isRemote) {
			int id = world.rand.nextInt(100000);
			ItemStack gerald = new ItemStack(ModItems.sat_gerald);
			ISatChip.setFreqS(gerald, id);
			Satellite.setOwner(gerald, player.getDisplayName());
			Satellite.orbit(world, Satellite.getIDFromItem(gerald.getItem()), ISatChip.getFreqS(gerald), player.posX, player.posY, player.posZ, gerald);

			player.addChatMessage(
				ChatBuilder.start("New Gerald in orbit! [ID: ").color(EnumChatFormatting.GREEN)
				.next(String.valueOf(id)).color(EnumChatFormatting.GREEN)
				.next("]").color(EnumChatFormatting.GREEN)
				.flush());
		}

		return stack;
	}

}
