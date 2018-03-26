package com.tagipedia.tmaps;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import im.delight.android.webview.AdvancedWebView;

public class MainActivity extends Activity implements AdvancedWebView.Listener{
    WebView tmapWebView;
    private AdvancedWebView mWebView;
    private String mapId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final MainActivity self = this;
        mWebView = (AdvancedWebView) findViewById(R.id.webview);
        mWebView.setListener(self, self);
        Bundle bundle = getIntent().getExtras();
        mapId = bundle.getString("mapId");
        // tmapWebView=(WebView) findViewById(R.id.mapWebView);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setAllowFileAccessFromFileURLs(true);
        mWebView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public void onLoadResource(WebView view, String url) {
                setTagipediaObjectAndLoadMap();
                super.onLoadResource(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

        });
        mWebView.addJavascriptInterface(new JavascriptInterface(){
            @JavascriptInterface
            public void dispatch(String message) {
                System.out.println("Recieved Action !");
                try {
                    MainActivity.this.onMessageReceived(Utils.jsonToMap(message));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public Class<? extends Annotation> annotationType() {
                return null;
            }
        },"__tmaps_bridge__");
        mWebView.reload();
        mWebView.loadUrl("file:///android_asset/tmapswww/index.html");
        mWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                self.runOnUiThread(new Runnable() {
                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void run() {

                        request.grant(request.getResources());

                    }
                });
            }

        });


    }
    public void dispatchMessage(final HashMap<String, Object> hashMap) {
        final JSONObject jsonObject = new JSONObject(hashMap);
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.evaluateJavascript(String.format("Tagipedia.dispatch(%s);",jsonObject),null);
            }
        });
    }
    private void setTagipediaObjectAndLoadMap() {
        String tbString =
                "window.__tb__ = {dispatch: function(action){__tmaps_bridge__.dispatch(JSON.stringify(action));}}";
        injectScript(tbString);
    }

    private void injectScript(String tbString) {
        mWebView.evaluateJavascript(tbString,null);
    }

    @Override
    public void onPageStarted(String url, Bitmap favicon) { }

    @Override
    public void onPageFinished(String url) { }

    @Override
    public void onPageError(int errorCode, String description, String failingUrl) { }

    @Override
    public void onDownloadRequested(String url, String suggestedFilename, String mimeType, long contentLength, String contentDisposition, String userAgent) { }

    @Override
    public void onExternalPageRequest(String url) { }

    public void onMessageReceived(final Map<String, Object> message) {
        System.out.println(message);
        if (message.get("type").equals("READY")){
//            dispatchMessage(new LinkedHashMap<String, Object>() {
//                {
//                    put("type", "SET_TENANT_DATA");
//                    put("payload", MainActivity.this.getTenantsJSON() );
//                }
//            });
//
//            dispatchMessage(new LinkedHashMap<String, Object>() {
//                {
//                    put("type", "SET_DEFAULT_FEATURE_POPUP_TEMPLATE");
//                    put("template", MainActivity.this.getCustomTemplate());
//                }
//            });

            dispatchMessage(new LinkedHashMap<String, Object>() {
                {
                    put("type", "LOAD_MAP");
                    put("map_id", mapId );
                }
            });
        } else if (message.get("type").equals("MAP_LOADED")){

        } else if(message.get("type").equals("FEATURES_TAPPED")){
            dispatchMessage(new LinkedHashMap<String, Object>() {
                {
                    put("type", "HIGHLIGHT_FEATURE");
                    put("feature_id", ((Map)(((List) message.get("features")).get(0))).get("id"));
                }
            });
        } else if (message.get("type").equals("PROFILE_BUTTON_CLICKED")){
            new AlertDialog.Builder(this)
                            .setTitle((String)message.get("type2"))
                            .show();
        }
    }

    public List getTenantsJSON() {
        try{
            return Utils.jsonToList(Utils.readFileFromAssets(this, "tenants.json"));
        } catch (IOException e){
            return null;
        } catch (JSONException e){
            return null;
        }
    }

    public String getCustomTemplate() {
        try{
            return Utils.readFileFromAssets(this, "template.html");
        } catch (IOException e){
            return null;
        }
    }

}
