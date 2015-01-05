package org.pquery.filter;

import android.content.res.Resources;

import junit.framework.Assert;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Represents a list of CacheTypes
 * Used as a creation filter
 */
public class CacheTypeList implements Iterable<CacheType> {

    /**
     * wrapped list
     */
    private LinkedList<CacheType> inner = new LinkedList<CacheType>();

    public CacheTypeList() {
    }


    /**
     * Construct from a string of comma separated string
     * Used to retrieve from preferences
     */
    public CacheTypeList(String s) {
        Assert.assertNotNull(s);

        String[] cacheList = s.split(",");

        for (String cache : cacheList) {
            if (cache.length() > 0)
                inner.add(CacheType.valueOf(cache));
        }

        if (inner.size() == 0) {
            setAll();
        }
    }

    /**
     * Create list from an array
     * Expect array to contain a boolean for each CacheType
     */
    public CacheTypeList(boolean[] selection) {

        Assert.assertEquals(CacheType.values().length, selection.length);

        for (int i = 0; i < selection.length; i++) {

            if (selection[i]) {
                inner.add(CacheType.values()[i]);
            }
        }

        if (CacheType.values().length == inner.size())
            inner.clear();

    }

    /**
     * Convert to comma seperated string. Uses enum type name
     */
    public String toString() {
        String ret = "";
        for (CacheType cache : inner) {
            ret += cache.toString() + ",";
        }
        return ret;
    }

    /**
     * A nice, comma separated list for presentation to user
     * Enum value converted into localized
     */
    public String toLocalisedString(Resources res) {
        StringBuffer ret = new StringBuffer();

        for (CacheType cache : inner) {
            ret.append(res.getString(cache.getResourceId()) + ", ");
        }
        // Knock off ending ', '
        if (ret.length() > 0)
            ret.setLength(ret.length() - 2);

        return ret.toString();
    }

    public boolean add(CacheType cache) {
        return inner.add(cache);
    }

    public void clear() {
        inner.clear();
    }

    public void setAll() {
        inner.clear();
    }

    /**
     * Does list contain all CacheTypes
     * Using an empty list as a shortcut to represent all
     */
    public boolean isAll() {
        if (inner.size() == 0)
            return true;
        return false;
    }

    @Override
    public Iterator<CacheType> iterator() {
        return inner.iterator();
    }

    public boolean[] getAsBooleanArray() {
        boolean[] ret = new boolean[CacheType.values().length];

        if (isAll()) {
            for (int i = 0; i < ret.length; i++) {
                ret[i] = true;
            }
            return ret;
        }

        for (int i = 0; i < CacheType.values().length; i++) {
            if (inner.contains(CacheType.values()[i]))
                ret[i] = true;
        }

        return ret;
    }
}
