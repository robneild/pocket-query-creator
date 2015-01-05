package org.pquery.util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationListener;
import android.location.LocationManager;

import com.gisgraphy.gishraphoid.GeoNamesGeocoder;
import com.gisgraphy.gishraphoid.GisgraphyGeocoder;

import org.pquery.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GPS {

    /**
     * Subscribe to all location providers
     */
    public static void requestLocationUpdates(LocationManager locationManager, LocationListener listener) {

        Logger.d("start gps");
        List<String> providers = locationManager.getAllProviders();

        for (String provider : providers) {
            locationManager.requestLocationUpdates(provider, 2000, 5, listener);
        }
    }

    public static void stopLocationUpdate(LocationManager locationManager, LocationListener listener) {
        Logger.d("stop gps");
        locationManager.removeUpdates(listener);
    }

    public static String getLocality(Context cxt, double lat, double lon) {

        String provider = Prefs.getGeocoderProvider(cxt);
        String providerList[] = cxt.getResources().getStringArray(R.array.geocoder_provider_values);

        List<Address> addresses = null;

        if (provider.equals(providerList[0])) {
            addresses = getLocalityGoogle(cxt, lat, lon);
        } else if (provider.equals(providerList[1])) {
            addresses = getLocalityGisgraphy(cxt, lat, lon);
        } else if (provider.equals(providerList[2])) {
            addresses = getLocalityGeoNames(cxt, lat, lon);
        } else {
            Logger.e("Unknown geocoder provider [" + provider + "]");
        }

        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            StringBuffer ret = new StringBuffer();
            if (address.getAddressLine(0) != null)
                ret.append(address.getAddressLine(0));
            if (address.getLocality() != null) {
                if (ret.length() == 0)
                    ret.append(address.getLocality());
                else
                    ret.append("," + address.getLocality());
            }
            if (ret.length() != 0)
                return ret.toString();
        }
        return null;
    }

    private static List<Address> getLocalityGeoNames(Context cxt, double lat, double lon) {

        Locale locale = Locale.getDefault();
        GeoNamesGeocoder geonamesGeocoder = new GeoNamesGeocoder(cxt, locale);

        try {
            return geonamesGeocoder.getFromLocation(lat, lon, 1);
        } catch (IOException e) {
            Logger.e("failed", e);
        }
        return null;
    }

    private static List<Address> getLocalityGoogle(Context cxt, double lat, double lon) {

        Geocoder geocoder = new Geocoder(cxt, Locale.ENGLISH);
        try {
            return geocoder.getFromLocation(lat, lon, 1);
        } catch (IOException e) {
            Logger.e("failed", e);
        }

        return null;
    }

    public static List<Address> getLocalityGisgraphy(Context cxt, double lat, double lon) {

        Locale locale = Locale.getDefault();
        GisgraphyGeocoder gisgraphyGeocoder = new GisgraphyGeocoder(cxt, locale);

        try {
            return gisgraphyGeocoder.getFromLocation(lat, lon, 1);
        } catch (IOException e) {
            Logger.e("failed", e);
        }
        return null;
    }

}
