package org.pquery.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.pquery.R;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
    
    /**
     * Read a 'Reader' (e.g. FileReader), into a String
     */
    public static String readerIntoString(Reader read) throws IOException {
        StringBuffer out = new StringBuffer();
        BufferedReader buffreader = new BufferedReader (read) ;

        String line = buffreader.readLine () ;
        while (line != null ) {
            out.append(line);
            out.append("\r\n");     // 'readLine' strips newlines so have to put back on
            line = buffreader.readLine ();
        }
        return out.toString();
    }
    
    public static String inputStreamIntoString(InputStream streamIn) throws IOException {
        return readerIntoString(new InputStreamReader(streamIn, "utf8"));
    }
    

    public static Drawable toGrey(Resources res, int resource) {
        
        Drawable draw = res.getDrawable(resource);

        Bitmap b = ((BitmapDrawable) draw).getBitmap().copy(Config.ARGB_8888, true);
       
        //Bitmap copy = src.copy(Config.ARGB_8888, true);
        
        for(int x = 0;x < b.getWidth();x++)
            for(int y = 0;y < b.getHeight();y++) {
                int color = b.getPixel(x, y);
                int newColor = Color.argb(Color.alpha(color)/3, Color.red(color), Color.green(color), Color.blue(color));
                b.setPixel(x, y, newColor);
        
            }
        
        Drawable greyDrawable = new BitmapDrawable(res, b);
        
        return greyDrawable;
    }
}
