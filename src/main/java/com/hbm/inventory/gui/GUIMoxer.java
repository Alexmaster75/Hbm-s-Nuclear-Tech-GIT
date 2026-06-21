package com.hbm.inventory.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.NBTControlPacket;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.hbm.inventory.container.ContainerMoxer;
import com.hbm.inventory.gui.element.GUIElements;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.Mats.MoxerStack;
import com.hbm.inventory.material.NTMMaterial.SmeltingBehavior;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMoxer;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

public class GUIMoxer extends GuiInfoContainer {

	private static ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/processing/gui_moxer.png");
	private TileEntityMoxer moxer;
	protected GuiTextField field;

	int index;
	boolean return_prs;
	boolean return_old;

	public void initGui() {

		super.initGui();

		Keyboard.enableRepeatEvents(true);
		this.field = new GuiTextField(this.fontRendererObj, guiLeft + 109, guiTop + 83, 50, 14);
		this.field.setTextColor(-1);
		this.field.setDisabledTextColour(-1);
		this.field.setEnableBackgroundDrawing(false);
		this.field.setMaxStringLength(6);
	}

	public GUIMoxer(InventoryPlayer invPlayer, TileEntityMoxer tedf) {
		super(new ContainerMoxer(invPlayer, tedf));
		moxer = tedf;

		this.xSize = 176;
		this.ySize = 214;
	}

	@Override
	public void drawScreen(int x, int y, float interp) {
		super.drawScreen(x, y, interp);

		drawStackInfo(moxer.inpStack, x, y, 61, 17);

		moxer.tanks[0].renderTankInfo(this, x, y, guiLeft + 25, guiTop + 37, 18, 18);
		moxer.tanks[1].renderTankInfo(this, x, y, guiLeft + 25, guiTop + 58, 18, 18);
	}

	@Override
	protected void mouseClicked(int x, int y, int button) {
		super.mouseClicked(x, y, button);

		boolean flag = x >= this.field.xPosition && x < this.field.xPosition + this.field.width && y >= this.field.yPosition && y < this.field.yPosition + this.field.height;
		this.field.setFocused(flag);

		int count = getCount();

		if(count > 0) {

			if(guiLeft + 106 <= x && guiLeft + 106 + 18 > x && guiTop + 53 < y && guiTop + 53 + 18 >= y) {

				index--;
				if(index < 0)
					index = count - 1;
				mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
				return;
			}

			if(guiLeft + 124 <= x && guiLeft + 124 + 18 > x && guiTop + 53 < y && guiTop + 53 + 18 >= y) {

				index++;
				index %= count;
				mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
				return;
			}
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		String name = this.moxer.hasCustomInventoryName() ? this.moxer.getInventoryName() : I18n.format(this.moxer.getInventoryName());

		this.fontRendererObj.drawString(name, this.xSize / 2 - this.fontRendererObj.getStringWidth(name) / 2, 5, 4210752);

		return_prs = Keyboard.isKeyDown(Keyboard.KEY_RETURN);
		if (return_prs && !return_old) {
			mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));

			if(this.field.getText().isEmpty())
				return;

			NBTTagCompound data = new NBTTagCompound();
			// this is horrible and i hate it, but it works
			// example percentage = 1050 -> from the right, first 3 digits represent the actual percentage, everything after is the index
			int percentage;
			try {
				percentage = Integer.parseInt(this.field.getText());
			} catch (NumberFormatException e) {
				percentage = 0;
			}
			percentage = MathHelper.clamp_int(percentage, 0, 100);
			percentage += index * 1000;
			data.setInteger("percentage", percentage);
			PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, moxer.xCoord, moxer.yCoord, moxer.zCoord));

			this.field.setText("");
		}
		return_old = return_prs;

		List<String> names = moxer.getStringMaterials();

		String n = EnumChatFormatting.ITALIC + I18nUtil.resolveKey("turret.none");

		while(this.index >= this.getCount())
			this.index--;

		if(index < 0)
			index = 0;

		if(names != null) {
			n = I18nUtil.resolveKey(names.get(index));
		}

		String t = this.field.getText();
		String p = String.format("%d%s", moxer.inpStack.isEmpty() ? 0 : moxer.inpStack.get(index).percentage, "%");

		int amount = 0;
		for (MoxerStack mat : moxer.inpStack)
			amount += mat.amount;
		String total = String.format("Total: %d mB", amount);

		String cursor = System.currentTimeMillis() % 1000 < 500 ? " " : "||";

		if(this.field.isFocused())
			t = t.substring(0, this.field.getCursorPosition()) + cursor + t.substring(this.field.getCursorPosition(), t.length());

		double scale = 2;

		GL11.glScaled(1D / scale, 1D / scale, 1);
		this.fontRendererObj.drawString(total, (int)(109 * scale), (int)(20 * scale), 0x00ff00);
		this.fontRendererObj.drawString(n, (int)(109 * scale), (int)(26 * scale), 0x00ff00);
		this.fontRendererObj.drawString(p, (int)(109 * scale), (int)(32 * scale), 0x00ff00);
		this.fontRendererObj.drawString(t, (int)(109 * scale), (int)(87 * scale), 0x00ff00);
		GL11.glScaled(scale, scale, 1);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		if(!moxer.inpStack.isEmpty()) drawStack(moxer.inpStack, moxer.inpZCapacity, 62, 97);

		// selector down
		if(guiLeft + 106 <= p_146976_2_ && guiLeft + 106 + 18 > p_146976_2_ && guiTop + 53 < p_146976_3_ && guiTop + 53 + 18 >=p_146976_3_) {
			drawTexturedModalRect(guiLeft + 106, guiTop + 53, 210, 0, 18, 18);
		}
		// selector up
		if(guiLeft + 124 <= p_146976_2_ && guiLeft + 124 + 18 > p_146976_2_ && guiTop + 53 < p_146976_3_ && guiTop + 53 + 18 >=p_146976_3_) {
			drawTexturedModalRect(guiLeft + 124, guiTop + 53, 228, 0, 18, 18);
		}
		// shitter
		if(guiLeft + 142 <= p_146976_2_ && guiLeft + 142 + 18 > p_146976_2_ && guiTop + 53 < p_146976_3_ && guiTop + 53 + 18 >=p_146976_3_) {
			drawTexturedModalRect(guiLeft + 142, guiTop + 53, 210, 18, 18, 18);
		}

		GUIElements.drawSmoothGauge(guiLeft + 34, guiTop + 47, this.zLevel, (double) moxer.tanks[0].getFill() / (double) moxer.tanks[0].getMaxFill(), 5, 2, 1, 0x7F0000);
		GUIElements.drawSmoothGauge(guiLeft + 34, guiTop + 68, this.zLevel, (double) moxer.tanks[1].getFill() / (double) moxer.tanks[1].getMaxFill(), 5, 2, 1, 0x7F0000);
	}

	protected void drawStackInfo(List<MoxerStack> stack, int mouseX, int mouseY, int x, int y) {

		List<String> list = new ArrayList();

		if(stack.isEmpty())
			list.add(EnumChatFormatting.RED + "Empty");

		for(MoxerStack sta : stack) {
			list.add(EnumChatFormatting.YELLOW + String.format("(%3d%s) %s: %d mB", sta.percentage, "%", I18nUtil.resolveKey(sta.material.getUnlocalizedName()), sta.amount));
			if (sta.percentage != 0)
				list.add(EnumChatFormatting.RED + String.format("↪ -%d mB", (int) (144.0 * sta.percentage / 100.0)));
		}

		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + x, guiTop + y, 36, 81, mouseX, mouseY, list);
	}

	protected void drawStack(List<MoxerStack> stack, int capacity, int x, int y) {

		if(stack.isEmpty()) return;

		int lastHeight = 0;
		int lastQuant = 0;

		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

		for(MoxerStack sta : stack) {

			int targetHeight = (lastQuant + sta.amount) * 79 / capacity;

			if(lastHeight == targetHeight) continue; //skip draw calls that would be 0 pixels high

			int offset = sta.material.smeltable == SmeltingBehavior.ADDITIVE ? 34 : 0; //additives use a differnt texture

			int hex = sta.material.moltenColor;
			//hex = 0xC18336;
			Color color = new Color(hex);
			GL11.glColor3f(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
			drawTexturedModalRect(guiLeft + x, guiTop + y - targetHeight, 176 + offset, 89 - targetHeight, 34, targetHeight - lastHeight);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glColor4f(1F, 1F, 1F, 0.3F);
			drawTexturedModalRect(guiLeft + x, guiTop + y - targetHeight, 176 + offset, 89 - targetHeight, 34, targetHeight - lastHeight);
			GL11.glDisable(GL11.GL_BLEND);

			lastQuant += sta.amount;
			lastHeight = targetHeight;
		}

		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		GL11.glColor3f(255, 255, 255);
	}

	private int getCount() {

		List<Mats.MoxerStack> materials = moxer.inpStack;

		if(materials == null)
			return 0;

		return materials.size();
	}

	protected void keyTyped(char c, int i) {
		if(this.field.textboxKeyTyped(c, i)) return;
		super.keyTyped(c, i);
	}
}
