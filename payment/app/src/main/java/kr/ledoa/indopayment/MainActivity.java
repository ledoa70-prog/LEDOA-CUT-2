package kr.ledoa.indopayment;

import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.os.Environment;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebResourceRequest;
import android.widget.Toast;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

public final class MainActivity extends Activity {
    private static final String SITE = "https://ledoa-indopayment.vercel.app/";
    private WebView webView;
    private final AtomicBoolean sharing = new AtomicBoolean(false);

    // The hosted calculator already renders the JPEG. Replace the browser's
    // unreliable Web Share implementation with a real Android photo-share.
    private static final String SHARE_JS =
        "(function(){"
      + "if(window.__LEDOA_APK_SHARE__)return;"
      + "window.__LEDOA_APK_SHARE__=true;"
      + "function canShare(d){return !!(d&&d.files&&d.files.length);}"
      + "function share(d){"
      + "var f=d&&d.files&&d.files[0];"
      + "if(!f)return Promise.reject(new TypeError('Image file missing'));"
      + "return f.arrayBuffer().then(function(buf){"
      + "var ar=new Uint8Array(buf),s='',step=16384;"
      + "for(var i=0;i<ar.length;i+=step){"
      + "s+=String.fromCharCode.apply(null,ar.subarray(i,i+step));"
      + "}"
      + "window.AndroidShare.shareJpeg(btoa(s));"
      + "return true;"
      + "});"
      + "}"
      + "try{Object.defineProperty(navigator,'canShare',{configurable:true,value:canShare});"
      + "Object.defineProperty(navigator,'share',{configurable:true,value:share});}"
      + "catch(e){navigator.canShare=canShare;navigator.share=share;}"
      + "})();";

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(23,39,55));
        getWindow().setNavigationBarColor(Color.BLACK);
        webView = new WebView(this);
        webView.setBackgroundColor(Color.WHITE);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(true);
        settings.setSupportMultipleWindows(false);
        webView.addJavascriptInterface(new ShareBridge(), "AndroidShare");
        webView.setWebViewClient(new WebViewClient() {
            @Override public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view,url);
                if (url.startsWith(SITE)) view.evaluateJavascript(SHARE_JS, null);
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri=request.getUrl();
                if ("https".equals(uri.getScheme()) && "ledoa-indopayment.vercel.app".equals(uri.getHost())) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, uri)); }
                catch (Exception ignored) {}
                return true;
            }
        });
        setContentView(webView);
        webView.loadUrl(SITE);
    }

    public final class ShareBridge {
        @JavascriptInterface
        public void shareJpeg(String base64) {
            if (!sharing.compareAndSet(false, true)) {
                showMessage("사진 공유가 진행 중입니다.");
                return;
            }
            new Thread(() -> {
                Uri photoUri = null;
                try {
                    byte[] data = Base64.decode(base64, Base64.DEFAULT);
                    if (data.length < 100 || data.length > 15000000)
                        throw new IllegalArgumentException("이미지 크기가 올바르지 않습니다.");
                    ContentResolver resolver = getContentResolver();
                    ContentValues values = new ContentValues();
                    String fileName = "LEDOA_인도금_"+System.currentTimeMillis()+".jpg";
                    values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                    values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES+"/LEDOA");
                    values.put(MediaStore.Images.Media.IS_PENDING, 1);
                    photoUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    if (photoUri == null) throw new IllegalStateException("갤러리 사진 저장소에 접근할 수 없습니다.");
                    try (OutputStream os = resolver.openOutputStream(photoUri, "w")) {
                        if (os == null) throw new IllegalStateException("사진을 저장할 수 없습니다.");
                        os.write(data);
                    }
                    ContentValues ready = new ContentValues();
                    ready.put(MediaStore.Images.Media.IS_PENDING, 0);
                    resolver.update(photoUri,ready,null,null);
                    final Uri completed = photoUri;
                    runOnUiThread(() -> {
                        try { launchKakaoShare(completed); }
                        catch (Exception ex) { showMessage("공유 오류: "+ex.getMessage()); }
                        finally { sharing.set(false); }
                    });
                } catch (Exception ex) {
                    if (photoUri != null) {
                        try { getContentResolver().delete(photoUri,null,null); } catch (Exception ignored) {}
                    }
                    sharing.set(false);
                    showMessage("이미지 저장·공유 오류: "+ex.getMessage());
                }
            }, "LEDOA-Payment-Share").start();
        }
    }

    private void launchKakaoShare(Uri photoUri) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_STREAM, photoUri);
        intent.setClipData(ClipData.newUri(getContentResolver(), "LEDOA payment", photoUri));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setPackage("com.kakao.talk");
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            intent.setPackage(null);
            startActivity(Intent.createChooser(intent, "사진을 전달할 앱 선택"));
        }
    }
    private void showMessage(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_LONG).show());
    }
    @Override public void onBackPressed() {
        if (webView!=null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }
    @Override protected void onDestroy() {
        if (webView!=null) {
            webView.removeJavascriptInterface("AndroidShare");
            webView.destroy();
        }
        super.onDestroy();
    }
}
