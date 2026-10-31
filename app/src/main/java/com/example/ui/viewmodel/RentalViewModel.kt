package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.BundlePreset
import com.example.data.model.Item
import com.example.data.model.Rental
import com.example.data.model.RentalWithDetails
import com.example.data.model.WhatsAppMessage
import com.example.data.repository.RentalRepository
import com.example.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

data class CheckoutFormState(
    val customerName: String = "",
    val customerPhone: String = "",
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1),
    val guaranteeItem: String = "KTP",
    val notes: String = "",
    val cart: Map<Item, Int> = emptyMap(),
    val dpAmount: Long = 0L,
    val isCustomDp: Boolean = false
) {
    val durationDays: Int
        get() = DateUtils.calculateDays(startDate, endDate)

    val totalPrice: Long
        get() = cart.entries.sumOf { (item, qty) -> item.pricePerDay * qty * durationDays }

    val remainingBalance: Long
        get() = (totalPrice - dpAmount).coerceAtLeast(0L)

    val paymentStatus: String
        get() = when {
            totalPrice == 0L -> "Unpaid"
            remainingBalance <= 0L -> "Paid"
            dpAmount > 0L -> "Partial"
            else -> "Unpaid"
        }

    val isValid: Boolean
        get() = customerName.isNotBlank() &&
                customerPhone.isNotBlank() &&
                cart.isNotEmpty() &&
                guaranteeItem.isNotBlank()
}

class RentalViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: RentalRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = RentalRepository(
            database.itemDao(),
            database.rentalDao(),
            database.whatsAppMessageDao()
        )
        viewModelScope.launch {
            repository.seedInitialInventoryIfEmpty()
            repository.runReminderScheduler()
        }
    }

    val allItems: StateFlow<List<Item>> = repository.allItems.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allRentals: StateFlow<List<RentalWithDetails>> = repository.allRentals.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allWhatsAppMessages: StateFlow<List<WhatsAppMessage>> = repository.allWhatsAppMessages.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Filtering states
    private val _itemCategoryFilter = MutableStateFlow("Semua")
    val itemCategoryFilter = _itemCategoryFilter.asStateFlow()

    private val _itemSearchQuery = MutableStateFlow("")
    val itemSearchQuery = _itemSearchQuery.asStateFlow()

    val filteredItems: StateFlow<List<Item>> = combine(
        allItems,
        _itemCategoryFilter,
        _itemSearchQuery
    ) { items, category, query ->
        items.filter { item ->
            val matchCategory = if (category == "Semua") true else item.category.equals(category, ignoreCase = true)
            val matchQuery = if (query.isBlank()) true else {
                item.name.contains(query, ignoreCase = true) || item.category.contains(query, ignoreCase = true)
            }
            matchCategory && matchQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _rentalStatusFilter = MutableStateFlow("Semua")
    val rentalStatusFilter = _rentalStatusFilter.asStateFlow()

    val filteredRentals: StateFlow<List<RentalWithDetails>> = combine(
        allRentals,
        _rentalStatusFilter
    ) { rentals, statusFilter ->
        if (statusFilter == "Semua") {
            rentals
        } else {
            rentals.filter { it.rental.rentalStatus.equals(statusFilter, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected date for calendar view
    private val _calendarSelectedDate = MutableStateFlow(DateUtils.getStartOfDay(System.currentTimeMillis()))
    val calendarSelectedDate = _calendarSelectedDate.asStateFlow()

    // Checkout form state
    private val _checkoutState = MutableStateFlow(CheckoutFormState())
    val checkoutState = _checkoutState.asStateFlow()

    // Last completed transaction for receipt dialog
    private val _lastCreatedRental = MutableStateFlow<RentalWithDetails?>(null)
    val lastCreatedRental = _lastCreatedRental.asStateFlow()

    fun setCategoryFilter(category: String) {
        _itemCategoryFilter.value = category
    }

    fun setItemSearchQuery(query: String) {
        _itemSearchQuery.value = query
    }

    fun setRentalStatusFilter(status: String) {
        _rentalStatusFilter.value = status
    }

    fun setCalendarSelectedDate(dateMillis: Long) {
        _calendarSelectedDate.value = DateUtils.getStartOfDay(dateMillis)
    }

    // Checkout Form actions
    fun updateCustomerName(name: String) {
        _checkoutState.value = _checkoutState.value.copy(customerName = name)
    }

    fun updateCustomerPhone(phone: String) {
        _checkoutState.value = _checkoutState.value.copy(customerPhone = phone)
    }

    fun updateGuaranteeItem(guarantee: String) {
        _checkoutState.value = _checkoutState.value.copy(guaranteeItem = guarantee)
    }

    fun updateNotes(notes: String) {
        _checkoutState.value = _checkoutState.value.copy(notes = notes)
    }

    fun setRentalDates(startDateMillis: Long, endDateMillis: Long) {
        val validEnd = if (endDateMillis <= startDateMillis) {
            startDateMillis + TimeUnit.DAYS.toMillis(1)
        } else {
            endDateMillis
        }
        val current = _checkoutState.value
        val updated = current.copy(startDate = startDateMillis, endDate = validEnd)
        // If not custom DP, recalculate DP
        _checkoutState.value = if (!updated.isCustomDp && updated.dpAmount > 0) {
            updated.copy(dpAmount = updated.totalPrice)
        } else {
            updated
        }
    }

    fun addItemToCart(item: Item) {
        val currentCart = _checkoutState.value.cart.toMutableMap()
        val currentQty = currentCart[item] ?: 0
        if (currentQty < item.stockAvailable) {
            currentCart[item] = currentQty + 1
            val newState = _checkoutState.value.copy(cart = currentCart)
            // Auto update DP if full payment was previously selected
            _checkoutState.value = if (!newState.isCustomDp && newState.dpAmount > 0) {
                newState.copy(dpAmount = newState.totalPrice)
            } else {
                newState
            }
        }
    }

    fun removeItemFromCart(item: Item) {
        val currentCart = _checkoutState.value.cart.toMutableMap()
        val currentQty = currentCart[item] ?: 0
        if (currentQty > 1) {
            currentCart[item] = currentQty - 1
        } else {
            currentCart.remove(item)
        }
        val newState = _checkoutState.value.copy(cart = currentCart)
        _checkoutState.value = if (!newState.isCustomDp && newState.dpAmount > 0) {
            newState.copy(dpAmount = newState.totalPrice)
        } else {
            newState
        }
    }

    fun applyPresetBundle(bundle: BundlePreset): Int {
        val currentItems = allItems.value
        val currentCart = _checkoutState.value.cart.toMutableMap()
        var addedCount = 0

        for (spec in bundle.items) {
            val matchingItem = currentItems.find {
                it.name.contains(spec.itemNameQuery, ignoreCase = true) ||
                it.category.equals(spec.category, ignoreCase = true)
            } ?: currentItems.find { it.category.equals(spec.category, ignoreCase = true) }

            if (matchingItem != null) {
                val available = matchingItem.stockAvailable
                if (available > 0) {
                    val currentItemQty = currentCart[matchingItem] ?: 0
                    val targetQty = (currentItemQty + spec.qty).coerceAtMost(available)
                    currentCart[matchingItem] = targetQty
                    addedCount++
                }
            }
        }

        val newState = _checkoutState.value.copy(cart = currentCart)
        _checkoutState.value = if (!newState.isCustomDp && newState.dpAmount > 0) {
            newState.copy(dpAmount = newState.totalPrice)
        } else {
            newState
        }
        return addedCount
    }

    fun setDpAmount(amount: Long) {
        val bounded = amount.coerceIn(0L, _checkoutState.value.totalPrice)
        _checkoutState.value = _checkoutState.value.copy(
            dpAmount = bounded,
            isCustomDp = true
        )
    }

    fun setDpPercentage(percent: Int) {
        val total = _checkoutState.value.totalPrice
        val amount = when (percent) {
            100 -> total
            50 -> total / 2
            30 -> (total * 30) / 100
            else -> 0L
        }
        _checkoutState.value = _checkoutState.value.copy(
            dpAmount = amount,
            isCustomDp = (percent != 100)
        )
    }

    fun resetCheckoutForm() {
        val now = System.currentTimeMillis()
        _checkoutState.value = CheckoutFormState(
            startDate = now,
            endDate = now + TimeUnit.DAYS.toMillis(1)
        )
    }

    fun clearLastCreatedRental() {
        _lastCreatedRental.value = null
    }

    fun submitRentalTransaction(onSuccess: (RentalWithDetails) -> Unit, onError: (String) -> Unit) {
        val state = _checkoutState.value
        if (!state.isValid) {
            onError("Mohon lengkapi nama, nomor telepon WA, jaminan, dan pilih minimal 1 alat.")
            return
        }

        viewModelScope.launch {
            try {
                val selectedItems = state.cart.map { Pair(it.key, it.value) }
                val rentalId = repository.createRentalTransaction(
                    customerName = state.customerName.trim(),
                    customerPhone = state.customerPhone.trim(),
                    startDate = state.startDate,
                    endDate = state.endDate,
                    durationDays = state.durationDays,
                    totalPrice = state.totalPrice,
                    dpAmount = state.dpAmount,
                    remainingBalance = state.remainingBalance,
                    guaranteeItem = state.guaranteeItem,
                    notes = state.notes.trim(),
                    selectedItems = selectedItems
                )

                // Retrieve inserted rental
                val createdRental = allRentals.value.find { it.rental.id == rentalId }
                    ?: RentalWithDetails(
                        rental = com.example.data.model.Rental(
                            id = rentalId,
                            customerName = state.customerName,
                            customerPhone = state.customerPhone,
                            startDate = state.startDate,
                            endDate = state.endDate,
                            durationDays = state.durationDays,
                            totalPrice = state.totalPrice,
                            dpAmount = state.dpAmount,
                            remainingBalance = state.remainingBalance,
                            guaranteeItem = state.guaranteeItem,
                            paymentStatus = state.paymentStatus,
                            rentalStatus = "Booked",
                            notes = state.notes
                        ),
                        items = selectedItems.map {
                            com.example.data.model.RentalItem(
                                rentalId = rentalId,
                                itemId = it.first.id,
                                itemName = it.first.name,
                                pricePerDay = it.first.pricePerDay,
                                qty = it.second,
                                subtotal = it.first.pricePerDay * it.second * state.durationDays
                            )
                        }
                    )

                _lastCreatedRental.value = createdRental
                resetCheckoutForm()
                onSuccess(createdRental)
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Terjadi kesalahan saat memproses rental.")
            }
        }
    }

    // Rental lifecycle
    fun markAsRented(rentalId: Long) {
        viewModelScope.launch {
            repository.markAsRented(rentalId)
        }
    }

    fun completeRental(rentalId: Long) {
        viewModelScope.launch {
            repository.completeRental(rentalId)
        }
    }

    fun settlePayment(rentalId: Long) {
        viewModelScope.launch {
            repository.settlePayment(rentalId)
        }
    }

    fun deleteRental(rentalId: Long) {
        viewModelScope.launch {
            repository.deleteRental(rentalId)
        }
    }

    fun calculateLateFee(rental: Rental): Pair<Int, Long> {
        val dailyRate = if (rental.durationDays > 0) (rental.totalPrice / rental.durationDays).coerceAtLeast(25000L) else 25000L
        return repository.calculateLateFee(rental.endDate, dailyRate)
    }

    fun applyLateFee(rentalId: Long, fee: Long) {
        viewModelScope.launch {
            repository.assessLateFee(rentalId, fee)
        }
    }

    fun sendWhatsAppMessage(message: WhatsAppMessage) {
        viewModelScope.launch {
            repository.markWhatsAppMessageSent(message.id)
        }
    }

    fun triggerAutoReminderScheduler(onResult: (Int) -> Unit) {
        viewModelScope.launch {
            val count = repository.runReminderScheduler()
            onResult(count)
        }
    }

    fun queueManualReminder(rentalWithDetails: RentalWithDetails, isOverdue: Boolean) {
        viewModelScope.launch {
            repository.queueManualReminder(rentalWithDetails, isOverdue)
        }
    }

    // Inventory operations
    fun addNewItem(name: String, category: String, pricePerDay: Long, stockTotal: Int, description: String) {
        viewModelScope.launch {
            repository.insertItem(
                Item(
                    name = name.trim(),
                    category = category.trim(),
                    pricePerDay = pricePerDay,
                    stockTotal = stockTotal,
                    stockAvailable = stockTotal,
                    description = description.trim()
                )
            )
        }
    }

    fun updateItem(item: Item) {
        viewModelScope.launch {
            repository.updateItem(item)
        }
    }

    fun deleteItem(item: Item) {
        viewModelScope.launch {
            repository.deleteItem(item)
        }
    }
}
