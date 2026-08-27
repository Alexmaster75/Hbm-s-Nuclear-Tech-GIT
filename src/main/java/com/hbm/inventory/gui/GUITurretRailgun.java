package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerTurretRailgun;
import com.hbm.inventory.gui.element.GUIElements;
import com.hbm.lib.RefStrings;
import com.hbm.main.NTMSounds;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.turret.TileEntityTurretRailgun;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector2f;

public class GUITurretRailgun extends GuiInfoContainer {

	protected static ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/weapon/gui_turret_railgun.png");

	private TileEntityTurretRailgun railgun;
	protected GuiTextField field;

	public GUITurretRailgun(InventoryPlayer invPlayer, TileEntityTurretRailgun tedf) {
		super(new ContainerTurretRailgun(invPlayer, tedf));

		railgun = tedf;

		this.xSize = 176;
		this.ySize = 222;
	}

	public void initGui() {

		super.initGui();

		Keyboard.enableRepeatEvents(true);
		this.field = new GuiTextField(this.fontRendererObj, guiLeft + 96, guiTop + 79, 50, 14);
		this.field.setTextColor(-1);
		this.field.setDisabledTextColour(-1);
		this.field.setEnableBackgroundDrawing(false);
		this.field.setMaxStringLength(25);
	}

	@Override
	public void drawScreen(int x, int y, float interp) {
		super.drawScreen(x, y, interp);

		this.drawElectricityInfo(this, x, y, guiLeft + 153, guiTop + 34, 16, 42, railgun.power, railgun.getMaxPower());
		this.drawElectricityInfo(this, x, y, guiLeft + 7, guiTop + 34, 16, 60, railgun.charge, railgun.maxCharge);
	}

	@Override
	protected void mouseClicked(int x, int y, int button) {
		super.mouseClicked(x, y, button);

		boolean flag = x >= this.field.xPosition && x < this.field.xPosition + this.field.width && y >= this.field.yPosition && y < this.field.yPosition + this.field.height;
		this.field.setFocused(flag);

		// on-off button
		if (guiLeft + 132 <= x && guiLeft + 132 + 29 > x && guiTop + 99 < y && guiTop + 99 + 17 >= y) {
			NBTTagCompound data = new NBTTagCompound();
			data.setBoolean("activeChange", !railgun.active);
			PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, railgun.xCoord, railgun.yCoord, railgun.zCoord));
			mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
			return;
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		String name = this.railgun.hasCustomInventoryName() ? this.railgun.getInventoryName() : I18n.format(this.railgun.getInventoryName());
		this.fontRendererObj.drawString(name, this.xSize / 2 - this.fontRendererObj.getStringWidth(name) / 2, 9, 4210752);

		if (railgun.active && railgun.power > 0) {
			if (Keyboard.isKeyDown(Keyboard.KEY_RETURN)) {
				mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation(NTMSounds.TECH_BOOP), 1.0F));

				if (this.field.getText().isEmpty())
					return;

				NBTTagCompound data = new NBTTagCompound();
				data.setString("dataInput", this.field.getText());
				PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, railgun.xCoord, railgun.yCoord, railgun.zCoord));

				this.field.setText("");
			}

			String t = this.field.getText();

			String cursor = System.currentTimeMillis() % 1000 < 500 ? " " : "||";

			if (this.field.isFocused())
				t = t.substring(0, this.field.getCursorPosition()) + cursor + t.substring(this.field.getCursorPosition(), t.length());

			double scale = 2;

			GL11.glScaled(1D / scale, 1D / scale, 1);
			this.fontRendererObj.drawString("Target:", (int) (97 * scale), (int) (36 * scale), 0x00ff00);
			if (railgun.targetVector != null) {
				this.fontRendererObj.drawString("X: " + (int) railgun.targetVector.xCoord, (int) (97 * scale), (int) (43 * scale), 0x00ff00);
				this.fontRendererObj.drawString("Y: " + (int) railgun.targetVector.yCoord, (int) (97 * scale), (int) (49 * scale), 0x00ff00);
				this.fontRendererObj.drawString("Z: " + (int) railgun.targetVector.zCoord, (int) (97 * scale), (int) (55 * scale), 0x00ff00);
			}
			this.fontRendererObj.drawString("Charging: " + (int) (railgun.charge * 100 / railgun.maxCharge) + "%", (int) (97 * scale), (int) (64 * scale), 0x00ff00);
			this.fontRendererObj.drawString("Aligning: " + railgun.aligning, (int) (97 * scale), (int) (70 * scale), 0x00ff00);

			this.fontRendererObj.drawString(t, (int) (97 * scale), (int) (85 * scale), 0x00ff00); // text field
			GL11.glScaled(scale, scale, 1);

			GUIElements.drawHollowCircle(59, 64, this.zLevel, 20.0f, 48, 0x00FF00);
			if (railgun.targetDirectionVector != null)
				GUIElements.drawArrowVector(59, 64, this.zLevel, new Vector2f((float) (59 + 15 * -railgun.targetDirectionVector.xCoord), (float) (64 + 15 * -railgun.targetDirectionVector.zCoord)), 1.0f, 0xFF0000);
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float v, int i, int i1) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		int scale = (int) (railgun.getPower() * 42 / railgun.getMaxPower());
		drawTexturedModalRect(guiLeft + 153, guiTop + 76 - scale, 176, 59 - scale, 16, scale);

		if (railgun.active)
			drawTexturedModalRect(guiLeft + 132, guiTop + 99, 176, 0, 29, 17);

		// yes im reusing the power gauge scale variable and also yes i dont care
		scale = (int) (railgun.charge * 3 * 60 / railgun.maxCharge); // the value is for all 3 of them, not just one, like they're one single gauge
		int intermediateScale = Math.min(scale, 60);
		drawTexturedModalRect(guiLeft +  7, guiTop + 94 - intermediateScale, 176, 119 - intermediateScale, 4, intermediateScale);
		intermediateScale = MathHelper.clamp_int(scale - 60, 0, 60);
		drawTexturedModalRect(guiLeft + 13, guiTop + 94 - intermediateScale, 176, 119 - intermediateScale, 4, intermediateScale);
		intermediateScale = MathHelper.clamp_int(scale - 120, 0, 60);
		drawTexturedModalRect(guiLeft + 19, guiTop + 94 - intermediateScale, 176, 119 - intermediateScale, 4, intermediateScale);
	}

	protected void keyTyped(char c, int i) {
		if(this.field.textboxKeyTyped(c, i)) return;
		super.keyTyped(c, i);
	}
}
