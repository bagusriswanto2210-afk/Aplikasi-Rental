package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.data.model.RentalWithDetails
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object WhatsAppHelper {

    fun formatPhoneNumberForWhatsApp(phone: String): String {
        var clean = phone.replace(Regex("[^0-9]"), "")
        if (clean.startsWith("0")) {
            clean = "62" + clean.substring(1)
        } else if (clean.startsWith("+62")) {
            clean = clean.substring(1)
        } else if (!clean.startsWith("62")) {
            clean = "62$clean"
        }
        return clean
    }

    fun generateBookingConfirmationMessage(rentalWithDetails: RentalWithDetails): String {
        val rental = rentalWithDetails.rental
        val items = rentalWithDetails.items

        val itemsList = StringBuilder()
        items.forEachIndexed { index, item ->
            itemsList.append("${index + 1}. *${item.itemName}* (x${item.qty})\n")
            itemsList.append("   @ ${CurrencyUtils.formatRupiah(item.pricePerDay)}/hari = ${CurrencyUtils.formatRupiah(item.subtotal)}\n")
        }

        val paymentInfo = if (rental.remainingBalance <= 0) {
            "✅ *LUNAS* (Rp 0)"
        } else {
            "⏳ *SISA TAGIHAN:* ${CurrencyUtils.formatRupiah(rental.remainingBalance)}"
        }

        return """
*🏕️ NOTA BOOKING RENTAL OUTDOOR*
*Penyewaan Alat Outdoor & Camping*
----------------------------------------
*No. Transaksi:* #RNT-${rental.id}
*Nama Penyewa:* ${rental.customerName}
*No. HP/WA:* ${rental.customerPhone}
*Jaminan:* ${rental.guaranteeItem}

📅 *Jadwal Sewa:*
• Ambil: ${DateUtils.formatDate(rental.startDate)}
• Kembali: ${DateUtils.formatDate(rental.endDate)}
• Durasi: *${rental.durationDays} Hari*

🎒 *Daftar Alat Disewa:*
$itemsList
----------------------------------------
💰 *Rincian Pembayaran:*
• Total Sewa: *${CurrencyUtils.formatRupiah(rental.totalPrice)}*
• Uang Muka (DP): ${CurrencyUtils.formatRupiah(rental.dpAmount)}
• Status Pelunasan: $paymentInfo

⚠️ *Ketentuan:*
1. Harap membawa identitas asli (${rental.guaranteeItem}) saat pengambilan alat.
2. Alat dikembalikan dalam kondisi bersih, kering, dan lengkap.
3. Keterlambatan tanpa konfirmasi dikenakan denda sesuai tarif harian.

Terima kasih atas kepercayaannya! Selamat berpetualang dan jaga kelestarian alam! 🌲⛰️
        """.trimIndent()
    }

    fun generateReturnReminderMessage(rentalWithDetails: RentalWithDetails): String {
        val rental = rentalWithDetails.rental
        val isTodayReturn = DateUtils.isToday(rental.endDate)
        val waktuLabel = if (isTodayReturn) "*HARI INI*" else "*BESOK*"

        val paymentNote = if (rental.remainingBalance > 0) {
            "\n💵 *Sisa Pelunasan:* ${CurrencyUtils.formatRupiah(rental.remainingBalance)}"
        } else {
            "\n✅ *Status:* Lunas"
        }

        return """
*🔔 PENGINGAT PENGEMBALIAN ALAT OUTDOOR*
Halo Kak *${rental.customerName}*,

Kami ingin mengingatkan bahwa masa sewa peralatan camping Anda jatuh tempo $waktuLabel:
📅 *Batas Pengembalian:* ${DateUtils.formatDate(rental.endDate)}
🔖 *No. Nota:* #RNT-${rental.id}$paymentNote

🪪 Jaminan (${rental.guaranteeItem}) akan langsung diserahkan kembali setelah pengecekan kelengkapan & kebersihan alat.

Jika membutuhkan perpanjangan masa sewa, mohon segera konfirmasikan kepada kami. Terima kasih dan salam lestari! ⛺✨
        """.trimIndent()
    }

    fun generateOverdueAlertMessage(rentalWithDetails: RentalWithDetails): String {
        val rental = rentalWithDetails.rental
        val lateFeeText = if (rental.lateFeeAmount > 0) {
            "\n⚠️ *Denda Keterlambatan:* ${CurrencyUtils.formatRupiah(rental.lateFeeAmount)}"
        } else ""

        return """
*⚠️ PERINGATAN KETERLAMBATAN PENGEMBALIAN*
Yth. Kak *${rental.customerName}*,

Pemberitahuan bahwa sewa alat outdoor (Nota #RNT-${rental.id}) telah *MELEWATI BATAS WAKTU PENGEMBALIAN* (${DateUtils.formatDate(rental.endDate)}).
$lateFeeText
💵 *Total Tagihan Pelunasan:* ${CurrencyUtils.formatRupiah(rental.effectiveRemainingBalance)}
🪪 *Jaminan Tertahan:* ${rental.guaranteeItem}

Mohon segera mengembalikan peralatan ke toko kami atau hubungi kami untuk konfirmasi kendala di lapangan. Terima kasih atas kerjasamanya. 🙏🌲
        """.trimIndent()
    }

    fun openWhatsApp(context: Context, phone: String, message: String) {
        try {
            val formattedPhone = formatPhoneNumberForWhatsApp(phone)
            val encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8.toString())
            val url = "https://api.whatsapp.com/send?phone=$formattedPhone&text=$encodedMessage"
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Tidak dapat membuka WhatsApp: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}
