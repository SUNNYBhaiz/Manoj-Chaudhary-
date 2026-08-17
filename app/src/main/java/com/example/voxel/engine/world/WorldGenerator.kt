package com.example.voxel.engine.world

import com.example.voxel.engine.blocks.BlockType
import com.example.voxel.engine.world.WorldConstants.BEDROCK_LEVEL
import com.example.voxel.engine.world.WorldConstants.CHUNK_SIZE_X
import com.example.voxel.engine.world.WorldConstants.CHUNK_SIZE_Y
import com.example.voxel.engine.world.WorldConstants.CHUNK_SIZE_Z
import com.example.voxel.engine.world.WorldConstants.WATER_LEVEL
import java.util.Random

enum class WorldType(val displayName: String, val description: String) {
    PLAINS_AND_HILLS("Plains & Hills", "Rolling green valleys, forests, lakes and ore caves"),
    EXTREME_MOUNTAINS("Extreme Mountains", "Towering stone peaks, snow caps and deep cliffs"),
    DESERT_OASIS("Desert Oasis", "Warm sand dunes, cacti and palm water springs"),
    FLOATING_ISLANDS("Floating Islands", "Sky islands floating above misty clouds"),
    FLAT_CREATIVE("Flat Creative", "Smooth grass canvas for limitless building")
}

class WorldGenerator(val seed: Long = 1337L, val worldType: WorldType = WorldType.PLAINS_AND_HILLS) {

    private val terrainNoise = FastNoise(seed)
    private val detailNoise = FastNoise(seed + 101L)
    private val caveNoise = FastNoise(seed + 202L)
    private val treeNoise = FastNoise(seed + 303L)
    private val biomeNoise = FastNoise(seed + 404L)

    fun generateChunk(chunk: Chunk) {
        val cx = chunk.worldOriginX
        val cz = chunk.worldOriginZ

        when (worldType) {
            WorldType.FLAT_CREATIVE -> generateFlatChunk(chunk)
            WorldType.FLOATING_ISLANDS -> generateFloatingIslandsChunk(chunk, cx, cz)
            else -> generateStandardChunk(chunk, cx, cz)
        }

        chunk.isGenerated = true
        chunk.isDirty = true
    }

    private fun generateFlatChunk(chunk: Chunk) {
        for (z in 0 until CHUNK_SIZE_Z) {
            for (x in 0 until CHUNK_SIZE_X) {
                chunk.setBlockId(x, BEDROCK_LEVEL, z, BlockType.BEDROCK.id)
                chunk.setBlockId(x, 1, z, BlockType.DIRT.id)
                chunk.setBlockId(x, 2, z, BlockType.DIRT.id)
                chunk.setBlockId(x, 3, z, BlockType.DIRT.id)
                chunk.setBlockId(x, 4, z, BlockType.GRASS.id)
            }
        }
    }

    private fun generateStandardChunk(chunk: Chunk, cx: Int, cz: Int) {
        val heightMap = IntArray(CHUNK_SIZE_X * CHUNK_SIZE_Z)

        for (lz in 0 until CHUNK_SIZE_Z) {
            for (lx in 0 until CHUNK_SIZE_X) {
                val wx = cx + lx
                val wz = cz + lz

                val height = computeTerrainHeight(wx, wz)
                heightMap[lz * CHUNK_SIZE_X + lx] = height

                val isDesert = worldType == WorldType.DESERT_OASIS ||
                        (worldType == WorldType.PLAINS_AND_HILLS && biomeNoise.noise2D(wx * 0.005f, wz * 0.005f) > 0.72f)

                for (ly in 0 until CHUNK_SIZE_Y) {
                    when {
                        ly == BEDROCK_LEVEL -> {
                            chunk.setBlockId(lx, ly, lz, BlockType.BEDROCK.id)
                        }
                        ly < height -> {
                            // 3D Cave carving (caves inside mountains / underground)
                            val caveDensity = caveNoise.noise3D(wx * 0.08f, ly * 0.12f, wz * 0.08f)
                            if (caveDensity > 0.68f && ly > 4 && ly < height - 2) {
                                // Cave air pocket
                                chunk.setBlockId(lx, ly, lz, BlockType.AIR.id)
                            } else {
                                if (ly == height - 1) {
                                    // Surface top block
                                    if (height <= WATER_LEVEL + 1) {
                                        chunk.setBlockId(lx, ly, lz, BlockType.SAND.id)
                                    } else if (isDesert) {
                                        chunk.setBlockId(lx, ly, lz, BlockType.SAND.id)
                                    } else if (ly >= 50) {
                                        chunk.setBlockId(lx, ly, lz, BlockType.SNOW.id)
                                    } else {
                                        chunk.setBlockId(lx, ly, lz, BlockType.GRASS.id)
                                    }
                                } else if (ly >= height - 4) {
                                    // Subsurface
                                    if (isDesert || height <= WATER_LEVEL + 1) {
                                        chunk.setBlockId(lx, ly, lz, BlockType.SAND.id)
                                    } else {
                                        chunk.setBlockId(lx, ly, lz, BlockType.DIRT.id)
                                    }
                                } else {
                                    // Deep underground stone with ore veins
                                    val oreHash = (wx * 73856093L xor ly.toLong() * 19349663L xor wz * 83492791L xor seed)
                                    val rng = Random(oreHash)
                                    val r = rng.nextFloat()

                                    val block = when {
                                        ly <= 14 && r < 0.015f -> BlockType.DIAMOND_ORE
                                        ly <= 24 && r < 0.035f -> BlockType.GOLD_ORE
                                        ly <= 45 && r < 0.07f -> BlockType.IRON_ORE
                                        r < 0.12f -> BlockType.COAL_ORE
                                        else -> BlockType.STONE
                                    }
                                    chunk.setBlockId(lx, ly, lz, block.id)
                                }
                            }
                        }
                        ly in height..WATER_LEVEL -> {
                            // Water body (lake/ocean)
                            chunk.setBlockId(lx, ly, lz, BlockType.WATER.id)
                        }
                        else -> {
                            chunk.setBlockId(lx, ly, lz, BlockType.AIR.id)
                        }
                    }
                }
            }
        }

        // Decorate surface: Trees, Flowers, Cacti
        for (lz in 2 until CHUNK_SIZE_Z - 2) {
            for (lx in 2 until CHUNK_SIZE_X - 2) {
                val wx = cx + lx
                val wz = cz + lz
                val surfaceY = heightMap[lz * CHUNK_SIZE_X + lx]

                if (surfaceY > WATER_LEVEL && surfaceY < CHUNK_SIZE_Y - 10) {
                    val currentTop = chunk.getBlock(lx, surfaceY - 1, lz)
                    val aboveTop = chunk.getBlock(lx, surfaceY, lz)

                    if (aboveTop == BlockType.AIR) {
                        val treeDensity = treeNoise.noise2D(wx * 0.05f, wz * 0.05f)
                        val isBirch = detailNoise.noise2D(wx * 0.04f, wz * 0.04f) > 0.55f

                        if (currentTop == BlockType.GRASS && treeDensity > 0.65f && (wx + wz) % 5 == 0) {
                            placeTree(chunk, lx, surfaceY, lz, isBirch)
                        } else if (currentTop == BlockType.SAND && worldType == WorldType.DESERT_OASIS && (wx * wz) % 19 == 0) {
                            placeCactus(chunk, lx, surfaceY, lz)
                        } else if (currentTop == BlockType.GRASS && (wx * 13 + wz * 17) % 23 == 0) {
                            val flowerType = if ((wx + wz) % 2 == 0) BlockType.FLOWER_RED else BlockType.FLOWER_YELLOW
                            chunk.setBlockId(lx, surfaceY, lz, flowerType.id)
                        }
                    }
                }
            }
        }
    }

    private fun generateFloatingIslandsChunk(chunk: Chunk, cx: Int, cz: Int) {
        for (lz in 0 until CHUNK_SIZE_Z) {
            for (lx in 0 until CHUNK_SIZE_X) {
                val wx = cx + lx
                val wz = cz + lz

                for (ly in 0 until CHUNK_SIZE_Y) {
                    val density = terrainNoise.noise3D(wx * 0.04f, ly * 0.06f, wz * 0.04f)
                    val heightAtten = 1.0f - Math.abs((ly - 32) / 20.0f).coerceIn(0f, 1f)
                    val finalDensity = density * heightAtten

                    if (finalDensity > 0.42f) {
                        val aboveDensity = terrainNoise.noise3D(wx * 0.04f, (ly + 1) * 0.06f, wz * 0.04f) *
                                (1.0f - Math.abs(((ly + 1) - 32) / 20.0f).coerceIn(0f, 1f))
                        if (aboveDensity <= 0.42f) {
                            chunk.setBlockId(lx, ly, lz, BlockType.GRASS.id)
                        } else if (ly < 20) {
                            chunk.setBlockId(lx, ly, lz, BlockType.STONE.id)
                        } else {
                            chunk.setBlockId(lx, ly, lz, BlockType.DIRT.id)
                        }
                    } else {
                        chunk.setBlockId(lx, ly, lz, BlockType.AIR.id)
                    }
                }
            }
        }
    }

    private fun computeTerrainHeight(wx: Int, wz: Int): Int {
        val fx = wx * 0.015f
        val fz = wz * 0.015f

        val baseNoise = terrainNoise.fbm2D(fx, fz, octaves = 3, persistence = 0.5f)
        val detail = detailNoise.noise2D(wx * 0.06f, wz * 0.06f) * 0.2f

        return when (worldType) {
            WorldType.EXTREME_MOUNTAINS -> {
                val mountainFactor = Math.pow(baseNoise.toDouble(), 2.2).toFloat()
                (18 + mountainFactor * 42 + detail * 6).toInt().coerceIn(4, CHUNK_SIZE_Y - 4)
            }
            WorldType.DESERT_OASIS -> {
                (20 + baseNoise * 14 + detail * 4).toInt().coerceIn(4, CHUNK_SIZE_Y - 8)
            }
            else -> {
                (18 + baseNoise * 20 + detail * 5).toInt().coerceIn(4, CHUNK_SIZE_Y - 8)
            }
        }
    }

    private fun placeTree(chunk: Chunk, lx: Int, baseY: Int, lz: Int, isBirch: Boolean) {
        val trunkHeight = 4 + ((lx + lz) % 2)
        val logType = if (isBirch) BlockType.WOOD_BIRCH else BlockType.WOOD_OAK

        // Trunk
        for (y in 0 until trunkHeight) {
            val ty = baseY + y
            if (ty < CHUNK_SIZE_Y) {
                chunk.setBlockId(lx, ty, lz, logType.id)
            }
        }

        // Leaves canopy (spherical/box 3x3 and 5x5)
        val canopyBottom = baseY + trunkHeight - 2
        val canopyTop = baseY + trunkHeight + 1

        for (cy in canopyBottom..canopyTop) {
            if (cy >= CHUNK_SIZE_Y) continue
            val radius = if (cy >= canopyTop) 1 else 2
            for (dz in -radius..radius) {
                for (dx in -radius..radius) {
                    val nx = lx + dx
                    val nz = lz + dz
                    if (nx in 0 until CHUNK_SIZE_X && nz in 0 until CHUNK_SIZE_Z) {
                        // Skip corners on wide layer for natural rounded look
                        if (radius == 2 && Math.abs(dx) == 2 && Math.abs(dz) == 2 && (dx * dz) % 2 == 0) continue
                        if (chunk.getBlock(nx, cy, nz) == BlockType.AIR) {
                            chunk.setBlockId(nx, cy, nz, BlockType.LEAVES.id)
                        }
                    }
                }
            }
        }
    }

    private fun placeCactus(chunk: Chunk, lx: Int, baseY: Int, lz: Int) {
        val height = 2 + ((lx * lz) % 2)
        for (y in 0 until height) {
            val cy = baseY + y
            if (cy < CHUNK_SIZE_Y) {
                chunk.setBlockId(lx, cy, lz, BlockType.CACTUS.id)
            }
        }
    }
}
