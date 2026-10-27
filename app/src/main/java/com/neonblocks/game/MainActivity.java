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
import android.webkit.JavascriptInterface;
import android.view.View;
import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

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
        browser.addJavascriptInterface(new HapticsBridge(), "AndroidHaptics");
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
    private final class HapticsBridge {
        @JavascriptInterface public void pulse(int strength, String kind) {
            if (strength <= 0) return;
            runOnUiThread(() -> {
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                if (vibrator == null || !vibrator.hasVibrator()) return;
                int amp = strength == 1 ? 45 : (strength == 2 ? 90 : 150);
                long base = strength == 1 ? 6 : (strength == 2 ? 10 : 15);
                long[] pattern;
                int[] amps;
                if ("line4".equals(kind)) {
                    pattern = new long[]{0, base+3, 24, base+8, 22, base+15};
                    amps = new int[]{0, amp, 0, Math.min(255,amp+35), 0, Math.min(255,amp+70)};
                } else if ("line3".equals(kind)) {
                    pattern = new long[]{0, base+2, 24, base+3, 24, base+5};
                    amps = new int[]{0, amp, 0, amp, 0, Math.min(255,amp+30)};
                } else if ("line2".equals(kind)) {
                    pattern = new long[]{0, base+2, 28, base+4};
                    amps = new int[]{0, amp, 0, Math.min(255,amp+25)};
                } else {
                    long d = base;
                    if ("hard".equals(kind)) d += 5;
                    else if ("lock".equals(kind) || "line1".equals(kind)) d += 3;
                    else if ("move".equals(kind)) d = Math.max(3, base-3);
                    pattern = new long[]{0, d};
                    amps = new int[]{0, amp};
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, amps, -1));
                } else {
                    vibrator.vibrate(pattern, -1);
                }
            });
        }
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
