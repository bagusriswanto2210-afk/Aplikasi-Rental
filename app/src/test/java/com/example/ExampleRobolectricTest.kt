package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.BundlePreset
import com.example.data.model.Rental
import com.example.util.CurrencyUtils
import com.example.util.DateUtils
import com.example.util.WhatsAppHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Outdoor POS", appName)
    }

    @Test
    fun `test dp and remaining balance calculation`() {
        val totalPrice = 150000L
        val dpAmount = 50000L
        val remainingBalance = (totalPrice - dpAmount).coerceAtLeast(0L)
        assertEquals(100000L, remainingBalance)
    }

    @Test
    fun `test date duration calculation`() {
        val start = System.currentTimeMillis()
        val end = start + TimeUnit.DAYS.toMillis(3)
        val days = DateUtils.calculateDays(start, end)
        assertEquals(3, days)
    }

    @Test
    fun `test phone number cleanup for whatsapp`() {
        val phone1 = "081234567890"
        val formatted1 = WhatsAppHelper.formatPhoneNumberForWhatsApp(phone1)
        assertEquals("6281234567890", formatted1)

        val phone2 = "+62 812-3456-7890"
        val formatted2 = WhatsAppHelper.formatPhoneNumberForWhatsApp(phone2)
        assertEquals("6281234567890", formatted2)
    }

    @Test
    fun `test bundling presets configuration`() {
        val bundles = BundlePreset.PRESET_BUNDLES
        assertTrue(bundles.isNotEmpty())
        val summitBundle = bundles.first()
        assertTrue(summitBundle.savingsPerDay > 0)
        assertEquals("bundle_summit_2p", summitBundle.id)
    }

    @Test
    fun `test late fee engine and effective balance`() {
        val rental = Rental(
            id = 1,
            customerName = "Bagus",
            customerPhone = "08123456789",
            startDate = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(4),
            endDate = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2), // 2 days overdue
            durationDays = 2,
            totalPrice = 100000L,
            dpAmount = 30000L,
            remainingBalance = 70000L,
            guaranteeItem = "KTP",
            paymentStatus = "Partial",
            rentalStatus = "Overdue",
            lateFeeAmount = 50000L
        )

        assertEquals(150000L, rental.grandTotal)
        assertEquals(120000L, rental.effectiveRemainingBalance)
    }
}
