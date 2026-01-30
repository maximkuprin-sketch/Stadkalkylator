package com.example.flyttstadning.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.io.OutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceListScreen(
    viewModel: PriceListViewModel = viewModel(factory = PriceListViewModel.Factory),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let { viewModel.importFile(it) }
        }
    )

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"),
        onResult = { uri: Uri? ->
            uri?.let {
                viewModel.exportCsv { content ->
                    try {
                        context.contentResolver.openOutputStream(it)?.use { os ->
                            os.write(content.toByteArray())
                        }
                        Toast.makeText(context, "Exported successfully", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Prislista") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            
            if (uiState.error != null) {
                AlertDialog(
                    onDismissRequest = { viewModel.clearError() },
                    title = { Text("Error") },
                    text = { Text(uiState.error!!) },
                    confirmButton = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("OK")
                        }
                    }
                )
            }

            Text("Senast importerad: ${uiState.lastImportDate}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { 
                    importLauncher.launch(arrayOf("text/plain", "text/csv", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) 
                }) {
                    Text("Importera fil")
                }
                
                OutlinedButton(onClick = { 
                    exportLauncher.launch("prislista_export.csv")
                }) {
                    Text("Exportera")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                LazyColumn {
                    items(uiState.priceList) { band ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${band.rangeStart}-${band.rangeEnd} kvm")
                                Text("${band.price} kr", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
