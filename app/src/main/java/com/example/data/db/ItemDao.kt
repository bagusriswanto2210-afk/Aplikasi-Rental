package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Item
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM items ORDER BY category ASC, name ASC")
    fun getAllItems(): Flow<List<Item>>

    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun getItemById(id: Long): Item?

    @Query("SELECT * FROM items WHERE category = :category ORDER BY name ASC")
    fun getItemsByCategory(category: String): Flow<List<Item>>

    @Query("SELECT * FROM items WHERE name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchItems(query: String): Flow<List<Item>>

    @Query("SELECT COUNT(*) FROM items")
    suspend fun getItemsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: Item): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<Item>)

    @Update
    suspend fun updateItem(item: Item)

    @Delete
    suspend fun deleteItem(item: Item)

    @Query("UPDATE items SET stock_available = MAX(0, stock_available - :qty) WHERE id = :itemId")
    suspend fun decrementAvailableStock(itemId: Long, qty: Int)

    @Query("UPDATE items SET stock_available = MIN(stock_total, stock_available + :qty) WHERE id = :itemId")
    suspend fun incrementAvailableStock(itemId: Long, qty: Int)
}
