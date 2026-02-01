package com.panaadhaar.extractor.india.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

data class RecentScan(
    val id: String,
    val documentType: String,
    val name: String?,
    val date: String
)

data class HomeUiState(
    val isPro: Boolean = false,
    val scansRemaining: Int = 5,
    val recentScans: List<RecentScan> = emptyList(),
    val isLoading: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    uiState: HomeUiState,
    onScanDocument: () -> Unit,
    onPickImage: () -> Unit,
    onPickPdf: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PAN & Aadhaar Extractor") },
                actions = {
                    IconButton(onClick = { navController.navigate("profiles") }) {
                        Icon(Icons.Default.Person, contentDescription = "Profiles")
                    }
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            if (!uiState.isPro) {
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate("paywall") },
                    icon = { Icon(Icons.Default.Star, contentDescription = null) },
                    text = { Text("Unlock Pro") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.isPro) 
                        MaterialTheme.colorScheme.primaryContainer 
                    else 
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (uiState.isPro) Icons.Default.Star else Icons.Default.Info,
                        contentDescription = null,
                        tint = if (uiState.isPro) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (uiState.isPro) "Pro Account" else "Free Account",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (uiState.isPro) 
                                "Unlimited scans" 
                            else 
                                "${uiState.scansRemaining} free scans remaining",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    if (!uiState.isPro) {
                        TextButton(onClick = { navController.navigate("paywall") }) {
                            Text("Upgrade")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Scan Document",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { 
                    onScanDocument()
                    navController.navigate("camera")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Scan Document")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { onPickImage() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Image, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pick Image")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { onPickPdf() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pick PDF")
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.recentScans.isNotEmpty()) {
                Text(
                    text = "Recent Scans",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.recentScans) { scan ->
                        RecentScanItem(
                            scan = scan,
                            onClick = { navController.navigate("profile_detail/${scan.id}") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentScanItem(
    scan: RecentScan,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (scan.documentType) {
                    "PAN" -> Icons.Default.CreditCard
                    "Aadhaar" -> Icons.Default.Badge
                    else -> Icons.Default.Description
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = scan.documentType,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (scan.name != null) {
                    Text(
                        text = scan.name,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Text(
                    text = scan.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View details"
            )
        }
    }
}
