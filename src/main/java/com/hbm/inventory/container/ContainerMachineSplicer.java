package com.hbm.inventory.container;

import com.hbm.tileentity.machine.TileEntityMachineSplicer;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerMachineSplicer extends ContainerBase {

	public ContainerMachineSplicer(InventoryPlayer invPlayer, final TileEntityMachineSplicer tile) {
		super(invPlayer, tile);

		this.addSlotToContainer(new Slot(tile, 0, 6, 31) {
			@Override
			public boolean isItemValid(ItemStack stack) {
				return tile.isItemValidForSlot(0, stack);
			}
		});

		this.addSlotToContainer(new Slot(tile, 1, 6, 79) {
			@Override
			public boolean isItemValid(ItemStack stack) {
				return tile.isItemValidForSlot(1, stack);
			}
		});

		this.addSlotToContainer(new Slot(tile, 2, 85, 55) {
			@Override
			public boolean isItemValid(ItemStack stack) {
				return tile.isItemValidForSlot(2, stack);
			}
		});

		this.addSlotToContainer(new Slot(tile, 3, 188, 55) {
			@Override
			public boolean isItemValid(ItemStack stack) {
				return tile.isItemValidForSlot(3, stack);
			}
		});

		this.playerInv(invPlayer, 20, 154);
	}
}
