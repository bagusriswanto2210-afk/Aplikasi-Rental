package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.WhatsAppMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface WhatsAppMessageDao {
    @Query("SELECT * FROM whatsapp_messages ORDER BY is_sent ASC, scheduled_date ASC")
    fun getAllMessages(): Flow<List<WhatsAppMessage>>

    @Query("SELECT * FROM whatsapp_messages WHERE is_sent = 0 ORDER BY scheduled_date ASC")
    fun getPendingMessages(): Flow<List<WhatsAppMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: WhatsAppMessage): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<WhatsAppMessage>)

    @Update
    suspend fun updateMessage(message: WhatsAppMessage)

    @Query("UPDATE whatsapp_messages SET is_sent = 1, sent_at = :sentAt WHERE id = :id")
    suspend fun markAsSent(id: Long, sentAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM whatsapp_messages WHERE id = :id")
    suspend fun deleteMessage(id: Long)

    @Query("DELETE FROM whatsapp_messages WHERE is_sent = 1")
    suspend fun clearSentMessages()

    @Query("SELECT COUNT(*) FROM whatsapp_messages WHERE rental_id = :rentalId AND message_type = :type AND is_sent = 0")
    suspend fun countPendingForRental(rentalId: Long, type: String): Int
}
