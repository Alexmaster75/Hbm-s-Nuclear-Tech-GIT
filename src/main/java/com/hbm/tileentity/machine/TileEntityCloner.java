package com.hbm.tileentity.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.entity.mob.EntityHusk;
import com.hbm.inventory.container.ContainerCloner;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.gui.GUICloner;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.items.ModItems;
import com.hbm.items.special.ItemHumanPart;
import com.hbm.items.tool.ItemMedicalSyringe;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.main.NTMSounds;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.fauxpointtwelve.DirPos;

import api.hbm.energymk2.IBatteryItem;
import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityCloner extends TileEntityMachineBase implements IEnergyReceiverMK2, IFluidStandardTransceiverMK2, IGUIProvider, IControlReceiver {

	public static final int SLOT_SYRINGE = 0;
	public static final int SLOT_MODDING = 1;
	public static final int SLOT_BATTERY = 6;
	public static final int ALL_PARTS = 5;
	public static final int PROCESS_TIME = 100 * 20;

	public long power;
	public long maxPower = 1_000_000;
	public long consumption = 100;

	public boolean active = false;
	public int progress;

	public static final int SPAWN_DELAY = 25;
	public int spawnDelay = 0;
	public EntityHusk pendingClone;

	public float prevDoor = 0F;
	public float door = 0F;

	public FluidTank tank;

	private AudioWrapper audio;

	public void setTankType(FluidType type) {
		this.tank.setTankType(type);
		this.markDirty();
	}

	public TileEntityCloner() {
		super(7);
		this.tank = new FluidTank(Fluids.NONE, 16_000);
	}

	@Override
	public String getName() {
		return "container.cloner";
	}

	@Override
	public void updateEntity() {

		if(worldObj.isRemote) {
			this.updateAudio();
			return;
		}

		boolean wanted = this.active && this.getSampleUUID() != null && this.getLoadedParts() == ALL_PARTS;
		if(wanted) this.power = Library.chargeTEFromItems(slots, SLOT_BATTERY, power, maxPower);

		this.prevDoor = this.door;

		if(this.spawnDelay > 0 && this.door < 1F) this.door = Math.min(1F, this.door + 0.05F);
		if(this.spawnDelay <= 0 && this.door > 0F) this.door = Math.max(0F, this.door - 0.05F);

		if(this.spawnDelay > 0) {
			this.spawnDelay--;

			if(this.spawnDelay <= 0) {
				this.spawnDelay = 0;

				if(this.pendingClone != null) {
					ForgeDirection facing = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
					int[] dim = MultiblockHandlerXR.rotate(((BlockDummyable) this.getBlockType()).getDimensions(), facing);
					int minX = xCoord - dim[4];
					int maxX = xCoord + dim[5];
					int minZ = zCoord - dim[2];
					int maxZ = zCoord + dim[3];

					double spawnX = xCoord + 0.5D;
					double spawnZ = zCoord + 0.5D;

					switch(facing) {
					case NORTH: spawnX = (minX + maxX + 1) / 2.0; spawnZ = minZ - 0.5D; break;
					case SOUTH: spawnX = (minX + maxX + 1) / 2.0; spawnZ = maxZ + 1.5D; break;
					case WEST:  spawnX = minX - 0.5D; spawnZ = (minZ + maxZ + 1) / 2.0; break;
					case EAST:  spawnX = maxX + 1.5D; spawnZ = (minZ + maxZ + 1) / 2.0; break;
					}

					this.pendingClone.setLocationAndAngles(spawnX, yCoord, spawnZ, 0F, 0F);
					worldObj.spawnEntityInWorld(this.pendingClone);

					this.pendingClone = null;
					this.markDirty();
				}
			}
		}

		if(worldObj.getTotalWorldTime() % 20 == 0) {
			for(DirPos pos : getConPos()) {
				this.trySubscribe(worldObj, pos);
				if(tank.getTankType() != Fluids.NONE) this.trySubscribe(tank.getTankType(), worldObj, pos);
			}
		}

		if(canProcess()) {
			this.progress++;
			this.power -= this.consumption;

			if(this.progress >= PROCESS_TIME) {
				this.progress = 0;
				this.cloneBody();
			}
		} else if(this.getSampleUUID() == null) {
			this.progress = 0;
		}

		this.networkPackNT(25);
	}

	@Override public FluidTank[] getAllTanks() { return new FluidTank[] { tank }; }
	@Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { tank }; }
	@Override public FluidTank[] getSendingTanks() { return FluidTank.EMPTY_ARRAY; }

	@Override
	public boolean canConnect(ForgeDirection dir) {
		return dir == ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset).getOpposite();
	}

	@Override
	public boolean canConnect(FluidType type, ForgeDirection dir) {
		return dir == ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset).getOpposite();
	}

	public DirPos[] getConPos() {
		ForgeDirection facing = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
		int[] d = MultiblockHandlerXR.rotate(((BlockDummyable) this.getBlockType()).getDimensions(), facing);

		int minX = xCoord - d[4];
		int maxX = xCoord + d[5];
		int minZ = zCoord - d[2];
		int maxZ = zCoord + d[3];

		DirPos[] ports = new DirPos[2];
		for(int i = 0; i < 2; i++) {
			DirPos port = new DirPos(xCoord, yCoord, zCoord, facing.getOpposite());

			switch(facing) {
			case NORTH: port = new DirPos(minX + i, yCoord, maxZ + 1, ForgeDirection.SOUTH); break;
			case SOUTH: port = new DirPos(minX + i, yCoord, minZ - 1, ForgeDirection.NORTH); break;
			case WEST:  port = new DirPos(maxX + 1, yCoord, minZ + i, ForgeDirection.EAST); break;
			case EAST:  port = new DirPos(minX - 1, yCoord, minZ + i, ForgeDirection.WEST); break;
			}

			ports[i] = port;
		}
		return ports;
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if(bb == null) bb = AxisAlignedBB.getBoundingBox(xCoord - 1, yCoord, zCoord - 1, xCoord + 2, yCoord + 3, zCoord + 2);
		return bb;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	public boolean canProcess() {
		if(!this.active) return false;
		if(this.power < this.consumption) return false;
		if(this.getSampleUUID() == null) return false;
		if(this.tank.getTankType() == Fluids.NONE) return false;
		return this.getLoadedParts() == ALL_PARTS;
	}

	public List<ItemStack> getDistinctParts() {
		List<ItemStack> parts = new ArrayList<ItemStack>();
		boolean[] found = new boolean[ItemHumanPart.EnumHumanPart.values().length];

		for(int i = SLOT_MODDING; i < SLOT_BATTERY; i++) {
			ItemStack stack = slots[i];
			if(stack == null) continue;

			int meta;
			if(stack.getItem() == ModItems.human_part) meta = stack.getItemDamage();
			else if(stack.getItem() == ModItems.robotic_head) meta = ItemHumanPart.EnumHumanPart.HEAD.ordinal();
			else continue;

			if(meta < 0 || meta >= found.length || found[meta]) continue;
			found[meta] = true;
			parts.add(stack);
		}

		return parts;
	}

	public int getLoadedParts() {
		return this.getDistinctParts().size();
	}

	private String getSampleUUID() {
		ItemStack syringe = slots[SLOT_SYRINGE];
		if(syringe == null || !(syringe.getItem() instanceof ItemMedicalSyringe) || syringe.stackTagCompound == null) return null;

		String uuid = syringe.stackTagCompound.getString(ItemMedicalSyringe.KEY_OWNER_UUID);
		return uuid.isEmpty() ? null : uuid;
	}

	private void cloneBody() {
		ItemStack syringe = slots[SLOT_SYRINGE];
		String uuid = this.getSampleUUID();
		if(syringe == null || uuid == null) return;

		ItemStack[] parts = new ItemStack[ALL_PARTS];
		for(int i = 0; i < ALL_PARTS; i++) {
			parts[i] = slots[SLOT_MODDING + i];
			slots[SLOT_MODDING + i] = null;
		}

		FluidType blood = this.tank.getTankType();

		EntityHusk husk = new EntityHusk(worldObj);
		husk.setupClone(uuid, syringe.stackTagCompound.getString(ItemMedicalSyringe.KEY_OWNER_NAME), parts, blood);
		this.pendingClone = husk;
		this.spawnDelay = SPAWN_DELAY;

		syringe.stackTagCompound = null;
		this.tank.setFill(0);
		this.markDirty();

		worldObj.playSoundEffect(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D, NTMSounds.ASSEMBLER_STOP, 1F, 1F);
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(power);
		buf.writeLong(maxPower);
		buf.writeBoolean(active);
		buf.writeInt(progress);
		buf.writeInt(spawnDelay);
		buf.writeFloat(prevDoor);
		buf.writeFloat(door);
		tank.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		power = buf.readLong();
		maxPower = buf.readLong();
		active = buf.readBoolean();
		progress = buf.readInt();
		spawnDelay = buf.readInt();
		prevDoor = buf.readFloat();
		door = buf.readFloat();
		tank.deserialize(buf);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.power = nbt.getLong("power");
		this.maxPower = Math.max(1_000_000L, nbt.getLong("maxPower"));
		this.active = nbt.getBoolean("active");
		this.progress = nbt.getInteger("progress");
		this.tank.readFromNBT(nbt, "tank");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setLong("power", power);
		nbt.setLong("maxPower", maxPower);
		nbt.setBoolean("active", active);
		nbt.setInteger("progress", progress);
		this.tank.writeToNBT(nbt, "tank");
	}

	@Override public long getPower() { return Math.max(Math.min(power, maxPower), 0); }
	@Override public void setPower(long power) { this.power = power; }
	@Override public long getMaxPower() { return maxPower; }

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		if(slot == SLOT_SYRINGE) return stack.getItem() == ModItems.medical_syringe;
		if(slot == SLOT_BATTERY) return stack.getItem() instanceof IBatteryItem || stack.getItem() == ModItems.battery_creative;
		return slot >= SLOT_MODDING && slot < SLOT_BATTERY;
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, int side) {
		return slot < SLOT_BATTERY;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		return new int[] { 0, 1, 2, 3, 4, 5, 6 };
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerCloner(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUICloner(player.inventory, this);
	}

	@Override
	public boolean hasPermission(EntityPlayer player) {
		return this.isUseableByPlayer(player);
	}

	public void toggle() {
		this.active = !this.active;
		this.markDirty();
	}

	@Override
	public void receiveControl(NBTTagCompound data) {
		if(data.getBoolean("toggle")) this.toggle();
	}

	private void updateAudio() {
		boolean running = this.active && this.progress > 0
				&& MainRegistry.proxy.me().getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) < 15D * 15D;

		if(running) {
			if(this.audio == null) {
				this.audio = this.createAudioLoop();
				this.audio.startSound();
			} else if(!this.audio.isPlaying()) {
				this.audio = this.rebootAudio(this.audio);
			}

			this.audio.keepAlive();
			this.audio.updateVolume(this.getVolume(1F));

		} else if(this.audio != null) {
			this.audio.stopSound();
			this.audio = null;
		}
	}

	@Override
	public AudioWrapper createAudioLoop() {
		return MainRegistry.proxy.getLoopedSound(NTMSounds.CHEMPLANT_LOOP, xCoord, yCoord, zCoord, 1F, 10F, 1F, 10);
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		if(this.audio != null) {
			this.audio.stopSound();
			this.audio = null;
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		if(this.audio != null) {
			this.audio.stopSound();
			this.audio = null;
		}
	}
}
