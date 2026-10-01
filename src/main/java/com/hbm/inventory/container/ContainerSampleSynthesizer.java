package com.hbm.inventory.container;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class ContainerSampleSynthesizer extends ContainerBase {

	public ContainerSampleSynthesizer(InventoryPlayer invPlayer, IInventory machine) {
		super(invPlayer, machine);



		addSlotToContainer(new Slot(machine, 0, 63, 21));
		addSlotToContainer(new Slot(machine, 1, 36, 44));
		addSlotToContainer(new Slot(machine, 2, 99, 21));

		addSlotToContainer(new Slot(machine, 3, 152, 72));

		playerInv(invPlayer, 8, 125, 183);
	}
}
