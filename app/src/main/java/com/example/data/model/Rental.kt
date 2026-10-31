package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rentals")
data class Rental(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "customer_name")
    val customerName: String,
    @ColumnInfo(name = "customer_phone")
    val customerPhone: String,
    @ColumnInfo(name = "start_date")
    val startDate: Long,
    @ColumnInfo(name = "end_date")
    val endDate: Long,
    @ColumnInfo(name = "duration_days")
    val durationDays: Int,
    @ColumnInfo(name = "total_price")
    val totalPrice: Long,
    @ColumnInfo(name = "dp_amount")
    val dpAmount: Long,
    @ColumnInfo(name = "remaining_balance")
    val remainingBalance: Long,
    @ColumnInfo(name = "guarantee_item")
    val guaranteeItem: String, // e.g. KTP, SIM, Paspor
    @ColumnInfo(name = "payment_status")
    val paymentStatus: String, // "Unpaid", "Partial", "Paid"
    @ColumnInfo(name = "rental_status")
    val rentalStatus: String, // "Booked", "Rented", "Completed", "Overdue"
    @ColumnInfo(name = "late_fee_amount")
    val lateFeeAmount: Long = 0L,
    @ColumnInfo(name = "is_stock_returned")
    val isStockReturned: Boolean = false,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    val notes: String = ""
) {
    val grandTotal: Long
        get() = totalPrice + lateFeeAmount

    val effectiveRemainingBalance: Long
        get() = (grandTotal - dpAmount).coerceAtLeast(0L)
}
