package org.pquery.util;

import static org.pquery.Util.APPNAME;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

public class Prefs {

    private static final String CACHE_FILTER = "cache_type_filter_preference";
    private static final String CONTAINER_FILTER = "container_type_filter_preference";
    private static final String RADUIS = "radius_preference";
    private static final String COOKIES = "cookies_preference";
    private static final String USERNAME = "username_preference";
    private static final String PASSWORD = "password_preference";
    
    private static final String COMMA = "\u001F";
    private static final String SEMI_COLON = "\u007F";
    
    public static String getDefaultRadius(Context cxt) {
        return PreferenceManager.getDefaultSharedPreferences(cxt).getString(RADUIS, "5");
    }

    public static void saveDefaultRadius(Context cxt, String radius) {
        Editor edit = PreferenceManager.getDefaultSharedPreferences(cxt).edit();
        edit.putString(RADUIS, radius);
        edit.commit();
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
            Log.e(APPNAME, "getCookies", e);
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
                BasicClientCookie basicCookie = new BasicClientCookie(parts[0], parts[1]);
                basicCookie.setDomain("www.geocaching.com");
                basicCookie.setPath("/");
                ret.add(basicCookie);
            }
        }
        
        return ret;
    }
    
    public static String getUsername(Context cxt) {
        return PreferenceManager.getDefaultSharedPreferences(cxt).getString(USERNAME, "");
    }
    
    public static String getPassword(Context cxt) {
        return PreferenceManager.getDefaultSharedPreferences(cxt).getString(PASSWORD, "");
    }

}
