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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.RentalStatusBadge
import com.example.ui.viewmodel.RentalViewModel
import com.example.util.DateUtils
import com.example.util.WhatsAppHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun CalendarScheduleScreen(
    viewModel: RentalViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedDate by viewModel.calendarSelectedDate.collectAsState()
    val allRentals by viewModel.allRentals.collectAsState()
    val allItems by viewModel.allItems.collectAsState()

    // Generate date strip for 14 days (previous 2 days to next 12 days)
    val dateStrip = remember {
        val list = mutableListOf<Long>()
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -2)
        for (i in 0..14) {
            list.add(DateUtils.getStartOfDay(cal.timeInMillis))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    val selectedStartOfDay = DateUtils.getStartOfDay(selectedDate)
    val selectedEndOfDay = DateUtils.getEndOfDay(selectedDate)

    // Filter rentals active on selected date
    val activeRentalsOnDate = remember(allRentals, selectedStartOfDay, selectedEndOfDay) {
        allRentals.filter { item ->
            val r = item.rental
            r.rentalStatus != "Completed" && r.startDate <= selectedEndOfDay && r.endDate >= selectedStartOfDay
        }
    }

    // Rentals starting on this date (Pickups)
    val pickupsToday = remember(allRentals, selectedStartOfDay, selectedEndOfDay) {
        allRentals.filter { item ->
            val start = DateUtils.getStartOfDay(item.rental.startDate)
            start == selectedStartOfDay && item.rental.rentalStatus != "Completed"
        }
    }

    // Rentals returning on this date (Returns)
    val returnsToday = remember(allRentals, selectedStartOfDay, selectedEndOfDay) {
        allRentals.filter { item ->
            val end = DateUtils.getStartOfDay(item.rental.endDate)
            end == selectedStartOfDay && item.rental.rentalStatus != "Completed"
        }
    }

    // Calculate gear utilization for the date
    val gearUsedOnDate = remember(activeRentalsOnDate) {
        val map = mutableMapOf<Long, Int>() // itemId -> qty
        activeRentalsOnDate.forEach { rental ->
            rental.items.forEach { rentalItem ->
                map[rentalItem.itemId] = (map[rentalItem.itemId] ?: 0) + rentalItem.qty
            }
        }
        map
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Screen Header
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Kalender Ketersediaan Alat",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Visualisasi jadwal ambil, kembali, dan ketersediaan peralatan",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }

        // Horizontal Date Strip (CalendarView strip)
        item {
            Text(
                text = "Pilih Tanggal Pantau:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(dateStrip) { dateMillis ->
                    val isSelected = DateUtils.getStartOfDay(dateMillis) == selectedStartOfDay
                    val isToday = DateUtils.isToday(dateMillis)

                    val dayFormat = SimpleDateFormat("EEE", Locale("id", "ID"))
                    val numFormat = SimpleDateFormat("dd", Locale("id", "ID"))
                    val monthFormat = SimpleDateFormat("MMM", Locale("id", "ID"))

                    Card(
                        modifier = Modifier
                            .width(62.dp)
                            .clickable { viewModel.setCalendarSelectedDate(dateMillis) }
                            .testTag("date_strip_${numFormat.format(dateMillis)}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else if (isToday)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            else
                                MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = dayFormat.format(dateMillis),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = numFormat.format(dateMillis),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = monthFormat.format(dateMillis),
                                fontSize = 10.sp,
                                color = if (isSelected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }

        // Selected Date Metric Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Ringkasan Jadwal • ${DateUtils.formatDate(selectedDate)}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Pickups Counter
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CallMade, contentDescription = null, tint = Color(0xFF1565C0), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Jadwal Ambil", fontSize = 11.sp, color = Color(0xFF1565C0), fontWeight = FontWeight.SemiBold)
                                }
                                Text("${pickupsToday.size} Nota", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                            }
                        }

                        // Returns Counter
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CallReceived, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Jadwal Kembali", fontSize = 11.sp, color = Color(0xFFE65100), fontWeight = FontWeight.SemiBold)
                                }
                                Text("${returnsToday.size} Nota", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                            }
                        }
                    }
                }
            }
        }

        // Section: Jadwal Pengembalian Hari Ini (With instant reminder trigger)
        if (returnsToday.isNotEmpty()) {
            item {
                Text(
                    text = "🚨 Batas Waktu Pengembalian (${returnsToday.size} Penyewa)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            items(returnsToday) { rentalWithDetails ->
                val rental = rentalWithDetails.rental
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(rental.customerName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("No. WA: ${rental.customerPhone} • Sisa: Rp ${rental.remainingBalance}", fontSize = 11.sp, color = Color.Gray)
                            Text(
                                rentalWithDetails.items.joinToString(", ") { "${it.itemName} (x${it.qty})" },
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }

                        Button(
                            onClick = {
                                val msg = WhatsAppHelper.generateReturnReminderMessage(rentalWithDetails)
                                WhatsAppHelper.openWhatsApp(context, rental.customerPhone, msg)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("send_return_reminder_${rental.id}")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ingatkan WA", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        // Section: Jadwal Pengambilan Hari Ini
        if (pickupsToday.isNotEmpty()) {
            item {
                Text(
                    text = "📦 Jadwal Pengambilan Alat (${pickupsToday.size} Penyewa)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            items(pickupsToday) { rentalWithDetails ->
                val rental = rentalWithDetails.rental
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(rental.customerName, fontWeight = FontWeight.Bold)
                            RentalStatusBadge(rental.rentalStatus)
                        }
                        Text("No. WA: ${rental.customerPhone} • Jaminan: ${rental.guaranteeItem}", fontSize = 11.sp, color = Color.Gray)
                        Text(
                            rentalWithDetails.items.joinToString(", ") { "${it.itemName} (x${it.qty})" },
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Section: Matriks Ketersediaan Alat Pada Tanggal Tersebut
        item {
            Text(
                text = "Ketersediaan Stok Alat di Tanggal Ini",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(allItems) { item ->
            val rentedQty = gearUsedOnDate[item.id] ?: 0
            val availableOnDate = (item.stockTotal - rentedQty).coerceAtLeast(0)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("${item.category} • Total: ${item.stockTotal} unit", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Tersedia: $availableOnDate unit",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (availableOnDate > 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                        )
                        Text(
                            text = "Disewa: $rentedQty unit",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
