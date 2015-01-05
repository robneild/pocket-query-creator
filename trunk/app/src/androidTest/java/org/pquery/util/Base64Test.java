package org.pquery.util;

import android.test.AndroidTestCase;

import java.io.IOException;


public class Base64Test extends AndroidTestCase {

    public void testEncodeDecode() throws IOException {
        byte[] data = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 12, 100};

        String encoded = Base64.encodeBytes(data);
        String decoded = new String(Base64.decode(encoded));

        assertEquals(new String(data), decoded);
    }

    public void testWikipediaEncode() {
        assertEquals("cGxlYXN1cmUu", Base64.encodeBytes("pleasure.".getBytes()));
    }

    public void testWikipediaDecode() throws IOException {
        String data = "cGxlYXN1cmUu";

        assertEquals("pleasure.", new String(Base64.decode(data)));
    }
}
