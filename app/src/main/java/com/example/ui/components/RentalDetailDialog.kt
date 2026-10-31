package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.RentalWithDetails
import com.example.util.CurrencyUtils
import com.example.util.DateUtils
import com.example.util.WhatsAppHelper

@Composable
fun RentalDetailDialog(
    rentalWithDetails: RentalWithDetails,
    onDismiss: () -> Unit,
    onSettlePayment: () -> Unit,
    onMarkCompleted: () -> Unit,
    onMarkRented: () -> Unit
) {
    val context = LocalContext.current
    val rental = rentalWithDetails.rental
    val items = rentalWithDetails.items

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Nota Transaksi Rental",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "#RNT-${rental.id} • ${DateUtils.formatDateTime(rental.createdAt)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_dialog_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Status row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RentalStatusBadge(rental.rentalStatus)
                    PaymentStatusBadge(rental.paymentStatus)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Customer & Guarantee Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Informasi Penyewa",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Nama Penyewa:", style = MaterialTheme.typography.bodyMedium)
                            Text(rental.customerName, fontWeight = FontWeight.SemiBold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("No. WhatsApp:", style = MaterialTheme.typography.bodyMedium)
                            Text(rental.customerPhone, fontWeight = FontWeight.SemiBold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Jaminan Ditahan:", style = MaterialTheme.typography.bodyMedium)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(rental.guaranteeItem, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Schedule Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Jadwal Sewa (${rental.durationDays} Hari)",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tanggal Ambil:", style = MaterialTheme.typography.bodyMedium)
                            Text(DateUtils.formatDate(rental.startDate), fontWeight = FontWeight.SemiBold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Batas Pengembalian:", style = MaterialTheme.typography.bodyMedium)
                            Text(DateUtils.formatDate(rental.endDate), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Items Table
                Text(
                    text = "Rincian Peralatan Outdoor",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    items.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.itemName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${item.qty} unit x ${CurrencyUtils.formatRupiah(item.pricePerDay)}/hari",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            Text(
                                text = CurrencyUtils.formatRupiah(item.subtotal),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Finance Breakdown Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Biaya Sewa:", fontWeight = FontWeight.Bold)
                            Text(CurrencyUtils.formatRupiah(rental.totalPrice), fontWeight = FontWeight.Bold)
                        }

                        if (rental.lateFeeAmount > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Denda Keterlambatan:", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                Text("+ " + CurrencyUtils.formatRupiah(rental.lateFeeAmount), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Grand Total:", fontWeight = FontWeight.Bold)
                                Text(CurrencyUtils.formatRupiah(rental.grandTotal), fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Uang Muka (DP):")
                            Text(CurrencyUtils.formatRupiah(rental.dpAmount))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Sisa Tagihan Pelunasan:", fontWeight = FontWeight.Bold)
                            Text(
                                text = CurrencyUtils.formatRupiah(rental.effectiveRemainingBalance),
                                fontWeight = FontWeight.Bold,
                                color = if (rental.effectiveRemainingBalance > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // QR Code Verification Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1B4332))
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("QR", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("#${rental.id}", color = Color(0xFFD8F3DC), fontSize = 10.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("QR Nota Digital Resmi", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("ID: RNT-${rental.id}-${rental.customerPhone.takeLast(4)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            Text("Scan untuk verifikasi pengembalian & cek jaminan", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }

                if (rental.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Catatan: ${rental.notes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                // 1. WhatsApp Button
                Button(
                    onClick = {
                        val msg = WhatsAppHelper.generateBookingConfirmationMessage(rentalWithDetails)
                        WhatsAppHelper.openWhatsApp(context, rental.customerPhone, msg)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("whatsapp_share_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Kirim Nota via WhatsApp", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Status transition buttons
                if (rental.rentalStatus == "Booked") {
                    Button(
                        onClick = {
                            onMarkRented()
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("mark_rented_button")
                    ) {
                        Text("Tandai Alat Diambil (Aktifkan Sewa)")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (rental.rentalStatus != "Completed") {
                    Button(
                        onClick = {
                            onMarkCompleted()
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("mark_completed_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Tandai Dikembalikan (Stok Otomatis Kembali)")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (rental.remainingBalance > 0) {
                    OutlinedButton(
                        onClick = {
                            onSettlePayment()
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settle_payment_button")
                    ) {
                        Text("Pelunasan Sisa Pembayaran (Rp ${CurrencyUtils.formatRupiah(rental.remainingBalance)})")
                    }
                }
            }
        }
    }
}
