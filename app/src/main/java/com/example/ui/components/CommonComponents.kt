package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StatusBooked
import com.example.ui.theme.StatusCompleted
import com.example.ui.theme.StatusOverdue
import com.example.ui.theme.StatusPaid
import com.example.ui.theme.StatusPartial
import com.example.ui.theme.StatusRented
import com.example.ui.theme.StatusUnpaid

@Composable
fun PaymentStatusBadge(status: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor, label) = when (status) {
        "Paid" -> Triple(StatusPaid.copy(alpha = 0.15f), StatusPaid, "Lunas")
        "Partial" -> Triple(StatusPartial.copy(alpha = 0.15f), StatusPartial, "DP (Sebagian)")
        else -> Triple(StatusUnpaid.copy(alpha = 0.15f), StatusUnpaid, "Belum Bayar")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun RentalStatusBadge(status: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor, label) = when (status) {
        "Booked" -> Triple(StatusBooked.copy(alpha = 0.15f), StatusBooked, "Booked")
        "Rented" -> Triple(StatusRented.copy(alpha = 0.15f), StatusRented, "Disewa (Aktif)")
        "Completed" -> Triple(StatusCompleted.copy(alpha = 0.15f), StatusCompleted, "Selesai")
        "Overdue" -> Triple(StatusOverdue.copy(alpha = 0.15f), StatusOverdue, "Overdue")
        else -> Triple(Color.Gray.copy(alpha = 0.15f), Color.DarkGray, status)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
