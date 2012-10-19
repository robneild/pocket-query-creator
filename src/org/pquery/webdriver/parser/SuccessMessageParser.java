package org.pquery.webdriver.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pquery.R;
import org.pquery.webdriver.FailureDontRetry;

import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;

/**
 * Parser the message we get back after pq creation
 * 
 * Should look like this
 * 
 * <p class="Success">
 * Thanks! Your pocket query has been saved and currently results in 10 caches.
 * You can <a href=
 * "http://www.geocaching.com/seek/nearest.aspx?pq=dd2e3f12-2c04-4090-bd1b-4e601f521fe5"
 * title="Preview the Search">preview the search</a> on the nearest cache page.
 * 
 * <p class="Success">
 * Merci ! Votre pocket query a été modifié et comprend présentement 10 caches.
 * Vous pouvez <a href=
 * "http://www.geocaching.com/seek/nearest.aspx?pq=692a0d6f-1494-4ba6-9ac9-5877aff7e6b4"
 * title="Prévisualiser la recherche">prévisualiser la recherche</a> sur la page
 * de caches la plus près.
 * </p>
 */
public class SuccessMessageParser {

    private static final String PARSE_ERROR_DOWNLOAD = "Parse error. Can't extract download link";
    private static final String PARSE_ERROR_NUMB = "Parse error. Can't extract numb generated pqs";
    private static final String NOT_FIND = "Response seemed to indicate creation failed";

    public String successMessage;

    /**
     * Try to parse out success message from passed in html
     */
    public SuccessMessageParser(String html) throws FailureDontRetry {

        final String SUCCESS_START = Pattern.quote("<p class=\"Success\">");
        final String SUCCESS_END = Pattern.quote("</p>");
        
        Matcher m = Pattern.compile(SUCCESS_START + "(.+)" + SUCCESS_END).matcher(html);
        
        if (m.find()) {
            String name = m.group(1);
            successMessage = name;
        } else
            throw new FailureDontRetry(NOT_FIND);
    }


    public String extractDownload() throws FailureDontRetry {

        final String START = "nearest.aspx?pq=";

        int start = successMessage.indexOf(START); // start
        if (start == -1)
            throw new FailureDontRetry(PARSE_ERROR_DOWNLOAD);

        int end = successMessage.indexOf("\" title=", start + START.length());
        if (end == -1)
            throw new FailureDontRetry(PARSE_ERROR_DOWNLOAD);

        String guid = successMessage.substring(start + START.length(), end);
        return "pocket/downloadpq.ashx?g=" + guid;
    }

    /**
     * Return number of created Pocket Queries
     * 
     * Matches any digit group before the download link starts
     */
    public int extractNumberPQ() throws FailureDontRetry {

        Matcher m = Pattern.compile("(\\d+).*?<a href").matcher(successMessage);
        
        while (m.find()) {
            String name = m.group(1);
            return Integer.parseInt(name);
        }
        
        throw new FailureDontRetry(PARSE_ERROR_NUMB);
    }

    public String toString(Resources res) throws FailureDontRetry {
        return res.getString(R.string.created_ok) + " " + extractNumberPQ() + " caches";
    }
}
