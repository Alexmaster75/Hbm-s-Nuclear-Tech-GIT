package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.main.ResourceManager;
import com.hbm.render.util.SkinCache;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityCloner;
import com.mojang.authlib.GameProfile;

import java.nio.DoubleBuffer;

import org.lwjgl.BufferUtils;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.util.ResourceLocation;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.common.util.ForgeDirection;

public class RenderCloner extends TileEntitySpecialRenderer implements IItemRendererProvider {

	private static final ModelBiped cloneModel = new ModelBiped(0.0F);

	@Override
	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float interp) {
		GL11.glPushMatrix();
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glShadeModel(GL11.GL_SMOOTH);

		ForgeDirection facing = ForgeDirection.getOrientation(te.getBlockMetadata() - BlockDummyable.offset);
		int[] dim = MultiblockHandlerXR.rotate(((BlockDummyable) te.getBlockType()).getDimensions(), facing);
		double cx = x + (dim[5] - dim[4] + 1) / 2.0;
		double cz = z + (dim[3] - dim[2] + 1) / 2.0;
		double bottom = y - dim[1];

		GL11.glTranslated(cx, bottom, cz);

		switch(facing.ordinal()) {
		case 2: GL11.glRotatef(0F, 0F, 1F, 0F); break;
		case 4: GL11.glRotatef(90, 0F, 1F, 0F); break;
		case 3: GL11.glRotatef(180, 0F, 1F, 0F); break;
		case 5: GL11.glRotatef(270, 0F, 1F, 0F); break;
		}

		float door = 0F;
		if(te instanceof TileEntityCloner) {
			TileEntityCloner cloner = (TileEntityCloner) te;
			door = cloner.prevDoor + (cloner.door - cloner.prevDoor) * interp;
		}

		bindTexture(ResourceManager.vat_tex);
		ResourceManager.vat.renderPart("main");

		GL11.glPushMatrix();
		GL11.glTranslatef(0.545F, 0F, 0.25F);
		GL11.glRotatef(-90F * door, 0F, 1F, 0F);
		GL11.glTranslatef(-0.545F, 0F, -0.25F);
		ResourceManager.vat.renderPart("door");
		GL11.glPopMatrix();

		TileEntityCloner cloner = (TileEntityCloner) te;
		float build = cloner.spawnDelay > 0 ? 1F : cloner.progress / (float) TileEntityCloner.PROCESS_TIME;

		if(cloner.spawnDelay > 0 || (cloner.active && build > 0F)) {
			cloneModel.isChild = false;

			ResourceLocation skin = AbstractClientPlayer.locationStevePng;
			GameProfile owner = SkinCache.owner(cloner.slots[TileEntityCloner.SLOT_SYRINGE]);

			if(owner != null) {
				ResourceLocation cached = SkinCache.getSkin(owner);
				if(cached != null) skin = cached;
			}

			DoubleBuffer plane = BufferUtils.createDoubleBuffer(4);
			plane.put(new double[] { 0.0D, -1.0D, 0.0D, 0.3D + 2.05D * build });
			plane.flip();

			GL11.glPushMatrix();
			GL11.glTranslatef(0.31F, 0.25F, -0.31F);
			GL11.glClipPlane(GL11.GL_CLIP_PLANE0, plane);
			GL11.glEnable(GL11.GL_CLIP_PLANE0);
			GL11.glScalef(-1.0F, -1.0F, 1.0F);
			GL11.glTranslatef(0F, -1.8F, 0F);
			bindTexture(skin);
			cloneModel.render(null, 0F, 0F, 0F, 0F, 0F, 0.0625F);
			GL11.glDisable(GL11.GL_CLIP_PLANE0);
			GL11.glPopMatrix();
		}

		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glPopMatrix();
	}

	@Override
	public Item getItemForRenderer() {
		return Item.getItemFromBlock(ModBlocks.machine_cloner);
	}

	@Override
	public IItemRenderer getRenderer() {
		return new ItemRenderBase() {
			public void renderInventory() {
				GL11.glTranslated(0, -3.5, 0);
				GL11.glScaled(5.25, 5.25, 5.25);
			}
			public void renderCommon() {
				GL11.glScaled(0.75, 0.75, 0.75);
				GL11.glShadeModel(GL11.GL_SMOOTH);
				bindTexture(ResourceManager.vat_tex);
				ResourceManager.vat.renderPart("main");
				ResourceManager.vat.renderPart("door");
				GL11.glShadeModel(GL11.GL_FLAT);
			}
		};
	}
}
