package com.hbm.tileentity.machine;

import java.util.HashMap;
import java.util.Map;

import com.hbm.handler.contagion.DiseaseDefinition;
import com.hbm.handler.contagion.GenomeSample;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerMachineSplicer;
import com.hbm.inventory.gui.GUIMachineSplicer;
import com.hbm.items.ModItems;
import com.hbm.items.tool.ItemFloppyDisk;
import com.hbm.lib.Library;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;

import api.hbm.energymk2.IBatteryItem;
import api.hbm.energymk2.IEnergyReceiverMK2;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityMachineSplicer extends TileEntityMachineBase implements IEnergyReceiverMK2, IGUIProvider, IControlReceiver {

	public static final String[] GENOME_SEGMENTS = { "severity", "resistance", "transmission" };

	public long power;
	public long maxPower = 100_000;
	public long consumption = 100;

	public int progress;
	public boolean active;

	public int displayBits;
	public String displayGenome = "";

	public TileEntityMachineSplicer() {
		super(4);
	}

	@Override
	public String getName() {
		return "container.machineSplicer";
	}

	@Override
	public void updateEntity() {

		if(!worldObj.isRemote) {

			this.power = Library.chargeTEFromItems(slots, 3, this.getPower(), this.getMaxPower());

			if(worldObj.getTotalWorldTime() % 20 == 0) {
				for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
					this.trySubscribe(worldObj, xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ, dir);
				}
			}

			if(this.active) {
				if(!canProcess()) {
					this.active = false;
					this.progress = 0;
				} else {
					this.power -= this.consumption;
					this.progress++;

					if(this.progress >= 400) {
						this.progress = 0;
						this.active = false;
						this.finish();
					}
				}
			}

			this.updateDisplay();

			this.maxPower = Math.max(this.consumption * 20, this.power);
			this.networkPackNT(20);
		}
	}

	public static boolean isGenomeSegment(String key) {
		for(String segment : GENOME_SEGMENTS) if(segment.equals(key)) return true;
		return false;
	}

	public static int segmentOffset(String key) {
		for(int i = 0; i < GENOME_SEGMENTS.length; i++) if(GENOME_SEGMENTS[i].equals(key)) return i * GenomeSample.GENOME_SEGMENT;
		return 0;
	}

	public boolean isSample(ItemStack stack) {
		return stack != null && stack.getItem() == ModItems.vial && GenomeSample.getKey(stack) != null && GenomeSample.getValue(stack) != null;
	}

	public boolean isFloppy(ItemStack stack) {
		return stack != null && stack.getItem() == ModItems.floppy_disk;
	}

	public boolean isPharma(ItemStack stack) {
		return stack != null && stack.getItem() == ModItems.pharma_computing_unit;
	}

	public boolean isBattery(ItemStack stack) {
		return stack != null && (stack.getItem() instanceof IBatteryItem || stack.getItem() == ModItems.battery_creative);
	}

	private Map<String, String> sections() {
		ItemStack floppy = slots[1];
		return isFloppy(floppy) ? ItemFloppyDisk.getSections(floppy) : new HashMap<String, String>();
	}

	private void updateDisplay() {

		Map<String, String> sections = this.sections();
		String genome = GenomeSample.buildGenome(sections);

		int bits = 0;
		for(String segment : GENOME_SEGMENTS) if(sections.containsKey(segment)) bits += GenomeSample.GENOME_SEGMENT;

		if(this.active && this.progress > 200) {
			String key = GenomeSample.getKey(slots[0]);
			String value = GenomeSample.getValue(slots[0]);

			if(isGenomeSegment(key) && value != null && value.length() >= GenomeSample.GENOME_SEGMENT) {
				int written = Math.min(GenomeSample.GENOME_SEGMENT, (this.progress - 200) * GenomeSample.GENOME_SEGMENT / 200);
				int offset = segmentOffset(key);
				genome = genome.substring(0, offset) + value.substring(0, written) + genome.substring(offset + written);
				if(!sections.containsKey(key)) bits += written;
			}
		}

		this.displayGenome = genome;
		this.displayBits = bits;
	}

	public boolean canProcess() {
		if(this.power < this.consumption) return false;
		if(!isSample(slots[0])) return false;
		if(!isFloppy(slots[1])) return false;
		if(!isPharma(slots[2])) return false;
		return true;
	}

	private void finish() {

		ItemStack vial = slots[0];
		ItemStack floppy = slots[1];

		ItemFloppyDisk.putSection(floppy, GenomeSample.getKey(vial), GenomeSample.getValue(vial));

		Map<String, String> sections = ItemFloppyDisk.getSections(floppy);
		DiseaseDefinition def = GenomeSample.buildDefinition(sections);
		ItemFloppyDisk.writeGenome(floppy, def, GenomeSample.buildGenome(sections));

		vial.stackSize--;
		if(vial.stackSize <= 0) slots[0] = null;

		ItemStack pharma = slots[2];
		pharma.setItemDamage(pharma.getItemDamage() + 1);
		if(pharma.getItemDamage() >= pharma.getMaxDamage()) slots[2] = null;

		this.markDirty();
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(power);
		buf.writeLong(maxPower);
		buf.writeLong(consumption);
		buf.writeInt(progress);
		buf.writeBoolean(active);
		buf.writeInt(displayBits);
		buf.writeInt(displayGenome.length());
		for(int i = 0; i < displayGenome.length(); i++) buf.writeChar(displayGenome.charAt(i));
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		power = buf.readLong();
		maxPower = buf.readLong();
		consumption = buf.readLong();
		progress = buf.readInt();
		active = buf.readBoolean();
		displayBits = buf.readInt();
		int length = buf.readInt();
		StringBuilder sb = new StringBuilder(length);
		for(int i = 0; i < length; i++) sb.append(buf.readChar());
		displayGenome = sb.toString();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.power = nbt.getLong("power");
		this.maxPower = nbt.getLong("maxPower");
		this.progress = nbt.getInteger("progress");
		this.active = nbt.getBoolean("active");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setLong("power", power);
		nbt.setLong("maxPower", maxPower);
		nbt.setInteger("progress", progress);
		nbt.setBoolean("active", active);
	}

	@Override public long getPower() { return Math.max(Math.min(power, maxPower), 0); }
	@Override public void setPower(long power) { this.power = power; }
	@Override public long getMaxPower() { return maxPower; }

	@Override
	public boolean canConnect(ForgeDirection dir) {
		return dir != ForgeDirection.UNKNOWN;
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		if(slot == 0) return isSample(stack);
		if(slot == 1) return isFloppy(stack);
		if(slot == 2) return isPharma(stack);
		if(slot == 3) return isBattery(stack);
		return false;
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, int side) {
		return slot <= 3;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		return new int[] { 0, 1, 2, 3 };
	}

	@Override
	public boolean hasPermission(EntityPlayer player) {
		return player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) < 16 * 16;
	}

	@Override
	public void receiveControl(NBTTagCompound data) {

		if(data.getBoolean("start")) {
			if(!this.active && canProcess()) {
				this.active = true;
				this.progress = 0;
				this.markDirty();
			}
		}

		if(data.getBoolean("delete")) {
			ItemStack floppy = slots[1];
			if(isFloppy(floppy)) {
				ItemFloppyDisk.clearPathogen(floppy);
				this.updateDisplay();
				this.markDirty();
			}
		}
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerMachineSplicer(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIMachineSplicer(player.inventory, this);
	}
}
