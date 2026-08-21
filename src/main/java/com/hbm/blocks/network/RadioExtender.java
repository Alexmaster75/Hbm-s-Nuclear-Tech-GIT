package com.hbm.blocks.network;

import com.hbm.blocks.ILookOverlay;
import com.hbm.tileentity.network.TileEntityRadioExtender;
import com.hbm.util.i18n.I18nUtil;
import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import java.util.ArrayList;
import java.util.List;

public class RadioExtender extends BlockContainer implements ILookOverlay {

	public RadioExtender() {
		super(Material.circuits);
	}

	public static int renderID = RenderingRegistry.getNextAvailableRenderId();

	@Override
	public int getRenderType() {
		return renderID;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public void printHook(RenderGameOverlayEvent.Pre event, World world, int x, int y, int z) {
		TileEntity te = world.getTileEntity(x, y, z);

		if (te instanceof TileEntityRadioExtender) {
			TileEntityRadioExtender radio_extender = (TileEntityRadioExtender) te;
			List<String> text = new ArrayList();

			if (radio_extender.valid_p || radio_extender.valid_i) {
				text.add("> " + EnumChatFormatting.GREEN + "VALID");
			} else {
				text.add("> " + EnumChatFormatting.RED + "ERROR");
			}

			ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getUnlocalizedName() + ".name"), 0xffff00, 0x404000, text);
		}
	}

	@Override
	public int onBlockPlaced(World world, int x, int y, int z, int side, float fX, float fY, float fZ, int meta) {
		return side;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int i) {
		return new TileEntityRadioExtender();
	}
}
