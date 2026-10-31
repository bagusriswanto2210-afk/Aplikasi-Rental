package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "whatsapp_messages")
data class WhatsAppMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "rental_id")
    val rentalId: Long,
    @ColumnInfo(name = "customer_name")
    val customerName: String,
    @ColumnInfo(name = "customer_phone")
    val customerPhone: String,
    @ColumnInfo(name = "message_type")
    val messageType: String, // "CONFIRMATION_DP", "RETURN_REMINDER", "OVERDUE_ALERT", "SETTLEMENT_RECEIPT"
    @ColumnInfo(name = "message_content")
    val messageContent: String,
    @ColumnInfo(name = "scheduled_date")
    val scheduledDate: Long,
    @ColumnInfo(name = "is_sent")
    val isSent: Boolean = false,
    @ColumnInfo(name = "sent_at")
    val sentAt: Long? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
