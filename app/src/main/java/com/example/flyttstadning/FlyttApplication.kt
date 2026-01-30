package com.example.flyttstadning

import android.app.Application
import com.example.flyttstadning.data.database.AppDatabase
import com.example.flyttstadning.data.database.PriceDao
import com.example.flyttstadning.data.repository.PriceRepository
import com.example.flyttstadning.data.repository.PriceRepositoryImpl

class FlyttApplication : Application() {
    // Manual dependency injection
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

class AppContainer(context: Application) {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(context) }
    val priceDao: PriceDao by lazy { database.priceDao() }
    val priceRepository: PriceRepository by lazy { PriceRepositoryImpl(priceDao) }
    val preferencesRepository: PreferencesRepository by lazy { PreferencesRepository(context) }
}
