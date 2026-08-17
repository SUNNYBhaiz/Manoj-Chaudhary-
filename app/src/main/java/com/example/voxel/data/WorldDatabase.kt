package com.example.voxel.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.voxel.engine.blocks.BlockType
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "worlds")
data class WorldEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val seed: Long,
    val worldType: String,
    val gameMode: String,
    val playerX: Float = 8f,
    val playerY: Float = 36f,
    val playerZ: Float = 8f,
    val playerYaw: Float = 0f,
    val playerPitch: Float = 0f,
    val playerHealth: Int = 20,
    val isFlying: Boolean = false,
    val selectedHotbarIndex: Int = 0,
    val inventoryData: String = "", // Comma-separated BlockType enum names for hotbar slots (9 items)
    val craftingGridData: String = "", // Comma-separated 4 slots (AIR or BlockType name)
    val modifiedBlocksData: String = "", // Compact delta string: "x,y,z,id;x,y,z,id"
    val timeOfDay: Float = 0.25f,
    val lastPlayedTimestamp: Long = System.currentTimeMillis()
)

object InventorySerializer {
    val DEFAULT_HOTBAR = listOf(
        BlockType.GRASS,
        BlockType.DIRT,
        BlockType.STONE,
        BlockType.COBBLESTONE,
        BlockType.WOOD_PLANKS,
        BlockType.BRICK,
        BlockType.GLASS,
        BlockType.TORCH,
        BlockType.TNT
    )

    fun serializeHotbar(hotbar: List<BlockType>): String {
        return hotbar.joinToString(",") { it.name }
    }

    fun deserializeHotbar(data: String): List<BlockType> {
        if (data.isBlank()) return DEFAULT_HOTBAR
        val parsed = data.split(",").mapNotNull { name ->
            try {
                BlockType.valueOf(name.trim())
            } catch (_: Exception) {
                null
            }
        }
        return if (parsed.size == 9) parsed else DEFAULT_HOTBAR
    }

    fun serializeCraftingGrid(grid: List<BlockType?>): String {
        return grid.joinToString(",") { it?.name ?: "AIR" }
    }

    fun deserializeCraftingGrid(data: String): List<BlockType?> {
        if (data.isBlank()) return listOf(null, null, null, null)
        val list = data.split(",").map { name ->
            val trimmed = name.trim()
            if (trimmed == "AIR" || trimmed == "null" || trimmed.isEmpty()) {
                null
            } else {
                try {
                    BlockType.valueOf(trimmed)
                } catch (_: Exception) {
                    null
                }
            }
        }
        return if (list.size == 4) list else listOf(null, null, null, null)
    }
}

@Dao
interface WorldDao {
    @Query("SELECT * FROM worlds ORDER BY lastPlayedTimestamp DESC")
    fun getAllWorlds(): Flow<List<WorldEntity>>

    @Query("SELECT * FROM worlds WHERE id = :id")
    suspend fun getWorldById(id: Long): WorldEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorld(world: WorldEntity): Long

    @Update
    suspend fun updateWorld(world: WorldEntity)

    @Delete
    suspend fun deleteWorld(world: WorldEntity)

    @Query("DELETE FROM worlds WHERE id = :id")
    suspend fun deleteWorldById(id: Long)
}

@Database(entities = [WorldEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun worldDao(): WorldDao
}

