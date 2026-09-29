package com.hbm.inventory.container;

import com.hbm.inventory.SlotTakeOnly;
import com.hbm.items.ModItems;
import com.hbm.tileentity.machine.TileEntityMedicineSynthesizer;

import api.hbm.fluidmk2.IFillableItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerMedicineSynthesizer extends ContainerBase {

	public ContainerMedicineSynthesizer(InventoryPlayer invPlayer, final TileEntityMedicineSynthesizer tile) {
		super(invPlayer, tile);

		addSlotToContainer(new Slot(tile, 0, 43, 22) {
			@Override
			public boolean isItemValid(ItemStack stack) {
				return stack.getItem() == ModItems.medical_syringe && IFillableItem.getFluidFill(stack) == 0;
			}
		});
		addSlotToContainer(new Slot(tile, 1, 43, 40) {
			@Override
			public boolean isItemValid(ItemStack stack) {
				return !tile.isDiseaseMode() && stack.getItem() == ModItems.vial;
			}
			@Override
			public boolean canTakeStack(EntityPlayer player) {
				return !tile.isDiseaseMode() && super.canTakeStack(player);
			}
		});
		addSlotToContainer(new Slot(tile, 2, 43, 58));
		addSlotToContainer(new Slot(tile, 3, 94, 81));

		addSlotToContainer(new Slot(tile, 4, 247, 84));

		addSlotToContainer(new SlotTakeOnly(tile, 5, 150, 40));

		playerInv(invPlayer, 48, 135);
	}
}
