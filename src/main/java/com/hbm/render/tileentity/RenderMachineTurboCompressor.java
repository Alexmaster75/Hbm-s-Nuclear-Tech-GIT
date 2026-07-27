package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineTurboCompressor;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;

public class RenderMachineTurboCompressor extends TileEntitySpecialRenderer implements IItemRendererProvider {
	@Override
	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float interp) {
		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5D, y, z + 0.5D);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_CULL_FACE);

		switch(tile.getBlockMetadata() - BlockDummyable.offset) {
			case 3:
				GL11.glTranslated(0, 0, -0.5);
				GL11.glRotatef(-90, 0F, 1F, 0F);
				break;
			case 5:
				GL11.glTranslated(-0.5, 0, 0);
				GL11.glRotatef(0, 0F, 1F, 0F);
				break;
			case 2:
				GL11.glTranslated(0, 0, 0.5);
				GL11.glRotatef(90, 0F, 1F, 0F);
				break;
			case 4:
				GL11.glTranslated(0.5, 0, 0);
				GL11.glRotatef(180, 0F, 1F, 0F);
				break;
		}

		TileEntityMachineTurboCompressor turboCompressor = (TileEntityMachineTurboCompressor) tile;

		float rot = turboCompressor.lastSpin + (turboCompressor.spin - turboCompressor.lastSpin) * interp;

		bindTexture(ResourceManager.turbocompressor_tex);
		ResourceManager.turbocompressor.renderPart("Base");

		GL11.glPushMatrix();
		GL11.glTranslated(0, 2, 0);
		GL11.glRotatef(-rot * 2, 0, 0, 1);
		GL11.glTranslated(0, -2, 0);
		ResourceManager.turbocompressor.renderPart("Fan");
		GL11.glPopMatrix();

		GL11.glPopMatrix();
	}

	@Override
	public Item getItemForRenderer() {
		return Item.getItemFromBlock(ModBlocks.machine_turbocompressor);
	}

	@Override
	public IItemRenderer getRenderer() {
		return new ItemRenderBase( ) {
			public void renderInventory() {
				GL11.glTranslated(0, -1.5, 0);
				GL11.glScaled(3, 3, 3);
			}
			public void renderCommonWithStack(ItemStack item) {
				GL11.glScaled(0.75, 0.75, 0.75);
				GL11.glShadeModel(GL11.GL_SMOOTH);
				bindTexture(ResourceManager.turbocompressor_tex);
				ResourceManager.turbocompressor.renderPart("Base");
				ResourceManager.turbocompressor.renderPart("Fan");
				GL11.glShadeModel(GL11.GL_FLAT);
			}};
	}
}
