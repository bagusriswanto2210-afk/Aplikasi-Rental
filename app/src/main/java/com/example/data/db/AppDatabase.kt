package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.Item
import com.example.data.model.Rental
import com.example.data.model.RentalItem
import com.example.data.model.WhatsAppMessage

@Database(
    entities = [
        Item::class,
        Rental::class,
        RentalItem::class,
        WhatsAppMessage::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun rentalDao(): RentalDao
    abstract fun whatsAppMessageDao(): WhatsAppMessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "outdoor_rental_pos.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
