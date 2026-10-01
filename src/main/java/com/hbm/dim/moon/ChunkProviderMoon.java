package com.hbm.dim.moon;

import com.hbm.blocks.BlockEnums;
import com.hbm.blocks.ModBlocks;
import com.hbm.config.WorldConfig;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.ChunkProviderCelestial;
import com.hbm.dim.mapgen.MapGenCrater;
import com.hbm.dim.mapgen.MapGenGreg;
import com.hbm.dim.mapgen.MapgenRavineButBased;
import com.hbm.world.gen.terrain.MapGenBubble;

import com.hbm.world.gen.terrain.MapGenGeode;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class ChunkProviderMoon extends ChunkProviderCelestial {

	private MapGenGreg caveGenV3 = new MapGenGreg();
	private MapgenRavineButBased rgen = new MapgenRavineButBased();

	private MapGenCrater smallCrater = new MapGenCrater(6);
	private MapGenCrater largeCrater = new MapGenCrater(64);
	private MapGenGeode saltGeode = new MapGenGeode(32);

	private MapGenBubble brine = new MapGenBubble(WorldConfig.munBrineSpawn);

	public ChunkProviderMoon(World world, long seed) {
		super(world, seed);
		caveGenV3.stoneBlock = ModBlocks.moon_rock;
		rgen.stoneBlock = ModBlocks.moon_rock;

		smallCrater.setSize(8, 32);
		largeCrater.setSize(96, 128);

		smallCrater.regolith = largeCrater.regolith = ModBlocks.basalt;
		smallCrater.rock = largeCrater.rock = ModBlocks.moon_rock;

		brine.block = ModBlocks.ore_brine;
		brine.meta = (byte)CelestialBody.getMeta(world);
		brine.replace = ModBlocks.moon_rock;
		brine.setSize(8, 16);

		stoneBlock = ModBlocks.moon_rock;
		seaBlock = ModBlocks.basalt;
		seaLevel = 64;

		saltGeode.shell = ModBlocks.basalt;
		saltGeode.crystal = ModBlocks.block_crystal_3;
		saltGeode.crystalMeta = (byte) (BlockEnums.EnumCrystalBlockType.SALT.ordinal() - 32);
		saltGeode.budding = ModBlocks.budding_salt;
		saltGeode.buds = new Block[] { ModBlocks.salt_bud_small, ModBlocks.salt_bud_medium, ModBlocks.salt_bud_large};
		saltGeode.setSize(3, 5);
		saltGeode.setHeight(15, 45);
	}

	@Override
	protected Block getFlatWorldBlock(Block block) {
		if(block == Blocks.water || block == Blocks.flowing_water) return ModBlocks.basalt;
		if(block == Blocks.grass || block == Blocks.sand) return ModBlocks.moon_turf;
		if(block == Blocks.dirt || block == Blocks.stone || block == Blocks.sandstone) return ModBlocks.moon_rock;
		if(block == Blocks.snow_layer) return Blocks.air;
		return block;
	}

	@Override
	public BlockMetaBuffer getChunkPrimer(int x, int z) {
		BlockMetaBuffer buffer = super.getChunkPrimer(x, z);
		brine.setMetas(buffer.metas);

		// NEW CAVES
		caveGenV3.func_151539_a(this, worldObj, x, z, buffer.blocks);
		rgen.func_151539_a(this, worldObj, x, z, buffer.blocks);
		smallCrater.func_151539_a(this, worldObj, x, z, buffer.blocks);
		largeCrater.func_151539_a(this, worldObj, x, z, buffer.blocks);
		brine.func_151539_a(this, worldObj, x, z, buffer.blocks);
		saltGeode.setMetas(buffer.metas);
		saltGeode.func_151539_a(this, worldObj, x, z, buffer.blocks);

		return buffer;
	}

}
