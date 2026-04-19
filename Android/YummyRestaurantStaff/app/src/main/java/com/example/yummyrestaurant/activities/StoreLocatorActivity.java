package com.example.yummyrestaurant.activities;

import android.os.Bundle;
import android.preference.PreferenceManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow;
import org.osmdroid.api.IMapController;

import com.example.yummyrestaurant.R;

public class StoreLocatorActivity extends ThemeBaseActivity {

    private MapView mapView;
    private IMapController mapController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load OSMDroid configuration using application context
        Configuration.getInstance().load(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

        setContentView(R.layout.activity_store_locator);

        // Initialize MapView
        mapView = findViewById(R.id.mapview);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        // Set up map controller
        mapController = mapView.getController();
        mapController.setZoom(15);

        // Set map center to store location
        GeoPoint storeLocation = new GeoPoint(22.2788, 114.1747); // Hong Kong
        mapController.setCenter(storeLocation);

        // Add marker for the store
        Marker marker = new Marker(mapView);
        marker.setPosition(storeLocation);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle("Yummy Restaurant");
        marker.setSubDescription("Delicious food in the heart of Hong Kong!");
        marker.setInfoWindow(new BasicInfoWindow(R.layout.bubble, mapView));
        mapView.getOverlays().add(marker);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume(); // Needed for compass, my location overlays, etc.
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause(); // Needed for compass, my location overlays, etc.
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDetach(); // Clean up resources
    }
}