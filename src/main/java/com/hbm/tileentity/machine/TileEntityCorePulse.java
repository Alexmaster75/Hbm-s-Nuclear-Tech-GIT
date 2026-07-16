package com.hbm.tileentity.machine;

import api.hbm.block.ILaserable;
import api.hbm.redstoneoverradio.IRORInteractive;

import com.hbm.entity.effect.EntityCloudFleija;
import com.hbm.entity.logic.EntityNukeExplosionMK3;
import com.hbm.explosion.ExplosionNT;
import com.hbm.handler.CompatHandler;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerCorePulse;
import com.hbm.inventory.gui.GUICorePulse;
import com.hbm.lib.ModDamageSource;
import com.hbm.main.NTMSounds;
import com.hbm.particle.helper.ExplosionCreator;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;

import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers")})
public class TileEntityCorePulse extends TileEntityMachineBase implements ILaserable, SimpleComponent, IGUIProvider, CompatHandler.OCComponent, IRORInteractive, IControlReceiver {

	public long timer;
	public long maxTimer;
	public int beam;
	public int powIndex; // changes the keypad's input order of magnitude
	public long joules; // input (Spk/t)
	public long stored; // stored (Spk)
	public long target; // output (Spk)
	public long jammerTime = 125; // last ticks before starting the irreversible procedure
	public final long fakePulseDelta = 20; // amount of ticks between the fake pulse and jammers blowing up (cinematic effect)
	public long timerOld; // used in combination with fakePulseDelta
	public boolean fakePulse; // flag for fake pulse
	public long beepTime; // uhh, time for beep delay or smth
	public boolean jammerBoom; // flag for jammers kaboom
	public boolean commit; // flag for keypad commit (letter E)

	public static final int range = 50; // range of beam

	public TileEntityCorePulse() {
		super(0);
		commit = false;
		jammerBoom = false;
		fakePulse = false;
		timerOld = 0;
		timer = 0;
	}

	@Override
	public String getName() {
		return "container.dfcPulse";
	}

	@Override
	public void updateEntity() {

		if (!worldObj.isRemote) {
			target = stored + joules * timer;

			if (commit && timer > 0) {
				// this shit better not getting an overflow since people are batshit crazy with setups
				stored = Long.MAX_VALUE - stored < joules ? Long.MAX_VALUE : stored+joules;

				beepTime = (long) Math.max(Math.min((double) timer / jammerTime * 20, 20L), 1L);
				if (timer % beepTime == 0)
					worldObj.playSoundEffect(xCoord, yCoord, zCoord, NTMSounds.TECH_BOOP, 2.0F, 0.9F + 2.0F / beepTime);

				if (timer <= jammerTime && !fakePulse) {
					worldObj.playSoundEffect(xCoord, yCoord, zCoord, NTMSounds.NUKE_CHARGE, 5.0F, 1.0F);
					// jammers? heck no, they're getting reduced to atoms lmao
					EntityNukeExplosionMK3 ex = EntityNukeExplosionMK3.statFacFleija(worldObj, xCoord, yCoord, zCoord, 0);
					if(!ex.isDead) {
						worldObj.playSoundEffect(xCoord, yCoord, zCoord, "random.explode", 100.0F, worldObj.rand.nextFloat() * 0.1F + 0.9F);
						worldObj.spawnEntityInWorld(ex);
						EntityCloudFleija cloud = new EntityCloudFleija(worldObj, 20);
						cloud.setPosition(xCoord, yCoord, zCoord);
						worldObj.spawnEntityInWorld(cloud);
					}
					fakePulse = true;
					jammerBoom = true;
					timerOld = timer;
				}

				if (jammerBoom && timerOld - timer > fakePulseDelta) {
					jammerBoom = false;
					ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata());
					for (int i = 1; i <= range; i++) {
						int x = xCoord + dir.offsetX * i;
						int y = yCoord + dir.offsetY * i;
						int z = zCoord + dir.offsetZ * i;

						Block block = worldObj.getBlock(x, y, z);
						TileEntity te = worldObj.getTileEntity(x, y, z);

						if(block instanceof ILaserable) { ((ILaserable)block).addEnergy(worldObj, x, y, z, 0, dir); break; }
						if(te instanceof ILaserable) { ((ILaserable)te).addEnergy(worldObj, x, y, z, 0, dir); break; }
						if(te instanceof TileEntityCore) {
							TileEntityCore core = (TileEntityCore) te;
							Iterator<Map.Entry<EntityNukeExplosionMK3.ATEntry, Long>> it = EntityNukeExplosionMK3.at.entrySet().iterator();
							while(it.hasNext()) {
								Map.Entry<EntityNukeExplosionMK3.ATEntry, Long> next = it.next();
								if(next.getValue() < worldObj.getTotalWorldTime()) {
									it.remove();
									continue;
								}
								EntityNukeExplosionMK3.ATEntry entry = next.getKey();
								if(entry.dim != worldObj.provider.dimensionId)  continue;
								Vec3 vec = Vec3.createVectorHelper(core.xCoord + 0.5 - entry.x, core.yCoord + 0.5 - entry.y, core.zCoord + 0.5 - entry.z);
								if(vec.lengthVector() < 300) {
									new ExplosionNT(worldObj, null, entry.x + 0.5, entry.y + 0.5, entry.z + 0.5, 5).overrideResolution(64).explode();
									ExplosionCreator.composeEffectStandard(worldObj, entry.x + 0.5, entry.y + 1, entry.z + 0.5);
									worldObj.setBlockToAir(entry.x, entry.y, entry.z);
								}
							}
						}
					}
				}

				timer--;
			}
			if (commit && timer <= 0) {
				beam = 0;
				ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata());
				for(int i = 1; i <= range; i++) {

					beam = i;

					int x = xCoord + dir.offsetX * i;
					int y = yCoord + dir.offsetY * i;
					int z = zCoord + dir.offsetZ * i;

					Block block = worldObj.getBlock(x, y, z);
					TileEntity te = worldObj.getTileEntity(x, y, z);

					if(block instanceof ILaserable) { ((ILaserable)block).addEnergy(worldObj, x, y, z, stored, dir); break; }
					if(te instanceof ILaserable) { ((ILaserable)te).addEnergy(worldObj, x, y, z, stored, dir); break; }
					// all this is just so the dfc can explode with maximum destruction regardless of its input energy
					if(te instanceof TileEntityCore) {
						TileEntityCore core = (TileEntityCore) te;
						int lastHeat = core.heat;
						// 0.5 multiplier because otherwise tanks would be set to 0 and there would be no fuel left for the explosion ¯\_(ツ)_/¯
						int lastFuel = (int) (0.5 * Math.min(core.tanks[0].getFill(), core.tanks[1].getFill()));
						long out = lastFuel * 1000L - lastHeat * 10000L;
						out = ((TileEntityCore)te).burn(Math.min(out, stored));
						continue;
					}

					Block b = worldObj.getBlock(x, y, z);

					if(!b.isAir(worldObj, x, y, z)) {

						if(b.getMaterial().isLiquid()) {
							worldObj.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, "random.fizz", 1.0F, 1.0F);
							worldObj.setBlockToAir(x, y, z);
							break;
						}

						float hardness = b.getExplosionResistance(null);
						if(hardness < 6000 && worldObj.rand.nextInt(20) == 0) {
							worldObj.func_147480_a(x, y, z, false);
						}

						break;
					}
				}

				double blx = Math.min(xCoord, xCoord + dir.offsetX * beam) + 0.2;
				double bux = Math.max(xCoord, xCoord + dir.offsetX * beam) + 0.8;
				double bly = Math.min(yCoord, yCoord + dir.offsetY * beam) + 0.2;
				double buy = Math.max(yCoord, yCoord + dir.offsetY * beam) + 0.8;
				double blz = Math.min(zCoord, zCoord + dir.offsetZ * beam) + 0.2;
				double buz = Math.max(zCoord, zCoord + dir.offsetZ * beam) + 0.8;

				List<Entity> list = worldObj.getEntitiesWithinAABB(Entity.class, AxisAlignedBB.getBoundingBox(blx, bly, blz, bux, buy, buz));

				for(Entity e : list) {
					e.attackEntityFrom(ModDamageSource.amsCore, 50);
					e.setFire(10);
				}

				commit = false;
				fakePulse = false;
				timer = 0L;
				stored = 0L;
			}

			this.markDirty();

			this.networkPackNT(250);
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);

		buf.writeLong(timer);
		buf.writeLong(maxTimer);
		buf.writeLong(jammerTime);
		buf.writeLong(joules);
		buf.writeLong(stored);
		buf.writeLong(target);
		buf.writeBoolean(commit);
		buf.writeBoolean(jammerBoom);
		buf.writeInt(beam);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);

		this.timer = buf.readLong();
		this.maxTimer = buf.readLong();
		this.jammerTime = buf.readLong();
		this.joules = buf.readLong();
		this.stored = buf.readLong();
		this.target = buf.readLong();
		this.commit = buf.readBoolean();
		this.jammerBoom = buf.readBoolean();
		this.beam = buf.readInt();
	}

	@Override
	public void addEnergy(World world, int x, int y, int z, long energy, ForgeDirection dir) {
		joules = energy;
	}

	public String getMinutes() {

		String mins = "" + (timer / 1200);

		if(mins.length() == 1)
			mins = "0" + mins;

		return mins;
	}

	public String getSeconds() {

		String mins = "" + ((timer / 20) % 60);

		if(mins.length() == 1)
			mins = "0" + mins;

		return mins;
	}

	@Override
	public boolean hasPermission(EntityPlayer player) {
		return this.isUseableByPlayer(player);
	}

	@Override
	public void receiveControl(NBTTagCompound data) {
		if (data.hasKey("keypad") && !commit) {
			long input = data.getLong("keypad");
			if (input >= 0L && input <= 180L && powIndex <= 2) {
				// keypad numbers
				timer = timer * 10L + input;
				powIndex++;
			} else if (input == -20L) {
				// cancel
				timer = 0L;
				powIndex = 0;
			} else if (input == -40L) {
				// confirm
				if (joules > 0) {
					timer = Math.max(timer, jammerTime);
					maxTimer = timer;
					commit = true;
				} else {
					worldObj.playSoundEffect(xCoord, yCoord, zCoord, NTMSounds.BUTTON_INCORRECT, 1.0F, 1.0F);
				}
			}
		}
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return TileEntity.INFINITE_EXTENT_AABB;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		return 65536.0D;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		timer = nbt.getLong("timer");
		maxTimer = nbt.getLong("maxTimer");
		commit = nbt.getBoolean("commit");
		joules = nbt.getLong("joules");
		stored = nbt.getLong("stored");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setLong("timer", timer);
		nbt.setLong("maxTimer", maxTimer);
		nbt.setBoolean("commit", commit);
		nbt.setLong("joules", joules);
		nbt.setLong("stored", stored);
	}

	// do some opencomputer stuff
	@Override
	@Optional.Method(modid = "OpenComputers")
	public String getComponentName() {
		return "dfc_pulse";
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerCorePulse(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUICorePulse(player.inventory, this);
	}

	@Override
	public String[] getFunctionInfo() {
		return new String[] {
			PREFIX_FUNCTION + "setpower" + NAME_SEPARATOR + "percent",
			PREFIX_FUNCTION + "toggle",
			PREFIX_FUNCTION + "switch" + NAME_SEPARATOR + "on/off",
		};
	}

	@Override
	public String runRORFunction(String name, String[] params) {

		return null;
	}
}
