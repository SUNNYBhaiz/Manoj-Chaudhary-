package com.example.voxel.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class WorldRepository(private val worldDao: WorldDao) {

    val allWorlds: Flow<List<WorldEntity>> = worldDao.getAllWorlds()

    suspend fun getWorldById(id: Long): WorldEntity? = withContext(Dispatchers.IO) {
        worldDao.getWorldById(id)
    }

    suspend fun insertWorld(world: WorldEntity): Long = withContext(Dispatchers.IO) {
        worldDao.insertWorld(world)
    }

    suspend fun updateWorld(world: WorldEntity) = withContext(Dispatchers.IO) {
        worldDao.updateWorld(world)
    }

    suspend fun deleteWorld(world: WorldEntity) = withContext(Dispatchers.IO) {
        worldDao.deleteWorld(world)
    }

    suspend fun deleteWorldById(id: Long) = withContext(Dispatchers.IO) {
        worldDao.deleteWorldById(id)
    }
}
