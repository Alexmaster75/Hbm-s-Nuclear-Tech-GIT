package com.hbm.blocks.machine;

import api.hbm.block.IToolable;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.items.ModItems;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachineTurboCompressor;
import com.hbm.util.BobMathUtil;
import com.hbm.util.i18n.I18nUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.List;

public class MachineTurboCompressor extends BlockDummyable implements ILookOverlay, ITooltipProvider, IToolable {
	public MachineTurboCompressor() {
		super(Material.iron);
	}

	@Override
	public int[] getDimensions() {
		return new int[] {2, 0, 1, 0, 1, 1};
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

			if (pos == null)
				return false;

			TileEntity te = world.getTileEntity(pos[0], pos[1], pos[2]);

			if (!(te instanceof TileEntityMachineTurboCompressor))
				return false;

			TileEntityMachineTurboCompressor turboCompressor = (TileEntityMachineTurboCompressor) te;

			if (!turboCompressor.hasBypass && player.getHeldItem() != null && player.getHeldItem().getItem() == ModItems.turbocompressor_bypass) {
				player.getHeldItem().stackSize--;
				turboCompressor.hasBypass = true;
				turboCompressor.markDirty();
				world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, "hbm:block.pipePlaced", 1.5F, 0.75F);
				return true;
			}
		}

		return false;
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block b, int i) {

		TileEntity te = world.getTileEntity(x, y, z);

		if (te instanceof TileEntityMachineTurboCompressor) {
			TileEntityMachineTurboCompressor turboCompressor = (TileEntityMachineTurboCompressor) te;

			if (turboCompressor.hasBypass) {
				EntityItem item = new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, new ItemStack(ModItems.turbocompressor_bypass));
				world.spawnEntityInWorld(item);
			}
		}

		super.breakBlock(world, x, y, z, b, i);
	}

	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, int side, float fX, float fY, float fZ, ToolType tool) {

		if (tool != ToolType.SCREWDRIVER && tool != ToolType.HAND_DRILL) return false;

		int[] pos = this.findCore(world, x, y, z);

		if (pos == null)
			return false;

		TileEntity te = world.getTileEntity(pos[0], pos[1], pos[2]);

		if (!(te instanceof TileEntityMachineTurboCompressor))
			return false;

		TileEntityMachineTurboCompressor turboCompressor = (TileEntityMachineTurboCompressor) te;

		if (tool == ToolType.SCREWDRIVER) {
			if (turboCompressor.slots[0] == null) return false;

			if (!player.inventory.addItemStackToInventory(turboCompressor.slots[0].copy())) {
				EntityItem item = new EntityItem(world, player.posX, player.posY, player.posZ, turboCompressor.slots[0].copy());
				world.spawnEntityInWorld(item);
			} else {
				player.inventoryContainer.detectAndSendChanges();
			}

			turboCompressor.markDirty();
			world.markBlockForUpdate(x, y, z);

			turboCompressor.slots[0] = null;
			turboCompressor.markDirty();
			return true;
		} else if (turboCompressor.hasBypass) {
			if (!player.inventory.addItemStackToInventory(new ItemStack(ModItems.turbocompressor_bypass))) {
				EntityItem item = new EntityItem(world, player.posX, player.posY, player.posZ, new ItemStack(ModItems.turbocompressor_bypass));
				world.spawnEntityInWorld(item);
			} else {
				player.inventoryContainer.detectAndSendChanges();
			}

			turboCompressor.hasBypass = false;
			turboCompressor.markDirty();

			world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, "hbm:item.upgradePlug", 1.5F, 0.75F);
			return true;
		}

		return false;
	}

	@Override
	public void printHook(RenderGameOverlayEvent.Pre event, World world, int x, int y, int z) {
		int[] pos = this.findCore(world, x, y, z);

		if (pos == null)
			return;

		TileEntity te = world.getTileEntity(pos[0], pos[1], pos[2]);

		if (!(te instanceof TileEntityMachineTurboCompressor))
			return;

		TileEntityMachineTurboCompressor turboCompressor = (TileEntityMachineTurboCompressor) te;

		List<String> text = new ArrayList();

		text.add(turboCompressor.rpm * 100 + " RPM");

		if (turboCompressor.hasBypass) {
			text.add(EnumChatFormatting.GREEN + "-> " + EnumChatFormatting.RESET + "Bypass installed!");
		}

		text.add(EnumChatFormatting.RED + "<- " + EnumChatFormatting.RESET + turboCompressor.compair.getTankType().getLocalizedName() + ": " + turboCompressor.compair.getFill() + "/" + turboCompressor.compair.getMaxFill() + "mB");

		if (turboCompressor.slots[0] != null) {
			text.add(EnumChatFormatting.RED + "<- " + EnumChatFormatting.RESET + turboCompressor.slots[0].getDisplayName() + (turboCompressor.slots[0].stackSize > 1 ? " x" + turboCompressor.slots[0].stackSize : ""));
		}

		if (turboCompressor.rpm > turboCompressor.maxRpm) {
			text.add("&[" + (BobMathUtil.getBlink() ? 0xff0000 : 0xffff00) + "&]! ! ! OVERSPEED ! ! !");
		}

		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getUnlocalizedName() + ".name"), 0xffff00, 0x404000, text);
	}

	@Override
	public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		super.fillSpace(world, x, y, z, dir, o);
		this.makeExtra(world, x + dir.offsetX * o + 1, y, z + dir.offsetZ * o);
		this.makeExtra(world, x + dir.offsetX * o - 1, y, z + dir.offsetZ * o);
		this.makeExtra(world, x + dir.offsetX * o, y, z + dir.offsetZ * o + 1);
		this.makeExtra(world, x + dir.offsetX * o, y, z + dir.offsetZ * o - 1);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		this.addStandardInfo(stack, player, list, ext);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {

		if(meta >= 12) return new TileEntityMachineTurboCompressor();
		if(meta >= 6) return new TileEntityProxyCombo().fluid();
		return null;
	}
}
