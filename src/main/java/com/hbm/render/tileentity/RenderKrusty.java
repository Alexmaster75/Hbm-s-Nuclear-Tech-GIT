package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;

import com.hbm.main.ResourceManager;
import com.hbm.tileentity.machine.TileEntityMachineKrusty;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class RenderKrusty extends TileEntitySpecialRenderer {

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float f) {
		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5D, y, z + 0.5D);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glRotatef(180, 0F, 1F, 0F);

		TileEntityMachineKrusty krusty = (TileEntityMachineKrusty) tileEntity;

		bindTexture(ResourceManager.reactor_krusty_base_tex);
		ResourceManager.reactor_krusty_base.renderAll();
		
		double level = (krusty.lastLevel + (krusty.level - krusty.lastLevel) * f);
		double temp = krusty.heat * 0.00002 * 980 + 20 + 273.15;
		float r, g, b, v;
		
		GL11.glPushMatrix();
		GL11.glTranslated(0.0D, level, 0.0D);

		bindTexture(ResourceManager.reactor_krusty_rod_tex);
		ResourceManager.reactor_krusty_rod.renderAll();

		GL11.glPopMatrix();
		
		if(krusty.heat > 0) {

			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			GL11.glDisable(GL11.GL_ALPHA_TEST);

			Tessellator tess = Tessellator.instance;

			for(double d = 0.285; d < 0.7; d += 0.05) {

				tess.startDrawingQuads();
				r = blackBodyRadiation(temp, 7 * Math.pow(10, -7));
				g = blackBodyRadiation(temp, 5.5 * Math.pow(10, -7));
				b = blackBodyRadiation(temp, 4.5 * Math.pow(10, -7));
				v = (float) Vec3.createVectorHelper(r, g, b).lengthVector();
				tess.setColorRGBA_F(r/v, g/v, b/v, MathHelper.clamp_float((float) (v / (2.87 * Math.pow(10, 11))), 0.0F, 1.0F));

				double top = 1.375;
				double bottom = 1.375;

				tess.addVertex(d, bottom - d, -d);
				tess.addVertex(d, top + d, -d);
				tess.addVertex(d, top + d, d);
				tess.addVertex(d, bottom - d, d);
				
				tess.addVertex(-d, bottom - d, -d);
				tess.addVertex(-d, top + d, -d);
				tess.addVertex(-d, top + d, d);
				tess.addVertex(-d, bottom - d, d);

				tess.addVertex(-d, bottom - d, d);
				tess.addVertex(-d, top + d, d);
				tess.addVertex(d, top + d, d);
				tess.addVertex(d, bottom - d, d);

				tess.addVertex(-d, bottom - d, -d);
				tess.addVertex(-d, top + d, -d);
				tess.addVertex(d, top + d, -d);
				tess.addVertex(d, bottom - d, -d);

				tess.addVertex(-d, top + d, -d);
				tess.addVertex(-d, top + d, d);
				tess.addVertex(d, top + d, d);
				tess.addVertex(d, top + d, -d);

				tess.addVertex(-d, bottom - d, -d);
				tess.addVertex(-d, bottom - d, d);
				tess.addVertex(d, bottom - d, d);
				tess.addVertex(d, bottom - d, -d);

				tess.draw();
			}

			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_ALPHA_TEST);
		}

		GL11.glEnable(GL11.GL_CULL_FACE);

		GL11.glPopMatrix();
	}
	
	public float blackBodyRadiation(double temp, double freq) {
		double h = 6.626D * Math.pow(10, -34); // planck constant
		double k = 1.810D * Math.pow(10, -23); // boltzmann constant
		double c = 300000000.0D; // light speed in vacuum
		return (float) (2.0D * h * c*c / Math.pow(freq, 5) * 1 / (Math.pow(Math.E, h * c / (temp * k * freq)) - 1.0D)); // uhhhh
	}
}
