package com.hbm.tileentity.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.machine.BlockMedicineSynthesizer;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.handler.contagion.DiseaseDefinition;
import com.hbm.handler.contagion.GenomeSample;
import com.hbm.handler.contagion.PharmaProfile;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerMedicineSynthesizer;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Pathogen;
import com.hbm.inventory.gui.GUIMedicineSynthesizer;
import com.hbm.items.ItemVial;
import com.hbm.items.ModItems;
import com.hbm.items.tool.ItemFloppyDisk;
import com.hbm.items.tool.ItemMedicalSyringe;
import com.hbm.lib.Library;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BufferUtil;
import com.hbm.util.fauxpointtwelve.DirPos;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluid.IFluidStandardTransceiver;
import api.hbm.fluidmk2.IFillableItem;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityMedicineSynthesizer extends TileEntityMachineBase implements IGUIProvider, IControlReceiver, IEnergyReceiverMK2, IFluidStandardTransceiver {

	public FluidTank tank;
	public int progress;
	public int maxProgress = 100;

	public long power;
	public long maxPower = 1000000;

	public boolean showFrameTop = true;
	public boolean active;
	public String displayGenome = "";

	public TileEntityMedicineSynthesizer() {
		super(6);
		this.tank = new FluidTank(Fluids.ANTISERUM, 16000);
	}

	@Override
	public String getName() {
		return "container.medicineSynthesizer";
	}

	public boolean isDiseaseMode() {
		return tank.getTankType().hasTrait(FT_Pathogen.class);
	}

	public DiseaseDefinition getDefinition() {
		ItemStack floppy = slots[2];
		if(floppy == null || floppy.getItem() != ModItems.floppy_disk) return null;

		DiseaseDefinition def = ItemFloppyDisk.getStoredDef(floppy);
		if(def == null) def = GenomeSample.buildDefinition(ItemFloppyDisk.getSections(floppy));
		return def;
	}

	@Override
	public void updateEntity() {
		if(!worldObj.isRemote) {

			this.power = Library.chargeTEFromItems(slots, 4, power, maxPower);

			for(DirPos pos : getConPos()) {
				this.trySubscribe(worldObj, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
				if(tank.getTankType() != Fluids.NONE) this.trySubscribe(tank.getTankType(), worldObj, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
			}

			DiseaseDefinition def = getDefinition();
			maxProgress = def == null ? 100 : Math.max(1, def.incubationTicks);
			displayGenome = readGenome();

			boolean can = canProcess(def);
			active = can || progress > 0;

			if(can) {
				power -= 200;
				progress++;
				if(progress >= maxProgress) {
					progress = 0;
					process(def);
					this.markDirty();
				}
			} else {
				progress = 0;
			}

			networkPackNT(15);
		}
	}

	private String readGenome() {
		ItemStack floppy = slots[2];
		if(floppy == null || floppy.getItem() != ModItems.floppy_disk) return "";
		String genome = ItemFloppyDisk.getGenome(floppy);
		return genome == null ? "" : genome;
	}

	private boolean canProcess(DiseaseDefinition def) {

		if(power < 200) return false;
		if(tank.getFill() < 100) return false;
		if(def == null) return false;

		ItemStack syringe = slots[0];
		if(syringe == null || syringe.getItem() != ModItems.medical_syringe) return false;
		if(IFillableItem.getFluidFill(syringe) > 0) return false;

		ItemStack pharma = slots[3];
		if(pharma == null || pharma.getItem() != ModItems.pharma_computing_unit) return false;

		ItemStack output = slots[5];
		if(output != null && (output.getItem() != ModItems.medical_syringe || IFillableItem.getFluidFill(output) > 0)) return false;

		if(!isDiseaseMode()) {
			ItemStack vial = slots[1];
			if(vial == null || vial.getItem() != ModItems.vial) return false;
			String vialFrame = ItemVial.readFrame(vial);
			if(vialFrame == null || !vialFrame.equals(def.id)) return false;
		}

		return true;
	}

	private void process(DiseaseDefinition def) {

		ItemStack syringe = slots[0];
		String genome = readGenome();

		if(isDiseaseMode()) {
			IFillableItem.setFluidFill(syringe, tank.getTankType(), (short) ItemMedicalSyringe.MAX_DOSE);

			NBTTagCompound mut = new NBTTagCompound();
			if(genome != null && !genome.isEmpty()) mut.setString("genome", genome);
			mut.setTag("def", def.toNBT());

			NBTTagCompound entry = new NBTTagCompound();
			entry.setString("frame", def.id == null ? "" : def.id);
			entry.setFloat("amount", 100F);
			entry.setTag("mut", mut);

			NBTTagList list = new NBTTagList();
			list.appendTag(entry);
			if(!syringe.hasTagCompound()) syringe.stackTagCompound = new NBTTagCompound();
			syringe.stackTagCompound.setTag(ItemMedicalSyringe.KEY_PATHOGENS, list);

		} else {
			IFillableItem.setFluidFill(syringe, Fluids.ANTISERUM, (short) ItemMedicalSyringe.MAX_DOSE);

			PharmaProfile profile = new PharmaProfile();
			profile.target = def.id;
			profile.targetGenome = genome;
			if(!syringe.hasTagCompound()) syringe.stackTagCompound = new NBTTagCompound();
			syringe.stackTagCompound.setTag("pharma", profile.toNBT());

			slots[1] = null;
		}

		ItemStack pharma = slots[3];
		pharma.setItemDamage(pharma.getItemDamage() + 1);
		if(pharma.getItemDamage() >= pharma.getMaxDamage()) {
			slots[3] = null;
		}

		tank.setFill(tank.getFill() - 100);

		slots[5] = syringe;
		slots[0] = null;
	}

	public long getPowerScaled(long i) {
		return (power * i) / maxPower;
	}

	public int getProgressScaled(int i) {
		return (progress * i) / Math.max(maxProgress, 1);
	}

	protected DirPos[] getConPos() {
		ForgeDirection facing = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
		int[] dim = MultiblockHandlerXR.rotate(((BlockMedicineSynthesizer) this.getBlockType()).getDimensions(), facing);
		int west = dim[4], east = dim[5], north = dim[2], south = dim[3];

		List<DirPos> positions = new ArrayList<DirPos>();
		addPorts(positions, facing, 0, west, east, north, south);
		addPorts(positions, facing.getOpposite(), 0, west, east, north, south);
		addPorts(positions, facing.getOpposite(), 1, west, east, north, south);
		return positions.toArray(new DirPos[0]);
	}

	private void addPorts(List<DirPos> positions, ForgeDirection dir, int dy, int west, int east, int north, int south) {
		int y = yCoord + dy;
		if(dir == ForgeDirection.EAST) {
			for(int z = zCoord - north; z <= zCoord + south; z++) positions.add(new DirPos(xCoord + east + 1, y, z, Library.POS_X));
		} else if(dir == ForgeDirection.WEST) {
			for(int z = zCoord - north; z <= zCoord + south; z++) positions.add(new DirPos(xCoord - west - 1, y, z, Library.NEG_X));
		} else if(dir == ForgeDirection.SOUTH) {
			for(int x = xCoord - west; x <= xCoord + east; x++) positions.add(new DirPos(x, y, zCoord + south + 1, Library.POS_Z));
		} else if(dir == ForgeDirection.NORTH) {
			for(int x = xCoord - west; x <= xCoord + east; x++) positions.add(new DirPos(x, y, zCoord - north - 1, Library.NEG_Z));
		}
	}

	@Override
	public boolean canConnect(ForgeDirection dir) {
		ForgeDirection facing = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
		return dir == facing || dir == facing.getOpposite();
	}

	@Override
	public boolean canConnect(FluidType type, ForgeDirection dir) {
		return canConnect(dir);
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return new FluidTank[] {tank};
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return new FluidTank[0];
	}

	@Override
	public FluidTank[] getAllTanks() {
		return new FluidTank[] {tank};
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(power);
		buf.writeInt(progress);
		buf.writeInt(maxProgress);
		tank.serialize(buf);
		buf.writeBoolean(showFrameTop);
		buf.writeBoolean(active);
		BufferUtil.writeString(buf, displayGenome);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		power = buf.readLong();
		progress = buf.readInt();
		maxProgress = buf.readInt();
		tank.deserialize(buf);
		showFrameTop = buf.readBoolean();
		active = buf.readBoolean();
		displayGenome = BufferUtil.readString(buf);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		power = nbt.getLong("power");
		progress = nbt.getInteger("progress");
		maxProgress = nbt.hasKey("maxProgress") ? nbt.getInteger("maxProgress") : 100;
		displayGenome = nbt.getString("displayGenome");
		tank.readFromNBT(nbt, "tank");
		showFrameTop = !nbt.hasKey("showFrameTop") || nbt.getBoolean("showFrameTop");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setLong("power", power);
		nbt.setInteger("progress", progress);
		nbt.setInteger("maxProgress", maxProgress);
		nbt.setString("displayGenome", displayGenome);
		tank.writeToNBT(nbt, "tank");
		nbt.setBoolean("showFrameTop", showFrameTop);
	}

	@Override
	public void receiveControl(NBTTagCompound data) {
	}

	@Override
	public boolean hasPermission(EntityPlayer player) {
		return player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) < 16 * 16;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerMedicineSynthesizer(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIMedicineSynthesizer(player.inventory, this);
	}

	@Override
	public int getInventoryStackLimit() {
		return 1;
	}

	@Override public long getPower() { return power; }
	@Override public void setPower(long power) { this.power = power; }
	@Override public long getMaxPower() { return maxPower; }
}
