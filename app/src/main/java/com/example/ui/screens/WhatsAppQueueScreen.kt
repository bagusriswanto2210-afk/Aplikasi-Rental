package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Visibility
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
import com.example.data.model.WhatsAppMessage
import com.example.ui.viewmodel.RentalViewModel
import com.example.util.DateUtils
import com.example.util.WhatsAppHelper

@Composable
fun WhatsAppQueueScreen(
    viewModel: RentalViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val messages by viewModel.allWhatsAppMessages.collectAsState()

    var statusFilter by remember { mutableStateOf("Semua") } // "Semua", "Belum Dikirim", "Sudah Dikirim"
    var previewMessage by remember { mutableStateOf<WhatsAppMessage?>(null) }
    var cronResultText by remember { mutableStateOf<String?>(null) }

    val pendingCount = messages.count { !it.isSent }
    val sentCount = messages.count { it.isSent }

    val filteredMessages = remember(messages, statusFilter) {
        when (statusFilter) {
            "Belum Dikirim" -> messages.filter { !it.isSent }
            "Sudah Dikirim" -> messages.filter { it.isSent }
            else -> messages
        }
    }

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
                text = "Antrean Notifikasi WhatsApp",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Manajemen antrean pesan konfirmasi, pengingat kembali & tagihan",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }

        // Cron Scheduler Banner (Requested Feature)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Cron Scheduler Pengingat",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Mengecek tabel rentals setiap hari untuk memicu pesan pengingat jatuh tempo & denda keterlambatan secara otomatis.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.triggerAutoReminderScheduler { count ->
                                cronResultText = if (count > 0) {
                                    "Berhasil! Menemukan $count transaksi yang perlu diingatkan dan menambahkan pesan ke antrean."
                                } else {
                                    "Semua pengingat hari ini sudah tersusun dalam antrean atau tidak ada sewa yang jatuh tempo."
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("run_cron_scheduler_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Autorenew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Jalankan Auto-Reminder Hari Ini", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Status Tabs & Counter
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = statusFilter == "Semua",
                    onClick = { statusFilter = "Semua" },
                    label = { Text("Semua (${messages.size})", fontSize = 12.sp) }
                )
                FilterChip(
                    selected = statusFilter == "Belum Dikirim",
                    onClick = { statusFilter = "Belum Dikirim" },
                    label = { Text("Pending ($pendingCount)", fontSize = 12.sp) }
                )
                FilterChip(
                    selected = statusFilter == "Sudah Dikirim",
                    onClick = { statusFilter = "Sudah Dikirim" },
                    label = { Text("Terkirim ($sentCount)", fontSize = 12.sp) }
                )
            }
        }

        // Messages List
        if (filteredMessages.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.MarkEmailRead,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Antrean pesan kosong",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        } else {
            items(filteredMessages, key = { it.id }) { message ->
                WhatsAppQueueItemCard(
                    message = message,
                    onSendNow = {
                        WhatsAppHelper.openWhatsApp(context, message.customerPhone, message.messageContent)
                        viewModel.sendWhatsAppMessage(message)
                    },
                    onPreview = {
                        previewMessage = message
                    },
                    onMarkSent = {
                        viewModel.sendWhatsAppMessage(message)
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Cron Result Dialog
    if (cronResultText != null) {
        AlertDialog(
            onDismissRequest = { cronResultText = null },
            title = { Text("Scheduler Pengingat") },
            text = { Text(cronResultText ?: "") },
            confirmButton = {
                Button(onClick = { cronResultText = null }) {
                    Text("OK")
                }
            }
        )
    }

    // Message Preview Dialog
    if (previewMessage != null) {
        val msg = previewMessage!!
        AlertDialog(
            onDismissRequest = { previewMessage = null },
            title = {
                Text("Draft Pesan WhatsApp", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("Tujuan: ${msg.customerName} (${msg.customerPhone})", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = msg.messageContent,
                            fontSize = 12.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        WhatsAppHelper.openWhatsApp(context, msg.customerPhone, msg.messageContent)
                        viewModel.sendWhatsAppMessage(msg)
                        previewMessage = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    modifier = Modifier.testTag("dialog_send_wa_button")
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Buka WhatsApp & Kirim", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("WhatsApp Message", msg.messageContent)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Teks pesan disalin ke clipboard", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Salin Teks")
                }
            }
        )
    }
}

@Composable
fun WhatsAppQueueItemCard(
    message: WhatsAppMessage,
    onSendNow: () -> Unit,
    onPreview: () -> Unit,
    onMarkSent: () -> Unit
) {
    val (typeColor, typeLabel) = when (message.messageType) {
        "CONFIRMATION_DP" -> Pair(Color(0xFF1565C0), "Nota Booking & DP")
        "RETURN_REMINDER" -> Pair(Color(0xFFE65100), "Pengingat Pengembalian")
        "OVERDUE_ALERT" -> Pair(Color(0xFFC62828), "Peringatan Terlambat")
        else -> Pair(Color.DarkGray, message.messageType)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("wa_msg_card_${message.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (message.isSent)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (message.isSent) 0.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Message Type & Sent Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(typeColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = typeLabel,
                        color = typeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (message.isSent) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Terkirim", fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Color(0xFFE65100),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Menunggu Kirim", fontSize = 11.sp, color = Color(0xFFE65100), fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Customer Name & Phone
            Text(
                text = message.customerName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tujuan WA: ${message.customerPhone} • Nota #RNT-${message.rentalId}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Excerpt preview
            Text(
                text = message.messageContent.lines().take(3).joinToString("\n"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = DateUtils.formatDate(message.scheduledDate),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onPreview,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Lihat", fontSize = 11.sp)
                    }

                    if (!message.isSent) {
                        Button(
                            onClick = onSendNow,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("send_wa_btn_${message.id}")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Kirim WA", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
