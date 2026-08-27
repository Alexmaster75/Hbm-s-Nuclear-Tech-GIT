package com.hbm.tileentity.turret;

import api.hbm.energymk2.IEnergyReceiverMK2;
import com.hbm.entity.projectile.EntityBulletBeamBase;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerTurretRailgun;
import com.hbm.inventory.gui.GUITurretRailgun;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.factory.XFactoryFolly;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.main.NTMSounds;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BufferUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class TileEntityTurretRailgun extends TileEntityMachineBase implements IGUIProvider, IEnergyReceiverMK2, IControlReceiver {

	public final long maxPower = 5_000_000L;
	public long power;
	public final long consumption = 100_000L;
	public final long maxCharge = 30_000_000L;
	public long charge = 0L;

	public float velYaw = 1.0f;
	public float velPitch = 0.5f;
	public float barrelLength = 5.0f;

	public float rotationPitch;
	public float rotationYaw;
	public float targetRotationPitch;
	public float targetRotationYaw;

	public boolean active = false;
	public boolean aligning = false;
	public boolean charging = false;

	private AudioWrapper audio;
	private boolean chargeReadyAudio = false;

	public Vec3 targetVector = null;
	public Vec3 targetDirectionVector = null;

	public TileEntityTurretRailgun() {
		super(6);
	}

	@Override
	public String getName() {
		return "container.turretRailgun";
	}

	@Override public AudioWrapper createAudioLoop() {
		return MainRegistry.proxy.getLoopedSound("hbm:alarm.containerAlarm", xCoord, yCoord, zCoord, 1F, 15F, 1.0F, 20);
	}

	@Override
	public void updateEntity() {
		if (!worldObj.isRemote) {
			this.power = Library.chargeTEFromItems(slots, 0, this.power, this.getMaxPower());

			if (this.active && this.power > 0) {
				if (targetVector != null)
					targetDirectionVector = Vec3.createVectorHelper(
						targetVector.xCoord - xCoord,
						targetVector.yCoord - yCoord,
						targetVector.zCoord - zCoord
					).normalize();
			}

			if (this.charging && this.active) {
				long delta = Math.min(consumption, this.power);
				if (this.charge < maxCharge && delta > 0L) {
					this.power -= delta;
					this.charge += delta;
				}

				if (!this.chargeReadyAudio && ((maxCharge - this.charge) / consumption <= 120)) {
					this.chargeReadyAudio = true;
					worldObj.playSoundEffect(xCoord, yCoord, zCoord, NTMSounds.NUKE_CHARGE, 100.0F, 1.0F);
				}
			} else {
				this.chargeReadyAudio = false;
			}

			if (this.charge >= maxCharge) {
				this.spawnBullet(XFactoryFolly.folly_sm, 1000.0f);
				worldObj.playSoundEffect(xCoord, yCoord, zCoord, NTMSounds.GUN_PLEASE_REMOVE_MY_EARDRUMS_THANKS, 100.0F, 1.0F);
				this.charge = 0L;
				this.charging = false;
			}

			this.networkPackNT(250);
		} else {
			if (this.charging && MainRegistry.proxy.me().getDistance(xCoord, yCoord, zCoord) < 50) {
				if(audio == null) {
					audio = createAudioLoop();
					audio.startSound();
				} else if(!audio.isPlaying()) {
					audio = rebootAudio(audio);
				}
				audio.keepAlive();
				audio.updateVolume(this.getVolume(1F));
			} else {
				if(audio != null) {
					audio.stopSound();
					audio = null;
				}
			}
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);

		buf.writeBoolean(this.active);
		buf.writeLong(this.power);
		buf.writeLong(this.charge);
		BufferUtil.writeVec3(buf, this.targetVector);
		BufferUtil.writeVec3(buf, this.targetDirectionVector);
		buf.writeBoolean(this.charging);
		buf.writeBoolean(this.aligning);
		buf.writeFloat(this.rotationPitch);
		buf.writeFloat(this.rotationYaw);

		buf.writeBoolean(this.chargeReadyAudio);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);

		this.active = buf.readBoolean();
		this.power = buf.readLong();
		this.charge = buf.readLong();
		this.targetVector = BufferUtil.readVec3(buf);
		this.targetDirectionVector = BufferUtil.readVec3(buf);
		this.charging = buf.readBoolean();
		this.aligning = buf.readBoolean();
		this.rotationPitch = buf.readFloat();
		this.rotationYaw = buf.readFloat();

		this.chargeReadyAudio = buf.readBoolean();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		this.active = nbt.getBoolean("active");
		this.power = nbt.getLong("power");
		this.charge = nbt.getLong("charge");
		this.charging = nbt.getBoolean("charging");
		this.aligning = nbt.getBoolean("aligning");
		this.targetVector = Vec3.createVectorHelper(
			nbt.getDouble("targetX"),
			nbt.getDouble("targetY"),
			nbt.getDouble("targetZ")
		);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setBoolean("active", this.active);
		nbt.setLong("power", this.power);
		nbt.setLong("charge", this.charge);
		nbt.setBoolean("charging", this.charging);
		nbt.setBoolean("aligning", this.aligning);
		if (this.targetVector != null) {
			nbt.setDouble("targetX", this.targetVector.xCoord);
			nbt.setDouble("targetY", this.targetVector.yCoord);
			nbt.setDouble("targetZ", this.targetVector.zCoord);
		}
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerTurretRailgun(player.inventory, this);
	}

	@Override
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUITurretRailgun(player.inventory, this);
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
		return this.maxPower;
	}

	@Override
	public boolean hasPermission(EntityPlayer player) {
		return this.isUseableByPlayer(player);
	}

	@Override
	public void receiveControl(NBTTagCompound data) {
		if (data.hasKey("activeChange")) active = data.getBoolean("activeChange");
		if (data.hasKey("dataInput")) {
			String dataInput = data.getString("dataInput");
			String[] params = dataInput.split("!");
			if (params.length == 3) {
				try {
					targetVector = Vec3.createVectorHelper(
						Integer.parseInt(params[0]),
						Integer.parseInt(params[1]),
						Integer.parseInt(params[2])
					);
				} catch (NumberFormatException e) {}
			} else if (dataInput.equals("fire") && targetVector != null) {
				charging = true;
			}
		}
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack) {
		if (i == 0) return true; // battery
		if (i >= 1 && i <= 5) return true; // ammo slots
		return false;
	}

	public void spawnBullet(BulletConfig bullet, float baseDamage) {

		if (targetDirectionVector == null) return;

		EntityBulletBeamBase proj = new EntityBulletBeamBase(worldObj, bullet, baseDamage);
		proj.posX = xCoord + targetDirectionVector.xCoord * barrelLength;
		proj.posY = yCoord + targetDirectionVector.yCoord * barrelLength;
		proj.posZ = zCoord + targetDirectionVector.zCoord * barrelLength;
		proj.setRotationsFromVector(targetDirectionVector);
		proj.beamLength = 300.0D; // hardcoded because uhhh, idfk

		worldObj.spawnEntityInWorld(proj);
	}
}
