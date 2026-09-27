package com.hbm.inventory.gui;

import org.lwjgl.opengl.GL11;

import com.hbm.handler.contagion.GenomeSample;
import com.hbm.inventory.container.ContainerMachineSplicer;
import com.hbm.inventory.gui.element.GUIElements;
import com.hbm.items.tool.ItemFloppyDisk;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineSplicer;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

public class GUIMachineSplicer extends GuiInfoContainer {

	private static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/machine/gui_splicer.png");
	private static final float TEX = 320F;

	private final TileEntityMachineSplicer splicer;

	public GUIMachineSplicer(InventoryPlayer invPlayer, TileEntityMachineSplicer tile) {
		super(new ContainerMachineSplicer(invPlayer, tile));
		this.splicer = tile;
		this.xSize = 212;
		this.ySize = 240;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float interp) {
		super.drawScreen(mouseX, mouseY, interp);

		this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 188, guiTop + 19, 16, 34, splicer.power, splicer.maxPower);

		if(splicer.active || splicer.progress > 0) {
			this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 23, guiTop + 29, 89, 20, mouseX, mouseY,
					I18nUtil.resolveKey("gui.splicer.time", formatTime(400 - splicer.progress)));
		}

		if(hasData() && this.checkClick(mouseX, mouseY, 113, 6, 8, 8)) {
			this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 113, guiTop + 6, 8, 8, mouseX, mouseY,
					EnumChatFormatting.RED + I18nUtil.resolveKey("gui.splicer.delete"));
		}

		String missing = getMissing();
		if(!missing.isEmpty() && this.checkClick(mouseX, mouseY, 6, 115, 79, 10)) {
			this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 6, guiTop + 115, 79, 10, mouseX, mouseY,
					EnumChatFormatting.RED + I18nUtil.resolveKey("gui.splicer.required"), missing);
		}
	}

	@Override
	protected void mouseClicked(int x, int y, int button) {
		super.mouseClicked(x, y, button);

		this.clickSendFlag(splicer, x, y, 5, 53, 29, 17, "start");

		if(hasData()) this.clickSendFlag(splicer, x, y, 113, 6, 8, 8, "delete");
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		this.fontRendererObj.drawString(I18n.format("container.inventory"), 20, 142, 4210752);

		int remaining = splicer.active ? 400 - splicer.progress : 0;
		GUIElements.drawCenteredText(this.fontRendererObj, formatTime(remaining), 67, 6, 41, 13, 0.6F, 0x00FF00);

		if(hasData()) {
			GUIElements.drawCenteredText(this.fontRendererObj, splicer.displayGenome, 6, 115, 79, 10, 0.5F, 0x00FF00);
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float interp, int mouseX, int mouseY) {
		GL11.glColor4f(1F, 1F, 1F, 1F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		func_146110_a(guiLeft, guiTop, 0, 0, xSize, ySize, TEX, TEX);

		this.drawScreenHelix();

		int read = Math.min(splicer.progress, 200);
		int write = Math.max(0, splicer.progress - 200);

		int bar1 = read * 89 / 200;
		int bar2 = write * 89 / 200;

		if(bar1 > 0) func_146110_a(guiLeft + 23, guiTop + 29, 227, 91, bar1, 21, TEX, TEX);
		if(bar2 > 0) func_146110_a(guiLeft + 112 - bar2, guiTop + 77, 316 - bar2, 115, bar2, 21, TEX, TEX);

		if(splicer.active) {
			func_146110_a(guiLeft + 5, guiTop + 53, 227, 72, 29, 17, TEX, TEX);
		}

		if(hasData()) {
			func_146110_a(guiLeft + 113, guiTop + 6, 235, 11, 5, 5, TEX, TEX);
		}

		int power = (int) (splicer.power * 34 / Math.max(splicer.maxPower, 1));
		if(power > 0) func_146110_a(guiLeft + 188, guiTop + 53 - power, 235, 50 - power, 16, power, TEX, TEX);

		if(splicer.power >= splicer.consumption) {
			func_146110_a(guiLeft + 192, guiTop + 4, 235, 50, 9, 12, TEX, TEX);
		}
	}

	private void drawScreenHelix() {

		if(!hasData()) return;

		boolean writing = splicer.progress >= 200;
		double phase = (splicer.getWorldObj().getTotalWorldTime() % 240) / 240D * Math.PI * 2;

		this.pushScissor(113, 6, 56, 120);
		GL11.glPushMatrix();
		GL11.glTranslated(guiLeft + 141, guiTop + 66, 0D);
		GL11.glRotated(90D, 0D, 0D, 1D);
		GUIElements.drawHelix(-59D, 59D, 0D, this.zLevel, 16D, 1D, 1D, phase,
				splicer.displayGenome.length(), splicer.displayGenome,
				writing ? 0xFF00FF00 : 0xFF007000, 0xFF555555);
		GL11.glPopMatrix();
		this.popScissor();
	}

	private boolean hasData() {
		if(splicer.active) return true;
		ItemStack floppy = splicer.slots[1];
		return !ItemFloppyDisk.getSections(floppy).isEmpty();
	}

	private String getMissing() {
		ItemStack floppy = splicer.slots[1];
		return GenomeSample.getMissing(ItemFloppyDisk.getSections(floppy));
	}

	private static String formatTime(int ticks) {
		int seconds = Math.max(0, ticks / 20);
		return String.format("%02d:%02d", seconds / 60, seconds % 60);
	}
}
