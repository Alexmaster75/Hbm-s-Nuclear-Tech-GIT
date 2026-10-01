package com.hbm.tileentity.machine;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardReceiverMK2;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerMachineGrowthChamber;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.gui.GUIMachineGrowthChamber;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.main.NTMSounds;
import com.hbm.module.machine.ModuleMachineGrowthChamber;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BobMathUtil;
import com.hbm.util.fauxpointtwelve.DirPos;
import com.hbm.util.i18n.I18nUtil;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;

public class TileEntityMachineGrowthChamber extends TileEntityMachineBase implements IEnergyReceiverMK2, IFluidStandardReceiverMK2, IGUIProvider, IControlReceiver, IUpgradeInfoProvider {
	public long power;
	public long maxPower = 100_000;
	public FluidTank tank;
	public ModuleMachineGrowthChamber module;
	public boolean didProcess = false;

	public UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

	private AudioWrapper audio;

	public TileEntityMachineGrowthChamber() {
		super(5);
		this.tank = new FluidTank(Fluids.NONE, 16_000);
		this.module = new ModuleMachineGrowthChamber(0, this, slots).itemInput(1).itemOutput(4).fluidInput(tank);
	}

	public DirPos[] getConPos() {
		return new DirPos[] {
			new DirPos(xCoord + 2, yCoord, zCoord, Library.POS_X),
			new DirPos(xCoord - 2, yCoord, zCoord, Library.NEG_X),
			new DirPos(xCoord, yCoord, zCoord + 2, Library.POS_Z),
			new DirPos(xCoord, yCoord, zCoord - 2, Library.NEG_Z),
			new DirPos(xCoord + 2, yCoord + 2, zCoord, Library.POS_X),
			new DirPos(xCoord - 2, yCoord + 2, zCoord, Library.NEG_X),
			new DirPos(xCoord, yCoord + 2, zCoord + 2, Library.POS_Z),
			new DirPos(xCoord, yCoord + 2, zCoord - 2, Library.NEG_Z)
		};
	}

	@Override
	public long getPower() {
		return this.power;
	}

	@Override
	public void setPower(long power) {
		this.power = power;
	}

	@Override
	public long getMaxPower() {
		return maxPower;
	}

	@Override
	public boolean hasPermission(EntityPlayer player) {
		return this.isUseableByPlayer(player);
	}

	@Override
	public void receiveControl(NBTTagCompound data) {
		if(data.hasKey("index") && data.hasKey("selection")) {
			int index = data.getInteger("index");
			String selection = data.getString("selection");
			if(index == 0) {
				this.module.setRecipe(selection, false);
				this.markChanged();
			}
		}
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerMachineGrowthChamber(player.inventory, this);
	}

	@Override @SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIMachineGrowthChamber(player.inventory, this);
	}

	@Override
	public String getName() { return ""; }

	private AxisAlignedBB bb = null;
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if (bb == null) bb = AxisAlignedBB.getBoundingBox(xCoord - 1, yCoord, zCoord - 1, xCoord + 2, yCoord + 3, zCoord + 2);
		return bb;
	}

	@Override @SideOnly(Side.CLIENT) public double getMaxRenderDistanceSquared() {return 65536d; }

	@Override
	public void updateEntity() {
		if (!worldObj.isRemote) {
			GenericRecipe recipe = module.getRecipe();
			if (recipe != null) this.maxPower = recipe.power * 100;
			this.maxPower = BobMathUtil.max(this.power, this.maxPower, 100_000);
			this.power = Library.chargeTEFromItems(slots, 0, power, maxPower);
			upgradeManager.checkSlots(slots, 2, 3);

			for (DirPos pos : getConPos()) {
				this.trySubscribe(worldObj, pos);
				if (tank.getTankType() != Fluids.NONE) this.trySubscribe(tank.getTankType(), worldObj, pos);
			}

			double speed = 1;
			double pow = 1;

			speed += Math.min(upgradeManager.getLevel(UpgradeType.SPEED), 3) / 3D;
			speed += Math.min(upgradeManager.getLevel(UpgradeType.OVERDRIVE), 3);

			pow -= Math.min(upgradeManager.getLevel(UpgradeType.POWER), 3) * 0.25D;
			pow += Math.min(upgradeManager.getLevel(UpgradeType.SPEED), 3) * 1D;
			pow += Math.min(upgradeManager.getLevel(UpgradeType.OVERDRIVE), 3) * 10D / 3D;

			this.module.update(speed, pow, true, null);
			this.didProcess = module.didProcess;
			if (module.markDirty) this.markDirty();

			this.networkPackNT(100);
		} else {
			if (this.didProcess && MainRegistry.proxy.me().getDistance(xCoord, yCoord ,zCoord) < 30) {
				if (audio == null) {
					audio = createAudioLoop();
					audio.startSound();
				} else if (!audio.isPlaying()) {
					audio = rebootAudio(audio);
				}
				audio.keepAlive();
				audio.updateVolume(getVolume(2f));
			} else {
				if (audio != null) {
					audio.stopSound();
					audio = null;
				}
			}
		}
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		if (slot == 0) return true; //Battery
		if(slot >= 2 && slot <= 3 && stack.getItem() instanceof ItemMachineUpgrade) return true; // upgrades
		return module.isItemValid(slot, stack); // input
	}
	@Override public boolean canExtractItem(int slot, ItemStack itemStack, int side) { return slot == 4 || module.isSlotClogged(slot); }
	@Override public int[] getAccessibleSlotsFromSide(int side) { return new int[] {1, 4}; }

	@Override public FluidTank[] getReceivingTanks() { return new FluidTank[]{tank}; }
	@Override public FluidTank[] getAllTanks() { return new FluidTank[]{tank}; }

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt); // Slots
		this.power = nbt.getLong("power");
		this.maxPower = nbt.getLong("maxPower");
		this.tank.readFromNBT(nbt, "t");
		this.module.readFromNBT(nbt);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setLong("power", power);
		nbt.setLong("maxPower", maxPower);
		tank.writeToNBT(nbt, "t");
		module.writeToNBT(nbt);
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(power);
		buf.writeLong(maxPower);
		tank.serialize(buf);
		module.serialize(buf);
		buf.writeBoolean(didProcess);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.power = buf.readLong();
		this.maxPower = buf.readLong();
		tank.deserialize(buf);
		module.deserialize(buf);
		this.didProcess = buf.readBoolean();
	}

	@Override
	public boolean canProvideInfo(ItemMachineUpgrade.UpgradeType type, int level, boolean extendedInfo) {
		return type == UpgradeType.SPEED || type == UpgradeType.POWER || type == UpgradeType.OVERDRIVE;
	}

	@Override
	public void provideInfo(ItemMachineUpgrade.UpgradeType type, int level, List<String> info, boolean extendedInfo) {
		info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_growth_chamber));
		if(type == UpgradeType.SPEED) {
			info.add(EnumChatFormatting.GREEN + I18nUtil.resolveKey(KEY_SPEED, "+" + (level * 100 / 3) + "%"));
			info.add(EnumChatFormatting.RED + I18nUtil.resolveKey(KEY_CONSUMPTION, "+" + (level * 50) + "%"));
		}
		if(type == UpgradeType.POWER) {
			info.add(EnumChatFormatting.GREEN + I18nUtil.resolveKey(KEY_CONSUMPTION, "-" + (level * 25) + "%"));
		}
		if(type == UpgradeType.OVERDRIVE) {
			info.add((BobMathUtil.getBlink() ? EnumChatFormatting.RED : EnumChatFormatting.DARK_GRAY) + "YES");
		}
	}

	@Override
	public HashMap<ItemMachineUpgrade.UpgradeType, Integer> getValidUpgrades() {
		HashMap<UpgradeType, Integer> upgrades = new HashMap<>();
		upgrades.put(UpgradeType.SPEED, 3);
		upgrades.put(UpgradeType.POWER, 3);
		upgrades.put(UpgradeType.OVERDRIVE, 3);
		return upgrades;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemStack) {
		super.setInventorySlotContents(i, itemStack);
		if (itemStack != null && i >= 2 && i <= 3 && itemStack.getItem() instanceof ItemMachineUpgrade)
			worldObj.playSoundEffect(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, "hbm:item.upgradePlug", 1f, 1f);
	}

	@Override
	public boolean shouldRenderInPass(int pass) {
		return pass == 0 || pass == 1;
	}

	@Override
	public AudioWrapper createAudioLoop() {
		return MainRegistry.proxy.getLoopedSound(NTMSounds.GROWTH_CHAMBER_LOOP, xCoord, yCoord, zCoord, 1f, 15f, 1f, 20);
	}

	@Override
	public void onChunkUnload() {
		if (audio != null) { audio.stopSound(); audio = null; }
	}

	@Override
	public void invalidate() {
		super.invalidate();
		if (audio != null) { audio.stopSound(); audio = null; }
	}
}
