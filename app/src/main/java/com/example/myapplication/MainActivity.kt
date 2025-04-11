package com.example.myapplication

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.ui.theme.MyApplicationTheme
import androidx.compose.ui.tooling.preview.Preview
import android.os.Build
import com.example.mylibrary.HelloWorld
import com.example.mylibrary.UserAgentManager
import com.example.mylibrary.ImageLoader
import com.example.mylibrary.UserAgentImage

class MainActivity : ComponentActivity() {
    companion object {
        const val BASE_IMAGE_ENGINE_URL = "https://blazing-fast-pics.cdn.imgeng.in"
        const val DEFAULT_IMAGE_URL = "images/pic_1_variation_2.jpg"
    }

    private lateinit var userAgentManager: UserAgentManager
    private lateinit var imageLoader: ImageLoader
    private val helloWorld = HelloWorld()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize library components
        userAgentManager = UserAgentManager(this)
        imageLoader = ImageLoader(this, userAgentManager)
        
        // Test the HelloWorld library
        val helloMessage = helloWorld.sayHello("ADITYA")
        Log.d("HelloLibrary", helloMessage)
        
        // Log User-Agent information
        userAgentManager.logUserAgentInfo()
        
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ImageDemoScreen(
                        modifier = Modifier.padding(innerPadding),
                        userAgentManager = userAgentManager,
                        imageLoader = imageLoader,
                        helloMessage = helloMessage
                    )
                }
            }
        }
    }
}

@Composable
fun ImageDemoScreen(
    modifier: Modifier = Modifier,
    userAgentManager: UserAgentManager,
    imageLoader: ImageLoader,
    helloMessage: String = ""
) {
    val context = LocalContext.current
    var currentImageUrl by remember { mutableStateOf<String?>(null) }
    var loadingState by remember { mutableStateOf("Initializing...") }

    LaunchedEffect(Unit) {
        val url = imageLoader.buildOptimizedImageUrl(
            MainActivity.BASE_IMAGE_ENGINE_URL,
            MainActivity.DEFAULT_IMAGE_URL
        )
        Log.d("ImageDemo", "Loading URL: $url")
        currentImageUrl = url
        loadingState = "Attempting to load: $url"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Hello World Card
        if (helloMessage.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                ) {
                    Text(
                        text = "Library Test:",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = helloMessage,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        
        // Debug Info Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Debug Info:",
                    style = MaterialTheme.typography.titleLarge
                )
                
                Text(
                    text = "Loading State: $loadingState",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "Network Type: ${if(userAgentManager.isSlowNetwork()) "Slow" else "Fast"}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "Native User Agent:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = userAgentManager.getNativeUserAgent(),
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = "WebView User Agent:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = userAgentManager.fullUserAgent,
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = "Client Hints:",
                    style = MaterialTheme.typography.bodyMedium
                )
                userAgentManager.getClientHintsHeaders().forEach { (key, value) ->
                    Text(
                        text = "$key: $value",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Image Display
        currentImageUrl?.let { url ->
            UserAgentImage(
                url = url,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentScale = ContentScale.Fit,
                contentDescription = "Demo Image",
                onError = { 
                    Log.e("ImageDemo", "Error loading image: $it")
                    loadingState = "Error loading image: ${it.message}"
                },
                onSuccess = { 
                    Log.d("ImageDemo", "Image loaded successfully")
                    loadingState = "Image loaded successfully!"
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ImageDemoScreenPreview() {
    val context = LocalContext.current
    val userAgentManager = UserAgentManager(context)
    val imageLoader = ImageLoader(context, userAgentManager)
    
    MyApplicationTheme {
        ImageDemoScreen(
            modifier = Modifier,
            userAgentManager = userAgentManager,
            imageLoader = imageLoader,
            helloMessage = "Hello World from MyLibrary!"
        )
    }
}