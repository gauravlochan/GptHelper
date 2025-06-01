package net.lochan.gpthelper

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.lochan.gpthelper.ui.theme.GptHelperTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Handle shared URL if the app was launched from a share
        handleIntent(intent)
        
        setContent {
            GptHelperTheme {
                SharedUrlScreen()
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
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

@Composable
fun SharedUrlScreen() {
    val urls by SharedUrlState.urls.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shared URLs") }
            )
        }
    ) { padding ->
        if (urls.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No URLs shared yet")
            }
        } else {
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
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// Simple state holder for shared URLs
object SharedUrlState {
    private val _urls = mutableStateListOf<String>()
    val urls: List<String> = _urls

    fun addUrl(url: String) {
        _urls.add(0, url) // Add new URLs at the top
    }
}