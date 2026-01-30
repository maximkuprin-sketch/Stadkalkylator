package com.example.flyttstadning.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PriceBand::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun priceDao(): PriceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "flyttstadning_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
