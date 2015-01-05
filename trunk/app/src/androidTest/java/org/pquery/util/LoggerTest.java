package org.pquery.util;

import android.test.AndroidTestCase;

public class LoggerTest extends AndroidTestCase {

    public void testReplaceNewlineWithBr() {
        assertEquals("x<br>x", Logger.replaceNewlineWithBr("x\nx"));
    }

    public void testReplaceNewlineWithBr2() {
        assertEquals("x<br>x", Logger.replaceNewlineWithBr("x\r\nx"));
    }
}
