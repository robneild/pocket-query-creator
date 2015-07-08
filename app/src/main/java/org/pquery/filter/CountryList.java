package org.pquery.filter;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;

import org.pquery.R;
import org.pquery.util.MyColors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class CountryList implements Iterable<Country> {

    /**
     * The 'inner' list that this class wraps
     */
    private List<Country> inner = new ArrayList<Country>();

    /**
     * How many countries are there
     */
    private int maxCountries;


    /**
     * Create list containing ALL countries
     *
     * @param cxt
     */
    public CountryList(Context cxt) {

        String [] allCountryNames = cxt.getResources().getStringArray(R.array.country_names);
        String [] allCountryCodes = cxt.getResources().getStringArray(R.array.country_codes);
        TypedArray allCountryFlags = cxt.getResources().obtainTypedArray(R.array.country_flags);

        for (int i=0; i<allCountryNames.length; i++) {
            //Drawable flagDrawable = cxt.getResources().getDrawable(allCountryFlags.getResourceId(i, -1));
            inner.add(new Country(allCountryNames[i], allCountryCodes[i], allCountryFlags.getResourceId(i, -1)));
        }

        maxCountries = allCountryNames.length;

        Collections.sort(inner);
    }

    /**
     * Create list of countries from a comma separated encoded string (as stored in prefs)
     *
     * @param cxt
     * @param s comma separated country code list. If null assume ALL countries. If empty assume NO countries
     */
    public CountryList(Context cxt, String s) {        // no countries;

        String [] allCountryNames = cxt.getResources().getStringArray(R.array.country_names);
        String [] allCountryCodes = cxt.getResources().getStringArray(R.array.country_codes);
        TypedArray allCountryFlags = cxt.getResources().obtainTypedArray(R.array.country_flags);

        maxCountries = allCountryNames.length;


        if (s == null)
            s = "";     // means ALL countries
        else if (s.length() == 0)
            return;     // no countries, so don't proceed;

        List countyCodes = Arrays.asList(s.split(","));



        for (int i=0; i<allCountryNames.length; i++) {

            // s.length() picks up the ALL countries case
            if (s.length() == 0 || countyCodes.contains(allCountryCodes[i])) {
                //Drawable flagDrawable = cxt.getResources().getDrawable(allCountryFlags.getResourceId(i, -1));
                inner.add(new Country(allCountryNames[i], allCountryCodes[i], allCountryFlags.getResourceId(i, -1)));
            }
        }

        Collections.sort(inner);
    }

    /**
     * Encode the country list into a comma separated country code string
     * If ALL countries are selected then 'null' is returned
     *
     * @return encoded string or null
     */
    public String toString() {
        if (isAll())
            return null;                             // all countries

        String ret = "";
        for (Country country : inner) {
            ret += country.getCode() + ",";
        }
        return ret;
    }

    public String toLocalisedString(Resources res) {
        if (isAll())
            return "<font color='" + MyColors.LIME + "'>" + res.getString(R.string.any) + "</font>";
        if (isNone())
            return "<font color='red'>" + res.getString(R.string.none) + "</font>";

        StringBuffer ret = new StringBuffer();
        ret.append("<font color='" + MyColors.MEGENTA + "'>");

        for (Country country : inner) {
            ret.append(country.getName());
            ret.append(", ");
        }
        // Knock off ending ', '
        if (ret.length() > 0)
            ret.setLength(ret.length() - 2);

        ret.append("</font>");
        return ret.toString();
    }

    public boolean isNone() {
        return inner.size() == 0;
    }

    public boolean isAll() {
        return inner.size() == maxCountries;
    }

    /*
    public boolean add(ContainerType container) {
        return inner.add(container);
    }

    public void clear() {
        inner.clear();
    }

    public void setAll() {
        inner.clear();
    }

    public boolean isAll() {
        if (inner.size() == 0)
            return true;
        return false;
    }
    */

    @Override
    public Iterator<Country> iterator() {
        return inner.iterator();
    }

    public Country get(int position) {
        return inner.get(position);
    }

    public boolean contains(Country country) {
        return inner.contains(country);
    }

    public List<Country> getInner() {
        return inner;
    }

    public void remove(Country clickedCountry) {
        inner.remove(clickedCountry);
        Collections.sort(inner);
    }

    public void add(Country clickedCountry) {
        inner.add(clickedCountry);
        Collections.sort(inner);
    }

    public void clear() { inner.clear(); };
}
