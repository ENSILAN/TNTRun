package me.nathanfallet.ensilan.tntrun.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import me.nathanfallet.ensilan.tntrun.TNTRun;

public class TNTRunGenerator extends ChunkGenerator {

	@Override
	public List<BlockPopulator> getDefaultPopulators(World world) {
		return Arrays.asList();
	}

	@Override
	public boolean canSpawn(World world, int x, int z) {
		return true;
	}

	public int xyzToByte(int x, int y, int z) {
		return (x * 16 + z) * 128 + y;
	}

	@Override
	public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
		ChunkData chunk = createChunkData(world);
		if (chunkX >= -1 && chunkZ >= -1 && chunkZ <= TNTRun.SIZE) {
            if ((chunkZ == -1 || chunkZ == TNTRun.SIZE) && (chunkX + 1) % (TNTRun.SIZE + 1) == 0) {
                // Empty chunk
            } else if (chunkZ == -1) {
                // Border (gauche)
                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < TNTRun.DISTANCE * TNTRun.COUNT; y++) {
                        chunk.setBlock(x, y, 15, Material.STONE_BRICKS);
                    }
                }
            } else if (chunkZ == TNTRun.SIZE) {
                // Border (droite)
                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < TNTRun.DISTANCE * TNTRun.COUNT; y++) {
                        chunk.setBlock(x, y, 0, Material.STONE_BRICKS);
                    }
                }
            } else if ((chunkX + 1) % (TNTRun.SIZE + 1) == 0) {
                // Border (milieu)
                for (int z = 0; z < 16; z++) {
                    for (int y = 0; y < TNTRun.DISTANCE * TNTRun.COUNT; y++) {
                        if (chunkX != -1) {
                            chunk.setBlock(0, y, z, Material.STONE_BRICKS);
                        }
                        chunk.setBlock(15, y, z, Material.STONE_BRICKS);
                    }
                }
            } else {
                // Sand
                for (int y = 0; y < TNTRun.COUNT; y++) {
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            chunk.setBlock(x, y * TNTRun.DISTANCE, z, Material.TNT);
                            chunk.setBlock(x, y * TNTRun.DISTANCE + 1, z, Material.SAND);
                        }
                    }
                }
            }
		}
		return chunk;
	}

}
