package com.gisgraphy.gishraphoid;

import android.content.Context;
import android.location.Address;

import com.gisgraphy.domain.valueobject.GeoNamesDto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class GeoNamesGeocoder {

    private static String LOG_TAG = GeoNamesGeocoder.class.getSimpleName();
    protected AndroidAddressBuilder addressBuilder;

    protected static final String USERNAME_PARAMETER_NAME = "username";
    protected static final String LATITUDE_PARAMETER_NAME = "lat";
    protected static final String LONGITUDE_PARAMETER_NAME = "lng";

    /**
     * the default base url of GeoName services.
     */
    public static final String DEFAULT_BASE_URL = "http://api.geonames.org/";

    /**
     * the URI to the geocoding services.
     *
     * @see GisgraphyGeocoder#DEFAULT_BASE_URL;
     */
    public static final String REVERSE_GEOCODING_URI = "findNearbyPlaceNameJSON";
    private String baseUrl = DEFAULT_BASE_URL;

    /**
     * Constructs a Geocoder whose responses will be localized for the given
     * Locale. You should prefer the
     * {@link #GisgraphyGeocoder(Context, Locale, String)} constructor. This
     * method is only to suite the android geocoder interface the Gisgraphy base
     * URL will be the {@link #DEFAULT_BASE_URL}
     *
     * @param context the Context of the calling Activity
     * @param locale  the desired Locale for the query results
     * @throws NullPointerException if Locale is null
     */
    public GeoNamesGeocoder(Context context, Locale locale) {
        if (locale == null) {
            throw new IllegalArgumentException(LOG_TAG + " does not accept null locale");
        }
        addressBuilder = new AndroidAddressBuilder(locale);
    }


    /**
     * Constructs a Gisgraphy Geocoder whose responses will be localized for the
     * default system Locale. You should prefer the
     * {@link #GisgraphyGeocoder(Context, String)} constructor. This method is
     * only here to suite the android geocoder interface
     *
     * @param context the desired Locale for the query results
     */
    public GeoNamesGeocoder(Context context) {
        addressBuilder = new AndroidAddressBuilder();
    }

    /**
     * Returns a list of Addresses that are known to describe the area
     * immediately surrounding the given latitude and longitude.
     * <p/>
     * The returned values may be obtained by means of a network lookup. The
     * results are a best guess and are not guaranteed to be meaningful or
     * correct. It may be useful to call this method from a thread separate from
     * your primary UI thread.
     *
     * @param latitude   the latitude a point for the search
     * @param longitude  the longitude a point for the search
     * @param maxResults max number of addresses to return. Smaller numbers (1 to 5)
     *                   are recommended
     * @return a list of Address objects. Returns empty list if no matches were
     * found or there is no backend service available.
     * @throws IllegalArgumentException if latitude is less than -90 or greater than 90
     * @throws IllegalArgumentException if longitude is less than -180 or greater than 180
     * @throws IOException              if the network is unavailable or any other I/O problem occurs
     */
    public List<Address> getFromLocation(double latitude, double longitude, int maxResults) throws IOException {

        if (maxResults < 0) {
            throw new IllegalArgumentException("maxResults should be positive");
        }
        if (maxResults == 0) {
            // shortcut filtering
            return new ArrayList<Address>();
        }
        List<Address> androidAddresses = new ArrayList<Address>();
        try {
            RestClient webService = createRestClient();

            // Pass the parameters if needed , if not then pass dummy one as
            // follows
            Map<String, String> params = new HashMap<String, String>();

            // params.put(COUNTRY_PARAMETER_NAME, iso2countryCode);
            params.put(LATITUDE_PARAMETER_NAME, latitude + "");
            params.put(LONGITUDE_PARAMETER_NAME, longitude + "");
            params.put(USERNAME_PARAMETER_NAME, "subvii");

            GeoNamesDto response = webService.get(REVERSE_GEOCODING_URI, GeoNamesDto.class, params);
            if (response != null && response.getResult() != null && response.getResult().length > 0) {
                androidAddresses = addressBuilder.transformStreetsToAndroidAddresses(response.getResult());
            }
            if (androidAddresses.size() > maxResults) {
                return androidAddresses.subList(0, maxResults);
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
        return androidAddresses;
    }


    protected RestClient createRestClient() {
        return new RestClient(baseUrl);
    }
}
