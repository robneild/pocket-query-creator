package org.pquery.webdriver.parser;

import android.test.AndroidTestCase;

import org.pquery.webdriver.FailurePermanentException;

public class SuccessMessageParserTest extends AndroidTestCase {

    private static final String GOOD = "<p class=\"Success\">Thanks! Your pocket query has been saved and currently results in 10 caches. You can <a href=\"http://www.geocaching.com/seek/nearest.aspx?pq=dd2e3f12-2c04-4090-bd1b-4e601f521fe5\" title=\"Preview the Search\">preview the search</a> on the nearest cache page.</p>";

    /**
     * Extract download link from good English html
     *
     * @throws FailurePermanentException
     */
    public void testExtractDownloadLink() throws FailurePermanentException {
        assertEquals("dd2e3f12-2c04-4090-bd1b-4e601f521fe5", new SuccessMessageParser(GOOD).extractDownloadGuid());
    }

    /**
     * Throw exception. End of download link is missing
     *
     * @throws FailurePermanentException
     */
    public void testExtractDownloadLinkBad() throws FailurePermanentException {
        SuccessMessageParser p = new SuccessMessageParser("<p class=\"Success\">Thanks! Your pocket query has been saved and currently results in 10 caches. You can <a href=\"http://www.geocaching.com/seek/nearest.aspx?pq=dd2e3f12-2c04-4090-bd1b-4e601f</p>");
        try {
            p.extractDownloadGuid();
            fail();
        } catch (FailurePermanentException e) {
        }
    }

    /**
     * Extract out DownloadablePQ number from good English html
     *
     * @throws FailurePermanentException
     */
    public void testExtractNumberPQ() throws FailurePermanentException {
        assertEquals(10, new SuccessMessageParser(GOOD).extractNumberPQ());
    }

    public void testExtractNumberPQBad() throws FailurePermanentException {
        SuccessMessageParser p = new SuccessMessageParser("<p class=\"Success\">Thanks! Your pocket query has been saved and currently. You can <a href=\"http://www.geocaching.com/seek/nearest.aspx?pq=dd2e3f12-2c04-4090-bd1b-4e601f521fe5\" title=\"Preview the Search\">preview the search</a> on the nearest cache page.</p>");
        try {
            assertEquals(10, p.extractNumberPQ());
            fail();
        } catch (FailurePermanentException e) {
        }
    }
}
