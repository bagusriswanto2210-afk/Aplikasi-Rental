package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.BundlePreset
import com.example.data.model.Item
import com.example.data.model.RentalWithDetails
import com.example.ui.components.PaymentStatusBadge
import com.example.ui.theme.CampfireOrange
import com.example.ui.theme.ForestGreenContainer
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.NatureCardBorder
import com.example.ui.theme.PineDark
import com.example.ui.theme.WoodBarkBrown
import com.example.ui.viewmodel.RentalViewModel
import com.example.util.CurrencyUtils
import com.example.util.DateUtils
import com.example.util.WhatsAppHelper
import java.util.Calendar
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    viewModel: RentalViewModel,
    onNavigateToRentals: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val formState by viewModel.checkoutState.collectAsState()
    val filteredItems by viewModel.filteredItems.collectAsState()
    val allItems by viewModel.allItems.collectAsState()
    val selectedCategory by viewModel.itemCategoryFilter.collectAsState()
    val searchQuery by viewModel.itemSearchQuery.collectAsState()

    var showSuccessDialog by remember { mutableStateOf(false) }
    var successRental by remember { mutableStateOf<RentalWithDetails?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Categories list
    val categories = remember(allItems) {
        listOf("Semua") + allItems.map { it.category }.distinct()
    }

    // Date Picker Dialog Helpers
    val calendar = Calendar.getInstance()

    val startDatePicker = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val cal = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth, 10, 0, 0)
                }
                viewModel.setRentalDates(cal.timeInMillis, formState.endDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    val endDatePicker = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val cal = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth, 18, 0, 0)
                }
                viewModel.setRentalDates(formState.startDate, cal.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Actions Header
        if (formState.cart.isNotEmpty() || formState.customerName.isNotBlank()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { viewModel.resetCheckoutForm() },
                        modifier = Modifier.testTag("reset_form_button")
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset Form", fontSize = 12.sp)
                    }
                }
            }
        }

        // Nature Hero Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_outdoor_nature_hero_1793457939977),
                        contentDescription = "Pemandangan Alam Outdoor",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x22000000),
                                        Color(0x880F2A1D),
                                        Color(0xEE0F2A1D)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CampfireOrange)
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Terrain, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("TEMA ALAM RIMBA", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("⛰️ Gunung & Camping Ground", color = Color(0xFFD8F3DC), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Penyewaan Alat Camping & Gunung",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Peralatan bersih, kering, steril & siap pakai",
                            color = Color(0xFFE8ECE5),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Section: Preset Bundles
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Terrain, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Paket Bundling Praktis (Hemat)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text("1-Tap Add", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                    Text(
                        text = "Pilih paket siap pakai, otomatis mengisi daftar tenda, alat masak & sleeping gear",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(BundlePreset.PRESET_BUNDLES) { bundle ->
                            Card(
                                modifier = Modifier
                                    .width(220.dp)
                                    .testTag("bundle_card_${bundle.id}"),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(bundle.badge, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(bundle.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(bundle.tagline, fontSize = 10.sp, color = Color.Gray, maxLines = 2)

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                CurrencyUtils.formatRupiah(bundle.bundledPricePerDay) + "/hari",
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                "Normal " + CurrencyUtils.formatRupiah(bundle.normalPricePerDay),
                                                fontSize = 10.sp,
                                                color = Color.Gray,
                                                style = androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                                            )
                                        }

                                        Button(
                                            onClick = {
                                                viewModel.applyPresetBundle(bundle)
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = CampfireOrange),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text("Pilih", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 1: Tanggal Sewa (Rental Schedule)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "1. Jadwal Sewa",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Durasi: ${formState.durationDays} Hari",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Start Date Button
                        OutlinedButton(
                            onClick = { startDatePicker.show() },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("pick_start_date_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.Start) {
                                Text("Tgl Mulai/Ambil", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        DateUtils.formatDateShort(formState.startDate),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        // End Date Button
                        OutlinedButton(
                            onClick = { endDatePicker.show() },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("pick_end_date_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.Start) {
                                Text("Tgl Pengembalian", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        DateUtils.formatDateShort(formState.endDate),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }

                    // Quick duration presets
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val now = System.currentTimeMillis()
                        FilterChip(
                            selected = formState.durationDays == 1,
                            onClick = {
                                viewModel.setRentalDates(now, now + TimeUnit.DAYS.toMillis(1))
                            },
                            label = { Text("1 Hari (Besok)", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = formState.durationDays == 2,
                            onClick = {
                                viewModel.setRentalDates(now, now + TimeUnit.DAYS.toMillis(2))
                            },
                            label = { Text("2 Hari (Weekend)", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = formState.durationDays == 3,
                            onClick = {
                                viewModel.setRentalDates(now, now + TimeUnit.DAYS.toMillis(3))
                            },
                            label = { Text("3 Hari (Pendakian)", fontSize = 11.sp) }
                        )
                    }
                }
            }
        }

        // Section 2: Pilih Peralatan Outdoor (Gear Selection)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "2. Pilih Alat & Paket",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (formState.cart.isNotEmpty()) {
                            Text(
                                text = "${formState.cart.values.sum()} item dipilih",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Search Field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setItemSearchQuery(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_items_input"),
                        placeholder = { Text("Cari tenda, carrier, kompor...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setItemSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Category filter chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { category ->
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = { viewModel.setCategoryFilter(category) },
                                label = { Text(category, fontSize = 12.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Items selection grid/list
                    filteredItems.forEach { item ->
                        val selectedQty = formState.cart[item] ?: 0
                        val isAvailable = item.stockAvailable > 0

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedQty > 0)
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                else
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.name,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${CurrencyUtils.formatRupiah(item.pricePerDay)}/hari",
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "• Sisa Stok: ${item.stockAvailable}/${item.stockTotal}",
                                            fontSize = 11.sp,
                                            color = if (item.stockAvailable <= 2) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }

                                // Stepper
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (selectedQty > 0) {
                                        IconButton(
                                            onClick = { viewModel.removeItemFromCart(item) },
                                            modifier = Modifier
                                                .size(32.dp)
                                                .background(MaterialTheme.colorScheme.surface, CircleShape)
                                                .testTag("remove_item_${item.id}")
                                        ) {
                                            Icon(
                                                Icons.Default.Remove,
                                                contentDescription = "Kurangi",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        Text(
                                            text = "$selectedQty",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.addItemToCart(item) },
                                        enabled = isAvailable && selectedQty < item.stockAvailable,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(
                                                if (isAvailable && selectedQty < item.stockAvailable)
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                                CircleShape
                                            )
                                            .testTag("add_item_${item.id}")
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "Tambah",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Ringkasan Biaya & Kalkulasi DP (DP Calculation Component)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "3. Kalkulasi DP & Tagihan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        PaymentStatusBadge(formState.paymentStatus)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Selected items list preview
                    if (formState.cart.isNotEmpty()) {
                        formState.cart.forEach { (item, qty) ->
                            val subtotal = item.pricePerDay * qty * formState.durationDays
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${item.name} (x$qty)",
                                    fontSize = 12.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = CurrencyUtils.formatRupiah(subtotal),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                    }

                    // Total Price
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Biaya Sewa:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = CurrencyUtils.formatRupiah(formState.totalPrice),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick DP Presets
                    Text(
                        text = "Pilih Nominal Uang Muka (DP):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = formState.dpAmount == formState.totalPrice && formState.totalPrice > 0,
                            onClick = { viewModel.setDpPercentage(100) },
                            label = { Text("Lunas (100%)", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = formState.dpAmount == formState.totalPrice / 2 && formState.totalPrice > 0,
                            onClick = { viewModel.setDpPercentage(50) },
                            label = { Text("DP 50%", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = formState.dpAmount == (formState.totalPrice * 30) / 100 && formState.totalPrice > 0,
                            onClick = { viewModel.setDpPercentage(30) },
                            label = { Text("DP 30%", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = formState.dpAmount == 0L,
                            onClick = { viewModel.setDpPercentage(0) },
                            label = { Text("DP Rp 0", fontSize = 11.sp) }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Custom DP input
                    OutlinedTextField(
                        value = if (formState.dpAmount == 0L && !formState.isCustomDp) "" else formState.dpAmount.toString(),
                        onValueChange = {
                            val parsed = CurrencyUtils.parseRupiahInput(it)
                            viewModel.setDpAmount(parsed)
                        },
                        label = { Text("Nominal DP (Rp)", fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dp_amount_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        supportingText = {
                            Text(
                                "Format: ${CurrencyUtils.formatRupiah(formState.dpAmount)}",
                                fontSize = 11.sp
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Dynamic Remaining Balance Card (Selisih Sisa Tagihan)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (formState.remainingBalance == 0L && formState.totalPrice > 0)
                                Color(0xFFE8F5E9)
                            else
                                Color(0xFFFFF3E0)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Sisa Tagihan Pelunasan:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "(Total - DP Dibayar)",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                            Text(
                                text = CurrencyUtils.formatRupiah(formState.remainingBalance),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (formState.remainingBalance == 0L && formState.totalPrice > 0)
                                    Color(0xFF2E7D32)
                                else
                                    Color(0xFFE65100)
                            )
                        }
                    }
                }
            }
        }

        // Section 4: Data Penyewa & Jaminan (Customer Details)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "4. Data Penyewa & Jaminan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Customer Name
                    OutlinedTextField(
                        value = formState.customerName,
                        onValueChange = { viewModel.updateCustomerName(it) },
                        label = { Text("Nama Lengkap Penyewa") },
                        placeholder = { Text("Contoh: Rizky Ananda") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("customer_name_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Customer Phone (WA)
                    OutlinedTextField(
                        value = formState.customerPhone,
                        onValueChange = { viewModel.updateCustomerPhone(it) },
                        label = { Text("Nomor WhatsApp (Tujuan Notifikasi)") },
                        placeholder = { Text("Contoh: 081234567890") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("customer_phone_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        supportingText = {
                            Text("Akan dikirimi nota digital dan pengingat jadwal pengembalian otomatis via WA.", fontSize = 11.sp)
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Guarantee Item Selection
                    Text(
                        text = "Pilihan Jaminan (Wajib Diserahkan):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val guaranteeOptions = listOf("KTP Asli", "SIM A / C", "Paspor", "Kartu Mahasiswa", "STNK Motor")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(guaranteeOptions) { opt ->
                            FilterChip(
                                selected = formState.guaranteeItem == opt,
                                onClick = { viewModel.updateGuaranteeItem(opt) },
                                label = { Text(opt, fontSize = 12.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Notes
                    OutlinedTextField(
                        value = formState.notes,
                        onValueChange = { viewModel.updateNotes(it) },
                        label = { Text("Catatan Tambahan (Opsional)") },
                        placeholder = { Text("Contoh: Ambil jam 19.00 malam, tujuan Gn. Prau") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("notes_input"),
                        maxLines = 2,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // Section 5: Submit Action
        item {
            Button(
                onClick = {
                    viewModel.submitRentalTransaction(
                        onSuccess = { rental ->
                            successRental = rental
                            showSuccessDialog = true
                        },
                        onError = { err ->
                            errorMessage = err
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("submit_rental_button"),
                enabled = formState.isValid,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Simpan Transaksi & Buat Nota WA",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Error Alert Dialog
    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text("Perhatian") },
            text = { Text(errorMessage ?: "") },
            confirmButton = {
                Button(onClick = { errorMessage = null }) {
                    Text("OK")
                }
            }
        )
    }

    // Success & WhatsApp Notification Dialog
    if (showSuccessDialog && successRental != null) {
        val rental = successRental!!
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                viewModel.clearLastCreatedRental()
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Transaksi Berhasil!", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text("Nota #${rental.rental.id} untuk ${rental.rental.customerName} telah tersimpan di SQLite Room.")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Stok barang terkait telah otomatis dikurangi di sistem.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Total: ${CurrencyUtils.formatRupiah(rental.rental.totalPrice)}", fontWeight = FontWeight.SemiBold)
                            Text("DP Masuk: ${CurrencyUtils.formatRupiah(rental.rental.dpAmount)}")
                            Text(
                                "Sisa Tagihan: ${CurrencyUtils.formatRupiah(rental.rental.remainingBalance)}",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val msg = WhatsAppHelper.generateBookingConfirmationMessage(rental)
                        WhatsAppHelper.openWhatsApp(context, rental.rental.customerPhone, msg)
                        showSuccessDialog = false
                        viewModel.clearLastCreatedRental()
                        onNavigateToRentals()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    modifier = Modifier.testTag("dialog_send_whatsapp_button")
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Kirim Nota via WA", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showSuccessDialog = false
                        viewModel.clearLastCreatedRental()
                        onNavigateToRentals()
                    }
                ) {
                    Text("Lihat Daftar Sewa")
                }
            }
        )
    }
}
