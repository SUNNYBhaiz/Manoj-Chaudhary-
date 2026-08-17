package com.example.voxel.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.voxel.data.DatabaseProvider
import com.example.voxel.data.InventorySerializer
import com.example.voxel.data.WorldEntity
import com.example.voxel.data.WorldRepository
import com.example.voxel.engine.audio.SoundEngine
import com.example.voxel.engine.blocks.BlockType
import com.example.voxel.engine.physics.GameMode
import com.example.voxel.engine.physics.PlayerController
import com.example.voxel.engine.render.VoxelRenderer
import com.example.voxel.engine.world.World
import com.example.voxel.engine.world.WorldType
import com.example.voxel.model.CraftingManager
import com.example.voxel.model.ItemStack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Random

enum class AppTab {
    LOBBY,
    MARKET,
    SOCIAL,
    OPTIONS
}

data class GameSettings(
    val renderDistance: Int = 4, // 2 to 8 chunks
    val fov: Float = 75f, // 60 to 100
    val isDayNightCycle: Boolean = true,
    val soundVolume: Float = 0.8f,
    val musicVolume: Float = 0.5f,
    val isPerformanceEngineEnabled: Boolean = true,
    val touchSensitivity: Float = 1.0f
)

class VoxelViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DatabaseProvider.getDatabase(application)
    private val repository = WorldRepository(db.worldDao())

    val allWorlds: StateFlow<List<WorldEntity>> = repository.allWorlds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentTab = MutableStateFlow(AppTab.LOBBY)
    val currentTab = _currentTab.asStateFlow()

    private val _settings = MutableStateFlow(GameSettings())
    val settings = _settings.asStateFlow()

    // Active Game Session
    private val _isPlayingGame = MutableStateFlow(false)
    val isPlayingGame = _isPlayingGame.asStateFlow()

    private val _activeWorldEntity = MutableStateFlow<WorldEntity?>(null)
    val activeWorldEntity = _activeWorldEntity.asStateFlow()

    var activeWorld: World? = null
        private set
    var activePlayer: PlayerController? = null
        private set
    var activeRenderer: VoxelRenderer? = null
        private set

    val soundEngine = SoundEngine()

    // Hotbar & Selected Block
    private val _hotbar = MutableStateFlow(InventorySerializer.DEFAULT_HOTBAR)
    val hotbar = _hotbar.asStateFlow()

    private val _selectedHotbarIndex = MutableStateFlow(0)
    val selectedHotbarIndex = _selectedHotbarIndex.asStateFlow()

    // Crafting & Inventory State
    private val _isInventoryOpen = MutableStateFlow(false)
    val isInventoryOpen = _isInventoryOpen.asStateFlow()

    private val _isPauseMenuOpen = MutableStateFlow(false)
    val isPauseMenuOpen = _isPauseMenuOpen.asStateFlow()

    // 2x2 Crafting Grid
    val craftingGrid = MutableStateFlow<List<BlockType?>>(listOf(null, null, null, null))
    val craftingResult = MutableStateFlow<ItemStack?>(null)

    init {
        // Seed default sample worlds if database is empty
        viewModelScope.launch {
            allWorlds.collect { worlds ->
                if (worlds.isEmpty()) {
                    createWorld(
                        name = "Creative Sandbox",
                        seed = 133742L,
                        worldType = WorldType.PLAINS_AND_HILLS,
                        gameMode = GameMode.CREATIVE
                    )
                    createWorld(
                        name = "Survival: Emerald Peaks",
                        seed = 987654L,
                        worldType = WorldType.EXTREME_MOUNTAINS,
                        gameMode = GameMode.SURVIVAL
                    )
                }
            }
        }
    }

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun updateSettings(newSettings: GameSettings) {
        _settings.value = newSettings
        soundEngine.sfxVolume = newSettings.soundVolume
        soundEngine.musicVolume = newSettings.musicVolume
        activeRenderer?.renderDistanceChunks = newSettings.renderDistance
        activeRenderer?.updateFov(newSettings.fov)
        activeRenderer?.isDayNightCycleActive = newSettings.isDayNightCycle
    }

    fun togglePerformanceEngine() {
        val current = _settings.value
        val next = !current.isPerformanceEngineEnabled
        val renderDist = if (next) 3 else 5
        updateSettings(current.copy(isPerformanceEngineEnabled = next, renderDistance = renderDist))
    }

    fun createWorld(
        name: String,
        seed: Long = Random().nextLong(),
        worldType: WorldType = WorldType.PLAINS_AND_HILLS,
        gameMode: GameMode = GameMode.CREATIVE
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val entity = WorldEntity(
                name = name.ifBlank { "New World" },
                seed = seed,
                worldType = worldType.name,
                gameMode = gameMode.name,
                playerX = 8f,
                playerY = 36f,
                playerZ = 8f,
                playerYaw = 0f,
                playerPitch = 0f,
                playerHealth = 20,
                isFlying = (gameMode == GameMode.CREATIVE),
                selectedHotbarIndex = 0,
                inventoryData = InventorySerializer.serializeHotbar(InventorySerializer.DEFAULT_HOTBAR),
                craftingGridData = InventorySerializer.serializeCraftingGrid(listOf(null, null, null, null)),
                lastPlayedTimestamp = System.currentTimeMillis()
            )
            val id = repository.insertWorld(entity)
            launchGame(entity.copy(id = id))
        }
    }

    fun deleteWorld(world: WorldEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteWorld(world)
        }
    }

    fun launchGame(worldEntity: WorldEntity) {
        val wType = try {
            WorldType.valueOf(worldEntity.worldType)
        } catch (_: Exception) {
            WorldType.PLAINS_AND_HILLS
        }
        val gMode = try {
            GameMode.valueOf(worldEntity.gameMode)
        } catch (_: Exception) {
            GameMode.CREATIVE
        }

        val world = World(seed = worldEntity.seed, worldType = wType)
        val player = PlayerController(gameMode = gMode).apply {
            posX = worldEntity.playerX
            posY = worldEntity.playerY
            posZ = worldEntity.playerZ
            camera.yaw = worldEntity.playerYaw
            camera.pitch = worldEntity.playerPitch
            camera.updateView()
            health = worldEntity.playerHealth.coerceIn(1, 20)
            isFlying = worldEntity.isFlying
        }

        // Restore inventory & hotbar slots from Room persistence
        val loadedHotbar = InventorySerializer.deserializeHotbar(worldEntity.inventoryData)
        _hotbar.value = loadedHotbar
        _selectedHotbarIndex.value = worldEntity.selectedHotbarIndex.coerceIn(0, loadedHotbar.size - 1)

        // Restore crafting bench grid
        val loadedCraftingGrid = InventorySerializer.deserializeCraftingGrid(worldEntity.craftingGridData)
        craftingGrid.value = loadedCraftingGrid
        checkCrafting()

        // Apply saved modified blocks
        if (worldEntity.modifiedBlocksData.isNotBlank()) {
            val tokens = worldEntity.modifiedBlocksData.split(";")
            for (token in tokens) {
                val parts = token.split(",")
                if (parts.size == 4) {
                    val bx = parts[0].toIntOrNull() ?: continue
                    val by = parts[1].toIntOrNull() ?: continue
                    val bz = parts[2].toIntOrNull() ?: continue
                    val id = parts[3].toByteOrNull() ?: continue
                    val bt = BlockType.fromId(id)
                    world.modifiedBlocks[World.blockKey(bx, by, bz)] = bt.id
                }
            }
        }

        val renderer = VoxelRenderer(world, player).apply {
            renderDistanceChunks = _settings.value.renderDistance
            updateFov(_settings.value.fov)
            timeOfDay = worldEntity.timeOfDay
            isDayNightCycleActive = _settings.value.isDayNightCycle
        }

        // Connect audio events
        world.onBlockBreakListener = { _, _, _, block ->
            soundEngine.playBlockBreak(block.sound)
        }
        world.onBlockPlaceListener = { _, _, _, block ->
            soundEngine.playBlockPlace(block.sound)
        }
        world.onExplosionListener = { _, _, _, _ ->
            soundEngine.playExplosion()
        }

        activeWorld = world
        activePlayer = player
        activeRenderer = renderer
        _activeWorldEntity.value = worldEntity
        _isPauseMenuOpen.value = false
        _isInventoryOpen.value = false
        _isPlayingGame.value = true

        // Update last played timestamp in Room
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateWorld(worldEntity.copy(lastPlayedTimestamp = System.currentTimeMillis()))
        }
    }

    /**
     * Persists the active player coordinates, health, inventory state, crafting grid,
     * modified blocks, and time of day into the Room Database.
     */
    fun saveCurrentWorldState() {
        val entity = _activeWorldEntity.value ?: return
        val world = activeWorld ?: return
        val player = activePlayer ?: return
        val renderer = activeRenderer

        // Serialize modified blocks delta
        val sb = StringBuilder()
        var first = true
        for ((key, blockId) in world.modifiedBlocks) {
            val x = (key and 0x3FFFFFFL).toInt().let { if (it >= 0x2000000) it - 0x4000000 else it }
            val z = ((key shr 26) and 0x3FFFFFFL).toInt().let { if (it >= 0x2000000) it - 0x4000000 else it }
            val y = ((key shr 52) and 0xFFFL).toInt()
            if (!first) sb.append(";")
            sb.append("$x,$y,$z,$blockId")
            first = false
        }

        val serializedHotbar = InventorySerializer.serializeHotbar(_hotbar.value)
        val serializedCrafting = InventorySerializer.serializeCraftingGrid(craftingGrid.value)

        val updated = entity.copy(
            playerX = player.posX,
            playerY = player.posY,
            playerZ = player.posZ,
            playerYaw = player.camera.yaw,
            playerPitch = player.camera.pitch,
            playerHealth = player.health,
            isFlying = player.isFlying,
            selectedHotbarIndex = _selectedHotbarIndex.value,
            inventoryData = serializedHotbar,
            craftingGridData = serializedCrafting,
            timeOfDay = renderer?.timeOfDay ?: entity.timeOfDay,
            modifiedBlocksData = sb.toString(),
            lastPlayedTimestamp = System.currentTimeMillis()
        )

        _activeWorldEntity.value = updated

        viewModelScope.launch(Dispatchers.IO) {
            repository.updateWorld(updated)
        }
    }

    fun saveAndExitGame() {
        saveCurrentWorldState()
        _isPlayingGame.value = false
        _isPauseMenuOpen.value = false
        _isInventoryOpen.value = false
    }

    fun selectHotbarSlot(index: Int) {
        if (index in 0 until _hotbar.value.size) {
            _selectedHotbarIndex.value = index
            // Persist hotbar selection state
            saveCurrentWorldState()
        }
    }

    fun setHotbarSlot(index: Int, blockType: BlockType) {
        val list = _hotbar.value.toMutableList()
        if (index in list.indices) {
            list[index] = blockType
            _hotbar.value = list
            // Persist inventory slot change
            saveCurrentWorldState()
        }
    }

    fun toggleInventory() {
        _isInventoryOpen.value = !_isInventoryOpen.value
        if (!_isInventoryOpen.value) {
            // Save state when inventory dialog closes
            saveCurrentWorldState()
        }
    }

    fun togglePauseMenu() {
        _isPauseMenuOpen.value = !_isPauseMenuOpen.value
        if (_isPauseMenuOpen.value) {
            // Save state upon pause
            saveCurrentWorldState()
        }
    }

    fun setCraftingSlot(slotIndex: Int, block: BlockType?) {
        val current = craftingGrid.value.toMutableList()
        current[slotIndex] = block
        craftingGrid.value = current
        checkCrafting()
        saveCurrentWorldState()
    }

    private fun checkCrafting() {
        val grid = craftingGrid.value
        val grid2x2 = listOf(
            listOf(grid[0], grid[1]),
            listOf(grid[2], grid[3])
        )
        craftingResult.value = CraftingManager.findRecipe(grid2x2)
    }

    fun takeCraftingResult() {
        val res = craftingResult.value ?: return
        // Put result in current hotbar slot
        setHotbarSlot(_selectedHotbarIndex.value, res.blockType)
        // Clear crafting grid
        craftingGrid.value = listOf(null, null, null, null)
        craftingResult.value = null
        soundEngine.playBlockPlace(res.blockType.sound)
        saveCurrentWorldState()
    }

    override fun onCleared() {
        super.onCleared()
        saveCurrentWorldState()
        soundEngine.release()
    }
}

