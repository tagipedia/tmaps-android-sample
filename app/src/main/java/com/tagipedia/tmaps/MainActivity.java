package com.tagipedia.tmaps;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.estimote.indoorsdk.EstimoteCloudCredentials;
import com.estimote.indoorsdk.IndoorLocationManagerBuilder;
import com.estimote.indoorsdk_module.algorithm.OnPositionUpdateListener;
import com.estimote.indoorsdk_module.algorithm.ScanningIndoorLocationManager;
import com.estimote.indoorsdk_module.cloud.CloudCallback;
import com.estimote.indoorsdk_module.cloud.EstimoteCloudException;
import com.estimote.indoorsdk_module.cloud.IndoorCloudManager;
import com.estimote.indoorsdk_module.cloud.IndoorCloudManagerFactory;
import com.estimote.indoorsdk_module.cloud.Location;
import com.estimote.indoorsdk_module.cloud.LocationPosition;
import com.estimote.internal_plugins_api.cloud.CloudCredentials;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.mapbox.geojson.Point;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import im.delight.android.webview.AdvancedWebView;

public class MainActivity extends Activity implements AdvancedWebView.Listener{
    WebView tmapWebView;
    private AdvancedWebView mWebView;
    private String mapId;
    private String mGeolocationOrigin;
    private GeolocationPermissions.Callback mGeolocationCallback;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final int REQUEST_NAVIGATION = 0x2;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public static String locationDetails = "";
    public double originLatitude = 0, originLongitude = 0;
    ScanningIndoorLocationManager indoorLocationManager;
    public static boolean bluetoothPermissionRequested = false;

    @RequiresApi(api = Build.VERSION_CODES.O)
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
        WebView.setWebContentsDebuggingEnabled(true);
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
            @JavascriptInterface
            public void reload() {
                runOnUiThread(new Runnable() { @Override public void run() { mWebView.reload(); } });
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
            public void onGeolocationPermissionsShowPrompt(final String origin, final GeolocationPermissions.Callback callback) {
                MainActivity.this.mGeolocationOrigin = origin;
                MainActivity.this.mGeolocationCallback = callback;
                if(checkLocationPermission()){
                    displayLocationSettingsRequest();
                }
            }
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

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("NearBy")
                        .setMessage("we cannot get nearby areas without Location access")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    private void displayLocationSettingsRequest() {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        mGeolocationCallback.invoke(mGeolocationOrigin, true, false);
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        mGeolocationCallback.invoke(mGeolocationOrigin, false, false);
                        break;
                }
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
    public void initializeBeaconLocation(final boolean userInteract) {
        final MainActivity mainActivity = this;
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String line, newjson = "";
                    URL urls = new URL(getResources().getString(R.string.location_url)+getResources().getString(R.string.location));
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(urls.openStream(), "UTF-8"))) {
                        while ((line = reader.readLine()) != null) {
                            newjson += line;
                        }
                        String json = newjson.toString();
                        locationDetails = json;
                        JSONObject jObj = new JSONObject(json);
                        originLatitude = (double)jObj.get("latitude");
                        originLongitude = (double)jObj.get("longitude");
                        final CloudCredentials cloudCredentials = new EstimoteCloudCredentials(getResources().getString(R.string.app_id), getResources().getString(R.string.app_token));
                        IndoorCloudManager cloudManager = new IndoorCloudManagerFactory().create(getApplicationContext(), new EstimoteCloudCredentials(getResources().getString(R.string.app_id), getResources().getString(R.string.app_token)));
                        cloudManager.getLocation(getResources().getString(R.string.location), new CloudCallback<Location>() {
                            @Override
                            public void success(Location location) {
                                indoorLocationManager =
                                        new IndoorLocationManagerBuilder(getApplicationContext(), location, cloudCredentials)
                                                .withDefaultScanner()
                                                .build();
                                indoorLocationManager.setOnPositionUpdateListener(new OnPositionUpdateListener() {
                                    @Override
                                    public void onPositionUpdate(final LocationPosition locationPosition) {
                                        dispatchMessage(new LinkedHashMap<String, Object>() {
                                            {
                                                put("type", "SET_USER_BEACON_LOCATION");
                                                put("origin_lat",originLatitude);
                                                put("origin_lng",originLongitude);
                                                put("x",locationPosition.getX());
                                                put("y",locationPosition.getY());
                                            }
                                        });
                                    }
                                    @Override
                                    public void onPositionOutsideLocation() {
                                        Log.d("on Position Outside", "outside: ");
                                    }
                                });
                                if (userInteract){
                                    indoorLocationManager.startPositioning();
                                }
                            }
                            @Override
                            public void failure(EstimoteCloudException e) {
                                Log.d("failed", "failed to get location from cloud: ");
                                if (userInteract){
                                    dispatchMessage(new LinkedHashMap<String, Object>() {
                                        {
                                            put("type", "START_UPDATING_BEACON_LOCATION");
                                            put("is_beacon_location_activated",false);
                                        }
                                    });
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (userInteract) {
                        dispatchMessage(new LinkedHashMap<String, Object>() {
                            {
                                put("type", "START_UPDATING_BEACON_LOCATION");
                                put("is_beacon_location_activated", false);
                            }
                        });
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                TUtils.showNoInternetAccssesDialog(mainActivity, "error", "something happen while retriving location from internet. check internet connection and try again");
                            }
                        });
                    }
                }
            }
        });

    }
    private void setTagipediaObjectAndLoadMap() {
        String tbString =
                "window.__tb__ = {dispatch: function(action){__tmaps_bridge__.dispatch(JSON.stringify(action));}}; window.__reload__ = function(){__tmaps_bridge__.reload();};";
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
                    put("type", "SET_APPLICATION_SECRETS");
                    put("client_id", getResources().getString(R.string.client_id));
                    put("client_secret", getResources().getString(R.string.client_secret));
                }
            });
            dispatchMessage(new LinkedHashMap<String, Object>() {
                {
                    put("type", "SET_DEVICE_DATA");
                    put("device_id", Settings.Secure.getString(getApplicationContext().getContentResolver(),Settings.Secure.ANDROID_ID).toString());
                    put("device_type", "ANDROID");
                }
            });
            dispatchMessage(new LinkedHashMap<String, Object>() {
                {
                    put("type", "SET_MAPBOX_ACCESS_TOKEN");
                    put("access_token", getResources().getString(R.string.mapbox_access_token));
                }
            });
            dispatchMessage(new LinkedHashMap<String, Object>() {
                {
                    put("type", "LOAD_MAP");
                    put("map_id", mapId );
                }
            });
        } else if (message.get("type").equals("MAP_LOADED")){
            dispatchMessage(new LinkedHashMap<String, Object>() {
                {
                    put("type", "ENABLE_GPS_BUTTON");
                }
            });
//            dispatchMessage(new LinkedHashMap<String, Object>() {
//                {
//                    put("type", "ENABLE_BEACON_LOCATION_BUTTON");
//                }
//            });
//
//            initializeBeaconLocation(false);
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
        } else if (message.get("type").equals("CHECK_GPS_AVAILABILITY")){
            dispatchMessage(new LinkedHashMap<String, Object>() {
                {
                    put("type", "START_UPDATING_LOCATION");
                    put("is_gps_activated",true);
                }
            });
        } else if (message.get("type").equals("CHECK_BEACON_LOCATION_AVAILABILITY")){
            if(indoorLocationManager == null){
                initializeBeaconLocation(true);
            }
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter.isEnabled()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    TUtils.showLocationDialog(this, "Location Permission", "get location permission");
                }
                dispatchMessage(new LinkedHashMap<String, Object>() {
                    {
                        put("type", "START_UPDATING_BEACON_LOCATION");
                        put("is_beacon_location_activated",true);
                    }
                });
            }
            else {
                bluetoothPermissionRequested = true;
                TUtils.showBluetoothDialog(this, "Bluetooth Needed" , "we use bluetoorh to detect nearest places to you");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    TUtils.showLocationDialog(this, "Location Permission", "get location permission");
                }
            }
        } else if (message.get("type").equals("START_POSITION_UPDATES_FOR_BEACON_LOCATION")){
            boolean stat_beacon_manager = (boolean) message.get("start_beacon_manager");
            if (stat_beacon_manager && indoorLocationManager != null){
                indoorLocationManager.startPositioning();
            }
            else {
                indoorLocationManager.stopPositioning();
            }
        }  else if (message.get("type").equals("START_NAVIGATION")){
            runOnUiThread(new Runnable() { @Override public void run() {
                Map originMap = (Map)message.get("origin");
                Map destinationMap = (Map)message.get("destination");
                String profile = (String)message.get("profile");
                Point origin = Point.fromLngLat((double)originMap.get("lng"), (double)originMap.get("lat"));
                Point destination = Point.fromLngLat((double)destinationMap.get("lng"), (double)destinationMap.get("lat"));
                Intent intent = new Intent(MainActivity.this,EmbeddedNavigationActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("origin", origin);
                bundle.putSerializable("destination", destination);
                bundle.putString("profile", profile);
                intent.putExtras(bundle);
                startActivityForResult(intent, REQUEST_NAVIGATION);
            } });
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
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        displayLocationSettingsRequest();
                    }
                } else {
                    // permission denied
                }
                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        if (mGeolocationCallback != null) {
                            mGeolocationCallback.invoke(mGeolocationOrigin, true, false);
                        }
                        if (bluetoothPermissionRequested){
                            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                            if(bluetoothAdapter.isEnabled()){
                                dispatchMessage(new LinkedHashMap<String, Object>() {
                                    {
                                        put("type", "START_UPDATING_BEACON_LOCATION");
                                        put("is_beacon_location_activated",true);
                                    }
                                });
                                bluetoothPermissionRequested = false;
                            }
                        }
                        break;
                    case Activity.RESULT_CANCELED:
                        if (mGeolocationCallback != null) {
                            mGeolocationCallback.invoke(mGeolocationOrigin, false, false);
                        }
                        if (bluetoothPermissionRequested){
                            dispatchMessage(new LinkedHashMap<String, Object>() {
                                {
                                    put("type", "START_UPDATING_BEACON_LOCATION");
                                    put("is_beacon_location_activated",false);
                                }
                            });
                            bluetoothPermissionRequested = false;
                        }
                        break;
                }
                break;
            case REQUEST_NAVIGATION:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Bundle bundle = data.getExtras();
                        Point origin = (Point) bundle.getSerializable("origin");
                        Point destination = (Point) bundle.getSerializable("destination");
                        String profile = bundle.getString("profile");
                        dispatchMessage(new LinkedHashMap<String, Object>() {
                            {
                                put("type", "NAVIGATION_END");
                                put("origin", Arrays.asList(origin.longitude(), origin.latitude()));
                                put("destination", Arrays.asList(destination.longitude(), destination.latitude()));
                                put("profile", profile);
                            }
                        });
                        break;
                    case Activity.RESULT_CANCELED:
                        dispatchMessage(new LinkedHashMap<String, Object>() {
                            {
                                put("type", "NAVIGATION_CANCELED");
                            }
                        });
                        break;
                }
                break;

        }
    }

}
