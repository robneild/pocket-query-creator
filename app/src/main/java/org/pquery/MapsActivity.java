package org.pquery;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.pquery.util.Prefs;

/**
 * Show map to select centre of pocket query
 * Using new Maps V2 API that requires google play services
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.map_activity);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(final GoogleMap map) {

        map.getUiSettings().setMapToolbarEnabled(false);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.setMyLocationEnabled(true);

        map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location myLocation) {
                if (myLocation != null) {
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), 12);
                    map.moveCamera(cameraUpdate);
                    map.setOnMyLocationChangeListener(null);
                }
            }
        });

        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                Intent data = new Intent();

                data.putExtra("lat", latLng.latitude);
                data.putExtra("lon", latLng.longitude);

                map.clear();

                LatLng point = new LatLng(latLng.latitude, latLng.longitude);

                // Create marker where touched
                MarkerOptions marker = new MarkerOptions().position(point).title("PQ Location");
                map.addMarker(marker);

                // Create circle round marker, depicting coverage of pocket query

                float radiusInKm;

                if (Prefs.isMetric(MapsActivity.this))
                    radiusInKm = Float.parseFloat(Prefs.getDefaultRadius(MapsActivity.this));
                else
                    radiusInKm = Float.parseFloat(Prefs.getDefaultRadius(MapsActivity.this)) * 1.609344f;

                CircleOptions circleOptions = new CircleOptions()
                        .center(point)
                        .radius(radiusInKm * 1000); // In meters

                Circle circle = map.addCircle(circleOptions);

                Paint innerCirclePaint = new Paint();
                innerCirclePaint.setColor(Color.BLUE);
                innerCirclePaint.setAlpha(25);
                innerCirclePaint.setAntiAlias(true);
                innerCirclePaint.setStyle(Paint.Style.FILL);

                circle.setFillColor(innerCirclePaint.getColor());
                circle.setStrokeWidth(0);

                setResult(RESULT_OK, data);
            }
        });


    }

}
