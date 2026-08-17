package com.example.voxel.data

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    @Volatile
    private var instance: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "voxel_worlds.db"
            ).fallbackToDestructiveMigration().build().also { instance = it }
        }
    }
}
