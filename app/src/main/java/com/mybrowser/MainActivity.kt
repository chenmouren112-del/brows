package com.mybrowser

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

data class BrowserTab(
    val webView: WebView,
    var title: String = "新标签页",
    var url: String = "https://www.google.com"
)

class MainActivity : AppCompatActivity() {

    private val tabs = mutableListOf<BrowserTab>()
    private var currentTabIndex = 0

    private lateinit var webViewContainer: FrameLayout
    private lateinit var addressBar: EditText
    private lateinit var tabStrip: LinearLayout
    private lateinit var btnBack: ImageButton
    private lateinit var btnForward: ImageButton
    private lateinit var btnNewTab: ImageButton
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        setupListeners()
        createNewTab("https://www.google.com")
    }

    private fun initViews() {
        webViewContainer = findViewById(R.id.webViewContainer)
        addressBar = findViewById(R.id.addressBar)
        tabStrip = findViewById(R.id.tabStrip)
        btnBack = findViewById(R.id.btnBack)
        btnForward = findViewById(R.id.btnForward)
        btnNewTab = findViewById(R.id.btnNewTab)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            tabs.getOrNull(currentTabIndex)?.webView?.let { wv ->
                if (wv.canGoBack()) wv.goBack()
            }
        }
        btnForward.setOnClickListener {
            tabs.getOrNull(currentTabIndex)?.webView?.let { wv ->
                if (wv.canGoForward()) wv.goForward()
            }
        }
        btnNewTab.setOnClickListener {
            createNewTab("https://www.google.com")
        }
        addressBar.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_GO ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                navigate(addressBar.text.toString())
                hideKeyboard(v)
                true
            } else false
        }
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun createWebView(): WebView {
        return WebView(this).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                builtInZoomControls = true
                displayZoomControls = false
                setSupportZoom(true)
                setSupportMultipleWindows(false)
                mediaPlaybackRequiresUserGesture = false
            }
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                    progressBar.visibility = View.VISIBLE
                    progressBar.progress = 10
                    if (tabs.getOrNull(currentTabIndex)?.webView == view) {
                        addressBar.setText(url)
                    }
                    updateNavigationButtons()
                }
                override fun onPageFinished(view: WebView, url: String) {
                    progressBar.visibility = View.GONE
                    val tab = tabs.find { it.webView == view }
                    tab?.url = url
                    tab?.title = view.title ?: url
                    if (tabs.getOrNull(currentTabIndex)?.webView == view) {
                        addressBar.setText(url)
                    }
                    updateTabStrip()
                    updateNavigationButtons()
                }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView, newProgress: Int) {
                    progressBar.progress = newProgress
                    if (newProgress == 100) progressBar.visibility = View.GONE
                }
                override fun onReceivedTitle(view: WebView, title: String) {
                    val tab = tabs.find { it.webView == view }
                    tab?.title = title
                    updateTabStrip()
                }
            }
        }
    }

    private fun createNewTab(url: String) {
        val wv = createWebView()
        val tab = BrowserTab(webView = wv, url = url)
        tabs.add(tab)
        webViewContainer.addView(wv, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))
        switchToTab(tabs.size - 1)
        wv.loadUrl(normalizeUrl(url))
    }

    private fun switchToTab(index: Int) {
        tabs.forEachIndexed { i, tab ->
            tab.webView.visibility = if (i == index) View.VISIBLE else View.GONE
        }
        currentTabIndex = index
        val cur = tabs.getOrNull(index)
        addressBar.setText(cur?.url ?: "")
        updateNavigationButtons()
        updateTabStrip()
    }

    private fun closeTab(index: Int) {
        if (tabs.size <= 1) {
            tabs[0].webView.loadUrl("https://www.google.com")
            return
        }
        val tab = tabs[index]
        webViewContainer.removeView(tab.webView)
        tab.webView.destroy()
        tabs.removeAt(index)
        val newIdx = if (index >= tabs.size) tabs.size - 1 else index
        switchToTab(newIdx)
    }

    private fun updateTabStrip() {
        tabStrip.removeAllViews()
        tabs.forEachIndexed { index, tab ->
            val tabView = LayoutInflater.from(this).inflate(R.layout.tab_item, tabStrip, false)
            val tabTitle = tabView.findViewById<TextView>(R.id.tabTitle)
            val tabClose = tabView.findViewById<ImageButton>(R.id.tabClose)
            val shortTitle = if (tab.title.length > 16) tab.title.take(16) + "…" else tab.title
            tabTitle.text = shortTitle
            if (index == currentTabIndex) {
                tabView.setBackgroundResource(R.drawable.tab_selected_bg)
                tabTitle.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            } else {
                tabView.setBackgroundResource(R.drawable.tab_normal_bg)
                tabTitle.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
            }
            tabView.setOnClickListener { switchToTab(index) }
            tabClose.setOnClickListener { closeTab(index) }
            tabStrip.addView(tabView)
        }
    }

    private fun updateNavigationButtons() {
        val wv = tabs.getOrNull(currentTabIndex)?.webView
        btnBack.isEnabled = wv?.canGoBack() == true
        btnBack.alpha = if (btnBack.isEnabled) 1f else 0.35f
        btnForward.isEnabled = wv?.canGoForward() == true
        btnForward.alpha = if (btnForward.isEnabled) 1f else 0.35f
    }

    private fun navigate(input: String) {
        val url = normalizeUrl(input)
        tabs.getOrNull(currentTabIndex)?.webView?.loadUrl(url)
        tabs.getOrNull(currentTabIndex)?.url = url
    }

    private fun normalizeUrl(input: String): String {
        val trimmed = input.trim()
        return when {
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
            trimmed.contains(".") && !trimmed.contains(" ") -> "https://$trimmed"
            else -> "https://www.google.com/search?q=${trimmed.replace(" ", "+")}"
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val wv = tabs.getOrNull(currentTabIndex)?.webView
        if (wv?.canGoBack() == true) wv.goBack()
        else super.onBackPressed()
    }

    override fun onDestroy() {
        tabs.forEach { it.webView.destroy() }
        super.onDestroy()
    }
}
