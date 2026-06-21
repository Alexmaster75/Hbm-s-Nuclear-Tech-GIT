package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.interfaces.IMultiblock;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachineKrusty;
import com.hbm.util.BobMathUtil;
import com.hbm.util.i18n.I18nUtil;

import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.common.util.ForgeDirection;

public class MachineKrusty extends BlockDummyable implements IMultiblock, ILookOverlay {

	private final Random field_149933_a = new Random();
	private static boolean keepInventory;

	public MachineKrusty(Material p_i45386_1_) {
		super(p_i45386_1_);
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		if(p_149915_2_ >= 12)
			return new TileEntityMachineKrusty();
		if(hasExtra(p_149915_2_))
			return new TileEntityProxyCombo().power().fluid();
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {1, 0, 0, 0, 0, 0};
	}

	@Override
	public int getOffset() {
		return 0;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		if(world.isRemote) {
			return true;
		} else if(!player.isSneaking()) {
			int[] pos = this.findCore(world, x, y, z);

			if(pos == null)
				return false;
			TileEntityMachineKrusty entity = (TileEntityMachineKrusty) world.getTileEntity(pos[0], pos[1], pos[2]);
			if(entity != null) {
				FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos[0], pos[1], pos[2]);
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected boolean checkRequirement(World world, int x, int y, int z, ForgeDirection dir, int o) {
		return super.checkRequirement(world, x, y, z, dir, o) &&
				MultiblockHandlerXR.checkSpace(world, x + dir.offsetX * o, y + dir.offsetY * o, z + dir.offsetZ * o, new int[] {1, 0, 0, 0, 0, 0}, x, y, z, dir);
	}

	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {
		int[] pos = this.findCore(world, x, y, z);

		if(pos == null)
			return;
		TileEntity te = world.getTileEntity(pos[0], pos[1], pos[2]);
		if(!(te instanceof TileEntityMachineKrusty)) return;

		TileEntityMachineKrusty krusty = (TileEntityMachineKrusty) te;
		List<String> text = new ArrayList();

		int flux = (int) krusty.totalFlux;
		int heat = krusty.heat;
		int maxHeat = krusty.maxHeat;
		long power = (long) krusty.power;
		int maxPower = (int) TileEntityMachineKrusty.maxPower;
		int coolant = krusty.tanks[0].getFill();
		int maxCoolant = krusty.tanks[0].getMaxFill();
		int level = (int) (krusty.level * 100);
		int timer = krusty.timer;
		int maxTimer = krusty.maxTimer;

		text.add(EnumChatFormatting.YELLOW + "Flux: " + EnumChatFormatting.RESET + String.format(Locale.US, "%,d", flux));
		text.add(EnumChatFormatting.RED + "Heat: " + EnumChatFormatting.RESET + String.format(Locale.US, "%,.2f", heat * 0.00002 * 980 + 20) + "C");
		text.add(EnumChatFormatting.BLUE + "Coolant: " + EnumChatFormatting.RESET + String.format(Locale.US, "%,d", coolant) + "/" + String.format(Locale.US, "%,d", maxCoolant) + "mB");
		text.add(EnumChatFormatting.GREEN + "Rod Level: " + EnumChatFormatting.RESET + level + "%");
		text.add(EnumChatFormatting.GOLD + "Energy: " + EnumChatFormatting.RESET + BobMathUtil.getShortNumber(power) + "/" + BobMathUtil.getShortNumber(maxPower) + "HE");

		if (heat > maxHeat * 0.8)
			text.add("&[" + (BobMathUtil.getBlink() ? 0xff0000 : 0xffff00) + "&]! ! ! EXTREME HEAT ! ! !");

		if (heat > maxHeat)
			text.add(EnumChatFormatting.YELLOW + "MELTDOWN: " + EnumChatFormatting.RESET + (int) (100 * timer / (float) (maxTimer)) + "%");

		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getUnlocalizedName() + ".name"), 0xff0000, 0x400000, text);
	}
}
