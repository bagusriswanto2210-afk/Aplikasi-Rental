package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.CalendarScheduleScreen
import com.example.ui.screens.CheckoutScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.RentalsScreen
import com.example.ui.screens.WhatsAppQueueScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.RentalViewModel

enum class NavScreen(val label: String) {
    CHECKOUT("Kasir POS"),
    RENTALS("Sewa"),
    INVENTORY("Inventaris"),
    CALENDAR("Kalender"),
    WHATSAPP("Antrean WA")
}

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: RentalViewModel = viewModel()
                var currentScreen by remember { mutableStateOf(NavScreen.CHECKOUT) }
                val messages by viewModel.allWhatsAppMessages.collectAsState()
                val pendingWaCount = messages.count { !it.isSent }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Space Sebelah Kiri Atas Untuk Logo ("Logo Anda")
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                                            .border(
                                                1.dp,
                                                MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 14.dp, vertical = 7.dp)
                                            .testTag("store_logo_slot"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.Image,
                                                contentDescription = "Logo Anda",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Logo Anda",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 6.dp
                        ) {
                            NavigationBarItem(
                                selected = currentScreen == NavScreen.CHECKOUT,
                                onClick = { currentScreen = NavScreen.CHECKOUT },
                                icon = {
                                    Icon(
                                        Icons.Default.PointOfSale,
                                        contentDescription = "Kasir POS"
                                    )
                                },
                                label = { Text(NavScreen.CHECKOUT.label, fontSize = 11.sp) },
                                modifier = Modifier.testTag("nav_pos")
                            )

                            NavigationBarItem(
                                selected = currentScreen == NavScreen.RENTALS,
                                onClick = { currentScreen = NavScreen.RENTALS },
                                icon = {
                                    Icon(
                                        Icons.Default.Assignment,
                                        contentDescription = "Sewa"
                                    )
                                },
                                label = { Text(NavScreen.RENTALS.label, fontSize = 11.sp) },
                                modifier = Modifier.testTag("nav_rentals")
                            )

                            NavigationBarItem(
                                selected = currentScreen == NavScreen.INVENTORY,
                                onClick = { currentScreen = NavScreen.INVENTORY },
                                icon = {
                                    Icon(
                                        Icons.Default.Inventory2,
                                        contentDescription = "Inventaris"
                                    )
                                },
                                label = { Text(NavScreen.INVENTORY.label, fontSize = 11.sp) },
                                modifier = Modifier.testTag("nav_inventory")
                            )

                            NavigationBarItem(
                                selected = currentScreen == NavScreen.CALENDAR,
                                onClick = { currentScreen = NavScreen.CALENDAR },
                                icon = {
                                    Icon(
                                        Icons.Default.CalendarMonth,
                                        contentDescription = "Kalender"
                                    )
                                },
                                label = { Text(NavScreen.CALENDAR.label, fontSize = 11.sp) },
                                modifier = Modifier.testTag("nav_calendar")
                            )

                            NavigationBarItem(
                                selected = currentScreen == NavScreen.WHATSAPP,
                                onClick = { currentScreen = NavScreen.WHATSAPP },
                                icon = {
                                    BadgedBox(
                                        badge = {
                                            if (pendingWaCount > 0) {
                                                Badge {
                                                    Text("$pendingWaCount")
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Chat,
                                            contentDescription = "Antrean WA"
                                        )
                                    }
                                },
                                label = { Text(NavScreen.WHATSAPP.label, fontSize = 11.sp) },
                                modifier = Modifier.testTag("nav_whatsapp")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentScreen) {
                            NavScreen.CHECKOUT -> CheckoutScreen(
                                viewModel = viewModel,
                                onNavigateToRentals = { currentScreen = NavScreen.RENTALS }
                            )
                            NavScreen.RENTALS -> RentalsScreen(
                                viewModel = viewModel
                            )
                            NavScreen.INVENTORY -> InventoryScreen(
                                viewModel = viewModel
                            )
                            NavScreen.CALENDAR -> CalendarScheduleScreen(
                                viewModel = viewModel
                            )
                            NavScreen.WHATSAPP -> WhatsAppQueueScreen(
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}
