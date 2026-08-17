package com.example.voxel.engine.blocks

enum class BlockSound {
    GRASS,
    STONE,
    WOOD,
    SAND,
    GLASS,
    WATER,
    WOOL,
    METAL,
    TNT
}

enum class BlockRenderType {
    OPAQUE,
    TRANSPARENT,
    TRANSLUCENT, // water
    CROSS // flowers, saplings
}

enum class BlockType(
    val id: Byte,
    val displayName: String,
    val renderType: BlockRenderType,
    val isSolid: Boolean,
    val hardness: Float, // time to break in seconds
    val lightEmission: Int = 0,
    val sound: BlockSound = BlockSound.STONE,
    // Texture Atlas coordinates (col, row) 0..15 in a 16x16 grid
    val topTexX: Int = 0,
    val topTexY: Int = 0,
    val sideTexX: Int = 0,
    val sideTexY: Int = 0,
    val bottomTexX: Int = 0,
    val bottomTexY: Int = 0,
    val dropItem: Byte = id
) {
    AIR(
        id = 0,
        displayName = "Air",
        renderType = BlockRenderType.TRANSPARENT,
        isSolid = false,
        hardness = 0f
    ),
    GRASS(
        id = 1,
        displayName = "Grass Block",
        renderType = BlockRenderType.OPAQUE,
        isSolid = true,
        hardness = 0.6f,
        sound = BlockSound.GRASS,
        topTexX = 0, topTexY = 0, // grass top
        sideTexX = 1, sideTexY = 0, // grass side
        bottomTexX = 2, bottomTexY = 0, // dirt
        dropItem = 2 // drops dirt
    ),
    DIRT(
        id = 2,
        displayName = "Dirt",
        renderType = BlockRenderType.OPAQUE,
        isSolid = true,
        hardness = 0.5f,
        sound = BlockSound.GRASS,
        topTexX = 2, topTexY = 0,
        sideTexX = 2, sideTexY = 0,
        bottomTexX = 2, bottomTexY = 0
    ),
    STONE(
        id = 3,
        displayName = "Stone",
        renderType = BlockRenderType.OPAQUE,
        isSolid = true,
        hardness = 1.5f,
        sound = BlockSound.STONE,
        topTexX = 3, topTexY = 0,
        sideTexX = 3, sideTexY = 0,
        bottomTexX = 3, bottomTexY = 0,
        dropItem = 4 // drops cobblestone
    ),
    COBBLESTONE(
        id = 4,
        displayName = "Cobblestone",
        renderType = BlockRenderType.OPAQUE,
        isSolid = true,
        hardness = 1.8f,
        sound = BlockSound.STONE,
        topTexX = 4, topTexY = 0,
        sideTexX = 4, sideTexY = 0,
        bottomTexX = 4, bottomTexY = 0
    ),
    WOOD_OAK(
        id = 5,
        displayName = "Oak Wood Log",
        renderType = BlockRenderType.OPAQUE,
        isSolid = true,
        hardness = 1.2f,
        sound = BlockSound.WOOD,
        topTexX = 6, topTexY = 0, // rings
        sideTexX = 5, sideTexY = 0, // bark
        bottomTexX = 6, bottomTexY = 0
    ),
    WOOD_PLANKS(
        id = 6,
        displayName = "Oak Planks",
        renderType = BlockRenderType.OPAQUE,
        isSolid = true,
        hardness = 1.0f,
        sound = BlockSound.WOOD,
        topTexX = 7, topTexY = 0,
        sideTexX = 7, sideTexY = 0,
        bottomTexX = 7, bottomTexY = 0
    ),
    LEAVES(
        id = 7,
        displayName = "Oak Leaves",
        renderType = BlockRenderType.TRANSPARENT,
        isSolid = true,
        hardness = 0.2f,
        sound = BlockSound.GRASS,
        topTexX = 8, topTexY = 0,
        sideTexX = 8, sideTexY = 0,
        bottomTexX = 8, bottomTexY = 0,
        dropItem = 0 // small chance or sapling/air
    ),
    SAND(
        id = 8,
        displayName = "Sand",
        renderType = BlockRenderType.OPAQUE,
        isSolid = true,
        hardness = 0.5f,
        sound = BlockSound.SAND,
        topTexX = 9, topTexY = 0,
        sideTexX = 9, sideTexY = 0,
        bottomTexX = 9, bottomTexY = 0
    ),
    GLASS(
        id = 9,
        displayName = "Glass",
        renderType = BlockRenderType.TRANSPARENT,
        isSolid = true,
        hardness = 0.3f,
        sound = BlockSound.GLASS,
        topTexX = 10, topTexY = 0,
        sideTexX = 10, sideTexY = 0,
        bottomTexX = 10, bottomTexY = 0,
        dropItem = 0
    ),
    WATER(
        id = 10,
        displayName = "Water",
        renderType = BlockRenderType.TRANSLUCENT,
        isSolid = false,
        hardness = 100f,
        sound = BlockSound.WATER,
        topTexX = 11, topTexY = 0,
        sideTexX = 11, sideTexY = 0,
        bottomTexX = 11, bottomTexY = 0,
        dropItem = 0
    ),
    BEDROCK(
        id = 11,
        displayName = "Bedrock",
        renderType = BlockRenderType.OPAQUE,
        isSolid = true,
        hardness = Float.POSITIVE_INFINITY,
        sound = BlockSound.STONE,
        topTexX = 12, topTexY = 0,
        sideTexX = 12, sideTexY = 0,
        bottomTexX = 12, bottomTexY = 0
    ),
    COAL_ORE(
        id = 12,
        displayName = "Coal Ore",
        renderType = BlockRenderType.OPAQUE,
        isSolid = true,
        hardness = 2.0f,
        sound = BlockSound.STONE,
        topTexX = 13, topTexY = 0,
        sideTexX = 13, sideTexY = 0,
        bottomTexX = 13, bottomTexY = 0
    ),
    IRON_ORE(
        id = 13,
        displayName = "Iron Ore",
        renderType = BlockRenderType.OPAQUE,
        isSolid = true,
        hardness = 2.5f,
        sound = BlockSound.STONE,
        topTexX = 14, topTexY = 0,
        sideTexX = 14, sideTexY = 0,
        bottomTexX = 14, bottomTexY = 0
    ),
    GOLD_ORE(
        id = 14,
        displayName = "Gold Ore",
        renderType = BlockRenderType.OPAQUE,
        isSolid = true,
        hardness = 2.5f,
        sound = BlockSound.STONE,
        topTexX = 15, topTexY = 0,
        sideTexX = 15, sideTexY = 0,
        bottomTexX = 15, bottomTexY = 0
    ),
    DIAMOND_ORE(
        id = 15,
        displayName = "Diamond Ore",
        renderType = BlockRenderType.OPAQUE,
        isSolid = true,
        hardness = 3.0f,
        sound = BlockSound.STONE,
        topTexX = 0, topTexY = 1,
        sideTexX = 0, sideTexY = 1,
        bottomTexX = 0, bottomTexY = 1
    ),
    BRICK(
        id = 16,
        displayName = "Bricks",
        renderType = BlockRenderType.OPAQUE,
        isSolid = true,
        hardness = 2.0f,
        sound = BlockSound.STONE,
        topTexX = 1, topTexY = 1,
        sideTexX = 1, sideTexY = 1,
        bottomTexX = 1, bottomTexY = 1
    ),
    BOOKSHELF(
        id = 17,
        displayName = "Bookshelf",
        renderType = BlockRenderType.OPAQUE,
        isSolid = true,
        hardness = 1.2f,
        sound = BlockSound.WOOD,
        topTexX = 7, topTexY = 0, // wood top/bottom
        sideTexX = 2, sideTexY = 1, // books
        bottomTexX = 7, bottomTexY = 0
    ),
    CRAFTING_TABLE(
        id = 18,
        displayName = "Crafting Table",
        renderType = BlockRenderType.OPAQUE,
        isSolid = true,
        hardness = 1.5f,
        sound = BlockSound.WOOD,
        topTexX = 3, topTexY = 1, // top grid
        sideTexX = 4, sideTexY = 1, // side tools
        bottomTexX = 7, bottomTexY = 0 // wood bottom
    ),
    FURNACE(
        id = 19,
        displayName = "Furnace",
        renderType = BlockRenderType.OPAQUE,
        isSolid = true,
        hardness = 2.0f,
        sound = BlockSound.STONE,
        topTexX = 5, topTexY = 1,
        sideTexX = 6, sideTexY = 1,
        bottomTexX = 5, bottomTexY = 1
    ),
    TNT(
        id = 20,
        displayName = "TNT Explosive",
        renderType = BlockRenderType.OPAQUE,
        isSolid = true,
        hardness = 0.1f,
        sound = BlockSound.TNT,
        topTexX = 7, topTexY = 1,
        sideTexX = 8, sideTexY = 1,
        bottomTexX = 9, bottomTexY = 1
    ),
    TORCH(
        id = 21,
        displayName = "Torch",
        renderType = BlockRenderType.CROSS,
        isSolid = false,
        hardness = 0.05f,
        lightEmission = 14,
        sound = BlockSound.WOOD,
        topTexX = 10, topTexY = 1,
        sideTexX = 10, sideTexY = 1,
        bottomTexX = 10, bottomTexY = 1
    ),
    SNOW(
        id = 22,
        displayName = "Snow Block",
        renderType = BlockRenderType.OPAQUE,
        isSolid = true,
        hardness = 0.3f,
        sound = BlockSound.GRASS,
        topTexX = 11, topTexY = 1,
        sideTexX = 11, sideTexY = 1,
        bottomTexX = 11, bottomTexY = 1
    ),
    CACTUS(
        id = 23,
        displayName = "Cactus",
        renderType = BlockRenderType.OPAQUE,
        isSolid = true,
        hardness = 0.4f,
        sound = BlockSound.GRASS,
        topTexX = 12, topTexY = 1,
        sideTexX = 13, sideTexY = 1,
        bottomTexX = 12, bottomTexY = 1
    ),
    GLOWSTONE(
        id = 24,
        displayName = "Glowstone",
        renderType = BlockRenderType.OPAQUE,
        isSolid = true,
        hardness = 0.5f,
        lightEmission = 15,
        sound = BlockSound.GLASS,
        topTexX = 14, topTexY = 1,
        sideTexX = 14, sideTexY = 1,
        bottomTexX = 14, bottomTexY = 1
    ),
    OBSIDIAN(
        id = 25,
        displayName = "Obsidian",
        renderType = BlockRenderType.OPAQUE,
        isSolid = true,
        hardness = 6.0f,
        sound = BlockSound.STONE,
        topTexX = 15, topTexY = 1,
        sideTexX = 15, sideTexY = 1,
        bottomTexX = 15, bottomTexY = 1
    ),
    FLOWER_RED(
        id = 26,
        displayName = "Poppy Flower",
        renderType = BlockRenderType.CROSS,
        isSolid = false,
        hardness = 0.05f,
        sound = BlockSound.GRASS,
        topTexX = 0, topTexY = 2,
        sideTexX = 0, sideTexY = 2,
        bottomTexX = 0, bottomTexY = 2
    ),
    FLOWER_YELLOW(
        id = 27,
        displayName = "Dandelion",
        renderType = BlockRenderType.CROSS,
        isSolid = false,
        hardness = 0.05f,
        sound = BlockSound.GRASS,
        topTexX = 1, topTexY = 2,
        sideTexX = 1, sideTexY = 2,
        bottomTexX = 1, bottomTexY = 2
    ),
    WOOD_BIRCH(
        id = 28,
        displayName = "Birch Log",
        renderType = BlockRenderType.OPAQUE,
        isSolid = true,
        hardness = 1.2f,
        sound = BlockSound.WOOD,
        topTexX = 6, topTexY = 0,
        sideTexX = 2, sideTexY = 2, // birch white bark with black spots
        bottomTexX = 6, bottomTexY = 0
    );

    companion object {
        private val ID_MAP = entries.associateBy { it.id }

        fun fromId(id: Byte): BlockType = ID_MAP[id] ?: AIR

        val CREATIVE_INVENTORY = entries.filter { it != AIR && it != BEDROCK }
    }
}
