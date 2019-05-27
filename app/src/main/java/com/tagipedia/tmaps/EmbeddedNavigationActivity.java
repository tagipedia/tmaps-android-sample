package com.tagipedia.tmaps;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;
import android.widget.TextView;

import com.mapbox.api.directions.v5.models.BannerInstructions;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.services.android.navigation.ui.v5.NavigationView;
import com.mapbox.services.android.navigation.ui.v5.NavigationViewOptions;
import com.mapbox.services.android.navigation.ui.v5.OnNavigationReadyCallback;
import com.mapbox.services.android.navigation.ui.v5.listeners.BannerInstructionsListener;
import com.mapbox.services.android.navigation.ui.v5.listeners.InstructionListListener;
import com.mapbox.services.android.navigation.ui.v5.listeners.NavigationListener;
import com.mapbox.services.android.navigation.ui.v5.listeners.SpeechAnnouncementListener;
import com.mapbox.services.android.navigation.ui.v5.voice.SpeechAnnouncement;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.mapbox.services.android.navigation.v5.routeprogress.ProgressChangeListener;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgressState;

import java.io.File;

import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;

/**
 * Created by shimaahassan on 5/27/19.
 */

public class EmbeddedNavigationActivity extends AppCompatActivity implements OnNavigationReadyCallback,
        NavigationListener, ProgressChangeListener, InstructionListListener, SpeechAnnouncementListener,
        BannerInstructionsListener {

    private Point origin;
    private Point destination;
    private String profile;
    private static final int INITIAL_ZOOM = 16;

    private NavigationView navigationView;
    private View spacer;
    private TextView speedWidget;
    private FloatingActionButton fabNightModeToggle;

    private boolean bottomSheetVisible = true;
    private boolean instructionListShown = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Mapbox.getInstance(getApplicationContext(), getResources().getString(R.string.mapbox_access_token));
        setTheme(R.style.Theme_AppCompat_Light_NoActionBar);
        initNightMode();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_embedded_navigation);
        Bundle bundle = getIntent().getExtras();
        origin = (Point) bundle.getSerializable("origin");
        destination = (Point) bundle.getSerializable("destination");
        profile = bundle.getString("profile");
        navigationView = findViewById(R.id.navigationView);
        fabNightModeToggle = findViewById(R.id.fabToggleNightMode);
        speedWidget = findViewById(R.id.speed_limit);
        spacer = findViewById(R.id.spacer);
        setSpeedWidgetAnchor(R.id.summaryBottomSheet);

        CameraPosition initialPosition = new CameraPosition.Builder()
                .target(new LatLng(origin.latitude(), origin.longitude()))
                .zoom(INITIAL_ZOOM)
                .build();
        navigationView.onCreate(savedInstanceState);
        navigationView.initialize(this, initialPosition);
    }

    @Override
    public void onNavigationReady(boolean isRunning) {
        fetchRoute();
    }

    @Override
    public void onStart() {
        super.onStart();
        navigationView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        navigationView.onResume();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        navigationView.onLowMemory();
    }

    @Override
    public void onBackPressed() {
        // If the navigation view didn't need to do anything, call super
        if (!navigationView.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        navigationView.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        navigationView.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        navigationView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        navigationView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        navigationView.onDestroy();
        if (isFinishing()) {
            saveNightModeToPreferences(AppCompatDelegate.MODE_NIGHT_AUTO);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
        }
    }

    @Override
    public void onCancelNavigation() {
        // Navigation canceled, finish the activity
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);
        finish();
    }

    @Override
    public void onNavigationFinished() {
        // Intentionally empty
        Intent returnIntent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("origin", origin);
        bundle.putSerializable("destination", destination);
        bundle.putString("profile", profile);
        returnIntent.putExtras(bundle);
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void onNavigationRunning() {
        // Intentionally empty
    }

    @Override
    public void onProgressChange(Location location, RouteProgress routeProgress) {
        setSpeed(location);
        if(routeProgress.currentState() == RouteProgressState.ROUTE_ARRIVED) {
            onNavigationFinished();
        }
    }

    @Override
    public void onInstructionListVisibilityChanged(boolean shown) {
        instructionListShown = shown;
        speedWidget.setVisibility(shown ? View.GONE : View.VISIBLE);
        if (instructionListShown) {
            fabNightModeToggle.hide();
        } else if (bottomSheetVisible) {
            fabNightModeToggle.show();
        }
    }

    @Override
    public SpeechAnnouncement willVoice(SpeechAnnouncement announcement) {
        return SpeechAnnouncement.builder().announcement("All announcements will be the same.").build();
    }

    @Override
    public BannerInstructions willDisplay(BannerInstructions instructions) {
        return instructions;
    }

    private void startNavigation(DirectionsRoute directionsRoute) {
        NavigationViewOptions.Builder options =
                NavigationViewOptions.builder()
                        .navigationListener(this)
                        .directionsRoute(directionsRoute)
                        .shouldSimulateRoute(true)
                        .progressChangeListener(this)
                        .instructionListListener(this)
                        .speechAnnouncementListener(this)
                        .bannerInstructionsListener(this)
                        .offlineRoutingTilesPath(obtainOfflineDirectory())
                        .offlineRoutingTilesVersion(obtainOfflineTileVersion());
        setBottomSheetCallback(options);
        setupNightModeFab();

        navigationView.startNavigation(options.build());
    }

    private String obtainOfflineDirectory() {
        File offline = Environment.getExternalStoragePublicDirectory("Offline");
        if (!offline.exists()) {
            Timber.d("Offline directory does not exist");
            offline.mkdirs();
        }
        return offline.getAbsolutePath();
    }

    private String obtainOfflineTileVersion() {
        return PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.offline_version_key), "");
    }

    private void fetchRoute() {
        NavigationRoute.builder(this)
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
                .profile(profile.replace("mapbox/", ""))
                .alternatives(true)
                .build()
                .getRoute(new SimplifiedCallback() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        DirectionsRoute directionsRoute = response.body().routes().get(0);
                        startNavigation(directionsRoute);
                    }
                });
    }

    /**
     * Sets the anchor of the spacer for the speed widget, thus setting the anchor for the speed widget
     * (The speed widget is anchored to the spacer, which is there because padding between items and
     * their anchors in CoordinatorLayouts is finicky.
     *
     * @param res resource for view of which to anchor the spacer
     */
    private void setSpeedWidgetAnchor(@IdRes int res) {
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) spacer.getLayoutParams();
        layoutParams.setAnchorId(res);
        spacer.setLayoutParams(layoutParams);
    }

    private void setBottomSheetCallback(NavigationViewOptions.Builder options) {
        options.bottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        bottomSheetVisible = false;
                        fabNightModeToggle.hide();
                        setSpeedWidgetAnchor(R.id.recenterBtn);
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        bottomSheetVisible = true;
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        if (!bottomSheetVisible) {
                            // View needs to be anchored to the bottom sheet before it is finished expanding
                            // because of the animation
                            fabNightModeToggle.show();
                            setSpeedWidgetAnchor(R.id.summaryBottomSheet);
                        }
                        break;
                    default:
                        return;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });
    }

    private void setupNightModeFab() {
        fabNightModeToggle.setOnClickListener(view -> toggleNightMode());
    }

    private void toggleNightMode() {
        int currentNightMode = getCurrentNightMode();
        alternateNightMode(currentNightMode);
    }

    private void initNightMode() {
        int nightMode = retrieveNightModeFromPreferences();
        AppCompatDelegate.setDefaultNightMode(nightMode);
    }

    private int getCurrentNightMode() {
        return getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
    }

    private void alternateNightMode(int currentNightMode) {
        int newNightMode;
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            newNightMode = AppCompatDelegate.MODE_NIGHT_NO;
        } else {
            newNightMode = AppCompatDelegate.MODE_NIGHT_YES;
        }
        saveNightModeToPreferences(newNightMode);
        recreate();
    }

    private int retrieveNightModeFromPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return preferences.getInt(getString(R.string.current_night_mode), AppCompatDelegate.MODE_NIGHT_AUTO);
    }

    private void saveNightModeToPreferences(int nightMode) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(getString(R.string.current_night_mode), nightMode);
        editor.apply();
    }

    private void setSpeed(Location location) {
        String string = String.format("%d\nMPH", (int) (location.getSpeed() * 2.2369));
        int mphTextSize = getResources().getDimensionPixelSize(R.dimen.mph_text_size);
        int speedTextSize = getResources().getDimensionPixelSize(R.dimen.speed_text_size);

        SpannableString spannableString = new SpannableString(string);
        spannableString.setSpan(new AbsoluteSizeSpan(mphTextSize),
                string.length() - 4, string.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        spannableString.setSpan(new AbsoluteSizeSpan(speedTextSize),
                0, string.length() - 3, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        speedWidget.setText(spannableString);
        if (!instructionListShown) {
            speedWidget.setVisibility(View.VISIBLE);
        }
    }
}