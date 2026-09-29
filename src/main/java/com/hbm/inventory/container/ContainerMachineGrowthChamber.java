package com.hbm.inventory.container;

import com.hbm.inventory.SlotCraftingOutput;
import com.hbm.inventory.SlotNonRetarded;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;

public class ContainerMachineGrowthChamber extends ContainerBase {
	public ContainerMachineGrowthChamber(InventoryPlayer invPlayer, IInventory chamber) {
		super(invPlayer, chamber);
		this.addSlotToContainer(new SlotNonRetarded(chamber, 0, 152, 72)); //Battery
		this.addSlotToContainer(new SlotNonRetarded(chamber, 1, 44, 36)); //Input Slot
		this.addSlots(chamber, 2, 89, 63, 1, 2); // Upgrades
		this.addSlotToContainer(new SlotCraftingOutput(invPlayer.player, chamber, 4, 107, 36)); // Output Slot

		this.playerInv(invPlayer, 8, 122);
	}
}
