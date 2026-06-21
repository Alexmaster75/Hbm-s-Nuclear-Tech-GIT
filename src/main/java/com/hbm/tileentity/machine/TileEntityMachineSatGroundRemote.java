package com.hbm.tileentity.machine;

import com.hbm.inventory.container.ContainerMachineSatGroundRemote;
import com.hbm.inventory.gui.GUIMachineSatGroundRemote;
import com.hbm.items.ISatChip;
import com.hbm.saveddata.SatelliteSavedData;
import com.hbm.saveddata.satellites.Satellite;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.util.BobMathUtil;
import li.cil.oc.api.network.SimpleComponent;

import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers")})
public class TileEntityMachineSatGroundRemote extends TileEntity implements ISidedInventory, IGUIProvider, SimpleComponent {
	private ItemStack[] slots;
	public Satellite satellite;
	public int id;
	public String name;

	//public static final int maxFill = 64 * 3;

	private static final int[] slots_top = new int[] {0};
	private static final int[] slots_bottom = new int[] {0};
	private static final int[] slots_side = new int[] {0};

	private String customName;

	public TileEntityMachineSatGroundRemote() {
		slots = new ItemStack[1];
	}

	@Override
	public int getSizeInventory() {
		return slots.length;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return slots[i];
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i) {
		if(slots[i] != null) {
			ItemStack itemStack = slots[i];
			slots[i] = null;
			return itemStack;
		} else {
			return null;
		}
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemStack) {
		slots[i] = itemStack;
		if(itemStack != null && itemStack.stackSize > getInventoryStackLimit()) {
			itemStack.stackSize = getInventoryStackLimit();
		}
	}

	@Override
	public String getInventoryName() {
		return this.hasCustomInventoryName() ? this.customName : "container.satGroundRemote";
	}

	@Override
	public boolean hasCustomInventoryName() {
		return this.customName != null && this.customName.length() > 0;
	}

	public void setCustomName(String name) {
		this.customName = name;
		markDirty();
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		if(worldObj.getTileEntity(xCoord, yCoord, zCoord) != this) {
			return false;
		} else {
			return player.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 64;
		}
	}

	@Override
	public void openInventory() {}
	@Override
	public void closeInventory() {}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack) {
		return false;
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		if(slots[i] != null) {
			if(slots[i].stackSize <= j) {
				ItemStack itemStack = slots[i];
				slots[i] = null;
				return itemStack;
			}
			ItemStack itemStack1 = slots[i].splitStack(j);
			if (slots[i].stackSize == 0) {
				slots[i] = null;
			}

			return itemStack1;
		} else {
			return null;
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		NBTTagList list = nbt.getTagList("items", 10);

		slots = new ItemStack[getSizeInventory()];

		for(int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound nbt1 = list.getCompoundTagAt(i);
			byte b0 = nbt1.getByte("slot");
			if(b0 >= 0 && b0 < slots.length) {
				slots[b0] = ItemStack.loadItemStackFromNBT(nbt1);
			}
		}

		customName = nbt.getString("name");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		NBTTagList list = new NBTTagList();

		for(int i = 0; i < slots.length; i++) {
			if(slots[i] != null) {
				NBTTagCompound nbt1 = new NBTTagCompound();
				nbt1.setByte("slot", (byte)i);
				slots[i].writeToNBT(nbt1);
				list.appendTag(nbt1);
			}
		}
		nbt.setTag("items", list);

		if (customName != null) {
			nbt.setString("name", customName);
		}
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int p_94128_1_) {
        return new int[] {0};
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemStack, int j) {
		return this.isItemValidForSlot(i, itemStack);
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, int j) {
		return true;
	}

	@Override
	public void updateEntity() {
		if(!worldObj.isRemote) {
			if(slots[0] != null && slots[0].getItem() instanceof ISatChip) {
				satellite = SatelliteSavedData.getData(worldObj, xCoord, zCoord).getSatFromFreq(ISatChip.getFreqS(slots[0]));
				if (satellite != null) {
					id = satellite.getID();
					name = satellite.toString();
				} else {
					id = -1;
					name = null;
				}
			} else {
				satellite = null;
			}
		}
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerMachineSatGroundRemote(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIMachineSatGroundRemote(player.inventory, this);
	}

	@Override
	@Optional.Method(modid = "OpenComputers")
	public String getComponentName() {
		return "ntm_satremote";
	}

	@Callback(direct = false)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getSatName(Context context, Arguments args) {
		return new Object[] {name};
	}

	@Callback(direct = false)
	@Optional.Method(modid = "OpenComputers")
	public Object[] setCoords(Context context, Arguments args) {
		if (satellite != null) {
			if (satellite.satIface == Satellite.Interfaces.SAT_PANEL) {
				satellite.onClick(worldObj, args.checkInteger(0), args.checkInteger(1));
				return new Object[]{true};
			} else if (satellite.satIface == Satellite.Interfaces.SAT_COORD) {
				satellite.onCoordAction(worldObj, null, args.checkInteger(0), args.checkInteger(1), args.checkInteger(2));
				return new Object[]{true};
			}
		}
		return new Object[] {false};
	}

	@Callback(direct = false)
	@Optional.Method(modid = "OpenComputers")
	public Object[] setID(Context context, Arguments args) {
		if (slots[0] != null && slots[0].getItem() instanceof ISatChip) {
			ISatChip.setFreqS(slots[0], MathHelper.clamp_int(args.checkInteger(0), 0, 100000));
			return new Object[]{true};
		}
		return new Object[] {false};
	}
}
