package id.komunitasku.lite

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream

class MainActivity : Activity() {

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Pure lightweight programmatic WebView (zero XML overhead)
        webView = WebView(this).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                allowFileAccess = true
                cacheMode = WebSettings.LOAD_DEFAULT
                setSupportZoom(false)
            }

            addJavascriptInterface(AndroidBridge(this@MainActivity), "AndroidNative")

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val url = request?.url?.toString() ?: return false
                    // Intercept WhatsApp links and open native WhatsApp app directly
                    if (url.startsWith("https://wa.me/") || url.startsWith("whatsapp://")) {
                        return try {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            true
                        } catch (e: Exception) {
                            Toast.makeText(
                                this@MainActivity,
                                "Aplikasi WhatsApp tidak ditemukan",
                                Toast.LENGTH_SHORT
                            ).show()
                            true
                        }
                    }
                    return false
                }
            }

            webChromeClient = WebChromeClient()
            loadUrl("file:///android_asset/index.html")
        }

        setContentView(webView)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (this::webView.isInitialized && webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    /**
     * Lightweight Native Kotlin Bridge for saving KTA PNG cards directly to Gallery
     * and copying WhatsApp reports to the Android clipboard.
     */
    class AndroidBridge(private val context: Context) {

        @JavascriptInterface
        fun copyToClipboard(text: String) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("Laporan KomunitasKu", text))
            Toast.makeText(context, "📋 Laporan disalin! Siap paste di Grup WA", Toast.LENGTH_SHORT).show()
        }

        @JavascriptInterface
        fun saveBase64Png(base64Data: String, fileName: String) {
            try {
                val pureBase64 = base64Data.substringAfter(",")
                val bytes = Base64.decode(pureBase64, Base64.DEFAULT)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val values = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                        put(
                            MediaStore.Images.Media.RELATIVE_PATH,
                            Environment.DIRECTORY_PICTURES + "/KomunitasKu"
                        )
                    }
                    val uri = context.contentResolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        values
                    )
                    if (uri != null) {
                        context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
                        Toast.makeText(
                            context,
                            "📥 Kartu KTA disimpan di Galeri (Pictures/KomunitasKu)",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    val dir = File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                        "KomunitasKu"
                    )
                    if (!dir.exists()) dir.mkdirs()
                    val file = File(dir, fileName)
                    FileOutputStream(file).use { it.write(bytes) }
                    Toast.makeText(
                        context,
                        "📥 Kartu KTA disimpan: ${file.absolutePath}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal menyimpan KTA: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
