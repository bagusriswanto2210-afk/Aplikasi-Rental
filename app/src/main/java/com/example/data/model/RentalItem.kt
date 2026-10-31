package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rental_items",
    foreignKeys = [
        ForeignKey(
            entity = Rental::class,
            parentColumns = ["id"],
            childColumns = ["rental_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Item::class,
            parentColumns = ["id"],
            childColumns = ["item_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["rental_id"]),
        Index(value = ["item_id"])
    ]
)
data class RentalItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "rental_id")
    val rentalId: Long,
    @ColumnInfo(name = "item_id")
    val itemId: Long,
    @ColumnInfo(name = "item_name")
    val itemName: String,
    @ColumnInfo(name = "price_per_day")
    val pricePerDay: Long,
    val qty: Int,
    val subtotal: Long
)
