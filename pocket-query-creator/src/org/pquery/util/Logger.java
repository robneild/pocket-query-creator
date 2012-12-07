package org.pquery.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.http.cookie.Cookie;
import org.apache.http.message.BasicNameValuePair;

import android.os.Environment;
import android.text.format.Time;
import android.util.Log;

public class Logger {

    public static final String APPNAME = "PocketQueryCreator";

    public static final String STORE_DIR = "/Android/data/org.pquery/files";
    public static final String LOG_NAME = "log.html";

    private static File file;
    private static FileOutputStream fout;

    private static boolean enabled;
    private static boolean fileOpenOk;

    /**
     * Turn on logging
     * An empty log file is created when first turned on
     */
    public static void setEnable(boolean e) {
        if (enabled == e)
            return;     // no change
        enabled = e;
        if (enabled)
            init();
    }

    public static boolean isEnabled() {
        return enabled;
    }
    
    public static void d(String message) {
        log(message, Log.DEBUG);
    }

    public static void w(String message) {
        log(message, Log.WARN);
    }
    
    public static void cookie(String message, List<Cookie> cookies) {
        if (enabled) {
            StringBuffer s = new StringBuffer();
            s.append(message + "\n");
            for (Cookie cookie : cookies) {
                s.append("name=" + cookie.getName() + ", value=" + cookie.getValue()+"\n");
            }
            d(s.toString());
        }
    }
    
    public static void d(List <BasicNameValuePair> list) {
        if (enabled) {
            StringBuffer s = new StringBuffer();
            for (BasicNameValuePair nvp : list) {
                // Don't log password into log file
                if (nvp.getName().equals("ctl00$tbPassword"))
                    s.append(nvp.getName()+ " = password not logged\n");
                else
                    s.append(nvp.getName()+ " = " + nvp.getValue()+"\n");
            }
            d(s.toString());
        }
    }
    public static void i(String message) {
        log(message, Log.INFO);
    }

    public static void e(String message) {
        log(message, Log.ERROR);
    }

    public static void e(String message, Throwable e) {
        log(message + "\n" + Log.getStackTraceString(e), Log.ERROR);
    }
    
    /**
     * Log to LogCat and attempt to log to external file (if open)
     */
    private static void log(String message, int level) {
        if (enabled) {
            StackTraceElement caller = Thread.currentThread().getStackTrace()[4];

            String[] classNameSplit = caller.getClassName().split("\\.");
            String className = classNameSplit[classNameSplit.length - 1];

            String location = className + ":" + caller.getMethodName() + "(" + caller.getLineNumber() + ")";

            Log.println(level, location, message);

            if (fileOpenOk) {
                try {
                    Time now = new Time();
                    now.setToNow();
                    StringBuffer out = new StringBuffer();
                    out.append("<font color=silver>" + now.format("%H:%M:%S") + "</font> ");
                    switch (level) {
                    case Log.DEBUG:
                        out.append("<font color=navy>DEBUG</font> ");
                        break;
                    case Log.INFO:
                        out.append("<font color=forestgreen>INFO</font> ");
                        break;
                    case Log.ERROR:
                        out.append("<font color=red>ERROR</font> ");
                        break;
                    }
                    out.append("<font color=navy>[" + location + "]</font> - " + replaceNewlineWithBr(message) + "<br>\r\n");
                    fout.write(out.toString().getBytes());
                } catch (IOException e) {
                    // Could happen if sdcard suddenly mounted
                    // When sdcard un-mounted we would need to re-create fout but can't be bothered
                }
            }
        }
    }

    /**
     * Initialise logging to LogCat and to external file. Any existing log file deleted
     * Has to handle no sdcard or sdcard mounted (fileOpenOk will remain false)
     */
    private static void init() {
        fileOpenOk = false;
        try {
            // Check for sdcard
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

                String appdir = getAppDirectory();

                new File(appdir).mkdirs();
                file = new File(appdir, LOG_NAME);

                if (file.exists()) {
                    if (!file.delete())
                        throw new IOException("unable to delete");
                }
                file.createNewFile();
                fout = new FileOutputStream(file);
                fout.write("<html><head><title>PocketQueryCreator log</title></head><body>\r\n".getBytes());
                fileOpenOk = true;
            }
        } catch (IOException e) {
            Log.e(APPNAME, "Unable to create external log file", e);
        }
    }

    public static File getLogFileName() {
        return new File(getAppDirectory(), LOG_NAME);
    }

    private static String getAppDirectory() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + STORE_DIR;
    }
    
    /**
     * Replace all \r\n or \n in a string with the html <br>
     * @param s
     * @return
     */
    public static String replaceNewlineWithBr(String s) {
        String out = s.replaceAll("\r\n|\n", "<br>");
        return out;
    }
}
