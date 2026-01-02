package com.example.myapplication.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.MainActivity
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.viewmodel.ShareUiState
import com.example.myapplication.ui.viewmodel.ShareViewModel

class ReceiveShareActivity : ComponentActivity() {
    private val viewModel: ShareViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleIntent(intent)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ShareScreen(
                        viewModel = viewModel,
                        onFinished = { bookInfo ->
                            val intent = Intent(this, MainActivity::class.java).apply {
                                putExtra("parsed_book_info", bookInfo)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            }
                            startActivity(intent)
                            finish()
                        },
                        onCancel = {
                            finish()
                        }
                    )
                }
            }
        }
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            intent.getStringExtra(Intent.EXTRA_TEXT)?.let { sharedText ->
                viewModel.parseUrl(sharedText)
            }
        } else {
            finish()
        }
    }
}

@Composable
fun ShareScreen(
    viewModel: ShareViewModel,
    onFinished: (com.example.myapplication.util.ParsedBookInfo?) -> Unit,
    onCancel: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (uiState) {
            is ShareUiState.Idle -> {
                Text("Preparing...")
            }
            is ShareUiState.Loading -> {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Parsing web page...")
            }
            is ShareUiState.Success -> {
                val info = (uiState as ShareUiState.Success).bookInfo
                Text("Parsed: ${info.title}")
                LaunchedEffect(Unit) {
                    onFinished(info)
                }
            }
            is ShareUiState.Error -> {
                Text("Error: ${(uiState as ShareUiState.Error).message}")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onCancel) {
                    Text("Close")
                }
                TextButton(onClick = { onFinished(null) }) {
                    Text("Skip and add manually")
                }
            }
        }
    }
}
