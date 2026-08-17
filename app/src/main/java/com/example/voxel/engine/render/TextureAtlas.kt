package com.example.voxel.engine.render

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.opengl.GLES20
import android.opengl.GLUtils
import java.util.Random

/**
 * High-performance texture atlas generator for Minecraft-style blocks and crack overlays.
 * Generates rich, crisp 16x16 pixel-art textures for each block face procedurally into a 256x256 OpenGL texture.
 */
object TextureAtlas {
    const val ATLAS_SIZE = 256
    const val TILE_SIZE = 16
    const val TILES_PER_ROW = ATLAS_SIZE / TILE_SIZE // 16

    var textureId: Int = 0
        private set

    fun initTexture(): Int {
        val bitmap = createAtlasBitmap()
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        textureId = textures[0]

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST_MIPMAP_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)

        bitmap.recycle()
        return textureId
    }

    private fun createAtlasBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(ATLAS_SIZE, ATLAS_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.TRANSPARENT)

        val rng = Random(1337L)
        val paint = Paint().apply { isAntiAlias = false }

        fun drawTile(col: Int, row: Int, generator: (x: Int, y: Int) -> Int) {
            val startX = col * TILE_SIZE
            val startY = row * TILE_SIZE
            val pixels = IntArray(TILE_SIZE * TILE_SIZE)
            for (y in 0 until TILE_SIZE) {
                for (x in 0 until TILE_SIZE) {
                    pixels[y * TILE_SIZE + x] = generator(x, y)
                }
            }
            bitmap.setPixels(pixels, 0, TILE_SIZE, startX, startY, TILE_SIZE, TILE_SIZE)
        }

        // Noise helper
        fun noiseColor(baseR: Int, baseG: Int, baseB: Int, variance: Int, alpha: Int = 255): Int {
            val v = (rng.nextFloat() * 2f - 1f) * variance
            val r = (baseR + v).toInt().coerceIn(0, 255)
            val g = (baseG + v).toInt().coerceIn(0, 255)
            val b = (baseB + v).toInt().coerceIn(0, 255)
            return Color.argb(alpha, r, g, b)
        }

        // ROW 0:
        // (0,0) Grass Top - Vibrant green with subtle variation
        drawTile(0, 0) { x, y ->
            val v = ((x * 7 + y * 13) % 5) * 6 - 12
            Color.rgb(85 + v, 175 + v, 45 + v)
        }

        // (1,0) Grass Side - Green top 3-4px with drips into brown dirt
        drawTile(1, 0) { x, y ->
            val drip = 3 + ((x * 3 + 1) % 3)
            if (y < drip) {
                Color.rgb(85, 175, 45)
            } else if (y == drip && ((x + y) % 2 == 0)) {
                Color.rgb(70, 145, 35)
            } else {
                val v = ((x * 11 + y * 7) % 7) * 4 - 12
                Color.rgb(134 + v, 96 + v, 67 + v)
            }
        }

        // (2,0) Dirt - Rich brown earthy texture
        drawTile(2, 0) { x, y ->
            val v = ((x * 13 + y * 17) % 7) * 5 - 15
            Color.rgb(134 + v, 96 + v, 67 + v)
        }

        // (3,0) Stone - Smooth gray with subtle natural mineral noise
        drawTile(3, 0) { x, y ->
            val v = ((x * 19 + y * 23) % 9) * 4 - 16
            Color.rgb(128 + v, 128 + v, 128 + v)
        }

        // (4,0) Cobblestone - Distinct stone mortar brick pattern
        drawTile(4, 0) { x, y ->
            val isBorder = (x % 4 == 0 || y % 4 == 0) && (x + y) % 3 == 0
            if (isBorder) {
                Color.rgb(75, 75, 75)
            } else {
                val v = ((x * 7 + y * 11) % 6) * 6 - 15
                Color.rgb(120 + v, 120 + v, 120 + v)
            }
        }

        // (5,0) Oak Bark Side - Vertical wood grain ridges
        drawTile(5, 0) { x, y ->
            val ridge = if (x % 4 == 0) -25 else if (x % 4 == 1) 15 else 0
            val v = ((y * 11) % 5) * 4 + ridge
            Color.rgb((103 + v).coerceIn(40, 180), (82 + v).coerceIn(30, 160), (56 + v).coerceIn(20, 140))
        }

        // (6,0) Wood Log Top/Bottom - Concentric rings
        drawTile(6, 0) { x, y ->
            val dx = x - 7.5f
            val dy = y - 7.5f
            val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
            if (dist > 6.8f) {
                Color.rgb(85, 65, 45) // outer bark
            } else {
                val ring = if ((dist.toInt() % 2) == 0) 15 else -10
                Color.rgb(175 + ring, 145 + ring, 100 + ring)
            }
        }

        // (7,0) Oak Planks - Horizontal wooden boards with nail dots
        drawTile(7, 0) { x, y ->
            val isSeam = (y % 4 == 0) || (y < 4 && x == 8) || (y in 4..7 && x == 3) || (y in 8..11 && x == 12) || (y in 12..15 && x == 6)
            if (isSeam) {
                Color.rgb(115, 85, 48)
            } else {
                val v = ((x * 5 + y * 9) % 5) * 4 - 8
                Color.rgb(168 + v, 130 + v, 78 + v)
            }
        }

        // (8,0) Oak Leaves - Crisp lush foliage with small transparency cutouts
        drawTile(8, 0) { x, y ->
            val isHole = (x * y + x + y) % 11 == 0
            if (isHole) {
                Color.TRANSPARENT
            } else {
                val v = ((x * 13 + y * 7) % 6) * 8 - 20
                Color.rgb(55 + v, 140 + v, 35 + v)
            }
        }

        // (9,0) Sand - Warm golden desert sand
        drawTile(9, 0) { x, y ->
            val v = ((x * 17 + y * 19) % 7) * 4 - 12
            Color.rgb(218 + v, 205 + v, 145 + v)
        }

        // (10,0) Glass - Clear with diagonal white shine streaks & thin border
        drawTile(10, 0) { x, y ->
            val isBorder = x == 0 || x == 15 || y == 0 || y == 15
            val isStreak = (x - y == 3 || x - y == 4) && x in 4..12
            if (isBorder) {
                Color.argb(220, 220, 240, 255)
            } else if (isStreak) {
                Color.argb(180, 255, 255, 255)
            } else {
                Color.argb(45, 180, 220, 255)
            }
        }

        // (11,0) Water - Animated/Translucent clean azure blue
        drawTile(11, 0) { x, y ->
            val wave = ((x * 3 + y * 5) % 4) * 10
            Color.argb(185, 45 + wave, 110 + wave, 220)
        }

        // (12,0) Bedrock - Indestructible dark mottled stone
        drawTile(12, 0) { x, y ->
            val v = ((x * 23 + y * 29) % 13) * 6
            Color.rgb(25 + v, 25 + v, 25 + v)
        }

        // (13,0) Coal Ore - Stone with black coal specks
        drawTile(13, 0) { x, y ->
            val isOre = (x in 3..6 && y in 3..6) || (x in 9..12 && y in 8..11) || (x in 2..4 && y in 10..12)
            if (isOre) {
                Color.rgb(30, 30, 30)
            } else {
                val v = ((x * 19 + y * 23) % 9) * 4 - 16
                Color.rgb(128 + v, 128 + v, 128 + v)
            }
        }

        // (14,0) Iron Ore - Stone with tan/brown iron specks
        drawTile(14, 0) { x, y ->
            val isOre = (x in 4..7 && y in 2..5) || (x in 8..11 && y in 9..12) || (x in 11..13 && y in 3..5)
            if (isOre) {
                Color.rgb(215, 175, 140)
            } else {
                val v = ((x * 19 + y * 23) % 9) * 4 - 16
                Color.rgb(128 + v, 128 + v, 128 + v)
            }
        }

        // (15,0) Gold Ore - Stone with shiny yellow gold nuggets
        drawTile(15, 0) { x, y ->
            val isOre = (x in 3..6 && y in 4..7) || (x in 9..12 && y in 2..5) || (x in 7..10 && y in 10..13)
            if (isOre) {
                Color.rgb(255, 215, 0)
            } else {
                val v = ((x * 19 + y * 23) % 9) * 4 - 16
                Color.rgb(128 + v, 128 + v, 128 + v)
            }
        }

        // ROW 1:
        // (0,1) Diamond Ore - Stone with brilliant cyan diamond crystals
        drawTile(0, 1) { x, y ->
            val isOre = (x in 3..6 && y in 3..6) || (x in 9..12 && y in 8..11) || (x in 10..12 && y in 2..4)
            if (isOre) {
                if (x == 4 && y == 4 || x == 10 && y == 9) Color.rgb(220, 255, 255)
                else Color.rgb(75, 225, 235)
            } else {
                val v = ((x * 19 + y * 23) % 9) * 4 - 16
                Color.rgb(128 + v, 128 + v, 128 + v)
            }
        }

        // (1,1) Bricks - Red clay bricks with white/gray mortar
        drawTile(1, 1) { x, y ->
            val isMortar = (y % 4 == 0) || (y in 1..3 && (x == 0 || x == 8)) || (y in 5..7 && (x == 4 || x == 12)) || (y in 9..11 && (x == 0 || x == 8)) || (y in 13..15 && (x == 4 || x == 12))
            if (isMortar) {
                Color.rgb(190, 185, 175)
            } else {
                val v = ((x * 7 + y * 13) % 5) * 6 - 12
                Color.rgb(165 + v, 65 + v, 45 + v)
            }
        }

        // (2,1) Bookshelf Side - Wood frame filled with colorful book spines
        drawTile(2, 1) { x, y ->
            val isWood = y == 0 || y == 7 || y == 8 || y == 15 || x == 0 || x == 15
            if (isWood) {
                Color.rgb(140, 100, 55)
            } else {
                val bookCol = when ((x + y / 8 * 3) % 5) {
                    0 -> Color.rgb(180, 40, 40) // Red
                    1 -> Color.rgb(40, 90, 180) // Blue
                    2 -> Color.rgb(50, 150, 60) // Green
                    3 -> Color.rgb(200, 160, 40) // Gold
                    else -> Color.rgb(120, 70, 150) // Purple
                }
                bookCol
            }
        }

        // (3,1) Crafting Table Top - Grid pattern with tool carving
        drawTile(3, 1) { x, y ->
            val isBorder = x == 0 || x == 15 || y == 0 || y == 15 || x == 7 || x == 8 || y == 7 || y == 8
            if (isBorder) {
                Color.rgb(115, 80, 45)
            } else {
                Color.rgb(185, 145, 90)
            }
        }

        // (4,1) Crafting Table Side - Planks with hammer and saw icons
        drawTile(4, 1) { x, y ->
            val isTopBorder = y < 2
            val isTool = (x in 4..6 && y in 5..12) || (x in 9..12 && y in 6..10)
            if (isTopBorder) {
                Color.rgb(115, 80, 45)
            } else if (isTool) {
                Color.rgb(80, 80, 80)
            } else {
                val v = ((x * 5 + y * 9) % 5) * 4 - 8
                Color.rgb(155 + v, 120 + v, 70 + v)
            }
        }

        // (5,1) Furnace Top/Bottom/Side - Dark cobblestone stone
        drawTile(5, 1) { x, y ->
            val v = ((x * 11 + y * 13) % 7) * 5 - 15
            Color.rgb(95 + v, 95 + v, 95 + v)
        }

        // (6,1) Furnace Front - Cobblestone with black furnace opening
        drawTile(6, 1) { x, y ->
            val isHole = x in 4..11 && y in 5..12
            if (isHole) {
                Color.rgb(30, 25, 20)
            } else {
                val v = ((x * 11 + y * 13) % 7) * 5 - 15
                Color.rgb(95 + v, 95 + v, 95 + v)
            }
        }

        // (7,1) TNT Top - Red with white fuse center
        drawTile(7, 1) { x, y ->
            val isCenter = x in 6..9 && y in 6..9
            val isFuse = x in 7..8 && y in 7..8
            if (isFuse) {
                Color.rgb(40, 40, 40)
            } else if (isCenter) {
                Color.rgb(230, 230, 230)
            } else {
                Color.rgb(210, 45, 30)
            }
        }

        // (8,1) TNT Side - Red with white banner and "TNT" lettering
        drawTile(8, 1) { x, y ->
            val isBanner = y in 6..10
            if (isBanner) {
                // Approximate TNT letters
                val isT1 = (y == 7 && x in 2..4) || (x == 3 && y in 7..9)
                val isN = (x == 6 || x == 8 || (x == 7 && y == 8)) && y in 7..9
                val isT2 = (y == 7 && x in 10..12) || (x == 11 && y in 7..9)
                if (isT1 || isN || isT2) {
                    Color.BLACK
                } else {
                    Color.WHITE
                }
            } else {
                val v = (x % 2) * 15
                Color.rgb(205 + v, 40, 25)
            }
        }

        // (9,1) TNT Bottom - Red wood base
        drawTile(9, 1) { x, y ->
            val v = ((x * 7 + y * 7) % 4) * 8
            Color.rgb(180 + v, 35, 20)
        }

        // (10,1) Torch - Wooden stick with glowing yellow/orange flame top
        drawTile(10, 1) { x, y ->
            val isStick = x in 7..8 && y in 5..14
            val isFlame = x in 6..9 && y in 2..4
            if (isFlame) {
                if (x in 7..8 && y == 3) Color.rgb(255, 255, 200) // core
                else Color.rgb(255, 160, 0)
            } else if (isStick) {
                Color.rgb(130, 95, 55)
            } else {
                Color.TRANSPARENT
            }
        }

        // (11,1) Snow Block - Crisp pristine white with soft blue shadows
        drawTile(11, 1) { x, y ->
            val v = ((x * 13 + y * 17) % 5) * 4 - 8
            Color.rgb(240 + v, 245 + v, 255)
        }

        // (12,1) Cactus Top - Green circle with spikes
        drawTile(12, 1) { x, y ->
            val dx = x - 7.5f
            val dy = y - 7.5f
            val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
            if (dist < 6.5f) {
                val v = ((x * 7 + y * 9) % 4) * 5
                Color.rgb(45 + v, 125 + v, 35 + v)
            } else {
                Color.TRANSPARENT
            }
        }

        // (13,1) Cactus Side - Green vertical ribs with dark thorn dots
        drawTile(13, 1) { x, y ->
            val isRib = x % 4 == 0
            val isThorn = (x % 4 == 2) && (y % 4 == 1)
            if (isThorn) {
                Color.rgb(20, 40, 15)
            } else if (isRib) {
                Color.rgb(35, 95, 25)
            } else {
                val v = (y % 3) * 6
                Color.rgb(55 + v, 140 + v, 40 + v)
            }
        }

        // (14,1) Glowstone - Radiant golden crystal clusters
        drawTile(14, 1) { x, y ->
            val crystal = ((x * 17 + y * 19) % 7)
            when (crystal) {
                0, 1 -> Color.rgb(255, 245, 180) // bright sparkle
                2, 3 -> Color.rgb(240, 195, 80)
                else -> Color.rgb(190, 140, 45)
            }
        }

        // (15,1) Obsidian - Deep dark purple/black crystalline stone
        drawTile(15, 1) { x, y ->
            val v = ((x * 23 + y * 17) % 8) * 4
            val isPurple = (x + y * 3) % 7 == 0
            if (isPurple) Color.rgb(65, 30, 85)
            else Color.rgb(20 + v, 15 + v, 28 + v)
        }

        // ROW 2:
        // (0,2) Poppy Flower - Vibrant red petals with green stem
        drawTile(0, 2) { x, y ->
            val isStem = (x == 7 || x == 8) && y in 7..15
            val isPetals = (x in 5..10 && y in 2..6)
            if (isPetals) {
                if (x in 7..8 && y in 3..4) Color.rgb(60, 10, 10) // flower center
                else Color.rgb(225, 35, 35)
            } else if (isStem) {
                Color.rgb(55, 135, 35)
            } else {
                Color.TRANSPARENT
            }
        }

        // (1,2) Dandelion - Bright yellow blossom with green stem
        drawTile(1, 2) { x, y ->
            val isStem = (x == 7 || x == 8) && y in 8..15
            val isPetals = (x in 5..10 && y in 3..7)
            if (isPetals) {
                if (x in 7..8 && y in 4..5) Color.rgb(230, 170, 20)
                else Color.rgb(255, 225, 30)
            } else if (isStem) {
                Color.rgb(55, 135, 35)
            } else {
                Color.TRANSPARENT
            }
        }

        // (2,2) Birch Bark Side - Clean white wood with distinct black lenticel markings
        drawTile(2, 2) { x, y ->
            val isMark = (y == 3 && x in 4..8) || (y == 8 && x in 10..14) || (y == 12 && x in 2..5)
            if (isMark) {
                Color.rgb(40, 40, 40)
            } else {
                val v = ((x * 5 + y * 7) % 4) * 3
                Color.rgb(230 + v, 230 + v, 225 + v)
            }
        }

        // ROW 15 (bottom row): CRACK STAGES 0..9 for block breaking overlay
        for (stage in 0..9) {
            drawTile(stage, 15) { x, y ->
                // Procedural progressive spider-web fracture
                val crackThreshold = (stage + 1) * 3
                val distFromCenter = Math.abs(x - 7.5f) + Math.abs(y - 7.5f)
                val isLine = ((x == y || x + y == 15 || x == 7 || y == 7) && distFromCenter <= crackThreshold)
                val isBranch = ((x * 3 + y * 5) % 7 == 0 && distFromCenter <= crackThreshold + 2)
                if (isLine || isBranch) {
                    Color.argb(190, 0, 0, 0)
                } else {
                    Color.TRANSPARENT
                }
            }
        }

        return bitmap
    }

    /**
     * UV coordinates helper for a tile index (col, row)
     * Returns floatArrayOf(uMin, vMin, uMax, vMax)
     */
    fun getUVs(col: Int, row: Int): FloatArray {
        val tileSizeNorm = 1.0f / TILES_PER_ROW
        val uMin = col * tileSizeNorm
        val vMin = row * tileSizeNorm
        val uMax = uMin + tileSizeNorm
        val vMax = vMin + tileSizeNorm
        return floatArrayOf(uMin, vMin, uMax, vMax)
    }
}
