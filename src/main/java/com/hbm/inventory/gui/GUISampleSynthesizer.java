package com.hbm.inventory.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.hbm.inventory.container.ContainerSampleSynthesizer;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ItemVial;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.TileEntitySampleSynthesizer;

import api.hbm.fluidmk2.IFillableItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class GUISampleSynthesizer extends GuiInfoContainer {

	private static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/machine/gui_sample_synthesizer.png");
	private static final float TEX = 320F;

	private TileEntitySampleSynthesizer machine;

	public GUISampleSynthesizer(InventoryPlayer invPlayer, TileEntitySampleSynthesizer machine) {
		super(new ContainerSampleSynthesizer(invPlayer, machine));
		this.machine = machine;

		this.xSize = 176;
		this.ySize = 207;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);

		this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 152, guiTop + 18, 16, 52, machine.power, machine.maxPower);

		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 36, guiTop + 20, 18, 18, mouseX, mouseY, new String[] {"Clone disk"});
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 12, guiTop + 43, 18, 18, mouseX, mouseY, new String[] {"Scan vial"});

		// source disk, blood sample and target disk
		if(this.isPreviewSlot(this.inventorySlots.getSlot(0), mouseX, mouseY)) this.drawStackPreview(getDisks(), mouseX, mouseY);
		if(this.isPreviewSlot(this.inventorySlots.getSlot(1), mouseX, mouseY)) this.drawStackPreview(getSamples(), mouseX, mouseY);
		if(this.isPreviewSlot(this.inventorySlots.getSlot(2), mouseX, mouseY)) this.drawStackPreview(getDisks(), mouseX, mouseY);
	}

	private static List<ItemStack> getDisks() {
		List<ItemStack> list = new ArrayList<ItemStack>();
		list.add(new ItemStack(ModItems.floppy_disk));
		return list;
	}

	private static List<ItemStack> getSamples() {
		List<ItemStack> list = new ArrayList<ItemStack>();
		ItemStack vial = new ItemStack(ModItems.vial);
		IFillableItem.setFluidFill(vial, Fluids.HUMAN_BLOOD, (short) ItemVial.MAX_FLUID);
		list.add(vial);
		return list;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float interp, int mX, int mY) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		func_146110_a(guiLeft, guiTop, 0, 0, xSize, ySize, TEX, TEX);

		int p = (int) (machine.power * 52 / machine.maxPower);
		func_146110_a(guiLeft + 152, guiTop + 18 + 52 - p, 176, 52 - p, 16, p, TEX, TEX);
		if(machine.power > 0) func_146110_a(guiLeft + 156, guiTop + 4, 176, 52, 9, 12, TEX, TEX);

		if(machine.progress > 0) {
			p = (int) (machine.progress * 116 / machine.maxProgress);
			func_146110_a(guiLeft + 11, guiTop + 71, 192, 0, p, 28, TEX, TEX);
		}

		fontRendererObj.drawString(machine.status, guiLeft + 66, guiTop + 47, 0xFFFFFF);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mx, int my) {
	}

	@Override
	protected void mouseClicked(int x, int y, int i) {
		super.mouseClicked(x, y, i);

		if(checkClick(x, y, 12, 43, 18, 18)) {
			mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));

			NBTTagCompound data = new NBTTagCompound();
			data.setBoolean("scan", true);

			PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, machine.xCoord, machine.yCoord, machine.zCoord));
		}

		if(checkClick(x, y, 36, 20, 18, 18)) {
			mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));

			NBTTagCompound data = new NBTTagCompound();
			data.setBoolean("clone", true);

			PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, machine.xCoord, machine.yCoord, machine.zCoord));
		}
	}
}
