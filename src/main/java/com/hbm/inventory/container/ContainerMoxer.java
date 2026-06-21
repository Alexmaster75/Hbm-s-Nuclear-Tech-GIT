package com.hbm.inventory.container;

import com.hbm.inventory.SlotNonRetarded;
import com.hbm.inventory.SlotTakeOnly;
import com.hbm.tileentity.machine.TileEntityMoxer;
import com.hbm.util.InventoryUtil;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerMoxer extends Container {

	protected TileEntityMoxer crucible;

	public ContainerMoxer(InventoryPlayer invPlayer, TileEntityMoxer crucible) {
		this.crucible = crucible;

		this.addSlotToContainer(new SlotNonRetarded(crucible, 0, 26, 18));
		this.addSlotToContainer(new SlotTakeOnly(crucible, 1, 26, 81));

		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 9; j++) {
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 132 + i * 18));
			}
		}

		for(int i = 0; i < 9; i++) {
			this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 190));
		}
	}

	@Override
	public ItemStack slotClick(int slot, int button, int mode, EntityPlayer player) {
		if(mode == 2) return null;
		return super.slotClick(slot, button, mode, player);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		ItemStack stack = null;
		Slot slot = (Slot) this.inventorySlots.get(index);

		if(slot != null && slot.getHasStack()) {
			ItemStack originalStack = slot.getStack();
			stack = originalStack.copy();

			if(index <= 0) {
				if(!this.mergeItemStack(originalStack, 1, this.inventorySlots.size(), true)) {
					return null;
				}

				slot.onSlotChange(originalStack, stack);

			} else if(!InventoryUtil.mergeItemStack(this.inventorySlots, originalStack, 0, 1, false)) {
				return null;
			}

			if(originalStack.stackSize == 0) {
				slot.putStack((ItemStack) null);
			} else {
				slot.onSlotChanged();
			}
		}

		return stack;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return crucible.isUseableByPlayer(player);
	}
}
