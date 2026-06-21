package com.hbm.tileentity.machine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerMoxer;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.gui.GUIMoxer;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.Mats.MoxerStack;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.tileentity.IConfigurableMachine;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.IMetalCopiable;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BobMathUtil;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public class TileEntityMoxer extends TileEntityMachineBase implements IGUIProvider, IConfigurableMachine, IMetalCopiable, IControlReceiver {

	public List<MoxerStack> inpStack = new ArrayList();

	/* CONFIGURABLE CONSTANTS */
	//because eclipse's auto complete is dumb as a fucking rock, it's now called "ZCapacity" so it's listed AFTER the actual stacks in the auto complete list.
	//also martin i know you read these: no i will not switch to intellij after using eclipse for 8 years.
	public static int inpZCapacity = MaterialShapes.BLOCK.q(16);

	public FluidTank[] tanks;

	@Override
	public String getConfigName() {
		return "moxer";
	}

	@Override
	public void readIfPresent(JsonObject obj) {
		inpZCapacity = IConfigurableMachine.grab(obj, "I:inpCapacity", inpZCapacity);
	}

	@Override
	public void writeConfig(JsonWriter writer) throws IOException {
		writer.name("I:inpCapacity").value(inpZCapacity);
	}

	public TileEntityMoxer() {
		super(2);
		this.tanks = new FluidTank[2];
		this.tanks[0] = new FluidTank(Fluids.KEROSENE, 16_000);
		this.tanks[1] = new FluidTank(Fluids.OXYGEN, 16_000);
	}

	@Override
	public String getName() {
		return "container.machineMoxer";
	}

	@Override
	public int getInventoryStackLimit() {
		return 1; //prevents clogging
	}

	@Override
	public void updateEntity() {

		if(!worldObj.isRemote) {

			int totalCap = inpZCapacity;
			int totalMass = 0;

			for(MoxerStack stack : inpStack) totalMass += stack.amount;

			double level = ((double) totalMass / (double) totalCap) * 0.875D;

			/* smelt items from buffer */
			trySmelt();

			/* clean up stacks */
			this.inpStack.removeIf(x -> x.amount <= 0);

			/* sync */
			this.networkPackNT(25);
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);

		buf.writeShort(inpStack.size());
		for(MoxerStack sta : inpStack) {
			if (sta.material == null)
				buf.writeInt(-1);
			else
				buf.writeInt(sta.material.id);
			buf.writeInt(sta.amount);
			buf.writeInt(sta.percentage);
		}
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);

		inpStack.clear();

		int mats = buf.readShort();
		for(int i = 0; i < mats; i++) {
			int id = buf.readInt();
			if (id == -1)
				continue;
			inpStack.add(new MoxerStack(Mats.matById.get(id), buf.readInt(), buf.readInt()));
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		int[] inp = nbt.getIntArray("inp");
		for(int i = 0; i < inp.length / 3; i++) {
			NTMMaterial mat = Mats.matById.get(inp[i * 3]);
			if(mat == null) continue;
			inpStack.add(new MoxerStack(mat, inp[i * 3 + 1], inp[i * 3 + 2]));
		}
		for(int i = 0; i < tanks.length; i++) tanks[i].readFromNBT(nbt, "t" + i);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		int[] inp = new int[inpStack.size() * 3];
		for(int i = 0; i < inpStack.size(); i++) {
			MoxerStack sta = inpStack.get(i);
			inp[i * 3] = sta.material.id;
			inp[i * 3 + 1] = sta.amount;
			inp[i * 3 + 2] =  sta.percentage;
		}
		nbt.setIntArray("inp", inp);
		for(int i = 0; i < tanks.length; i++) tanks[i].writeToNBT(nbt, "t" + i);
	}

	protected boolean trySmelt() {

		int slot = this.getFirstSmeltableSlot();
		if(slot == -1) return false;

		if(true) {

			List<MoxerStack> materials = Mats.getMoxerSmeltingMaterialsFromItem(slots[slot]);

			for(MoxerStack material : materials) {
				this.addToStack(this.inpStack, material);
			}

			this.decrStackSize(slot, 1);
		}

		return true;
	}

	protected int getFirstSmeltableSlot() {

		for(int i = 0; i < 1; i++) {

			ItemStack stack = slots[i];

			if(stack != null && isItemSmeltable(stack)) {
				return i;
			}
		}

		return -1;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack) {
		return isItemSmeltable(stack);
	}

	public boolean isItemSmeltable(ItemStack stack) {

		List<MoxerStack> materials = Mats.getMoxerSmeltingMaterialsFromItem(stack);

		//if there's no materials in there at all, don't smelt
		if(materials.isEmpty()) return false;

		//the total amount of the current waste stack, used for simulation
		int inpAmount = getQuantaFromType(this.inpStack, null);

		for(MoxerStack mat : materials) {
			//if this type isn't required by the recipe, add it to the waste stack
			inpAmount += mat.amount;
		}

		//if the amount doesn't exceed the capacity and the recipe matches (or isn't null), return true
		return inpAmount <= this.inpZCapacity;
	}

	public void addToStack(List<MoxerStack> stack, MoxerStack matStack) {

		for(MoxerStack mat : stack) {
			if(mat.material == matStack.material) {
				mat.amount += matStack.amount;
				return;
			}
		}

		stack.add(matStack.copy());
	}

	public int getQuantaFromType(List<MoxerStack> stacks, NTMMaterial mat) {
		int sum = 0;
		for(MoxerStack stack : stacks) {
			if(stack.material == mat) {
				return stack.amount;
			}
			if(mat == null) {
				sum += stack.amount;
			}
		}
		return sum;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int meta) {
		return new int[] { 0, 1 };
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerMoxer(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIMoxer(player.inventory, this);
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = AxisAlignedBB.getBoundingBox(
					xCoord - 1,
					yCoord,
					zCoord - 1,
					xCoord + 2,
					yCoord + 2,
					zCoord + 2
					);
		}

		return bb;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	@Override
	public int[] getMatsToCopy() {
		ArrayList<Integer> types = new ArrayList<>();

		for (MoxerStack stack : inpStack) {
			types.add(stack.material.id);
		}
		return BobMathUtil.intCollectionToArray(types);
	}

	@Override
	public boolean hasPermission(EntityPlayer player) {
		return this.isUseableByPlayer(player);
	}

	@Override
	public void receiveControl(NBTTagCompound data) {
		if(data.hasKey("percentage") && !inpStack.isEmpty()) {
			int setting = data.getInteger("percentage");
			// example setting = 1050 -> from the right, first 3 digits represent the percentage, everything after is the index
			int index = (int) Math.floor((double) setting / 1000);
			int percentage = setting - index * 1000;
			MoxerStack ref = inpStack.get(index);
			int tot_perc = 0;
			for (MoxerStack mat : inpStack)
				if (mat != ref)
					tot_perc += mat.percentage;
			if (tot_perc + percentage > 100)
				percentage = 100 - tot_perc;
			inpStack.get(index).percentage = percentage;
		}
	}

	public List<String> getStringMaterials() {

		if(!inpStack.isEmpty()) {
			List<String> array = new ArrayList();

			for (MoxerStack mat : inpStack) {
                array.add(mat.material.getUnlocalizedName());
            }

			if(array.isEmpty())
				return null;

			return array;
		}

		return null;
	}
}
