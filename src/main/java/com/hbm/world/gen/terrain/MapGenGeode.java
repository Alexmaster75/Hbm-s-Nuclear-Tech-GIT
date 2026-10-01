package com.hbm.world.gen.terrain;

import com.hbm.world.gen.MapGenBaseMeta;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.List;

public class MapGenGeode extends MapGenBaseMeta {
	private final int chancePerChunk;
	private int minSize = 3;
	private int maxSize = 5;
	private int minY = 15;
	private int maxY = 25;

	public int minCover = 4;

	public Block shell;
	public byte shellMeta = 0;

	public Block crystal;
	public byte crystalMeta = 0;

	public Block budding;
	public Block[] buds;

	public int buddingChance = 12;
	public int budChance = 3;

	public BiomeGenBase targetBiome;

	public MapGenGeode(int chancePerChunk) {
		this.chancePerChunk = chancePerChunk;
	}

	public MapGenGeode setSize(int min, int max) {
		this.minSize = min;
		this.maxSize = max;

		this.range = (max + 3) / 16 + 1;

		return this;
	}

	public MapGenGeode setHeight(int minY, int maxY) {
		this.minY = minY;
		this.maxY = maxY;
		return this;
	}

	@Override
	protected void func_151538_a(World world, int originX, int originZ, int chunkX, int chunkZ, Block[] blocks) {
		if (!(rand.nextInt(chancePerChunk) == Math.abs(originX) % chancePerChunk && rand.nextInt(chancePerChunk) == Math.abs(originZ) % chancePerChunk)) return;
		if (targetBiome != null && worldObj.getWorldChunkManager().getBiomeGenAt(originX * 16, originZ * 16) != targetBiome) return;

		int cx = (originX - chunkX) * 16 + rand.nextInt(16);
		int cz = (originZ - chunkZ) * 16 + rand.nextInt(16);
		int cy = minY + rand.nextInt(maxY - minY + 1);

		double hollowR = minSize + rand.nextInt(maxSize - minSize + 1);
		double crystalR = hollowR + 1.2;
		double shellR = hollowR + 2.4;
		int r = (int) Math.ceil(shellR);

		int yMin = Math.max(1, cy - r);
		int yMax = Math.min(254, cy + r);

		List<Integer> buddingBlocks = new ArrayList<>();

		for (int bx = 0; bx < 16; bx++) {
			for (int bz = 0; bz < 16; bz++) {
				int column = (bx * 16 + bz) * 256;

				int surface = 0;
				for (int y = 255; y > 0; y--) {
					Block b = blocks[column + y];
					if (b != null && b.isOpaqueCube()) {
						surface = y;
						break;
					}
				}
				int limit = Math.min(yMax, surface - minCover);

				for (int by = yMin; by <= limit; by++) {
					int dx = bx - cx, dy = by - cy, dz = bz - cz;
					double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
					if (dist > shellR) continue;

					int index = (bx * 16 + bz) * 256 + by;

					if (dist <= hollowR) {
						blocks[index] = null;
						metas[index] = 0;
					} else if (dist <= crystalR) {
						if (budding != null && rand.nextInt(buddingChance) == 0) {
							blocks[index] = budding;
							metas[index] = 0;
							buddingBlocks.add(index);
						} else {
							blocks[index] = crystal;
							metas[index] = crystalMeta;
						}
					} else if (blocks[index] != null) {
						blocks[index] = shell;
						metas[index] = shellMeta;
					}
				}
			}
		}

		if (buds == null) return;
		for (int index : buddingBlocks) {
			int bx = index / 4096;
			int bz = (index / 256) % 16;
			int by = index % 256;

			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				int tx = bx + dir.offsetX, ty = by + dir.offsetY, tz = bz + dir.offsetZ;

				if (tx < 0 || tx > 15 || tz < 0 || tz > 15) continue;
				int target = (tx * 16 + tz) * 256 + ty;
				if (blocks[target] == null && rand.nextInt(budChance) == 0) {
					blocks[target] = buds[rand.nextInt(buds.length)];
					metas[target] = (byte) dir.ordinal();
				}
			}
		}
	}


}
