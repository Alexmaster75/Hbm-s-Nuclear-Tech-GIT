package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerCorePulse;
import com.hbm.inventory.gui.element.GUIElements;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.TileEntityCorePulse;
import com.hbm.util.BobMathUtil;
import net.minecraft.nbt.NBTTagCompound;
import org.lwjgl.opengl.GL11;

import com.hbm.lib.RefStrings;
import com.hbm.packet.PacketDispatcher;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GUICorePulse extends GuiInfoContainer {

	private static ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/dfc/gui_pulse.png");
	private TileEntityCorePulse emitter;
	byte timer; // keypad
	int xTarget;
	int yTarget;

	public GUICorePulse(InventoryPlayer invPlayer, TileEntityCorePulse tedf) {
		super(new ContainerCorePulse(invPlayer, tedf));
		emitter = tedf;

		this.xSize = 176;
		this.ySize = 166;
	}

	public void initGui() {
		super.initGui();
		emitter.powIndex = 0;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);
	}

	protected void mouseClicked(int x, int y, int i) {
		super.mouseClicked(x, y, i);

		if(guiLeft + 127 <= x && guiLeft + 127 + 32 > x && guiTop + 20 < y && guiTop + 20 + 43 >= y) {
			int j;
			xTarget = (int) Math.ceil((double) (x - guiLeft - 127) / 11);
			yTarget = (int) Math.ceil((double) (y - guiTop - 20) / 11 - 1);
			NBTTagCompound data = new NBTTagCompound();
			if (guiTop + 20 < y && guiTop + 20 + 32 >= y) {
				j = xTarget + yTarget * 3;
				if (j == 0)
					j = 1;
			} else {
				j = xTarget;
				if (j == 0)
					j = 1;
				switch (j) {
					case 1:
						j = -2;
						break;
					case 2:
						j = 0;
						break;
					case 3:
						j = -1;
						break;
				}
			}
			timer = 15;

			data.setLong("keypad", j * 20L);
			PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, emitter.xCoord, emitter.yCoord, emitter.zCoord));
			mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		String timer = emitter.getMinutes() + ":" + emitter.getSeconds();
		double scale = 0.75;
		// timer
		GL11.glScaled(scale, scale, scale);
		this.fontRendererObj.drawString(timer, (int) ((90 - this.fontRendererObj.getStringWidth(timer) / 2) * (1 / scale)), (int) (63.5 * (1 / scale)), 0xff0000);
		// energy display stuff
		this.fontRendererObj.drawString("Inp: " + BobMathUtil.getShortNumber(emitter.joules), (int)(scale * 20), (int)(scale * 20), 0x00ff00);
		this.fontRendererObj.drawString("Crg: " + BobMathUtil.getShortNumber(emitter.stored), (int)(scale * 20), (int)(scale * 64), 0x00ff00);
		this.fontRendererObj.drawString("Out: " + BobMathUtil.getShortNumber(emitter.target), (int)(scale * 20), (int)(scale * 108), 0x00ff00);
		GL11.glScaled(1 / scale, 1 / scale, 1 / scale);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		if (emitter.commit && emitter.maxTimer != 0L) {
			GUIElements.drawSmoothTextureModalCircle(guiLeft + 64, guiTop + 11, this.zLevel, 176, 44, 48, 48, (double) emitter.timer / emitter.maxTimer);
			drawTexturedModalRect(guiLeft + 81, guiTop + 28, 177, 86 + (emitter.timer > emitter.jammerTime ? 0 : 16), 14, 14);
		}
		if (timer > 0) {
			drawTexturedModalRect(guiLeft + 127 + (xTarget - 1) * 11, guiTop + 20 +  + yTarget * 11, 176 + (xTarget - 1) * 11,  + yTarget * 11, 11, 11);
			timer--;
		}
	}

	@Override
	public void onGuiClosed() {
	}
}
