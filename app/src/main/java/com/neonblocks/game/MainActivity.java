package com.neonblocks.game;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.view.View;

public class MainActivity extends Activity {
    private static final int CHOOSE_AUDIO = 291;
    private WebView browser;
    private ValueCallback<Uri[]> fileCallback;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        browser = new WebView(this);
        browser.setBackgroundColor(0xff080b20);
        WebSettings settings = browser.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        browser.setWebViewClient(new WebViewClient());
        browser.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback=callback;
                Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("audio/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
                startActivityForResult(Intent.createChooser(intent,"Add music to Neon Blocks"), CHOOSE_AUDIO);
                return true;
            }
        });
        setContentView(browser);
        browser.loadUrl("file:///android_asset/index.html");
    }
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == CHOOSE_AUDIO && fileCallback != null) {
            Uri[] items=null;
            if(resultCode==RESULT_OK && data!=null) {
                if(data.getClipData()!=null) {
                    int n=data.getClipData().getItemCount();
                    items=new Uri[n];
                    for(int i=0;i<n;i++) items[i]=data.getClipData().getItemAt(i).getUri();
                } else if(data.getData()!=null) items=new Uri[]{data.getData()};
            }
            fileCallback.onReceiveValue(items);
            fileCallback=null;
        }
    }
    @Override public void onBackPressed() {
        if(browser!=null) browser.evaluateJavascript("window.togglePause && window.togglePause()",null);
        else super.onBackPressed();
    }
    @Override protected void onDestroy() {
        if(fileCallback!=null) { fileCallback.onReceiveValue(null); fileCallback=null; }
        if(browser!=null) { browser.destroy();browser=null; }
        super.onDestroy();
    }
}
