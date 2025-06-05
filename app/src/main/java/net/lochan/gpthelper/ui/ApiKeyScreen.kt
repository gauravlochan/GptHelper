package net.lochan.gpthelper.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.testTag
import net.lochan.gpthelper.viewmodel.ApiKeyState
import net.lochan.gpthelper.viewmodel.ApiKeyViewModel

/**
 * Screen for managing the ChatGPT API key.
 * This screen provides a user interface for:
 * - Entering a new API key
 * - Validating the entered key
 * - Viewing the current key status
 * - Clearing a saved key
 * 
 * The screen uses a ViewModel to handle the business logic and state management,
 * ensuring a clean separation of concerns between UI and data operations.
 * 
 * @param onNavigateBack Callback function to handle navigation back to the previous screen
 * @param viewModel The ViewModel that manages the API key state and operations
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiKeyScreen(
    onNavigateBack: () -> Unit,
    viewModel: ApiKeyViewModel = viewModel()
) {
    // Local state for the API key input field
    var apiKey by remember { mutableStateOf("") }
    // Collect the current API key state from the ViewModel
    val apiKeyState by viewModel.apiKeyState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("API Key Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // API Key Input Field
            // Uses password transformation to mask the key for security
            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = { Text("ChatGPT API Key") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    // Show clear button only when there's text to clear
                    if (apiKey.isNotEmpty()) {
                        IconButton(onClick = { apiKey = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                }
            )

            // Status Message Section
            // Shows different messages based on the current API key state
            when (apiKeyState) {
                is ApiKeyState.Validating -> {
                    // Show loading indicator and message during validation
                    CircularProgressIndicator(
                        modifier = Modifier.testTag("loading_indicator")
                    )
                    Text(
                        "Validating API key...",
                        modifier = Modifier.testTag("loading_text")
                    )
                }
                is ApiKeyState.Invalid -> {
                    // Show error message for invalid keys
                    Text(
                        (apiKeyState as ApiKeyState.Invalid).message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                is ApiKeyState.Configured -> {
                    // Show success message when key is valid and saved
                    Text(
                        "API key is configured and valid.",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                else -> {}
            }

            // Action Buttons Section
            // Buttons are enabled/disabled based on the current state
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Save button - enabled when there's text and not currently validating
                Button(
                    onClick = { viewModel.validateAndSaveApiKey(apiKey) },
                    modifier = Modifier.weight(1f),
                    enabled = apiKey.isNotEmpty() && apiKeyState !is ApiKeyState.Validating
                ) {
                    Text("Save API Key")
                }

                // Clear button - only shown when a key is configured
                if (apiKeyState is ApiKeyState.Configured) {
                    OutlinedButton(
                        onClick = { viewModel.clearApiKey() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Clear API Key")
                    }
                }
            }
        }
    }
} 