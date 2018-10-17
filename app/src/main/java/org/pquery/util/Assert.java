package org.pquery.util;

import android.os.PowerManager;

public class Assert {

    public static void assertNotNull(Object a) {
        if (a == null) {
            throw new IllegalArgumentException("null reference");
        }
    }

    public static void assertEquals(int a, int b) {
        if (a != b) {
            throw new IllegalArgumentException("not equal");
        }
    }

    public static void fail() {
        throw new IllegalArgumentException("fail");
    }

    public static void assertTrue(boolean a) {
        if (!a) {
            throw new IllegalArgumentException("not true");
        }
    }

    public static void assertFalse(boolean a) {
        if (a) {
            throw new IllegalArgumentException("true");
        }
    }

    public static void assertNull(Object a) {
        if (a != null) {
            throw new IllegalArgumentException("not null");
        }
    }
}
