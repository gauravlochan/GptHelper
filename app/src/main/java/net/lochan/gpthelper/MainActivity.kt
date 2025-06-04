package net.lochan.gpthelper

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.lochan.gpthelper.ui.ApiKeyScreen
import net.lochan.gpthelper.ui.theme.GptHelperTheme
import java.net.URL

/**
 * Main activity of the application that handles URL sharing functionality.
 * This activity can be launched in two ways:
 * 1. Directly from the app launcher
 * 2. As a share target when sharing URLs from other apps
 * 
 * The activity also manages navigation between the main URL list screen
 * and the API key configuration screen.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Enables edge-to-edge display
        
        // Check if the app was launched via URL sharing
        handleIntent(intent)
        
        setContent {
            GptHelperTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // State to control which screen is currently shown
                    // This enables simple navigation between the main screen and API key screen
                    var showApiKeyScreen by remember { mutableStateOf(false) }
                    
                    if (showApiKeyScreen) {
                        // Show API key configuration screen when showApiKeyScreen is true
                        ApiKeyScreen(
                            onNavigateBack = { showApiKeyScreen = false }
                        )
                    } else {
                        // Show main URL list screen with a button to access API key settings
                        SharedUrlScreen(
                            onApiKeyClick = { showApiKeyScreen = true }
                        )
                    }
                }
            }
        }
    }

    /**
     * Called when a new intent is delivered to an already running activity.
     * This is important for handling URL shares when the app is already running.
     */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    /**
     * Processes the incoming intent to extract shared URLs.
     * @param intent The intent that started the activity or was delivered via onNewIntent
     */
    private fun handleIntent(intent: Intent?) {
        // Check if this is a share action with text/plain content
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (sharedText != null) {
                // TODO: In the next step, we'll add this to a proper data store
                // For now, we'll just update the UI state
                SharedUrlState.addUrl(sharedText)
            }
        }
    }
}

/**
 * Main screen composable that displays the list of shared URLs.
 * Includes a settings button in the top app bar to access API key configuration.
 * 
 * @param onApiKeyClick Callback function to handle navigation to the API key screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedUrlScreen(
    onApiKeyClick: () -> Unit
) {
    // Collect the list of URLs as state to trigger recomposition when it changes
    val urls = SharedUrlState.urls
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shared URLs") },
                actions = {
                    // Settings button to access API key configuration
                    IconButton(onClick = onApiKeyClick) {
                        Icon(Icons.Default.Settings, contentDescription = "API Key Settings")
                    }
                }
            )
        }
    ) { padding ->
        if (urls.isEmpty()) {
            // Show a centered message when no URLs are available
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No URLs shared yet")
            }
        } else {
            // Display the list of URLs in a scrollable column
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(urls) { url ->
                    UrlItem(url)
                }
            }
        }
    }
}

/**
 * Composable that displays a single URL in a card.
 * @param url The URL to display
 */
@Composable
fun UrlItem(url: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = url,
                maxLines = 2, // Limit to 2 lines to prevent very long URLs from taking too much space
                overflow = TextOverflow.Ellipsis // Show ... for truncated text
            )
        }
    }
}

/**
 * Simple state holder for managing shared URLs.
 * This is a temporary solution - in a production app, we would use a proper data store.
 */
object SharedUrlState {
    // Private mutable state list that holds the URLs
    private val _urls = mutableStateListOf<String>()
    // Public immutable list that can be observed by the UI
    val urls: List<String> = _urls

    /**
     * Adds a new URL to the list.
     * New URLs are added at the beginning of the list.
     * @param url The URL to add
     * @throws IllegalArgumentException if url is null or invalid
     */
    fun addUrl(url: String) {
        require(url.isNotEmpty()) { "URL cannot be empty" }
        try {
            // Validate URL format
            URL(url)
            _urls.add(0, url) // Add new URLs at the top
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid URL format: $url", e)
        }
    }

    /**
     * Clears all URLs from the list.
     */
    fun clear() {
        _urls.clear()
    }
}