package com.example.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class RentalWithDetails(
    @Embedded
    val rental: Rental,

    @Relation(
        parentColumn = "id",
        entityColumn = "rental_id"
    )
    val items: List<RentalItem>
)
