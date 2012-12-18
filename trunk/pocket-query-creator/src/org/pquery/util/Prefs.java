package org.pquery.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.pquery.R;
import org.pquery.dao.PQ;
import org.pquery.filter.CacheTypeList;
import org.pquery.filter.CheckBoxesFilter;
import org.pquery.filter.ContainerTypeList;
import org.pquery.filter.OneToFiveFilter;

import com.google.gson.Gson;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class Prefs {

    public static final String MAX_CACHES = "maxcaches_preference";
    public static final String PREFIX = "nameprefix_preference";
    public static final String GEOCODER_PROVIDER = "geocoding_provider_preference";
    public static final String DEFAULT_DOWNLOAD_DIR = "default_download_dir_preference";
    public static final String USER_DOWNLOAD_DIR = "user_download_dir_preference";
    public static final String AUTO_NAME = "autoname_preference";
    public static final String DISABLED = "disabled_preference";
    public static final String DEBUG_PREFERENCE = "debug_preference";
    private static final String CACHE_FILTER = "cache_type_filter_preference";
    private static final String CONTAINER_FILTER = "container_type_filter_preference";
    private static final String DIFFICULTY_FILTER = "difficulty_filter_preference";
    private static final String TERRAIN_FILTER = "terrain_filter_preference";
    private static final String RADUIS = "radius_preference";
    private static final String COOKIES = "cookies_preference";
    public static final String USERNAME = "username_preference";
    public static final String PASSWORD = "password_preference";
    private static final String RADIUS = "radius_preference2";

    private static final String ENABLED_FILTER = "enabled_filter_preference";
    private static final String TRAVEL_BUG_FILTER = "travel_bug_filter";
    private static final String NOT_IGNORED_FILTER = "not_ignored_filter";
    private static final String FOUND_7DAYS_FILTER = "found_7days_filter";
    private static final String NOT_FOUND_FILTER = "not_found_filter";
    private static final String LOCATION_ACCURACY = "location_accuracy_preference";

    private static final String COMMA = "\u001F";
    private static final String SEMI_COLON = "\u007F";

    private static final String PQ_LIST_STATE = "pq_list_state";
    private static final String PQ_LIST_STATE_TIMESTAMP = "pq_list_state_time";

    public static String getGeocoderProvider(Context cxt) {
        return PreferenceManager.getDefaultSharedPreferences(cxt).getString(GEOCODER_PROVIDER, cxt.getResources().getStringArray(R.array.geocoder_provider_values)[0]);

    }
    public static int getLocationAccuracy(Context cxt) {
        return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(cxt).getString(LOCATION_ACCURACY, "150"));
    }

    public static CheckBoxesFilter getCheckBoxesFilter(Context cxt) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(cxt);
        boolean enabled = prefs.getBoolean(ENABLED_FILTER, true);
        boolean travelBug = prefs.getBoolean(TRAVEL_BUG_FILTER, false);
        boolean notOnIgnore = prefs.getBoolean(NOT_IGNORED_FILTER, true);
        boolean found7days = prefs.getBoolean(FOUND_7DAYS_FILTER, false);
        boolean notFound = prefs.getBoolean(NOT_FOUND_FILTER, false);

        CheckBoxesFilter ret = new CheckBoxesFilter();
        ret.enabled = enabled;
        ret.travelBug = travelBug;
        ret.notOnIgnore = notOnIgnore;
        ret.found7days = found7days;
        ret.notFound = notFound;
        return ret;
    }
    public static String getMaxCaches(Context cxt) {
        return PreferenceManager.getDefaultSharedPreferences(cxt).getString(MAX_CACHES, "500");
    }
    public static String getDownloadPrefix(Context cxt) {
        return PreferenceManager.getDefaultSharedPreferences(cxt).getString(PREFIX, "pocketquery_");
    }
    public static String getUserSpecifiedDownloadDir(Context cxt) {
        return PreferenceManager.getDefaultSharedPreferences(cxt).getString(USER_DOWNLOAD_DIR, Util.getDefaultDownloadDirectory());
    }
    public static boolean isDefaultDownloadDir(Context cxt) {
        return PreferenceManager.getDefaultSharedPreferences(cxt).getBoolean(DEFAULT_DOWNLOAD_DIR, true);
    }
    public static boolean getDisabled(Context cxt) {
        return PreferenceManager.getDefaultSharedPreferences(cxt).getBoolean(DISABLED, false);
    }
    public static int getRadius(Context cxt) {
        return PreferenceManager.getDefaultSharedPreferences(cxt).getInt(RADIUS, 5);
    }

    public static boolean getDownload(Context cxt) {
        return PreferenceManager.getDefaultSharedPreferences(cxt).getBoolean("download_preference", true);
    }
    public static int getRetryCount(Context cxt) {
        return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(cxt).getString("retry_preference", "3"));
    }

    public static boolean getDebug(Context cxt) {
        return PreferenceManager.getDefaultSharedPreferences(cxt).getBoolean(DEBUG_PREFERENCE, false);
    }

    public static String getDefaultRadius(Context cxt) {
        return PreferenceManager.getDefaultSharedPreferences(cxt).getString(RADUIS, "5");
    }

    public static void saveDefaultRadius(Context cxt, String radius) {
        Editor edit = PreferenceManager.getDefaultSharedPreferences(cxt).edit();
        edit.putString(RADUIS, radius);
        edit.commit();
    }

    public static boolean isAutoName(Context cxt) {
        return PreferenceManager.getDefaultSharedPreferences(cxt).getBoolean(AUTO_NAME, false);
    }

    public static boolean isMetric(Context cxt) {
        return PreferenceManager.getDefaultSharedPreferences(cxt).getBoolean("metric_preference", false);
    }

    public static ContainerTypeList getContainerTypeFilter(Context cxt) {
        String list = PreferenceManager.getDefaultSharedPreferences(cxt).getString(CONTAINER_FILTER, "");
        return new ContainerTypeList(list);
    }

    public static CacheTypeList getCacheTypeFilter(Context cxt) {
        String list = PreferenceManager.getDefaultSharedPreferences(cxt).getString(CACHE_FILTER, "");
        return new CacheTypeList(list);
    }

    public static OneToFiveFilter getDifficultyFilter(Context cxt) {
        String d = PreferenceManager.getDefaultSharedPreferences(cxt).getString(DIFFICULTY_FILTER, "1 - 5");
        return new OneToFiveFilter(d);
    }

    public static OneToFiveFilter getTerrainFilter(Context cxt) {
        String t = PreferenceManager.getDefaultSharedPreferences(cxt).getString(TERRAIN_FILTER, "1 - 5");
        return new OneToFiveFilter(t);
    }

    public static void saveCacheTypeFilter(Context cxt, CacheTypeList list) {
        Editor edit = PreferenceManager.getDefaultSharedPreferences(cxt).edit();
        edit.putString(CACHE_FILTER, list.toString());
        edit.commit();
    }

    public static void saveContainerTypeFilter(Context cxt, ContainerTypeList list) {
        Editor edit = PreferenceManager.getDefaultSharedPreferences(cxt).edit();
        edit.putString(CONTAINER_FILTER, list.toString());
        edit.commit();
    }

    public static void saveDifficultyFilter(Context cxt, OneToFiveFilter d) {
        Editor edit = PreferenceManager.getDefaultSharedPreferences(cxt).edit();
        edit.putString(DIFFICULTY_FILTER, d.toString());
        edit.commit();
    }

    public static void saveTerrainFilter(Context cxt, OneToFiveFilter t) {
        Editor edit = PreferenceManager.getDefaultSharedPreferences(cxt).edit();
        edit.putString(TERRAIN_FILTER, t.toString());
        edit.commit();
    }

    public static void saveCookies(Context cxt, List<Cookie> cookies) {

        String s = "";

        // Store the username and password at front of cookie
        // Allows us to detect if username or password is changed. We dump cookie in that case

        s += getUsername(cxt) + COMMA + getPassword(cxt) + SEMI_COLON;

        // Store the cookie list into a string

        for (Cookie c: cookies) {
            s += c.getName() + COMMA + c.getValue() + SEMI_COLON;
        }

        Editor edit = PreferenceManager.getDefaultSharedPreferences(cxt).edit();

        // Not sure what values we are allowed to have in a preferences string, but
        // I kept loosing the preference values on a reboot until I base64'ed the string

        edit.putString(COOKIES, Base64.encodeBytes(s.getBytes()));

        edit.commit();
    }

    public static void clearCookies(Context cxt) {
        Editor edit = PreferenceManager.getDefaultSharedPreferences(cxt).edit();
        edit.remove(COOKIES);
        edit.commit();
    }

    public static List<Cookie> getCookies(Context cxt) {
        ArrayList<Cookie> ret  = new ArrayList<Cookie>();

        // Decode Base64 wrapper

        String s;
        try {
            s = new String(Base64.decode(PreferenceManager.getDefaultSharedPreferences(cxt).getString(COOKIES, "")));
        } catch (IOException e) {
            Logger.e("base64 problem",e);
            return ret;
        }

        if (s.length()==0)          // No cookie yet
            return ret;

        // Split seperate cookies by SEMI_COLON, then name/value pair by COMMA

        String cookies[] = s.split(SEMI_COLON);

        for (int i=0; i<cookies.length; i++) {

            String cookie = cookies[i];
            String parts[] = cookie.split(COMMA);

            if (i==0) {
                // Username and password are stored in first cookie slot
                // Check match current preferences. If not we have to dump cookie

                if (parts[0].equals(getUsername(cxt)) && parts[1].equals(getPassword(cxt))) {
                    // Match ok
                } else {
                    // Cookie is no longer for the current user
                    clearCookies(cxt);      // Erase what we have
                    return ret;             // Return empty cookie
                }

            } else {

                // Check for bad cookie. Shouldn't happen
                if (parts.length==2)
                {
                    BasicClientCookie basicCookie = new BasicClientCookie(parts[0], parts[1]);
                    basicCookie.setDomain("www.geocaching.com");
                    basicCookie.setPath("/");
                    ret.add(basicCookie);
                }
                else
                {
                    Logger.e("got bad cookie [" + cookie +"]");
                } 
            }
        }

        return ret;
    }

    public static boolean isZip(Context cxt) {
        return PreferenceManager.getDefaultSharedPreferences(cxt).getBoolean("zip_preference", true);
    }
    public static String getUsername(Context cxt) {
        return PreferenceManager.getDefaultSharedPreferences(cxt).getString(USERNAME, "");
    }

    public static String getPassword(Context cxt) {
        return PreferenceManager.getDefaultSharedPreferences(cxt).getString(PASSWORD, "");
    }

    public static void saveCheckBoxesFilter(Context cxt, CheckBoxesFilter checkBoxesFilter) {

        Editor edit = PreferenceManager.getDefaultSharedPreferences(cxt).edit();
        edit.putBoolean(ENABLED_FILTER, checkBoxesFilter.enabled);
        edit.putBoolean(TRAVEL_BUG_FILTER, checkBoxesFilter.travelBug);
        edit.putBoolean(NOT_IGNORED_FILTER, checkBoxesFilter.notOnIgnore);
        edit.putBoolean(FOUND_7DAYS_FILTER, checkBoxesFilter.found7days);
        edit.putBoolean(NOT_FOUND_FILTER, checkBoxesFilter.notFound);
        edit.commit();
    }

    public static void saveAutoName(Context cxt, boolean autoName) {
        Editor edit = PreferenceManager.getDefaultSharedPreferences(cxt).edit();
        edit.putBoolean(AUTO_NAME, autoName);
        edit.commit();
    }
    public static void saveUserSpecifiedDownloadDir(Context cxt, String dir) {
        Editor edit = PreferenceManager.getDefaultSharedPreferences(cxt).edit();
        edit.putString(USER_DOWNLOAD_DIR, dir);
        edit.commit();
    }

    public static void savePQListState(Context cxt, PQ [] pqs) {
        String pqSerialized = null;

        if (pqs!=null)
        {
            pqSerialized = new Gson().toJson(pqs);
        
//            for (int i=0; i<pqs.length; i++) {
//                try {
//                    pqSerialized += new Gson().toJson(pqs);
//
//                    String bpb = Base64.encodeObject(pqs[i], Base64.GZIP);
//                    
//                    if (i+1 < pqs.length)
//                        pqSerialized += SEMI_COLON;
//
//                } catch (IOException e) {
//                    Logger.e("Unable to serialize PQ object",e);
//                    return;
//                }
//            }
        }

        Editor edit = PreferenceManager.getDefaultSharedPreferences(cxt).edit();
        edit.putString(PQ_LIST_STATE, pqSerialized);
        edit.putLong(PQ_LIST_STATE_TIMESTAMP, new Date().getTime());
        edit.commit();
    }

    public static long getPQListStateTimestamp(Context cxt) {
        return PreferenceManager.getDefaultSharedPreferences(cxt).getLong(PQ_LIST_STATE_TIMESTAMP, 0);
    }

    public static PQ[] getPQListState(Context cxt) {
        String pqsSerial = PreferenceManager.getDefaultSharedPreferences(cxt).getString(PQ_LIST_STATE, null);

        if (pqsSerial==null)
            return null;

        if (pqsSerial=="")
            return new PQ[0];

        
        return new Gson().fromJson(pqsSerial, PQ[].class);
        
//        String pqsSerialSplit[] = pqsSerial.split(SEMI_COLON);
//
//        PQ[] pqs = new PQ[pqsSerialSplit.length];
//
//        try {
//            for (int i=0; i<pqs.length; i++) {
//                pqs[i] = (PQ) Base64.decodeObject(pqsSerialSplit[i]);
//            }
//            return pqs;
//        } catch (IOException e) {
//            Logger.e("Error",e);
//        } catch (ClassNotFoundException e) {
//            Logger.e("Error",e);
//        }
       // return new PQ[0];
    }

    public static void userNameChanged(Context cxt) {
        Editor edit = PreferenceManager.getDefaultSharedPreferences(cxt).edit();
        edit.remove(PQ_LIST_STATE);
        edit.putLong(PQ_LIST_STATE_TIMESTAMP, new Date().getTime());
        edit.remove(COOKIES);
        edit.commit();
    }

    public static void erasePQListState(Context cxt) {
        Editor edit = PreferenceManager.getDefaultSharedPreferences(cxt).edit();
        edit.remove(PQ_LIST_STATE);
        edit.remove(PQ_LIST_STATE_TIMESTAMP);
        edit.commit();
    }
}
