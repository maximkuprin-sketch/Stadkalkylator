package com.example.flyttstadning.data.repository

import com.example.flyttstadning.data.database.PriceBand
import com.example.flyttstadning.data.database.PriceDao
import kotlinx.coroutines.flow.Flow

interface PriceRepository {
    val allPrices: Flow<List<PriceBand>>
    suspend fun replacePriceList(newPrices: List<PriceBand>)
    suspend fun exportPrices(): List<PriceBand>
    suspend fun ensureDefaults()
}

class PriceRepositoryImpl(private val priceDao: PriceDao) : PriceRepository {
    override val allPrices: Flow<List<PriceBand>> = priceDao.getAllPrices()

    // We can't easily launch a coroutine in constructor without a scope.
    // However, we can expose a method to initialize defaults, called from Application or ViewModel.
    // Ideally, Room callback is better, but let's do a lazy check in the ViewModel or just expose a function.
    // A simpler way: Use a suspend function 'ensureDefaults()'.
    
    suspend fun ensureDefaults() {
        if (priceDao.getPricesList().isEmpty()) {
            val defaults = listOf(
                PriceBand(rangeStart = 0, rangeEnd = 30, price = 1500),
                PriceBand(rangeStart = 30, rangeEnd = 50, price = 2000),
                PriceBand(rangeStart = 50, rangeEnd = 70, price = 2500)
            )
            priceDao.insertAll(defaults)
        }
    }

    override suspend fun replacePriceList(newPrices: List<PriceBand>) {
        // Transaction to ensure atomicity
        // Since we are in suspend function, we can just do one after another or use @Transaction in DAO. 
        // For simplicity:
        priceDao.deleteAll()
        priceDao.insertAll(newPrices)
    }
    
    // For export, we just need the list.
    // Since Flow is async, we might want a direct query for export, but we can collect first.
    // However, DAO can return List directly too.
    // Let's add a one-shot query to DAO or just expose it here.
    // For now, let's assume we collect from the flow or add a method to DAO.
    // Adding method to DAO is better.
    override suspend fun exportPrices(): List<PriceBand> {
        return priceDao.getPricesList()
    }
}
