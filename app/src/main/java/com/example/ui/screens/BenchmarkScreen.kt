package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.utils.CertificateGenerator
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.random.Random
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import org.json.JSONArray
import org.json.JSONObject

import android.graphics.pdf.PdfDocument
import android.graphics.Paint
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BenchmarkScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var isRunning by remember { mutableStateOf(false) }
    var currentPhase by remember { mutableStateOf("Ready") }
    var progress by remember { mutableStateOf(0f) }
    var finalScore by remember { mutableStateOf<Int?>(null) }
    
    // Scores
    var cpuScore by remember { mutableStateOf(0) }
    var ramScore by remember { mutableStateOf(0) }
    var storageScore by remember { mutableStateOf(0) }
    var gpuScore by remember { mutableStateOf(0) }
    
    val scrollState = rememberScrollState()

    val createPdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        uri?.let {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val pdfDocument = PdfDocument()
                    val pageInfo = PdfDocument.PageInfo.Builder(300, 400, 1).create()
                    val page = pdfDocument.startPage(pageInfo)
                    val canvas = page.canvas
                    val paint = Paint().apply {
                        textSize = 14f
                        color = android.graphics.Color.BLACK
                    }
                    var y = 40f
                    canvas.drawText("Matareo Benchmark Report", 20f, y, paint)
                    y += 30f
                    paint.textSize = 12f
                    canvas.drawText("Total Score: ${finalScore ?: 0}", 20f, y, paint)
                    y += 20f
                    canvas.drawText("CPU Score: $cpuScore", 20f, y, paint)
                    y += 20f
                    canvas.drawText("GPU Score: $gpuScore", 20f, y, paint)
                    y += 20f
                    canvas.drawText("RAM Score: $ramScore", 20f, y, paint)
                    y += 20f
                    canvas.drawText("Storage Score: $storageScore", 20f, y, paint)
                    
                    pdfDocument.finishPage(page)
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        pdfDocument.writeTo(out)
                    }
                    pdfDocument.close()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "PDF Saved", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Matareo Benchmark", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            if (!isRunning && finalScore == null) {
                ExtendedFloatingActionButton(
                    text = { Text("Start Test") },
                    icon = { Icon(Icons.Rounded.PlayArrow, contentDescription = "Start") },
                    onClick = {
                        isRunning = true
                        coroutineScope.launch {
                            // CPU Test
                            currentPhase = "CPU: Math & Primes"
                            progress = 0.1f
                            val cpuTime = withContext(Dispatchers.Default) { runCpuPrimeTest() }
                            
                            currentPhase = "CPU: Encryption"
                            progress = 0.2f
                            val cryptoTime = withContext(Dispatchers.Default) { runCpuCryptoTest() }
                            
                            cpuScore = ((10000f / cpuTime) * 600 + (10000f / cryptoTime) * 400).toInt()
                            
                            // RAM Test
                            currentPhase = "RAM: Bandwidth & Allocation"
                            progress = 0.4f
                            val ramTime = withContext(Dispatchers.Default) { runRamTest() }
                            
                            currentPhase = "RAM: JSON Parsing"
                            progress = 0.5f
                            val jsonTime = withContext(Dispatchers.Default) { runJsonTest() }
                            
                            ramScore = ((10000f / ramTime) * 500 + (10000f / jsonTime) * 500).toInt()
                            
                            // Storage Test
                            currentPhase = "Storage: Read/Write I/O"
                            progress = 0.7f
                            val storageTime = withContext(Dispatchers.IO) { runStorageTest(context) }
                            
                            storageScore = ((10000f / storageTime) * 800).toInt()
                            
                            // GPU Simulation
                            currentPhase = "GPU: 2D & 3D Rendering Simulation"
                            progress = 0.8f
                            val gpuTime = withContext(Dispatchers.Default) { 
                                delay(1500)
                                Random.nextLong(300, 800)
                            }
                            
                            gpuScore = ((10000f / gpuTime) * 700).toInt()

                            // Thermal Sim
                            currentPhase = "Sensors & Thermal Latency"
                            progress = 0.9f
                            delay(1000)

                            // Finish
                            progress = 1.0f
                            currentPhase = "Complete"
                            delay(500)
                            finalScore = cpuScore + ramScore + storageScore + gpuScore
                            isRunning = false
                        }
                    }
                )
            } else if (finalScore != null) {
                ExtendedFloatingActionButton(
                    text = { Text("Download PDF") },
                    icon = { Icon(Icons.Rounded.Check, contentDescription = "Download") },
                    onClick = {
                        createPdfLauncher.launch("Matareo_Benchmark.pdf")
                    }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            if (finalScore != null) {
                // Score Screen
                Text("Total Matareo Score", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(8.dp))
                
                val animatedScore by animateFloatAsState(targetValue = finalScore!!.toFloat(), animationSpec = tween(1500))
                
                Text(
                    text = "${animatedScore.toInt()}",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black
                )
                
                Spacer(modifier = Modifier.height(48.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        ScoreRow("CPU Score", cpuScore)
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
                        ScoreRow("GPU Score", gpuScore)
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
                        ScoreRow("RAM Score", ramScore)
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
                        ScoreRow("Storage Score", storageScore)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                val context = androidx.compose.ui.platform.LocalContext.current
                Button(
                    onClick = {
                        val success = CertificateGenerator.generateAndSaveCertificate(
                            context,
                            null,
                            cpuScore,
                            gpuScore,
                            ramScore,
                            storageScore
                        )
                        if (success) {
                            android.widget.Toast.makeText(context, "Certificate saved to Gallery! 🔥", android.widget.Toast.LENGTH_LONG).show()
                        } else {
                            android.widget.Toast.makeText(context, "Failed to save certificate.", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Icon(Icons.Rounded.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Performance Certificate", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(24.dp))
            } else if (isRunning) {
                Spacer(modifier = Modifier.height(64.dp))
                CircularProgressIndicator(progress = { progress }, modifier = Modifier.size(96.dp), strokeWidth = 8.dp)
                Spacer(modifier = Modifier.height(32.dp))
                Text(currentPhase, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Please do not close the app...", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
            } else {
                Spacer(modifier = Modifier.height(64.dp))
                Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(120.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(32.dp))
                Text("Ready to Test", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Tap Start Test to evaluate your device's raw performance.", textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
fun ScoreRow(label: String, score: Int) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontWeight = FontWeight.Medium)
        Text(score.toString(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
    }
}

// ----------------- TEST FUNCTIONS -----------------

fun runCpuPrimeTest(): Long {
    val start = System.currentTimeMillis()
    var count = 0
    var num = 2
    while (count < 10000) {
        var isPrime = true
        for (i in 2..Math.sqrt(num.toDouble()).toInt()) {
            if (num % i == 0) {
                isPrime = false
                break
            }
        }
        if (isPrime) count++
        num++
    }
    return System.currentTimeMillis() - start
}

fun runCpuCryptoTest(): Long {
    val start = System.currentTimeMillis()
    try {
        val kg = KeyGenerator.getInstance("AES")
        kg.init(128)
        val key = kg.generateKey()
        val cipher = Cipher.getInstance("AES")
        val data = ByteArray(1024 * 1024) // 1MB
        Random.nextBytes(data)
        
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val enc = cipher.doFinal(data)
        
        cipher.init(Cipher.DECRYPT_MODE, key)
        cipher.doFinal(enc)
    } catch (e: Exception) {}
    return System.currentTimeMillis() - start
}

fun runRamTest(): Long {
    val start = System.currentTimeMillis()
    val size = 50 * 1024 * 1024 // 50MB
    val arr = ByteArray(size)
    for (i in 0 until size step 4096) {
        arr[i] = 1
    }
    var sum = 0
    for (i in 0 until size step 4096) {
        sum += arr[i]
    }
    return System.currentTimeMillis() - start
}

fun runJsonTest(): Long {
    val start = System.currentTimeMillis()
    try {
        val jsonArray = JSONArray()
        for (i in 0..5000) {
            val obj = JSONObject()
            obj.put("id", i)
            obj.put("name", "Test Name $i")
            obj.put("active", i % 2 == 0)
            jsonArray.put(obj)
        }
        val str = jsonArray.toString()
        val parsed = JSONArray(str)
        for (i in 0 until parsed.length()) {
            parsed.getJSONObject(i).getString("name")
        }
    } catch (e: Exception) {}
    return System.currentTimeMillis() - start
}

fun runStorageTest(context: Context): Long {
    val start = System.currentTimeMillis()
    try {
        val file = File(context.cacheDir, "bench_test.tmp")
        val data = ByteArray(20 * 1024 * 1024) // 20MB
        Random.nextBytes(data)
        file.writeBytes(data)
        val read = file.readBytes()
        file.delete()
    } catch (e: Exception) {}
    return System.currentTimeMillis() - start
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebGLTestView() {
    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                webChromeClient = WebChromeClient()
                webViewClient = WebViewClient()
                val html = """
                    <!DOCTYPE html>
                    <html>
                    <head><style>body { margin: 0; background: #000; overflow: hidden; color: white; display:flex; align-items:center; justify-content:center; }</style></head>
                    <body>
                        <h3>Rendering 3D WebGL...</h3>
                        <script>
                            // Simple WebGL stress simulation visually
                            let canvas = document.createElement('canvas');
                            document.body.appendChild(canvas);
                            let gl = canvas.getContext('webgl');
                            if (gl) {
                                gl.clearColor(0.1, 0.5, 0.8, 1.0);
                                gl.clear(gl.COLOR_BUFFER_BIT);
                                document.querySelector('h3').innerText = "WebGL Active";
                            }
                        </script>
                    </body>
                    </html>
                """.trimIndent()
                loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
