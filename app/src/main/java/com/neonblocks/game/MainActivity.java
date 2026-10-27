package com.neonblocks.game;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    private WebView browser;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        browser = new WebView(this);
        browser.setBackgroundColor(0xff080b20);
        WebSettings settings = browser.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        browser.setWebChromeClient(new WebChromeClient());
        browser.setWebViewClient(new WebViewClient());
        setContentView(browser);
        browser.loadUrl("file:///android_asset/index.html");
    }
    @Override public void onBackPressed() {
        if (browser != null) browser.evaluateJavascript("window.togglePause && window.togglePause()", null);
        else super.onBackPressed();
    }
    @Override protected void onDestroy() {
        if (browser != null) { browser.destroy(); browser = null; }
        super.onDestroy();
    }
}
