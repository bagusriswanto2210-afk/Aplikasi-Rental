package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RentalWithDetails
import com.example.ui.components.PaymentStatusBadge
import com.example.ui.components.RentalDetailDialog
import com.example.ui.components.RentalStatusBadge
import com.example.ui.viewmodel.RentalViewModel
import com.example.util.CurrencyUtils
import com.example.util.DateUtils
import com.example.util.WhatsAppHelper

@Composable
fun RentalsScreen(
    viewModel: RentalViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val rentals by viewModel.filteredRentals.collectAsState()
    val allRentals by viewModel.allRentals.collectAsState()
    val selectedStatus by viewModel.rentalStatusFilter.collectAsState()

    var selectedRentalForDetail by remember { mutableStateOf<RentalWithDetails?>(null) }
    var rentalToDelete by remember { mutableStateOf<RentalWithDetails?>(null) }

    val statusTabs = listOf("Semua", "Booked", "Rented", "Overdue", "Completed")

    // Counters for tabs
    val bookedCount = allRentals.count { it.rental.rentalStatus == "Booked" }
    val rentedCount = allRentals.count { it.rental.rentalStatus == "Rented" }
    val overdueCount = allRentals.count { it.rental.rentalStatus == "Overdue" }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pelacakan Sewa Outdoor",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Kelola status rental, pengembalian alat, dan pelunasan tagihan",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }

        // Status Filter Tabs
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(statusTabs) { tab ->
                    val badgeText = when (tab) {
                        "Booked" -> if (bookedCount > 0) " ($bookedCount)" else ""
                        "Rented" -> if (rentedCount > 0) " ($rentedCount)" else ""
                        "Overdue" -> if (overdueCount > 0) " ($overdueCount)" else ""
                        else -> ""
                    }
                    FilterChip(
                        selected = selectedStatus == tab,
                        onClick = { viewModel.setRentalStatusFilter(tab) },
                        label = { Text("$tab$badgeText", fontSize = 12.sp) }
                    )
                }
            }
        }

        // Rentals List
        if (rentals.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.EventNote,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Belum ada data transaksi sewa di kategori ini",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        } else {
            items(rentals, key = { it.rental.id }) { item ->
                RentalCardItem(
                    rentalWithDetails = item,
                    onClick = { selectedRentalForDetail = item },
                    onMarkRented = { viewModel.markAsRented(item.rental.id) },
                    onMarkCompleted = { viewModel.completeRental(item.rental.id) },
                    onSettlePayment = { viewModel.settlePayment(item.rental.id) },
                    onDelete = { rentalToDelete = item },
                    onSendWhatsApp = {
                        val msg = if (item.rental.rentalStatus == "Overdue") {
                            WhatsAppHelper.generateOverdueAlertMessage(item)
                        } else if (item.rental.rentalStatus == "Rented") {
                            WhatsAppHelper.generateReturnReminderMessage(item)
                        } else {
                            WhatsAppHelper.generateBookingConfirmationMessage(item)
                        }
                        WhatsAppHelper.openWhatsApp(context, item.rental.customerPhone, msg)
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Detail & Invoice Dialog
    if (selectedRentalForDetail != null) {
        RentalDetailDialog(
            rentalWithDetails = selectedRentalForDetail!!,
            onDismiss = { selectedRentalForDetail = null },
            onSettlePayment = {
                viewModel.settlePayment(selectedRentalForDetail!!.rental.id)
            },
            onMarkCompleted = {
                viewModel.completeRental(selectedRentalForDetail!!.rental.id)
            },
            onMarkRented = {
                viewModel.markAsRented(selectedRentalForDetail!!.rental.id)
            }
        )
    }

    // Delete Confirmation Dialog
    if (rentalToDelete != null) {
        AlertDialog(
            onDismissRequest = { rentalToDelete = null },
            title = { Text("Hapus Transaksi Sewa") },
            text = {
                Text("Hapus nota #${rentalToDelete?.rental?.id} (${rentalToDelete?.rental?.customerName})? Jika belum berstatus Selesai, stok alat akan otomatis dikembalikan ke inventaris.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        rentalToDelete?.let { viewModel.deleteRental(it.rental.id) }
                        rentalToDelete = null
                    }
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { rentalToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun RentalCardItem(
    rentalWithDetails: RentalWithDetails,
    onClick: () -> Unit,
    onMarkRented: () -> Unit,
    onMarkCompleted: () -> Unit,
    onSettlePayment: () -> Unit,
    onDelete: () -> Unit,
    onSendWhatsApp: () -> Unit
) {
    val rental = rentalWithDetails.rental
    val items = rentalWithDetails.items

    val isOverdue = rental.rentalStatus == "Overdue"
    val isCompleted = rental.rentalStatus == "Completed"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("rental_card_${rental.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOverdue)
                Color(0xFFFFEBEE)
            else if (isCompleted)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCompleted) 0.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Customer Name & Status Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = rental.customerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Phone,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${rental.customerPhone} • Jaminan: ${rental.guaranteeItem}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    RentalStatusBadge(rental.rentalStatus)
                    PaymentStatusBadge(rental.paymentStatus)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dates and Duration Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${DateUtils.formatDate(rental.startDate)} ➔ ${DateUtils.formatDate(rental.endDate)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${rental.durationDays} Hari",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Items breakdown pills
            Text(
                text = items.joinToString(", ") { "${it.itemName} (x${it.qty})" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Payment values
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total: ${CurrencyUtils.formatRupiah(rental.totalPrice)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    if (rental.remainingBalance > 0) {
                        Text(
                            text = "Sisa: ${CurrencyUtils.formatRupiah(rental.remainingBalance)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Text(
                            text = "Lunas",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }

                // Action icon buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Send WA button
                    IconButton(
                        onClick = onSendWhatsApp,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("send_wa_${rental.id}")
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Kirim WA",
                            tint = Color(0xFF25D366),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Settle payment quick button
                    if (rental.remainingBalance > 0) {
                        IconButton(
                            onClick = onSettlePayment,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("settle_pay_${rental.id}")
                        ) {
                            Icon(
                                Icons.Default.Paid,
                                contentDescription = "Pelunasan",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Mark rented if booked
                    if (rental.rentalStatus == "Booked") {
                        IconButton(
                            onClick = onMarkRented,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("mark_rented_${rental.id}")
                        ) {
                            Icon(
                                Icons.Default.Assignment,
                                contentDescription = "Diambil",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Mark completed / returned
                    if (!isCompleted) {
                        IconButton(
                            onClick = onMarkCompleted,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("mark_completed_${rental.id}")
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Kembali (Selesai)",
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Hapus",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
