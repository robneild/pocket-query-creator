package org.pquery.util;

import java.text.DateFormat;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class Prefs {

    private static final String CACHE_FILTER = "cache_type_filter_preference";
    private static final String CONTAINER_FILTER = "container_type_filter_preference";
    private static final String RADUIS = "radius_preference";
    
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
    
}
