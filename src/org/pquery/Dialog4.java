/* 
 * Copyright (C) 2011 Robert Neild
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.pquery;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import junit.framework.Assert;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Dialog4 extends Activity implements LocationListener {

    private LocationManager locationManager;

    // References to UI components

    private TextView lat;
    private TextView lon;
    private TextView address;
    private TextView accuracyTextView;
    private TextView locationTextView;

    private Button map;

    // State

    private Location mapLocation;
    private Location gpsLocation;
    private boolean usingGPS = true;

    // Wizard state
    
    private QueryStore queryStore;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog4);

        // Setup GPS

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Store references to controls

        lat = (TextView) findViewById(R.id.textView_lat);
        lon = (TextView) findViewById(R.id.textView_lon);
        accuracyTextView = (TextView) findViewById(R.id.textView_accuracy);
        address = (TextView) findViewById(R.id.textView_address);
        map = (Button) findViewById(R.id.button_map);
        locationTextView = (TextView) findViewById(R.id.textView_location);
        Button cancelButton = (Button) findViewById(R.id.button_cancel);

        // Get parameters passed from previous wizard stage

        Bundle bundle = getIntent().getBundleExtra("QueryStore");
        Assert.assertNotNull(bundle);
        queryStore = new QueryStore(bundle);

        // Restore state

        if (savedInstanceState != null) {
            usingGPS = savedInstanceState.getBoolean("usingGPS");
            mapLocation = savedInstanceState.getParcelable("mapLocation");
            gpsLocation = savedInstanceState.getParcelable("gpsLocation");
        }

        // Handle cancel button

        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
            }
        });

        // Handle the map button

        map.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                if (usingGPS) {

                    Intent myIntent = new Intent(view.getContext(), MapsActivity.class);

                    // Try to open map at current location (if we have it)

                    if (gpsLocation!=null) {
                        myIntent.putExtra("lat",gpsLocation.getLatitude());
                        myIntent.putExtra("lon",gpsLocation.getLongitude());
                    }
                    startActivityForResult(myIntent, 123);

                } else {
                    setupForGPS();
                }
            }
        });

        final Button nextButton = (Button) findViewById(R.id.button_next);

        nextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                if (usingGPS) {
                    if (gpsLocation == null) {
                        Toast.makeText(getApplicationContext(), "Wait for GPS fix", Toast.LENGTH_LONG).show();
                        return;
                    }

                    queryStore.lat = gpsLocation.getLatitude();
                    queryStore.lon = gpsLocation.getLongitude();
                } else {
                    queryStore.lat = mapLocation.getLatitude();
                    queryStore.lon = mapLocation.getLongitude();
                }

                Bundle bundle = new Bundle();
                queryStore.saveToBundle(bundle);

                Intent myIntent = new Intent(view.getContext(), Dialog5.class);
                myIntent.putExtra("QueryStore", bundle);
                startActivity(myIntent);
                finish();
            }
        });

        if (usingGPS)
            setupForGPS();
        else
            setupForMap();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("usingGPS", usingGPS);
        outState.putParcelable("mapLocation", mapLocation);
        outState.putParcelable("gpsLocation", gpsLocation);
    } 


    /**
     * Gets result back from map point selection
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {

            Bundle bundle = data.getExtras();

            Object o1 = bundle.get("lat");
            Object o2 = bundle.get("lon");

            if (o1 !=null && o2 != null) {
                mapLocation = new Location("map");

                mapLocation.setLatitude((Double) o1);
                mapLocation.setLongitude((Double) o2);

                setupForMap();
            }
        }
    };

    /**
     * Tries to get street address for given location
     */
    private String getStreetAddress(double lat, double lon) {
        Geocoder geocoder = new Geocoder(this, Locale.ENGLISH);
        StringBuilder s = new StringBuilder("");
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);

            if(addresses != null) {
                Address address = addresses.get(0);
                for(int i=0; i<address.getMaxAddressLineIndex(); i++) {
                    s.append(address.getAddressLine(i)).append("\n");
                }
            }
        } catch (IOException e) {
        }

        return s.toString();
    }

    private void updateLocation(double latitude, double longitude, float accuracy) {
        lat.setText(Double.toString(latitude));
        lon.setText(Double.toString(longitude));

        if (accuracy==0)
            accuracyTextView.setText("");
        else
            accuracyTextView.setText("Accuracy: " + Float.toString(accuracy)+"m");

        //address.setText(getStreetAddress(latitude, longitude));
    }

    private void setupForGPS() {
       
        map.setCompoundDrawablesWithIntrinsicBounds(R.drawable.treasure_map, 0,0,0);
        map.setText("Choose point from map instead");
        locationTextView.setText("Current GPS location");
        locationTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ivak_satellite, 0,0,0);
        usingGPS = true;

        if (gpsLocation!=null)
            updateLocation(gpsLocation.getLatitude(), gpsLocation.getLongitude(), gpsLocation.getAccuracy());
        else {
            lat.setText("no fix yet");
            lon.setText("no fix yet");
        }
    }

    private void setupForMap() {
        map.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ivak_satellite, 0,0,0);
        map.setText("Use current GPS location instead");
        locationTextView.setText("Selected map point");
        locationTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.treasure_map, 0,0,0);
        accuracyTextView.setText("");
        usingGPS = false;

        if (mapLocation!=null)
            updateLocation(mapLocation.getLatitude(), mapLocation.getLongitude(), 0);
    }





    // Control GPS



    @Override
    protected void onResume() {
        super.onResume();
        GPS.requestLocationUpdates(locationManager, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    public void onLocationChanged(Location gpsLocation) {
        if (gpsLocation.getProvider().equals(LocationManager.NETWORK_PROVIDER) && 
                this.gpsLocation != null &&
                this.gpsLocation.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            // don't over write GPS with network provider
        } else {
            this.gpsLocation = gpsLocation;
        }

        if (usingGPS)
            updateLocation(gpsLocation.getLatitude(), gpsLocation.getLongitude(), gpsLocation.getAccuracy());

    }
    public void onProviderDisabled(String arg0) {}
    public void onProviderEnabled(String arg0) {}
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}
}
