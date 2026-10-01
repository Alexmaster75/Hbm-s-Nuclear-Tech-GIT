package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.main.MainRegistry;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.BeamPronter;
import com.hbm.tileentity.machine.TileEntityMachineGrowthChamber;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import org.lwjgl.opengl.GL11;

import java.util.Random;

public class RenderGrowthChamber extends TileEntitySpecialRenderer implements IItemRendererProvider {
	public static EntityItem dummy;
	private static final Random rand = new Random();

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
		int meta = tileEntity.getBlockMetadata();
		int pass = MinecraftForgeClient.getRenderPass();

		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y, z + 0.5);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_CULL_FACE);

		TileEntityMachineGrowthChamber chamber = (TileEntityMachineGrowthChamber) tileEntity;

		if (pass == 0) {
			GL11.glPushMatrix();
			rotate(meta);
			bindTexture(ResourceManager.growth_chamber_tex);
			ResourceManager.growth_chamber.renderPart("Base");
			GL11.glPopMatrix();

			GenericRecipe recipe = chamber.module.getRecipe();
			if (recipe != null && MainRegistry.proxy.me().getDistanceSq(tileEntity.xCoord + 0.5, tileEntity.yCoord + 1, tileEntity.zCoord + 0.5) < 35 * 35)
				renderRecipeItem(chamber, recipe);
		} else if (pass == 1) {

			renderFluid(chamber.tank);

			if (chamber.didProcess) renderLightning(tileEntity);

			GL11.glPushMatrix();
			rotate(meta);
			bindTexture(ResourceManager.growth_chamber_tex);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glDisable(GL11.GL_CULL_FACE);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			GL11.glDepthMask(false);
			ResourceManager.growth_chamber.renderPart("Glass");
			GL11.glDepthMask(true);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glPopMatrix();
		}

		GL11.glPopMatrix();
	}

	private void rotate(int meta) {
		switch (meta - BlockDummyable.offset) {
			case 2:
				GL11.glRotatef(90, 0, 1, 0);
				break;
			case 3:
				GL11.glRotatef(270, 0, 1, 0);
				break;
			case 4:
				GL11.glRotatef(180, 0, 1, 0);
				break;
			case 5:
				GL11.glRotatef(0, 0, 1, 0);
				break;
		}
	}

	private void renderRecipeItem(TileEntityMachineGrowthChamber te, GenericRecipe recipe) {
		GL11.glPushMatrix();
		GL11.glRotated(-RenderManager.instance.playerViewY, 0, 1, 0);
		GL11.glTranslated(0, 1.5, 0);

		ItemStack stack = recipe.getIcon().copy();
		stack.stackSize = 1;

		if (stack.getItemSpriteNumber() == 0 && stack.getItem() instanceof ItemBlock) {
			if (RenderBlocks.renderItemIn3d(Block.getBlockFromItem(stack.getItem()).getRenderType())) {
				GL11.glTranslated(0, -0.0625, 0);
			} else {
				GL11.glTranslated(0, -0.125, 0);
			}
		} else {
			GL11.glTranslated(0, -0.25, 0);
		}

		GL11.glScaled(1.25, 1.25, 1.25);

		if (dummy == null || dummy.worldObj != te.getWorldObj()) dummy = new EntityItem(te.getWorldObj(), 0 ,0 ,0, stack);
		dummy.setEntityItemStack(stack);
		dummy.hoverStart = 0;

		RenderItem.renderInFrame = true;
		RenderManager.instance.renderEntityWithPosYaw(dummy, 0, 0, 0, 0, 0);
		RenderItem.renderInFrame = false;

		GL11.glPopMatrix();
	}

	private static final double[][] GLASS_WALLS = {
		{ 1.375,   0.9375}, { 1.375,  -0.9375},
		{ 0.9375, -1.375 }, {-0.9375, -1.375 },
		{-1.375,  -0.9375}, {-1.375,   0.9375},
		{-0.9375,  1.375 }, { 0.9375,  1.375 }
	};

	private void renderFluid(FluidTank tank) {
		if (tank.getTankType() == Fluids.NONE || tank.getFill() <= 0) return;

		double inset = 0.95;
		double radius = 1.375 * inset;
		double bottom = 0.5, top = 2.49;

		double fill = (double) tank.getFill() / tank.getMaxFill();
		double height = bottom + (top - bottom) * fill;

		int color = tank.getTankType().getColor();
		float r = ((color >> 16) & 0xFF) / 255f;
		float g = ((color >> 8) & 0xFF) / 255f;
		float b = (color & 0xFF) / 255f;

		bindTexture(tank.getTankType().getTexture());

		GL11.glPushMatrix();
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_BLEND);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		GL11.glDepthMask(false);

		Tessellator tess = Tessellator.instance;
		int n = GLASS_WALLS.length;

		tess.startDrawingQuads();
		tess.setColorRGBA_F(r, g, b, 0.75F);
		for(int i = 0; i < n; i++) {
			double[] a = GLASS_WALLS[i];
			double[] c = GLASS_WALLS[(i + 1) % n];
			double ax = a[0] * inset, az = a[1] * inset;
			double cx = c[0] * inset, cz = c[1] * inset;

			tess.addVertexWithUV(ax, bottom, az, 0, 1);
			tess.addVertexWithUV(cx, bottom, cz, 1, 1);
			tess.addVertexWithUV(cx, height, cz, 1, 0);
			tess.addVertexWithUV(ax, height, az, 0, 0);
		}
		tess.draw();

		tess.startDrawing(GL11.GL_TRIANGLE_FAN);
		tess.setColorRGBA_F(r, g, b, 0.75F);
		for (double[] p : GLASS_WALLS) {
			double px = p[0] * inset, pz = p[1] * inset;
			tess.addVertexWithUV(px, height, pz, (px + radius) / (2 * radius), (pz + radius) / (2 * radius));
		}
		tess.draw();

		GL11.glDepthMask(true);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glColor4f(1,1,1,1);
		GL11.glPopMatrix();
	}

	private static final double[][] PRONGS = {
		{ 0.5, 1.0, 0}, {-0.5, 1.0, 0}, {0, 1.0,  0.5}, {0, 1.0, -0.5},
		{ 0.5, 2.0, 0}, {-0.5, 2.0, 0}, {0, 2.0,  0.5}, {0, 2.0, -0.5}
	};

	private void renderLightning(TileEntity te) {
		long time = System.currentTimeMillis();
		long duration = time / 200;
		int seed = (int) (time / 50);

		for (int i = 0; i < PRONGS.length; i++) {
			rand.setSeed(duration * 31 + i + te.xCoord * 7L + te.zCoord * 13L);
			if (rand.nextInt(3) != 0) continue;

			double[] p = PRONGS[i];
			Vec3 toItem = Vec3.createVectorHelper(-p[0], 1.5 - p[1], -p[2]);

			GL11.glPushMatrix();
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glTranslated(p[0], p[1], p[2]);
			BeamPronter.prontBeam(toItem, BeamPronter.EnumWaveType.RANDOM, BeamPronter.EnumBeamType.SOLID, 0x404040, 0x002040, seed + i, 8, 0.0625F, 3, 0.025F);
			BeamPronter.prontBeam(toItem, BeamPronter.EnumWaveType.RANDOM, BeamPronter.EnumBeamType.SOLID, 0x404040, 0x002040, seed + i, 1, 0F, 3, 0.025F);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glPopMatrix();
		}

		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glColor4f(1,1,1,1);
	}
}
