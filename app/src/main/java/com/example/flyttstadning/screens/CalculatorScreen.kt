package com.example.flyttstadning.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel = viewModel(factory = CalculatorViewModel.Factory),
    onNavigateToPriceList: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Flyttstädning Kalkylator") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = uiState.sqmInput,
                onValueChange = { viewModel.onSqmChange(it) },
                label = { Text("Antal kvm") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (uiState.calculatedPrice != null) {
                Text("Uppskattat pris:", style = MaterialTheme.typography.titleMedium)
                Text(
                    "${uiState.calculatedPrice} kr", 
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (uiState.error != null) {
                 Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onNavigateToPriceList,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Hantera Prislista")
            }
        }
    }
}
