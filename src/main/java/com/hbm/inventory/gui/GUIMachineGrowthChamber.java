package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerMachineGrowthChamber;
import com.hbm.inventory.gui.element.GUIElements;
import com.hbm.inventory.recipes.AssemblyMachineRecipes;
import com.hbm.inventory.recipes.GrowthChamberRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.items.machine.ItemBlueprints;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineGrowthChamber;
import com.hbm.util.i18n.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GUIMachineGrowthChamber extends GuiInfoContainer {
	private static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/processing/gui_growth_chamber.png");
	private final TileEntityMachineGrowthChamber chamber;

	public GUIMachineGrowthChamber(InventoryPlayer player, TileEntityMachineGrowthChamber te) {
		super(new ContainerMachineGrowthChamber(player, te));
		this.chamber = te;

		this.xSize = 176;
		this.ySize = 204;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);

		chamber.tank.renderTankInfo(this, mouseX, mouseY, guiLeft + 35, guiTop + 63, 34, 16);

		this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 152, guiTop + 18, 16, 52, chamber.power, chamber.maxPower);

		if(guiLeft <= mouseX && guiLeft + 18 > mouseX && guiTop < mouseY && guiTop + 18 >= mouseY) {
			if(this.chamber.module.getRecipeName() != null && AssemblyMachineRecipes.INSTANCE.recipeNameMap.containsKey(this.chamber.module.getRecipeName())) {
				GenericRecipe recipe = this.chamber.module.getRecipe();
				GUIElements.drawHoveringTextRecipe(recipe.print(), mouseX, mouseY, this.fontRendererObj, itemRender, this.width, this.height);
			} else {
				this.drawCreativeTabHoveringText(EnumChatFormatting.YELLOW + I18nUtil.resolveKey("gui.recipe.setRecipe"), mouseX, mouseY);
			}
		}
	}

	@Override
	protected void mouseClicked(int x, int y, int button) {
		super.mouseClicked(x, y, button);

		if(this.checkClick(x, y, 7, 10, 18, 18)) GUIScreenRecipeSelector.openSelector(GrowthChamberRecipes.INSTANCE, chamber, chamber.module.getRecipeName(), 0, null, this);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		this.fontRendererObj.drawString(I18n.format("container.inventory"), 8, 110, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		if (chamber.power > 0) {
			int p = (int) (chamber.power * 61 / chamber.maxPower);
			drawTexturedModalRect(guiLeft + 152, guiTop + 70 - p, 176, 52 - p, 16, p);
			drawTexturedModalRect(guiLeft + 156, guiTop + 4, 176, 52, 9, 12);
		}

		if(chamber.module.progress > 0) {
			int j = (int) Math.ceil(38 * chamber.module.progress);
			drawTexturedModalRect(guiLeft + 66, guiTop + 37, 204, 0, j, 14);
		}

		GenericRecipe recipe = chamber.module.getRecipe();

		this.renderItem(recipe != null ? recipe.getIcon() : TEMPLATE_FOLDER, 9, 10);

		if(recipe != null && recipe.inputItem != null) {
			for(int i = 0; i < recipe.inputItem.length; i++) {
				Slot slot = (Slot) this.inventorySlots.inventorySlots.get(chamber.module.inputSlots[i]);
				if(!slot.getHasStack()) this.renderItem(recipe.inputItem[i].extractForCyclingDisplay(20), slot.xDisplayPosition, slot.yDisplayPosition, 10F);
			}

			Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			GL11.glColor4f(1F, 1F, 1F, 0.5F);
			GL11.glEnable(GL11.GL_BLEND);
			this.zLevel = 300F;
			for(int i = 0; i < recipe.inputItem.length; i++) {
				Slot slot = (Slot) this.inventorySlots.inventorySlots.get(chamber.module.inputSlots[i]);
				if(!slot.getHasStack()) drawTexturedModalRect(guiLeft + slot.xDisplayPosition, guiTop + slot.yDisplayPosition, slot.xDisplayPosition, slot.yDisplayPosition, 16, 16);
			}
			this.zLevel = 0F;
			GL11.glColor4f(1F, 1F, 1F, 1F);
			GL11.glDisable(GL11.GL_BLEND);
		}

		chamber.tank.renderTank(guiLeft + 35, guiTop + 79, this.zLevel, 34, 16, 1);
	}
}
