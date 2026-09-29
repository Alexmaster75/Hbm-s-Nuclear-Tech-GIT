package com.hbm.render.block;

import com.hbm.blocks.BlockBudBase;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

public class RenderBud implements ISimpleBlockRenderingHandler {
	@Override
	public void renderInventoryBlock(Block block, int metaData, int modelId, RenderBlocks renderBlocks) {}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelID, RenderBlocks renderer) {
		int meta = world.getBlockMetadata(x, y, z);
		return renderBud(world, x, y, z, block, meta, renderer);
	}

	private boolean renderBud(IBlockAccess world, int x, int y, int z, Block block, int meta, RenderBlocks renderer) {
		Tessellator tess = Tessellator.instance;
		tess.setBrightness(block.getMixedBrightnessForBlock(world, x, y, z));
		tess.setColorOpaque_F(1, 1, 1);

		IIcon icon = renderer.hasOverrideBlockTexture() ? renderer.overrideBlockTexture : block.getIcon(0, meta);
		ForgeDirection dir = BlockBudBase.getDir(meta);

		double a = 0.05, b = 0.95;
		quad(tess, x, y, z, dir, a, a, b, b, icon);
		quad(tess, x, y, z, dir, a, b, b, a, icon);
		return true;
	}

	private void quad(Tessellator tess, int x, int y, int z, ForgeDirection dir, double h1, double d1, double h2, double d2, IIcon icon) {
		double minU = icon.getMinU(), maxU = icon.getMaxU(), minV = icon.getMinV(), maxV = icon.getMaxV();
		vertex(tess, x, y, z, dir, h1, 1, d1, minU, minV);
		vertex(tess, x, y, z, dir, h1, 0, d1, minU, maxV);
		vertex(tess, x, y, z, dir, h2, 0, d2, maxU, maxV);
		vertex(tess, x, y, z, dir, h2, 1, d2, maxU, minV);

		vertex(tess, x, y, z, dir, h2, 1, d2, maxU, minV);
		vertex(tess, x, y, z, dir, h2, 0, d2, maxU, maxV);
		vertex(tess, x, y, z, dir, h1, 0, d1, minU, maxV);
		vertex(tess, x, y, z, dir, h1, 1, d1, minU, minV);
	}

	private void vertex(Tessellator tess, int x, int y, int z, ForgeDirection dir, double lx, double up, double lz, double u, double v) {
		double px, py, pz;
		switch (dir) {
			case DOWN:
				px = lx;
				py = 1 - up;
				pz = lz;
				break;
			case NORTH:
				px = lx;
				py = lz;
				pz = 1 - up;
				break;
			case SOUTH:
				px = lx;
				py = lz;
				pz = up;
				break;
			case WEST:
				px = 1 - up;
				py = lz;
				pz = lx;
				break;
			case EAST:
				px = up;
				py = lz;
				pz = lx;
				break;
			default:
				px = lx;
				py = up;
				pz = lz;
				break;
		}
		tess.addVertexWithUV(x + px, y + py, z + pz, u, v);
	}

	@Override
	public boolean shouldRender3DInInventory(int i) {
		return false;
	}

	@Override
	public int getRenderId() {
		return BlockBudBase.getRenderID();
	}
}
