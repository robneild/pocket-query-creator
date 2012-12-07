package org.pquery.fragments;

import org.pquery.CreateSettingsChangedListener;
import org.pquery.MapsActivity;
import org.pquery.R;
import org.pquery.util.GPS;
import com.actionbarsherlock.app.SherlockFragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class CreateLocationFragment extends SherlockFragment implements LocationListener {

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

    private CreateSettingsChangedListener listener;
    
    public CreateLocationFragment() {
    }
    
    public CreateLocationFragment(Location initialLocation) {
        if (initialLocation.getLatitude()==0 && initialLocation.getLongitude()==0) {
            usingGPS = true;
        } else {
            usingGPS = false;
            mapLocation = initialLocation;
        }
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (CreateSettingsChangedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement CreateSettingsChangedListener");
        }
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        
        View view = inflater.inflate(R.layout.dialog3, null);
        
        // Setup GPS

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        // Store references to controls

        lat = (TextView) view.findViewById(R.id.textView_lat);
        lon = (TextView) view.findViewById(R.id.textView_lon);
        accuracyTextView = (TextView) view.findViewById(R.id.textView_accuracy);
        address = (TextView) view.findViewById(R.id.textView_address);
        map = (Button) view.findViewById(R.id.button_map);
        locationTextView = (TextView) view.findViewById(R.id.textView_location);

        // Get parameters passed from previous wizard stage

//        Bundle bundle = getIntent().getBundleExtra("QueryStore");
//        Assert.assertNotNull(bundle);
       // queryStore = new QueryStore(bundle);

        // Restore state

        if (savedInstanceState != null) {
            usingGPS = savedInstanceState.getBoolean("usingGPS");
            mapLocation = savedInstanceState.getParcelable("mapLocation");
            gpsLocation = savedInstanceState.getParcelable("gpsLocation");
        }

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

//        final Button nextButton = (Button) findViewById(R.id.button_next);
//
//        nextButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View view) {
//
//                // Save preferences
//                
//                if (usingGPS) {
//                    if (gpsLocation == null) {
//                        Logger.d("No gpx fix yet");
//                    }
//                    else
//                    {
//                        queryStore.lat = gpsLocation.getLatitude();
//                        queryStore.lon = gpsLocation.getLongitude();
//                    }
//                } else {
//                    queryStore.lat = mapLocation.getLatitude();
//                    queryStore.lon = mapLocation.getLongitude();
//                }
//
//                // Move onto next dialog
//                
//                Bundle bundle = new Bundle();
//                queryStore.saveToBundle(bundle);
//
//                Intent myIntent = new Intent(view.getContext(), Dialog4.class);
//                myIntent.putExtra("QueryStore", bundle);
//                startActivity(myIntent);
//                finish();
//            }
//        });

        if (usingGPS)
            setupForGPS();
        else
            setupForMap();
        
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("usingGPS", usingGPS);
        outState.putParcelable("mapLocation", mapLocation);
        outState.putParcelable("gpsLocation", gpsLocation);
    } 

    @Override
    public void onResume() {
        super.onResume();
        GPS.requestLocationUpdates(locationManager, this);
    }

    @Override
    public void onPause() {
        super.onPause();
        GPS.stopLocationUpdate(locationManager, this);
    }
    /**
     * Gets result back from map point selection
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK) {

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
        
        Bundle bundle = new Bundle();
        bundle.putParcelable("location", new Location("rob"));
        listener.onSettingsChange(bundle);
    }

    private void setupForMap() {
        map.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ivak_satellite, 0,0,0);
        map.setText("Use current GPS location instead");
        locationTextView.setText("Selected map point");
        locationTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.treasure_map, 0,0,0);
        accuracyTextView.setText("");
        usingGPS = false;

        if (mapLocation!=null) {
            updateLocation(mapLocation.getLatitude(), mapLocation.getLongitude(), 0);
            
            Bundle bundle = new Bundle();
            bundle.putParcelable("location", mapLocation);
            listener.onSettingsChange(bundle);
        }
    }





    // Control GPS



    

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