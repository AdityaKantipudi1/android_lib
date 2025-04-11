package com.example.mylibrary

import android.content.Context
import coil.request.ImageRequest
import coil.compose.AsyncImage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext

/**
 * Utility class for loading images with User-Agent headers
 */
class ImageLoader(private val context: Context, private val userAgentManager: UserAgentManager) {
    
    /**
     * Builds an ImageRequest with User-Agent headers
     */
    fun buildImageRequest(url: String): ImageRequest {
        return ImageRequest.Builder(context)
            .data(url)
            .apply {
                userAgentManager.getAllHeaders().forEach { (key, value) ->
                    addHeader(key, value)
                }
            }
            .crossfade(true)
            .build()
    }
    
    /**
     * Builds an optimized image URL based on network conditions
     */
    fun buildOptimizedImageUrl(baseUrl: String, imageUrl: String): String {
        return userAgentManager.buildImageUrl(baseUrl, imageUrl)
    }
}

/**
 * Composable function for loading images with User-Agent headers
 */
@Composable
fun UserAgentImage(
    url: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    contentDescription: String? = null,
    onSuccess: () -> Unit = {},
    onError: (Throwable) -> Unit = {}
) {
    val context = LocalContext.current
    val userAgentManager = remember { UserAgentManager(context) }
    val imageLoader = remember { ImageLoader(context, userAgentManager) }
    
    AsyncImage(
        model = imageLoader.buildImageRequest(url),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        onSuccess = { onSuccess() },
        onError = { onError(it.result.throwable) }
    )
} 