package com.gisgraphy.gishraphoid;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class JTSHelper {

    /**
     * @param latitude the latitude to test
     * @return true if correct
     * @throw new {@link IllegalArgumentException} if not correct
     */
    public static boolean checkLatitude(double latitude) {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("latitude is out of bound");
        }
        return true;
    }

    /**
     * @param longitude the latitude to test
     * @return true if correct
     * @throw new {@link IllegalArgumentException} if not correct
     */
    public static boolean checkLongitude(double longitude) {
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("longitude is out of bound");
        }
        return true;
    }

    /**
     * parse a string and return the corresponding double value, it accepts
     * comma or point as decimal separator
     *
     * @param number the number with a point or a comma as decimal separator
     * @return the float value corresponding to the parsed string
     * @throws ParseException in case of errors
     */
    public static Float parseInternationalDouble(String number) throws ParseException {
        NumberFormat nffrench = NumberFormat.getInstance(Locale.FRENCH);
        NumberFormat nfus = NumberFormat.getInstance(Locale.US);

        Number numberToReturn = number.indexOf(',') != -1 ? nffrench.parse(number) : nfus.parse(number);
        return numberToReturn.floatValue();
    }

}
