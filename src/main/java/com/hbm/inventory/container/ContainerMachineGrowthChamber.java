package com.hbm.inventory.container;

import api.hbm.energymk2.IBatteryItem;
import com.hbm.inventory.SlotCraftingOutput;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerMachineGrowthChamber extends ContainerBase {
	public ContainerMachineGrowthChamber(InventoryPlayer invPlayer, IInventory chamber) {
		super(invPlayer, chamber);
		this.addSlotToContainer(new SlotNonRetarded(chamber, 0, 152, 72)); //Battery
		this.addSlotToContainer(new SlotNonRetarded(chamber, 1, 44, 36)); //Input Slot
		this.addSlots(chamber, 2, 89, 63, 1, 2); // Upgrades
		this.addSlotToContainer(new SlotCraftingOutput(invPlayer.player, chamber, 4, 107, 36)); // Output Slot

		this.playerInv(invPlayer, 8, 122);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		ItemStack slotOriginal = null;
		Slot slot = (Slot) this.inventorySlots.get(index);

		if(slot != null && slot.getHasStack()) {
			ItemStack slotStack = slot.getStack();
			slotOriginal = slotStack.copy();

			if(index <= tile.getSizeInventory() - 1) {
				SlotCraftingOutput.checkAchievements(player, slotStack);
				if(!this.mergeItemStack(slotStack, tile.getSizeInventory(), this.inventorySlots.size(), true)) {
					return null;
				}
			} else {

				if(slotOriginal.getItem() instanceof IBatteryItem || slotOriginal.getItem() == ModItems.battery_creative) {
					if(!this.mergeItemStack(slotStack, 0, 1, false)) return null;
				} else if(slotOriginal.getItem() == ModItems.blueprints) {
					if(!this.mergeItemStack(slotStack, 1, 2, false)) return null;
				} else if(slotOriginal.getItem() instanceof ItemMachineUpgrade) {
					if(!this.mergeItemStack(slotStack, 2, 4, false)) return null;
				} else {
					if(!InventoryUtil.mergeItemStack(this.inventorySlots, slotStack, 1, 2, false)) return null;
				}
			}

			if(slotStack.stackSize == 0) {
				slot.putStack(null);
			} else {
				slot.onSlotChanged();
			}

			slot.onPickupFromSlot(player, slotStack);
		}

		return slotOriginal;
	}
}
