package fr.infodelegue.app;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebResourceRequest;
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebChromeClient;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;

public class MainActivity extends Activity {
    private WebView webView;
    private static final String HOME = "file:///android_asset/site/index.html";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        webView.setBackgroundColor(0xFFF3F6F4);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return routeUrl(request.getUrl());
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return routeUrl(Uri.parse(url));
            }
        });
        setContentView(webView);
        webView.loadUrl(HOME);
    }

    private boolean routeUrl(Uri uri) {
        String url = uri.toString();
        String path = uri.getPath() == null ? "" : uri.getPath();
        if (url.startsWith("file:///android_asset/site/") && path.toLowerCase().endsWith(".pdf")) {
            String assetPath = path.substring("/android_asset/".length());
            try {
                File out = new File(getCacheDir(), new File(assetPath).getName());
                try (InputStream in = getAssets().open(assetPath);
                     FileOutputStream fos = new FileOutputStream(out)) {
                    byte[] buffer = new byte[8192];
                    int n;
                    while ((n = in.read(buffer)) > 0) fos.write(buffer, 0, n);
                }
                Uri contentUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", out);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(contentUri, "application/pdf");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Impossible d’ouvrir le PDF hors connexion.", Toast.LENGTH_LONG).show();
            }
            return true;
        }
        if (url.startsWith("file:///android_asset/site/")) return false;
        if (!"https".equals(uri.getScheme()) && !"http".equals(uri.getScheme())) return true;
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) webView.destroy();
        super.onDestroy();
    }
}
