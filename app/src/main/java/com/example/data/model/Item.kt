package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class Item(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String,
    @ColumnInfo(name = "price_per_day")
    val pricePerDay: Long,
    @ColumnInfo(name = "stock_total")
    val stockTotal: Int,
    @ColumnInfo(name = "stock_available")
    val stockAvailable: Int,
    val description: String = ""
)
