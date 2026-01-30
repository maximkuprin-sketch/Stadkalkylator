package com.example.flyttstadning.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PriceDao {
    @Query("SELECT * FROM price_bands ORDER BY rangeStart ASC")
    fun getAllPrices(): Flow<List<PriceBand>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(prices: List<PriceBand>)

    @Query("DELETE FROM price_bands")
    suspend fun deleteAll()

    @Query("SELECT * FROM price_bands ORDER BY rangeStart ASC")
    suspend fun getPricesList(): List<PriceBand>
}
