package com.hbm.tileentity.machine;

import api.hbm.fluidmk2.IFluidReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardSenderMK2;
import api.hbm.redstoneoverradio.IRORValueProvider;
import com.hbm.dim.trait.CBT_Atmosphere;
import com.hbm.explosion.ExplosionNT;
import com.hbm.handler.atmosphere.AtmosphereBlob;
import com.hbm.handler.atmosphere.ChunkAtmosphereManager;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.inventory.OreDictManager;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Gaseous;
import com.hbm.items.ItemEnums.EnumAshType;
import com.hbm.items.ModItems;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.particle.helper.ExplosionCreator;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IBufPacketReceiver;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BufferUtil;
import com.hbm.util.fauxpointtwelve.DirPos;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.List;

public class TileEntityMachineTurboCompressor extends TileEntityMachineBase implements IFluidReceiverMK2, IBufPacketReceiver, IFluidStandardSenderMK2, IRORValueProvider {

	public int onTicks;
	public long rpm;
	public long demand;
	public float spin;
	public float lastSpin;
	private int warnCooldown = 0;
	private int overspeed = 0;
	public final long maxRpm = 1000;
	private AudioWrapper audio;

	public int ashLevelSoot;
	public static int thresholdSoot = 8000;
	public static int maxSoot = 64;
	public FluidTank compair;
	public boolean hasBypass;


	public TileEntityMachineTurboCompressor() {
		super(1);
		this.compair = new FluidTank(Fluids.AIR, 100_000);
	}

	@Override
	public String getName() { return ""; }

	@Override
	public void updateEntity() {

		if(!worldObj.isRemote) {

			if(worldObj.getTotalWorldTime() % 20 == 0) {
				FluidType[] types = new FluidType[] {Fluids.SMOKE, Fluids.SMOKE_LEADED, Fluids.SMOKE_POISON};

				for (FluidType type : types) {
					for (DirPos pos : getConPos()) {
						this.trySubscribe(type, worldObj, pos);
					}
				}
			}

			// ide complains it can be simplified, too bad it didn't account for slots[0] to be null
			if (onTicks > 0 && (slots[0] == null ? true : (slots[0].stackSize < maxSoot))) {
				if (slots[0] != null && slots[0].stackSize > maxSoot / 2)
					//this.demand *= Math.log10(9.0D * 2.0D * (maxSoot - slots[0].stackSize) / maxSoot + 1); // more linear
					this.demand *= Math.sqrt(1 - Math.pow(2.0D * (maxSoot - slots[0].stackSize) / maxSoot - 1, 2)); // perfectly goniometric, resists and collapses more
				this.rpm += this.demand;

				if (ashLevelSoot >= thresholdSoot) {
					if (slots[0] == null) {
						slots[0] = OreDictManager.DictFrame.fromOne(ModItems.powder_ash, EnumAshType.SOOT);
					} else if (slots[0].getItem() == ModItems.powder_ash && slots[0].getItemDamage() == EnumAshType.SOOT.ordinal()) {
						slots[0].stackSize++;
					}
					ashLevelSoot -= thresholdSoot;
				}
			}

			if (warnCooldown > 0)
				warnCooldown--;

			boolean isInPressurizedRoom = ChunkAtmosphereManager.proxy.hasAtmosphere(worldObj, xCoord, yCoord, zCoord);
			CBT_Atmosphere atmosphere = ChunkAtmosphereManager.proxy.getAtmosphere(worldObj, xCoord, yCoord, zCoord);

			if (rpm < 0) rpm = 0;
			if (rpm > 0) {
				if (atmosphere != null && atmosphere.getPressure() > 0.01D) {
					// 1mB of any given air -> 100mB of compressed air
					// reasoning being: this is the same conversion ratio of water -> steam
					int consumption = (int) (rpm * 2 / 10);

					if (consumption > 0) {
						if (!isInPressurizedRoom) {
							FT_Gaseous.capture(worldObj, atmosphere.getMainFluid(), consumption);
						} else {
							List<AtmosphereBlob> blobs = ChunkAtmosphereManager.proxy.getBlobs(worldObj, xCoord, yCoord, zCoord);
							for (AtmosphereBlob blob : blobs) {
								if (blob.hasPressure(0.1)) {
									blob.consume(consumption);
									break;
								}
							}
						}
					}

					// * 2 because 100 RPM = 200 mB/t ig
					// the bypass allows to remove soot automatically, but for balancing the air produced gets reduced
					this.compair.setFill((int) Math.min(this.compair.getMaxFill(), this.compair.getFill() + rpm * 2 * 10 * (this.hasBypass ? 0.9 : 1.0)));
				}

				if (rpm > maxRpm) {
					this.overspeed++;

					if (overspeed > 60 && warnCooldown == 0) {
						warnCooldown = 100;
						worldObj.playSoundEffect(xCoord + 0.5, yCoord + 1, zCoord + 0.5, "hbm:block.warnOverspeed", 2.0F, 1.0F);
					}

					if (overspeed > 300) {
						new ExplosionNT(worldObj, null, xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, (float) (Math.sqrt(Math.abs(rpm - maxRpm) + 1) / 100)).overrideResolution(64).explode();
						ExplosionCreator.composeEffectStandard(worldObj, xCoord + 0.5, yCoord + 0.5, zCoord + 0.5);
						this.markDirty();
						worldObj.setBlockToAir(xCoord, yCoord, zCoord); // it's technically useless, but this way im 100% sure it's dead
					}
				} else {
					this.overspeed = 0;
				}
			} else {
				this.overspeed = 0;
				this.warnCooldown = 0;
			}

			for (DirPos pos : getConPos()) {
				if (this.compair.getFill() > 0) this.tryProvide(compair, worldObj, pos);
			}

			this.rpm *= 0.99D;

			networkPackNT(150);

			if (onTicks > 0) onTicks--;

		} else {
			// propeller
			if (onTicks > 0)
				this.spawnParticles();
			this.lastSpin = this.spin;
			this.spin += rpm;

			if (this.spin >= 360F) {
				this.spin -= 360F;
				this.lastSpin -= 360F;
			}

			// audio
			if (rpm > 0) {
				if (audio == null) {
					audio = MainRegistry.proxy.getLoopedSound("hbm:block.turbinegasRunning", xCoord, yCoord, zCoord, getVolume(1.0F), 20F, 2.0F, 20);
					audio.startSound();
				} else if(!audio.isPlaying()) {
					audio.stopSound();
					audio = MainRegistry.proxy.getLoopedSound("hbm:block.turbinegasRunning", xCoord, yCoord, zCoord, getVolume(1.0F), 20F, 2.0F, 20);
					audio.startSound();
				}

				audio.updatePitch((float) (0.2 + 0.1 * rpm / 100));
				audio.updateVolume(getVolume((float) Math.sqrt(rpm) / 10F));
				audio.keepAlive();

			} else {
				if (audio != null) {
					audio.stopSound();
					audio = null;
				}
			}
		}
	}

	public DirPos[] getConPos() {
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
		return new DirPos[] {
			new DirPos(xCoord + 2, yCoord, zCoord, Library.POS_X),
			new DirPos(xCoord - 2, yCoord, zCoord, Library.NEG_X),
			new DirPos(xCoord, yCoord, zCoord + 2, Library.POS_Z),
			new DirPos(xCoord, yCoord, zCoord - 2, Library.NEG_Z)
		};
	}

	public void spawnParticles() {

		if(worldObj.getTotalWorldTime() % 2 == 0) {
			NBTTagCompound fx = new NBTTagCompound();
			fx.setString("type", "tower");
			fx.setFloat("lift", 5F);
			fx.setFloat("base", 0.5F);
			fx.setFloat("max", 3F);
			fx.setInteger("life", 300 + worldObj.rand.nextInt(80));
			fx.setInteger("color",0x000000);
			fx.setDouble("posX", xCoord + 0.5);
			fx.setDouble("posY", yCoord + 2.5);
			fx.setDouble("posZ", zCoord + 0.5);
			MainRegistry.proxy.effectNT(fx);
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		buf.writeInt(this.onTicks);
		buf.writeLong(this.rpm);
		compair.serialize(buf);
		buf.writeBoolean(hasBypass);

		for (ItemStack slot : slots) {
			BufferUtil.writeItemStack(buf, slot);
		}
	}

	@Override
	public void deserialize(ByteBuf buf) {
		this.onTicks = buf.readInt();
		this.rpm = buf.readLong();
		compair.deserialize(buf);
		this.hasBypass = buf.readBoolean();

		for(int i = 0; i < slots.length; i++) {
			slots[i] = BufferUtil.readItemStack(buf);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setLong("rpm", rpm);
		nbt.setInteger("ashLevelSoot", ashLevelSoot);
		compair.writeToNBT(nbt, "compair");
		nbt.setBoolean("hasBypass", hasBypass);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.rpm = nbt.getLong("rpm");
		this.ashLevelSoot = nbt.getInteger("ashLevelSoot");
		compair.readFromNBT(nbt, "compair");
		this.hasBypass = nbt.getBoolean("hasBypass");
	}

	@Override
	public void onChunkUnload() {
		if(audio != null) {
			audio.stopSound();
			audio = null;
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();

		if(audio != null) {
			audio.stopSound();
			audio = null;
		}
	}

	@Override
	public boolean canConnect(FluidType type, ForgeDirection dir) {
		return (dir == ForgeDirection.NORTH || dir == ForgeDirection.SOUTH || dir == ForgeDirection.EAST || dir == ForgeDirection.WEST) &&
			(type == Fluids.SMOKE || type == Fluids.SMOKE_LEADED || type == Fluids.SMOKE_POISON);
	}

	@Override
	public long transferFluid(FluidType type, int pressure, long fluid) {

		if(type != Fluids.SMOKE && type != Fluids.SMOKE_LEADED && type != Fluids.SMOKE_POISON) return fluid;

		onTicks = 20;

		demand = (long) (fluid * 0.2D);

		ashLevelSoot += (int) fluid;

		if(type == Fluids.SMOKE) PollutionHandler.incrementPollution(worldObj, xCoord, yCoord, zCoord, PollutionHandler.PollutionType.SOOT, fluid / 100F);
		if(type == Fluids.SMOKE_LEADED) PollutionHandler.incrementPollution(worldObj, xCoord, yCoord, zCoord, PollutionHandler.PollutionType.HEAVYMETAL, fluid / 100F);
		if(type == Fluids.SMOKE_POISON) PollutionHandler.incrementPollution(worldObj, xCoord, yCoord, zCoord, PollutionHandler.PollutionType.POISON, fluid / 100F);

		return 0;
	}

	@Override
	public long getDemand(FluidType type, int pressure) {
		return 1_000_000;
	}

	@Override
	public FluidTank[] getAllTanks() {
		return new FluidTank[] {compair};
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return new FluidTank[] {compair};
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, int j) {
		return hasBypass && rpm > 0;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		return new int[] {0};
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	public static final String[] ROR = new String[] { // not to be confused with RUR
		PREFIX_VALUE + "rpm",
		PREFIX_VALUE + "soot",
	};

	@Override
	public String[] getFunctionInfo() {
		return ROR;
	}

	@Override
	public String provideRORValue(String name) {
		if((PREFIX_VALUE + "rpm").equals(name))  return "" + (int) (rpm * 100);
		if((PREFIX_VALUE + "soot").equals(name)) return "" + (slots[0] != null ? slots[0].stackSize : 0);
		return null;
	}
}
