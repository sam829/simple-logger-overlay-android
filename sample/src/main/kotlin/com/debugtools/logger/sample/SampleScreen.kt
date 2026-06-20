package com.debugtools.logger.sample

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.debugtools.logger.LoggerOverlay
import com.debugtools.logger.telemetry.SessionTelemetry
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SampleScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Logger Overlay Sample") }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {

            SectionLabel("Basic Logging")

            Button(onClick = { LoggerOverlay.v("Sample", "Verbose message from sample app") },
                modifier = Modifier.fillMaxWidth()) {
                Text("Log VERBOSE")
            }
            Button(onClick = { LoggerOverlay.d("Sample", "Debug message", mapOf("key" to "value")) },
                modifier = Modifier.fillMaxWidth()) {
                Text("Log DEBUG with data")
            }
            Button(onClick = { LoggerOverlay.i("Sample", "Info message — something important happened") },
                modifier = Modifier.fillMaxWidth()) {
                Text("Log INFO")
            }
            Button(onClick = { LoggerOverlay.w("Sample", "Warning — check this out") },
                modifier = Modifier.fillMaxWidth()) {
                Text("Log WARN")
            }
            Button(onClick = {
                LoggerOverlay.e("Sample", "Simulated error", RuntimeException("Boom"))
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Log ERROR with exception")
            }

            Spacer(Modifier.height(8.dp))
            SectionLabel("JSON Logging")

            Button(onClick = {
                LoggerOverlay.logJson(
                    tag = "API",
                    message = "User response",
                    json = """{"id":1,"name":"Ada Lovelace","email":"ada@example.com"}""",
                )
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Log JSON")
            }

            Spacer(Modifier.height(8.dp))
            SectionLabel("SessionTelemetry")

            Button(onClick = {
                SessionTelemetry.startSession(
                    sessionId = "demo-session",
                    name = "DemoFlow",
                    metadata = mapOf("screen" to "SampleScreen"),
                )
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Start session")
            }
            Button(onClick = {
                SessionTelemetry.logMilestone("demo-session", "button_tapped")
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Log milestone")
            }
            Button(onClick = {
                SessionTelemetry.endSession("demo-session", success = true)
            }, modifier = Modifier.fillMaxWidth()) {
                Text("End session (success)")
            }

            Spacer(Modifier.height(8.dp))
            SectionLabel("Network")

            Button(onClick = {
                // Fire a real HTTP request through the instrumented OkHttpClient
                scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    val app = context.applicationContext as SampleApp
                    try {
                        val request = okhttp3.Request.Builder()
                            .url("https://jsonplaceholder.typicode.com/todos/1")
                            .build()
                        app.httpClient.newCall(request).execute().close()
                    } catch (e: Exception) {
                        LoggerOverlay.e("Sample", "Network call failed", e)
                    }
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Make network request")
            }

            Spacer(Modifier.height(8.dp))
            SectionLabel("Export")

            OutlinedButton(onClick = {
                scope.launch {
                    val uri = LoggerOverlay.exportLogs(context)
                    if (uri != null) {
                        val share = Intent(Intent.ACTION_SEND).apply {
                            type = "application/json"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(share, "Share logs"))
                    }
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Export & share logs")
            }

            OutlinedButton(onClick = { LoggerOverlay.clearLogs() },
                modifier = Modifier.fillMaxWidth()) {
                Text("Clear logs")
            }

            Spacer(Modifier.height(8.dp))
            SectionLabel("Overlay")

            Button(onClick = { LoggerOverlay.show(context) },
                modifier = Modifier.fillMaxWidth()) {
                Text("Open overlay")
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text = text, style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
}
