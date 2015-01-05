package org.pquery;

import android.location.LocationListener;
import android.location.LocationManager;

import java.util.List;

public class GPS {

    public static void requestLocationUpdates(LocationManager locationManager, LocationListener listener) {

        List<String> providers = locationManager.getAllProviders();

        for (String provider : providers) {
            locationManager.requestLocationUpdates(provider, 2000, 5, listener);
        }

    }

}
