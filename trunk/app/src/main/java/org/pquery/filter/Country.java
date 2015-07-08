package org.pquery.filter;

/**
 * Represents a single country. Usually held within a CountryList
 */
public class Country implements Comparable<Country> {

    private String name;    /* localized name */
    private String code;    /* geocaching country code? */
    private int flag;       /* image resource id */

    public Country(String name, String code, int flag) {
        this.name = name;
        this.code = code;
        this.flag = flag;
    }

    public String getName() {
        return name;
    }

    public int getFlag() {
        return flag;
    }

    public String getCode() {
        return code;
    }

    /**
     * Base on country code only
     */
    @Override
    public boolean equals(Object other) {
        if(other == null)
            return false;
        if(!(other instanceof Country))
            return false;

        Country otherCountry = (Country) other;
        return this.code.equals(otherCountry.code);
    }

    /**
     * Base on country code onlu
     */
    @Override
    public int hashCode() {
        return this.code.hashCode();
    }

    /**
     * Used to alphabetically sort. Base on the localized country name
     */
    @Override
    public int compareTo(Country another) {
        return name.compareTo(another.name);
    }
}