package org.pquery.util;

import java.io.File;

import android.os.Environment;

public class Util {

    /**
     * Produce a human download progress string given bytes downloaded and total
     * e.g 14/100 KiB
     */
    public static String humanDownloadCounter(int bytesDownloaded, int expected) {
        StringBuffer ret = new StringBuffer();

        ret.append(bytesDownloaded / 1024);
        if (expected > 0)
            ret.append("/" + expected / 1024);
        ret.append(" KiB");
        
        return ret.toString();
    }

    private static final String reservedCharsRegExp = "[|\\\\?*<\":>+\\[\\]/']";
    
    /**
     * Try to create a legal filename from what user entered
     * 
     * Strip illegal characters.
     * If that ends up with an empty filename, use a default
     */
    public static String sanitizeFileName(String name) {
        name = name.replaceAll(reservedCharsRegExp, "");
        if (name.length()==0)
            name = "pocketquery";
        return name;
    }
    
    /**
     * Create a sane, unique file
     * Strips illegal characters and adds numbers on end to make unique (if necessary)
     */
    public static File getUniqueFile(String path, String name, String extension) {
        
        name = sanitizeFileName(name);
        
        int i=0;
        File file = new File(path, name+"."+extension);
        while (file.exists()) {
            // Try to add a number at end to make unique
            i++;
            file = new File(path, name + "(" + i +")."+extension);
        }
        return file;
    }
    
    public static String getDefaultDownloadDirectory() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
        + "Download";
    }
}
