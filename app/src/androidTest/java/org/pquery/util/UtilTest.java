package org.pquery.util;

import android.test.AndroidTestCase;

public class UtilTest extends AndroidTestCase {

    public void testHumanDownloadCounter() {
        assertEquals("0 KiB", Util.humanDownloadCounter(0, 0));
    }

    public void testSanitizeFileName() {
        assertEquals("rob", Util.sanitizeFileName("rob"));
    }

    public void testSanitizeFileName3() {
        assertEquals("rob", Util.sanitizeFileName("rob[]<>"));
    }

    public void testSanitizeFileName4() {
        assertEquals("rob", Util.sanitizeFileName("rob|?*\""));
    }

    public void testSanitizeFileName5() {
        assertEquals("rob", Util.sanitizeFileName("rob:+\\/'"));
    }

    public void testSanitizeFileName_Default() {
        assertEquals("pocketquery", Util.sanitizeFileName("[]"));
    }
}
