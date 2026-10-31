package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Item
import com.example.ui.viewmodel.RentalViewModel
import com.example.util.CurrencyUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: RentalViewModel,
    modifier: Modifier = Modifier
) {
    val items by viewModel.filteredItems.collectAsState()
    val allItems by viewModel.allItems.collectAsState()
    val selectedCategory by viewModel.itemCategoryFilter.collectAsState()
    val searchQuery by viewModel.itemSearchQuery.collectAsState()

    var showAddEditDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<Item?>(null) }
    var itemToDelete by remember { mutableStateOf<Item?>(null) }

    // Aggregate statistics
    val totalKinds = allItems.size
    val totalUnits = allItems.sumOf { it.stockTotal }
    val availableUnits = allItems.sumOf { it.stockAvailable }
    val rentedUnits = (totalUnits - availableUnits).coerceAtLeast(0)

    val categories = remember(allItems) {
        listOf("Semua") + allItems.map { it.category }.distinct()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    itemToEdit = null
                    showAddEditDialog = true
                },
                modifier = Modifier.testTag("add_gear_fab"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Alat Baru")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Inventaris Alat Outdoor",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Monitoring stok otomatis dan ketersediaan sewa",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            // Stat Cards Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "Tersedia",
                        value = "$availableUnits Unit",
                        color = Color(0xFF2E7D32),
                        bgColor = Color(0xFFE8F5E9),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Disewa/Booked",
                        value = "$rentedUnits Unit",
                        color = Color(0xFFE65100),
                        bgColor = Color(0xFFFFF3E0),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Total Unit",
                        value = "$totalUnits Unit",
                        color = MaterialTheme.colorScheme.primary,
                        bgColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setItemSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("inventory_search_input"),
                    placeholder = { Text("Cari peralatan camping...") },
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
            }

            // Categories Filter Chips
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { viewModel.setCategoryFilter(category) },
                            label = { Text(category, fontSize = 12.sp) }
                        )
                    }
                }
            }

            // Items List
            if (items.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Inventory2,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Tidak ada alat ditemukan",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            } else {
                items(items, key = { it.id }) { item ->
                    GearInventoryCard(
                        item = item,
                        onEdit = {
                            itemToEdit = item
                            showAddEditDialog = true
                        },
                        onDelete = {
                            itemToDelete = item
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    // Add / Edit Dialog
    if (showAddEditDialog) {
        ItemAddEditDialog(
            item = itemToEdit,
            onDismiss = { showAddEditDialog = false },
            onSave = { name, category, price, totalStock, desc ->
                if (itemToEdit == null) {
                    viewModel.addNewItem(name, category, price, totalStock, desc)
                } else {
                    val current = itemToEdit!!
                    val diff = totalStock - current.stockTotal
                    val newAvailable = (current.stockAvailable + diff).coerceIn(0, totalStock)
                    viewModel.updateItem(
                        current.copy(
                            name = name,
                            category = category,
                            pricePerDay = price,
                            stockTotal = totalStock,
                            stockAvailable = newAvailable,
                            description = desc
                        )
                    )
                }
                showAddEditDialog = false
            }
        )
    }

    // Delete Confirmation Dialog
    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Hapus Peralatan") },
            text = { Text("Apakah Anda yakin ingin menghapus '${itemToDelete?.name}' dari daftar inventaris?") },
            confirmButton = {
                Button(
                    onClick = {
                        itemToDelete?.let { viewModel.deleteItem(it) }
                        itemToDelete = null
                    }
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { itemToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    color: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(title, fontSize = 11.sp, color = color, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = 14.sp, color = color, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun GearInventoryCard(
    item: Item,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val progress = if (item.stockTotal > 0) item.stockAvailable.toFloat() / item.stockTotal else 0f
    val isLowStock = item.stockAvailable <= 2

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("item_card_${item.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.category,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (item.description.isNotBlank()) {
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 2
                        )
                    }
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stock & Price Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${CurrencyUtils.formatRupiah(item.pricePerDay)} / hari",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Tersedia: ${item.stockAvailable} / ${item.stockTotal} unit",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isLowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isLowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
fun ItemAddEditDialog(
    item: Item?,
    onDismiss: () -> Unit,
    onSave: (name: String, category: String, price: Long, totalStock: Int, desc: String) -> Unit
) {
    var name by remember { mutableStateOf(item?.name ?: "") }
    var category by remember { mutableStateOf(item?.category ?: "Tenda") }
    var priceInput by remember { mutableStateOf(item?.pricePerDay?.toString() ?: "35000") }
    var stockInput by remember { mutableStateOf(item?.stockTotal?.toString() ?: "5") }
    var description by remember { mutableStateOf(item?.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (item == null) "Tambah Alat Baru" else "Edit Peralatan")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Alat") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Kategori (Tenda, Carrier, Cooking, dll)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = priceInput,
                    onValueChange = { priceInput = it.filter { c -> c.isDigit() } },
                    label = { Text("Harga Sewa / Hari (Rp)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                OutlinedTextField(
                    value = stockInput,
                    onValueChange = { stockInput = it.filter { c -> c.isDigit() } },
                    label = { Text("Total Stok Unit") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Deskripsi / Spesifikasi") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceInput.toLongOrNull() ?: 0L
                    val stock = stockInput.toIntOrNull() ?: 1
                    if (name.isNotBlank() && price > 0 && stock > 0) {
                        onSave(name, category, price, stock, description)
                    }
                },
                enabled = name.isNotBlank() && priceInput.isNotBlank() && stockInput.isNotBlank()
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
