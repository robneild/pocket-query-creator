package org.pquery.webdriver.parser;

import android.content.res.Resources;

import org.pquery.R;
import org.pquery.webdriver.FailurePermanentException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser the message we get back after pq creation. It appears at top of
 * creation page in reponse to the creation POST
 * <p/>
 * There are some examples in different languages
 * <p/>
 * <p class="Success">
 * Thanks! Your pocket query has been saved and currently results in 10 caches.
 * You can <a href=
 * "http://www.geocaching.com/seek/nearest.aspx?pq=dd2e3f12-2c04-4090-bd1b-4e601f521fe5"
 * title="Preview the Search">preview the search</a> on the nearest cache page.
 * <p/>
 * <p class="Success">
 * Merci ! Votre pocket query a �t� modifi� et comprend pr�sentement 10 caches.
 * Vous pouvez <a href=
 * "http://www.geocaching.com/seek/nearest.aspx?pq=692a0d6f-1494-4ba6-9ac9-5877aff7e6b4"
 * title="Pr�visualiser la recherche">pr�visualiser la recherche</a> sur la page
 * de caches la plus pr�s.
 * </p>
 */
public class SuccessMessageParser {

    private static final String PARSE_ERROR_DOWNLOAD = "Parse error. Can't extract download link";
    private static final String PARSE_ERROR_NUMB = "Parse error. Can't extract numb generated pqs";
    private static final String NOT_FIND = "Response seemed to indicate creation failed";

    public String successMessage;

    /**
     * Try to parse out success message from passed in html
     * <p/>
     * Extracts out everything between <p class="Success"> and </p>
     */
    public SuccessMessageParser(String html) throws FailurePermanentException {

        final String START = Pattern.quote("<p class=\"Success\">");
        final String END = Pattern.quote("</p>");

        Matcher m = Pattern.compile(START + "(.+)" + END).matcher(html);

        if (m.find()) {
            String name = m.group(1);
            successMessage = name;
        } else
            throw new FailurePermanentException(NOT_FIND);
    }


    /**
     * Extract out the Pocket Query guid (within the preview link)
     * We need it to calculate the download link
     * <p/>
     * Extract between nearest.aspx?pq= and end "
     * <p/>
     * <a href="http://www.geocaching.com/seek/nearest.aspx?pq=692a0d6f-1494-4ba6-9ac9-5877aff7e6b4"
     */
    public String extractDownloadGuid() throws FailurePermanentException {

        final String START = Pattern.quote("nearest.aspx?pq=");
        final String END = Pattern.quote("\"");

        Matcher m = Pattern.compile(START + "(.+?)" + END).matcher(successMessage);

        while (m.find()) {
            String guid = m.group(1);
            return guid;
        }

        throw new FailurePermanentException(PARSE_ERROR_DOWNLOAD);
    }

    /**
     * Return number of created Pocket Queries
     * <p/>
     * Matches any digit group before the download link starts
     */
    public int extractNumberPQ() throws FailurePermanentException {

        Matcher m = Pattern.compile("(\\d+).*?<a href").matcher(successMessage);

        while (m.find()) {
            String name = m.group(1);
            return Integer.parseInt(name);
        }

        throw new FailurePermanentException(PARSE_ERROR_NUMB);
    }

    public String toString(Resources res) throws FailurePermanentException {
        return res.getString(R.string.created_ok) + " " + extractNumberPQ() + " caches";
    }
}
