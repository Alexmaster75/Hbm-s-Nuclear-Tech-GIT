package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;

public class RenderGrowthChamber extends TileEntitySpecialRenderer implements IItemRendererProvider {
	@Override
	public Item getItemForRenderer() {
		return Item.getItemFromBlock(ModBlocks.machine_growth_chamber);
	}

	@Override
	public IItemRenderer getRenderer() {
		return new ItemRenderBase() {
			@Override
			public void renderInventory() {
				GL11.glTranslated(0, -2, 0);
				GL11.glScalef(3, 3, 3);
			}

			@Override
			public void renderCommon() {
				bindTexture(ResourceManager.growth_chamber_tex);
				ResourceManager.growth_chamber.renderAll();
			}
		};
	}

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float interp) {
		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y, z + 0.5);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_CULL_FACE);

		switch (tileEntity.getBlockMetadata() - BlockDummyable.offset) {
			case 2: GL11.glRotatef(90,0,1,0); break;
			case 3: GL11.glRotatef(270,0,1,0); break;
			case 4: GL11.glRotatef(180,0,1,0); break;
			case 5: GL11.glRotatef(0,0,1,0); break;
		}

		bindTexture(ResourceManager.growth_chamber_tex);
		ResourceManager.growth_chamber.renderAll();
//		ResourceManager.growth_chamber.renderPart("Body");
//
//		TileEntityMachineGrowthChamber chamber = (TileEntityMachineGrowthChamber) tileEntity;
//		if (chamber.module.isRunning()) {
//			float grow = (float) chamber.module.ticks / chamber.module.cycleDuration;
//			GL11.glPushMatrix();
//			GL11.glTranslated(0, CRYSTAL_BASE_Y, 0);
//			GL11.glScalef(grow, grow, grow);
//			GL11.glTranslated(0, -CRYSTAL_BASE_Y, 0);
//			ResourceManager.growth_chamber.renderPart("Crystals");
//			GL11.glPopMatrix();
//		}

		GL11.glPopMatrix();
	}


}
