package com.hbm.render.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;

import com.hbm.items.tool.ItemMedicalSyringe;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public class SkinCache {

	private static final Map<UUID, ResourceLocation> skins = new HashMap<UUID, ResourceLocation>();
	private static final Set<UUID> requested = new HashSet<UUID>();
	public static ResourceLocation getSkin(GameProfile profile) {
		UUID id = profile == null ? null : profile.getId();
		if(id == null) return null;

		ResourceLocation skin = skins.get(id);
		if(skin != null) return skin;

		if(requested.add(id)) request(profile);
		return null;
	}

	public static GameProfile owner(ItemStack syringe) {
		if(syringe == null || !syringe.hasTagCompound()) return null;

		String uuid = syringe.stackTagCompound.getString(ItemMedicalSyringe.KEY_OWNER_UUID);
		if(uuid.isEmpty()) return null;

		String name = syringe.stackTagCompound.getString(ItemMedicalSyringe.KEY_OWNER_NAME);

		try {
			return new GameProfile(UUID.fromString(uuid), name.isEmpty() ? uuid : name);
		} catch(IllegalArgumentException ex) {
			return null;
		}
	}

	private static void request(final GameProfile profile) {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					final GameProfile filled = Minecraft.getMinecraft().func_152347_ac().fillProfileProperties(profile, false);
					if(filled == null || filled.getProperties().isEmpty()) return;

					Minecraft.getMinecraft().func_152344_a(new Runnable() {
						@Override
						public void run() {
							Minecraft.getMinecraft().func_152342_ad().func_152790_a(filled, new SkinManager.SkinAvailableCallback() {
								@Override
								public void func_152121_a(MinecraftProfileTexture.Type type, ResourceLocation location) {
									if(type == MinecraftProfileTexture.Type.SKIN) skins.put(profile.getId(), location);
								}
							}, false);
						}
					});
				} catch(Exception ex) {
					// offline acc
				}
			}
		}, "NTM Skin Lookup");

		thread.setDaemon(true);
		thread.start();
	}
}
