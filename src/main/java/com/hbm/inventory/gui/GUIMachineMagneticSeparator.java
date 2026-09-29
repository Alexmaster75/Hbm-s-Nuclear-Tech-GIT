package com.hbm.inventory.gui;

import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

import com.hbm.inventory.container.ContainerMachineMagneticSeparator;
import com.hbm.inventory.gui.element.GUIElements;
import com.hbm.inventory.recipes.MagneticSeparatorRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemMagneticDisc;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineMagneticSeparator;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

public class GUIMachineMagneticSeparator extends GuiInfoContainer {

	private static ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/processing/gui_magnetic_separator.png");
	private TileEntityMachineMagneticSeparator machine;

	public GUIMachineMagneticSeparator(InventoryPlayer invPlayer, TileEntityMachineMagneticSeparator te) {
		super(new ContainerMachineMagneticSeparator(invPlayer, te));
		machine = te;

		this.xSize = 176;
		this.ySize = 256;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);

		machine.inputTank.renderTankInfo(this, mouseX, mouseY, guiLeft + 8, guiTop + 18, 16, 52);
		machine.outputTank.renderTankInfo(this, mouseX, mouseY, guiLeft + 116, guiTop + 36, 16, 52);

		this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 152, guiTop + 18, 16, 61, machine.power, machine.maxPower);

		if(guiLeft + 7 <= mouseX && guiLeft + 7 + 18 > mouseX && guiTop + 125 < mouseY && guiTop + 125 + 18 >= mouseY) {
			if(machine.module.getRecipeName() != null && MagneticSeparatorRecipes.INSTANCE.recipeNameMap.containsKey(machine.module.getRecipeName())) {
				GenericRecipe recipe = machine.module.getRecipe();
				GUIElements.drawHoveringTextRecipe(recipe.print(), mouseX, mouseY, this.fontRendererObj, itemRender, this.width, this.height);
			} else {
				this.drawCreativeTabHoveringText(EnumChatFormatting.YELLOW + I18nUtil.resolveKey("gui.recipe.setRecipe"), mouseX, mouseY);
			}
		}

		// magnetic disc
		if(this.isPreviewSlot(this.inventorySlots.getSlot(2), mouseX, mouseY)) {
			this.drawStackPreview(getDiscs(), mouseX, mouseY);
		}
	}

	private static List<ItemStack> getDiscs() {
		List<ItemStack> list = new ArrayList<ItemStack>();
		for(ItemMagneticDisc.EnumMagneticDisc type : ItemMagneticDisc.EnumMagneticDisc.values()) {
			list.add(new ItemStack(ModItems.magnetic_disc, 1, type.ordinal()));
		}
		return list;
	}

	@Override
	protected void mouseClicked(int x, int y, int button) {
		super.mouseClicked(x, y, button);

		if(this.checkClick(x, y, 7, 125, 18, 18)) GUIScreenRecipeSelector.openSelector(MagneticSeparatorRecipes.INSTANCE, machine, machine.module.getRecipeName(), 0, null, this);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		String name = this.machine.hasCustomInventoryName() ? this.machine.getInventoryName() : I18n.format(this.machine.getInventoryName());
		this.fontRendererObj.drawString(name, 70 - this.fontRendererObj.getStringWidth(name) / 2, 6, 4210752);
		this.fontRendererObj.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		int p = (int) (machine.power * 61 / machine.maxPower);
		drawTexturedModalRect(guiLeft + 152, guiTop + 79 - p, 176, 61 - p, 16, p);

		if(machine.module.progress > 0) {
			int j = (int) Math.ceil(70 * machine.module.progress);
			drawTexturedModalRect(guiLeft + 62, guiTop + 126, 176, 61, j, 16);
		}

		GenericRecipe recipe = machine.module.getRecipe();
		this.renderItem(recipe != null ? recipe.getIcon() : TEMPLATE_FOLDER, 8, 126);

		machine.inputTank.renderTank(guiLeft + 8, guiTop + 70, this.zLevel, 16, 52);
		machine.outputTank.renderTank(guiLeft + 116, guiTop + 88, this.zLevel, 16, 52);
	}
}
