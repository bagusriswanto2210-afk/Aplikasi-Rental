package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.Rental
import com.example.data.model.RentalItem
import com.example.data.model.RentalWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface RentalDao {
    @Transaction
    @Query("SELECT * FROM rentals ORDER BY created_at DESC")
    fun getAllRentalsWithDetails(): Flow<List<RentalWithDetails>>

    @Transaction
    @Query("SELECT * FROM rentals WHERE id = :id")
    suspend fun getRentalWithDetailsById(id: Long): RentalWithDetails?

    @Transaction
    @Query("SELECT * FROM rentals WHERE rental_status = :status ORDER BY created_at DESC")
    fun getRentalsByStatus(status: String): Flow<List<RentalWithDetails>>

    @Transaction
    @Query("SELECT * FROM rentals WHERE rental_status != 'Completed' AND end_date < :currentDate ORDER BY end_date ASC")
    suspend fun getOverdueRentals(currentDate: Long): List<RentalWithDetails>

    @Transaction
    @Query("SELECT * FROM rentals WHERE rental_status IN ('Booked', 'Rented') AND start_date <= :dateEnd AND end_date >= :dateStart")
    suspend fun getRentalsForDateRange(dateStart: Long, dateEnd: Long): List<RentalWithDetails>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRental(rental: Rental): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRentalItems(items: List<RentalItem>)

    @Update
    suspend fun updateRental(rental: Rental)

    @Delete
    suspend fun deleteRental(rental: Rental)

    @Query("UPDATE rentals SET rental_status = :status WHERE id = :rentalId")
    suspend fun updateRentalStatus(rentalId: Long, status: String)

    @Query("UPDATE rentals SET dp_amount = :dpAmount, remaining_balance = :remainingBalance, payment_status = :paymentStatus WHERE id = :rentalId")
    suspend fun updatePayment(rentalId: Long, dpAmount: Long, remainingBalance: Long, paymentStatus: String)

    @Query("UPDATE rentals SET late_fee_amount = :lateFee, remaining_balance = :remainingBalance, payment_status = :paymentStatus WHERE id = :rentalId")
    suspend fun updateLateFee(rentalId: Long, lateFee: Long, remainingBalance: Long, paymentStatus: String)

    @Query("UPDATE rentals SET rental_status = 'Completed', is_stock_returned = 1 WHERE id = :rentalId")
    suspend fun markStockReturnedAndComplete(rentalId: Long)

    @Query("SELECT * FROM rental_items WHERE rental_id = :rentalId")
    suspend fun getItemsForRental(rentalId: Long): List<RentalItem>
}
