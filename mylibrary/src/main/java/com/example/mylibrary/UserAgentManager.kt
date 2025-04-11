package com.example.mylibrary

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import android.webkit.WebView

/**
 * A manager class for handling User-Agent and Client Hints headers
 */
class UserAgentManager(private val context: Context) {
    private val webView by lazy { WebView(context) }
    
    /**
     * Returns the WebView User-Agent string
     */
    val fullUserAgent: String
        get() = webView.settings.userAgentString

    /**
     * Returns the native system User-Agent string
     */
    fun getNativeUserAgent(): String {
        return System.getProperty("http.agent") ?: "Unknown"
    }

    /**
     * Returns a map of User-Agent Client Hints headers
     */
    fun getClientHintsHeaders(): Map<String, String> {
        return mapOf(
            "Sec-CH-UA-Platform" to "\"Android\"",
            "Sec-CH-UA-Platform-Version" to "\"${Build.VERSION.RELEASE}\"",
            "Sec-CH-UA-Model" to "\"${Build.MODEL}\"",
            "Sec-CH-UA" to getSecChUAString(),
            "Sec-CH-UA-Mobile" to "?1"
        )
    }

    /**
     * Returns all headers (User-Agent + Client Hints)
     */
    fun getAllHeaders(): Map<String, String> {
        val headers = mutableMapOf<String, String>()
        headers["User-Agent"] = fullUserAgent
        headers.putAll(getClientHintsHeaders())
        return headers
    }

    /**
     * Checks if the current network is slow
     */
    fun isSlowNetwork(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return true
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return true

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> false
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                // Check if on slower cellular connection
                !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
            }
            else -> true
        }
    }

    /**
     * Builds an image URL with compression if on slow network
     */
    fun buildImageUrl(baseUrl: String, imageUrl: String): String {
        // If on slow network, add compression, otherwise just load normally
        val finalUrl = when {
            isSlowNetwork() -> "$baseUrl/cmpr_50/$imageUrl"
            else -> "$baseUrl/$imageUrl"
        }
        Log.d("UserAgentManager", "Built URL: $finalUrl")
        return finalUrl
    }

    /**
     * Logs all User-Agent and Client Hints information
     */
    fun logUserAgentInfo() {
        Log.d("UserAgent", "Native UA: ${getNativeUserAgent()}")
        Log.d("UserAgent", "WebView UA: $fullUserAgent")
        
        getClientHintsHeaders().forEach { (key, value) ->
            Log.d("ClientHints", "$key: $value")
        }
    }

    private fun getSecChUAString(): String {
        val androidVersion = Build.VERSION.RELEASE
        val manufacturer = Build.MANUFACTURER
        return "\"Android $androidVersion\"; \"$manufacturer\"; \"Mobile\""
    }
} 