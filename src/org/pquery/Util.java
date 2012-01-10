package org.pquery;

import static org.pquery.Util.APPNAME;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.os.Environment;
import android.util.Log;

public class Util {

    public static final String APPNAME = "PocketQueryCreator";

    public static final String STORE_DIR = "/Android/data/org.pquery/files";
    public static final String BAD_HTML_RESPONSE = "bad_response.html";

    /**
     * Store response from geocaching into file
     */
    public static void writeBadHTMLResponse(String place, String html) {
        try {
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

                String appdir = getAppDirectory();

                new File(appdir).mkdirs();                
                File file = new File(appdir, BAD_HTML_RESPONSE);

                if (file.exists()) {
                    if (!file.delete())
                        throw new IOException("unable to delete");
                }



                file.createNewFile();

                FileOutputStream fout = new FileOutputStream(file);
                fout.write(html.getBytes());
                fout.write((APPNAME+". ").getBytes());
                fout.write(place.getBytes());
                fout.close();


            }
        } catch (IOException e) {
            Log.e(APPNAME, "Unable to create bad_response.html",e);
        }

    }

    public static boolean isBadHTMLResponseExists() {
        File file = new File(getAppDirectory(), BAD_HTML_RESPONSE);
        return file.exists();
    }
    
    public static void deleteBadHTMLResponse() {
        File file = new File(getAppDirectory(), BAD_HTML_RESPONSE);
        file.delete();
    }
    
    public static File getBadHTMLResponseFile() {
        return new File(getAppDirectory(), BAD_HTML_RESPONSE);
    }
    
    private static String getAppDirectory() {
        return Environment.getExternalStorageDirectory().getAbsolutePath()+STORE_DIR;
    }
}
