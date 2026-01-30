package com.example.flyttstadning.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "price_bands")
data class PriceBand(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val rangeStart: Int, // Inclusive
    val rangeEnd: Int,   // Inclusive
    val price: Int
) {
    override fun toString(): String {
        return "$rangeStart-$rangeEnd: $price kr"
    }

    // Example: "30-35" -> rangeStart=30, rangeEnd=35 (interpreted as (30, 35] by business logic, 
    // but simplified storage here. We will assume business logic handles the "inclusive/exclusive" interpretation.
    // Actually user requirement: "30-35" meaning (30,35].
    // "0-30" interpreted as <= 30.
    // We'll store exactly what lines define.
}
