package com.hbm.inventory.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.hbm.inventory.container.ContainerMedicineSynthesizer;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.gui.element.GUIElements;
import com.hbm.items.ItemVial;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMedicineSynthesizer;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.fluidmk2.IFillableItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

public class GUIMedicineSynthesizer extends GuiInfoContainer {

	private static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/machine/gui_medicine_synthesizer.png");
	private static final float TEX = 320F;

	private final TileEntityMedicineSynthesizer synth;

	public GUIMedicineSynthesizer(InventoryPlayer invPlayer, TileEntityMedicineSynthesizer tile) {
		super(new ContainerMedicineSynthesizer(invPlayer, tile));
		this.synth = tile;

		this.xSize = 268;
		this.ySize = 216;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);

		this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 247, guiTop + 19, 16, 63, synth.power, synth.maxPower);
		synth.tank.renderTankInfo(this, mouseX, mouseY, guiLeft + 7, guiTop + 24, 16, 75);

		if(synth.active) {
			this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 85, guiTop + 31, 34, 34, mouseX, mouseY,
					I18nUtil.resolveKey("gui.dial.time", formatTime(synth.maxProgress - synth.progress)));
		}

		// empty syringe, cure vial, floppy disk and pharmaceutical computing unit
		if(this.isPreviewSlot(this.inventorySlots.getSlot(0), mouseX, mouseY)) this.drawStackPreview(getSyringes(), mouseX, mouseY);
		if(!synth.isDiseaseMode() && this.isPreviewSlot(this.inventorySlots.getSlot(1), mouseX, mouseY)) this.drawStackPreview(getVials(), mouseX, mouseY);
		if(this.isPreviewSlot(this.inventorySlots.getSlot(2), mouseX, mouseY)) this.drawStackPreview(getFloppies(), mouseX, mouseY);
		if(this.isPreviewSlot(this.inventorySlots.getSlot(3), mouseX, mouseY)) this.drawStackPreview(getPharmaUnits(), mouseX, mouseY);
	}

	private static List<ItemStack> getSyringes() {
		List<ItemStack> list = new ArrayList<ItemStack>();
		list.add(new ItemStack(ModItems.medical_syringe));
		return list;
	}

	private static List<ItemStack> getVials() {
		List<ItemStack> list = new ArrayList<ItemStack>();
		ItemStack vial = new ItemStack(ModItems.vial);
		IFillableItem.setFluidFill(vial, Fluids.HUMAN_BLOOD, (short) ItemVial.MAX_FLUID);
		list.add(vial);
		return list;
	}

	private static List<ItemStack> getFloppies() {
		List<ItemStack> list = new ArrayList<ItemStack>();
		list.add(new ItemStack(ModItems.floppy_disk));
		return list;
	}

	private static List<ItemStack> getPharmaUnits() {
		List<ItemStack> list = new ArrayList<ItemStack>();
		list.add(new ItemStack(ModItems.pharma_computing_unit));
		return list;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int x, int y) {
		this.fontRendererObj.drawString(I18n.format("container.inventory"), 44, 121, 4210752);
	}

	private static String formatTime(int ticks) {
		int seconds = ticks / 20;
		int hours = seconds / 3600;
		seconds %= 3600;
		int minutes = seconds / 60;
		seconds %= 60;
		if(hours > 0) return hours + "h " + minutes + "m " + seconds + "s";
		if(minutes > 0) return minutes + "m " + seconds + "s";
		return seconds + "s";
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
		GL11.glColor4f(1F, 1F, 1F, 1F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		func_146110_a(guiLeft, guiTop, 0, 0, xSize, ySize, TEX, TEX);

		boolean disease = synth.isDiseaseMode();

		if(disease) {
			func_146110_a(guiLeft + 39, guiTop + 5, 286, 126, 33, 9, TEX, TEX);
			func_146110_a(guiLeft + 145, guiTop + 20, 290, 35, 26, 56, TEX, TEX);
			func_146110_a(guiLeft + 94, guiTop + 81, 303, 136, 16, 16, TEX, TEX);
			func_146110_a(guiLeft + 43, guiTop + 40, 304, 157, 16, 16, TEX, TEX);
			func_146110_a(guiLeft + 78, guiTop + 5, 0, 220, 93, 10, TEX, TEX);
		}

		int p = synth.getProgressScaled(34);
		if(p > 0) {
			int v = (disease ? 92 : 0) + 34 - p;
			func_146110_a(guiLeft + 85, guiTop + 31 + 34 - p, 285, v, 34, p, TEX, TEX);
		}

		int k = (int) synth.getPowerScaled(63);
		if(k > 0) func_146110_a(guiLeft + 247, guiTop + 19 + 63 - k, 269, 63 - k, 16, k, TEX, TEX);
		if(synth.power >= 200) func_146110_a(guiLeft + 251, guiTop + 5, 269, 63, 9, 12, TEX, TEX);

		synth.tank.renderTank(guiLeft + 7, guiTop + 99, this.zLevel, 16, 75, 0);

		this.drawScreenHelix();

		GL11.glColor4f(1F, 1F, 1F, 1F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		func_146110_a(guiLeft + 227, guiTop + 5, disease ? 312 : 316, 153, 3, 3, TEX, TEX);

		if(!synth.displayGenome.isEmpty()) {
			GUIElements.drawCenteredText(this.fontRendererObj, colorGenome(synth.displayGenome), guiLeft + 143, guiTop + 98, 80, 11, 0.5F, 0x00FF00);
		}
	}

	private void drawScreenHelix() {

		if(synth.displayGenome.isEmpty()) return;

		boolean active = synth.progress > 0;
		double phase = (synth.getWorldObj().getTotalWorldTime() % 240) / 240D * Math.PI * 2;

		this.pushScissor(175, 5, 56, 79);
		GL11.glPushMatrix();
		GL11.glTranslated(guiLeft + 203, guiTop + 44, 0D);
		GL11.glRotated(90D, 0D, 0D, 1D);
		GUIElements.drawHelix(-38D, 38D, 0D, this.zLevel, 16D, 1D, 1D, phase, synth.displayGenome.length(), synth.displayGenome, active ? 0xFF00FF00 : 0xFF007000, 0xFF555555);
		GL11.glPopMatrix();
		this.popScissor();
	}

	private static String colorGenome(String genome) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < genome.length(); i++) {
			EnumChatFormatting c = i < 8 ? EnumChatFormatting.RED : i < 16 ? EnumChatFormatting.AQUA : EnumChatFormatting.GREEN;
			sb.append(c).append(genome.charAt(i));
		}
		return sb.toString();
	}

}
