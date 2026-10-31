package com.example.data.repository

import com.example.data.db.ItemDao
import com.example.data.db.RentalDao
import com.example.data.db.WhatsAppMessageDao
import com.example.data.model.Item
import com.example.data.model.Rental
import com.example.data.model.RentalItem
import com.example.data.model.RentalWithDetails
import com.example.data.model.WhatsAppMessage
import com.example.util.DateUtils
import com.example.util.WhatsAppHelper
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

class RentalRepository(
    private val itemDao: ItemDao,
    private val rentalDao: RentalDao,
    private val whatsAppMessageDao: WhatsAppMessageDao
) {
    val allItems: Flow<List<Item>> = itemDao.getAllItems()
    val allRentals: Flow<List<RentalWithDetails>> = rentalDao.getAllRentalsWithDetails()
    val allWhatsAppMessages: Flow<List<WhatsAppMessage>> = whatsAppMessageDao.getAllMessages()
    val pendingWhatsAppMessages: Flow<List<WhatsAppMessage>> = whatsAppMessageDao.getPendingMessages()

    suspend fun insertItem(item: Item): Long = itemDao.insertItem(item)
    suspend fun updateItem(item: Item) = itemDao.updateItem(item)
    suspend fun deleteItem(item: Item) = itemDao.deleteItem(item)

    fun searchItems(query: String): Flow<List<Item>> = itemDao.searchItems(query)
    fun getItemsByCategory(category: String): Flow<List<Item>> = itemDao.getItemsByCategory(category)
    fun getRentalsByStatus(status: String): Flow<List<RentalWithDetails>> = rentalDao.getRentalsByStatus(status)

    suspend fun createRentalTransaction(
        customerName: String,
        customerPhone: String,
        startDate: Long,
        endDate: Long,
        durationDays: Int,
        totalPrice: Long,
        dpAmount: Long,
        remainingBalance: Long,
        guaranteeItem: String,
        notes: String,
        selectedItems: List<Pair<Item, Int>> // Item to Qty
    ): Long {
        val paymentStatus = when {
            remainingBalance <= 0L -> "Paid"
            dpAmount > 0L -> "Partial"
            else -> "Unpaid"
        }

        val initialRentalStatus = "Booked"

        val rental = Rental(
            customerName = customerName,
            customerPhone = customerPhone,
            startDate = startDate,
            endDate = endDate,
            durationDays = durationDays,
            totalPrice = totalPrice,
            dpAmount = dpAmount,
            remainingBalance = remainingBalance,
            guaranteeItem = guaranteeItem,
            paymentStatus = paymentStatus,
            rentalStatus = initialRentalStatus,
            notes = notes
        )

        val rentalId = rentalDao.insertRental(rental)

        val rentalItems = selectedItems.map { (item, qty) ->
            RentalItem(
                rentalId = rentalId,
                itemId = item.id,
                itemName = item.name,
                pricePerDay = item.pricePerDay,
                qty = qty,
                subtotal = item.pricePerDay * qty * durationDays
            )
        }
        rentalDao.insertRentalItems(rentalItems)

        // Decrement stock_available in items table automatically
        selectedItems.forEach { (item, qty) ->
            itemDao.decrementAvailableStock(item.id, qty)
        }

        // Generate and queue confirmation WhatsApp message
        val savedRentalWithDetails = rentalDao.getRentalWithDetailsById(rentalId)
        if (savedRentalWithDetails != null) {
            val confirmationMsg = WhatsAppHelper.generateBookingConfirmationMessage(savedRentalWithDetails)
            whatsAppMessageDao.insertMessage(
                WhatsAppMessage(
                    rentalId = rentalId,
                    customerName = customerName,
                    customerPhone = customerPhone,
                    messageType = "CONFIRMATION_DP",
                    messageContent = confirmationMsg,
                    scheduledDate = System.currentTimeMillis(),
                    isSent = false
                )
            )
        }

        return rentalId
    }

    suspend fun markAsRented(rentalId: Long) {
        rentalDao.updateRentalStatus(rentalId, "Rented")
    }

    suspend fun completeRental(rentalId: Long) {
        val rentalWithDetails = rentalDao.getRentalWithDetailsById(rentalId) ?: return
        val rental = rentalWithDetails.rental

        // Idempotent: Only restore stock if not already returned and not already Completed
        if (!rental.isStockReturned && rental.rentalStatus != "Completed") {
            rentalWithDetails.items.forEach { rentalItem ->
                itemDao.incrementAvailableStock(rentalItem.itemId, rentalItem.qty)
            }
            rentalDao.markStockReturnedAndComplete(rentalId)
        }
    }

    suspend fun settlePayment(rentalId: Long) {
        val rentalWithDetails = rentalDao.getRentalWithDetailsById(rentalId) ?: return
        val rental = rentalWithDetails.rental
        val totalToPay = rental.totalPrice + rental.lateFeeAmount
        rentalDao.updatePayment(
            rentalId = rentalId,
            dpAmount = totalToPay,
            remainingBalance = 0L,
            paymentStatus = "Paid"
        )
    }

    fun calculateLateFee(endDate: Long, dailyRentalRate: Long): Pair<Int, Long> {
        val currentTime = System.currentTimeMillis()
        val endDay = DateUtils.getEndOfDay(endDate)
        if (currentTime <= endDay) return Pair(0, 0L)
        val diffMillis = currentTime - endDay
        val overdueDays = (TimeUnit.MILLISECONDS.toDays(diffMillis).toInt() + 1).coerceAtLeast(1)
        val lateFee = overdueDays * dailyRentalRate
        return Pair(overdueDays, lateFee)
    }

    suspend fun assessLateFee(rentalId: Long, lateFee: Long) {
        val rentalWithDetails = rentalDao.getRentalWithDetailsById(rentalId) ?: return
        val r = rentalWithDetails.rental
        val newRemaining = (r.totalPrice + lateFee - r.dpAmount).coerceAtLeast(0L)
        val newPaymentStatus = if (newRemaining == 0L) "Paid" else if (r.dpAmount > 0) "Partial" else "Unpaid"
        rentalDao.updateLateFee(rentalId, lateFee, newRemaining, newPaymentStatus)
    }

    suspend fun updateRentalStatusManually(rentalId: Long, newStatus: String) {
        rentalDao.updateRentalStatus(rentalId, newStatus)
    }

    suspend fun deleteRental(rentalId: Long) {
        val rentalWithDetails = rentalDao.getRentalWithDetailsById(rentalId) ?: return
        // If not completed and stock not returned, return stock before deletion
        if (rentalWithDetails.rental.rentalStatus != "Completed" && !rentalWithDetails.rental.isStockReturned) {
            rentalWithDetails.items.forEach { rentalItem ->
                itemDao.incrementAvailableStock(rentalItem.itemId, rentalItem.qty)
            }
        }
        rentalDao.deleteRental(rentalWithDetails.rental)
    }

    suspend fun markWhatsAppMessageSent(messageId: Long) {
        whatsAppMessageDao.markAsSent(messageId)
    }

    suspend fun queueManualReminder(rentalWithDetails: RentalWithDetails, isOverdue: Boolean) {
        val rental = rentalWithDetails.rental
        val content = if (isOverdue) {
            WhatsAppHelper.generateOverdueAlertMessage(rentalWithDetails)
        } else {
            WhatsAppHelper.generateReturnReminderMessage(rentalWithDetails)
        }
        val type = if (isOverdue) "OVERDUE_ALERT" else "RETURN_REMINDER"

        whatsAppMessageDao.insertMessage(
            WhatsAppMessage(
                rentalId = rental.id,
                customerName = rental.customerName,
                customerPhone = rental.customerPhone,
                messageType = type,
                messageContent = content,
                scheduledDate = System.currentTimeMillis(),
                isSent = false
            )
        )
    }

    // Cron scheduler checker to inspect rentals and queue return reminders / overdue notifications
    suspend fun runReminderScheduler(): Int {
        val currentTime = System.currentTimeMillis()
        val startOfToday = DateUtils.getStartOfDay(currentTime)
        val endOfTomorrow = DateUtils.getEndOfDay(currentTime + TimeUnit.DAYS.toMillis(1))

        var generatedCount = 0

        // 1. Check active rentals ending today or tomorrow
        val activeRentals = rentalDao.getRentalsForDateRange(startOfToday, endOfTomorrow)
        for (rentalWithDetails in activeRentals) {
            val rental = rentalWithDetails.rental
            if (rental.rentalStatus != "Completed") {
                val pendingCount = whatsAppMessageDao.countPendingForRental(rental.id, "RETURN_REMINDER")
                if (pendingCount == 0) {
                    val msg = WhatsAppHelper.generateReturnReminderMessage(rentalWithDetails)
                    whatsAppMessageDao.insertMessage(
                        WhatsAppMessage(
                            rentalId = rental.id,
                            customerName = rental.customerName,
                            customerPhone = rental.customerPhone,
                            messageType = "RETURN_REMINDER",
                            messageContent = msg,
                            scheduledDate = rental.endDate,
                            isSent = false
                        )
                    )
                    generatedCount++
                }
            }
        }

        // 2. Check overdue rentals
        val overdueRentals = rentalDao.getOverdueRentals(startOfToday)
        for (rentalWithDetails in overdueRentals) {
            val rental = rentalWithDetails.rental
            if (rental.rentalStatus != "Completed" && rental.rentalStatus != "Overdue") {
                rentalDao.updateRentalStatus(rental.id, "Overdue")
            }
            val pendingOverdueCount = whatsAppMessageDao.countPendingForRental(rental.id, "OVERDUE_ALERT")
            if (pendingOverdueCount == 0) {
                val msg = WhatsAppHelper.generateOverdueAlertMessage(rentalWithDetails)
                whatsAppMessageDao.insertMessage(
                    WhatsAppMessage(
                        rentalId = rental.id,
                        customerName = rental.customerName,
                        customerPhone = rental.customerPhone,
                        messageType = "OVERDUE_ALERT",
                        messageContent = msg,
                        scheduledDate = currentTime,
                        isSent = false
                    )
                )
                generatedCount++
            }
        }

        return generatedCount
    }

    suspend fun seedInitialInventoryIfEmpty() {
        if (itemDao.getItemsCount() == 0) {
            val initialGear = listOf(
                Item(
                    name = "Tenda Dome Kap 4 Arpenaz",
                    category = "Tenda",
                    pricePerDay = 45000,
                    stockTotal = 10,
                    stockAvailable = 10,
                    description = "Double layer waterproof 2000mm, kapasitas 4 orang, frame fiber kuat"
                ),
                Item(
                    name = "Tenda Ultralight Kap 2 Naturehike",
                    category = "Tenda",
                    pricePerDay = 35000,
                    stockTotal = 8,
                    stockAvailable = 8,
                    description = "Bobot ringan 1.8kg, silnylon 20D, cocok untuk summit & trekking cepat"
                ),
                Item(
                    name = "Carrier Deuter Aircontact 60+10L",
                    category = "Carrier",
                    pricePerDay = 35000,
                    stockTotal = 7,
                    stockAvailable = 7,
                    description = "Bantalan busa breathable, backsystem ergonomis, include rain cover"
                ),
                Item(
                    name = "Carrier Osprey Atmos 50L AG",
                    category = "Carrier",
                    pricePerDay = 40000,
                    stockTotal = 5,
                    stockAvailable = 5,
                    description = "Anti-gravity suspension, fit-on-the-fly hipbelt, material tangguh"
                ),
                Item(
                    name = "Sleeping Bag Polar Bulu Tebal",
                    category = "Sleeping Gear",
                    pricePerDay = 15000,
                    stockTotal = 20,
                    stockAvailable = 20,
                    description = "Comfort limit 5°C, lapisan polar hangat dan lembut untuk cuaca dingin"
                ),
                Item(
                    name = "Matras Foil Aluminium Double",
                    category = "Sleeping Gear",
                    pricePerDay = 7000,
                    stockTotal = 25,
                    stockAvailable = 25,
                    description = "Menahan suhu dingin tanah dengan maksimal, ukuran 200x120 cm"
                ),
                Item(
                    name = "Kompor Portable Mawar Windproof",
                    category = "Cooking",
                    pricePerDay = 15000,
                    stockTotal = 15,
                    stockAvailable = 15,
                    description = "Dilengkapi penahan angin lipat, koneksi tabung gas kaleng portable"
                ),
                Item(
                    name = "Nesting Cooking Set DS-308 (4 in 1)",
                    category = "Cooking",
                    pricePerDay = 20000,
                    stockTotal = 12,
                    stockAvailable = 12,
                    description = "Teko air, panci besar, panci sedang, wajan teflon anti lengket"
                ),
                Item(
                    name = "Headlamp Petzl Tikkina 300L",
                    category = "Penerangan",
                    pricePerDay = 15000,
                    stockTotal = 15,
                    stockAvailable = 15,
                    description = "Penerangan 300 lumens, 3 mode cahaya putih, hemat baterai AAA"
                ),
                Item(
                    name = "Lampu Tenda Rechargeable Solar",
                    category = "Penerangan",
                    pricePerDay = 10000,
                    stockTotal = 14,
                    stockAvailable = 14,
                    description = "Baterai 4000mAh, bisa jadi powerbank darurat, cantolan praktis"
                ),
                Item(
                    name = "Trekking Pole Duralumin Anti-Shock",
                    category = "Aksesoris",
                    pricePerDay = 10000,
                    stockTotal = 16,
                    stockAvailable = 16,
                    description = "Bahan duralumin 7075 kuat, pegangan busa EVA nyaman, 3 ruas"
                ),
                Item(
                    name = "Kursi Lipat Naturehike Ultralight",
                    category = "Aksesoris",
                    pricePerDay = 15000,
                    stockTotal = 12,
                    stockAvailable = 12,
                    description = "Beban maksimal 120kg, rangka aluminium kokoh, packing ringkas"
                ),
                Item(
                    name = "Paket Bundling Summit Berdua",
                    category = "Paket Bundling",
                    pricePerDay = 85000,
                    stockTotal = 5,
                    stockAvailable = 5,
                    description = "Includes: Tenda Kap 2 + 2 Sleeping Bag + 2 Matras + Kompor Windproof + Cooking Set"
                ),
                Item(
                    name = "Paket Bundling Family Berempat",
                    category = "Paket Bundling",
                    pricePerDay = 140000,
                    stockTotal = 4,
                    stockAvailable = 4,
                    description = "Includes: Tenda Kap 4 + 4 Sleeping Bag + 2 Matras Double + Kompor + Nesting DS-308 + Lampu Tenda"
                )
            )
            itemDao.insertItems(initialGear)
        }
    }
}
