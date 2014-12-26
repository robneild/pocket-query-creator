package com.gisgraphy.gishraphoid;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.gisgraphy.domain.valueobject.StreetSearchResultsDto;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.gisgraphy.gishraphoid.JTSHelper.checkLatitude;
import static com.gisgraphy.gishraphoid.JTSHelper.checkLongitude;

/**
 * A class for handling geocoding and reverse geocoding with Gisgraphy. with the
 * same API as {@link Geocoder}. <br/>
 * Geocoding is the process of transforming a street address or other
 * description of a location into a (latitude, longitude) coordinate. Reverse
 * geocoding is the process of transforming a (latitude, longitude) coordinate
 * into a (partial) address. <br/>
 * <br/>
 * The amount of detail in a reverse geocoded location description may vary, for
 * example one might contain the full street address of the closest building,
 * while another might contain only a city name and postal code. The Geocoder
 * class requires a backend service that is not included in the core android
 * framework. <br/>
 * <br/>
 * The Geocoder query methods will return an empty list if there no backend
 * service in the platform. Use the isPresent() method to determine whether a
 * Geocoder implementation exists.
 * <p/>
 * You can use this class as a singleton if the local is unchanged
 *
 * @author <a href="mailto:david.masclet@gisgraphy.com">David Masclet</a>
 */
public class GisgraphyGeocoder {

    private static String LOG_TAG = GisgraphyGeocoder.class.getSimpleName();
    protected AndroidAddressBuilder addressBuilder;
    /**
     * the api key parameter name. <br/>
     * This parameter is only required for Gisgraphy premium services
     *
     * @see #getApiKey();
     * @see #setApiKey(Long)
     */

    protected static final String APIKEY_PARAMETER_NAME = "apikey";
    protected static final String ADDRESS_PARAMETER_NAME = "address";
    protected static final String COUNTRY_PARAMETER_NAME = "country";
    protected static final String FORMAT_PARAMETER_NAME = "format";
    protected static final String LATITUDE_PARAMETER_NAME = "lat";
    protected static final String LONGITUDE_PARAMETER_NAME = "lng";
    /**
     * the default base url of gisgraphy services.
     */
    public static final String DEFAULT_BASE_URL = "http://services.gisgraphy.com/";
    /**
     * the format of the geocoding services.
     */
    protected static final String DEFAULT_FORMAT = "json";
    /**
     * the URI to the geocoding services.
     *
     * @see GisgraphyGeocoder#DEFAULT_BASE_URL;
     */
    public static final String GEOCODING_URI = "geocoding/geocode";
    public static final String REVERSE_GEOCODING_URI = "street/streetsearch";
    private Locale locale = Locale.getDefault();
    private Long apiKey;
    private String baseUrl = DEFAULT_BASE_URL;

    /**
     * this method purpose is only to mock the call to android logger during
     * tests and always log with the same tag.feel free to override
     */
    protected void log_d(String message) {
        Log.d(LOG_TAG, message);

    }

    static boolean isPresent() {
        return true;
    }

    /**
     * @param url the Gisgraphy base URL. It must follow the
     *            SCHEME://HOST[:PORT][/CONTEXT]/ Don't add the URI.<br/>
     *            Good base URL : http://services.gisgraphy.com/<br/>
     *            Bad base URL : http://services.gisgraphy.com/geocoding/geocode
     */
    public void setBaseUrl(String url) {
        if (url == null) {
            throw new IllegalArgumentException(LOG_TAG + " does not accept null URL");
        }
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(url + " is no a valid Url : " + e.getMessage(), e);
        }
        this.baseUrl = url;
    }

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
    public GisgraphyGeocoder(Context context, Locale locale) {
        if (locale == null) {
            throw new IllegalArgumentException(LOG_TAG + " does not accept null locale");
        }
        this.locale = locale;
        addressBuilder = new AndroidAddressBuilder(locale);
    }

    /**
     * Constructs a Geocoder whose responses will be localized for the given
     * Locale and URL
     *
     * @param context the Context of the calling Activity
     * @param locale  the desired Locale for the query results
     * @param url     the base url (scheme,host,port). see
     *                {@link #setBaseUrl(String)}
     * @see GisgraphyGeocoder#setBaseUrl(String)
     */
    public GisgraphyGeocoder(Context context, Locale locale, String url) {
        if (locale == null) {
            throw new IllegalArgumentException(LOG_TAG + " does not accept null locale");
        }
        setBaseUrl(url);
        this.locale = locale;
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
    public GisgraphyGeocoder(Context context) {
        addressBuilder = new AndroidAddressBuilder();
    }

    /**
     * Constructs a Geocoder whose responses will be localized for the default
     * system Locale.
     *
     * @param context the desired Locale for the query results
     * @param url     the base url (scheme,host,port). see
     *                {@link #setBaseUrl(String)}
     */
    public GisgraphyGeocoder(Context context, String url) {
        this.locale = Locale.getDefault();
        addressBuilder = new AndroidAddressBuilder(locale);
        setBaseUrl(url);
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
        log_d("getFromLocation: lat=" + latitude + ",longitude=" + longitude + ",maxResults=" + maxResults);
        if (!checkLatitude(latitude) || !checkLongitude(longitude)) {
            throw new IllegalArgumentException(
                    "lattitude should be > -90 and < 90 and longitude should be >-180 and < 180");
        }
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
            log_d("lat=" + latitude + ", long=" + longitude);
            // params.put(COUNTRY_PARAMETER_NAME, iso2countryCode);
            params.put(LATITUDE_PARAMETER_NAME, latitude + "");
            params.put(LONGITUDE_PARAMETER_NAME, longitude + "");
            params.put("radius", 500 + "");
            params.put(FORMAT_PARAMETER_NAME, DEFAULT_FORMAT);
            params.put("from", "1");
            params.put("to", "1");
            if (apiKey != null) {
                params.put(APIKEY_PARAMETER_NAME, apiKey + "");
            }
            StreetSearchResultsDto response = webService.get(REVERSE_GEOCODING_URI, StreetSearchResultsDto.class,
                    params);
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

    private void checkUrl() throws IOException {
        if (baseUrl == null) {
            throw new IOException(this.getClass().getSimpleName()
                    + " is not initialize, please call setUrl before calling geocoding methods");
        }
    }

    /**
     * @return the apikey. apikey is only used for Gisgraphy premium services.
     * It is not required for free services (when those lines are
     * written)
     * @see http://www.gisgraphy.com/premium
     */
    public Long getApiKey() {
        return apiKey;
    }

    /**
     * @param apiKey the apikey provided by gisgraphy apikey is used for Gisgraphy
     *               premium services. It is not required for free services (when
     *               those lines are written)
     * @see http://www.gisgraphy.com/premium
     */
    public void setApiKey(Long apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * @return the locale of the geocoder
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * @return the base url of the gisgraphy services.
     * @see #DEFAULT_BASE_URL
     * @see GisgraphyGeocoder#setBaseUrl(String)
     */
    public String getBaseUrl() {
        return baseUrl;
    }

}
