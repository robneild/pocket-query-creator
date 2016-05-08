package org.pquery.util;

import android.content.Context;
import android.util.Pair;

import org.pquery.webdriver.CancelledListener;
import org.pquery.webdriver.HttpClientFactory;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;


public class IOUtils {


    private static final String SET_COOKIE = "Set-Cookie";


    /**
     * Used to store response from downloading a file
     */
    public static class FileDetails {
        public String filename;        // sent to us from far end server in the HTTP headers response
        public byte[] contents;
    }


    private static final int ESTIMATED_COMPRESSION_RATIO = 5;
    private static final int ESTIMATED_CONTENT_SIZE = 48000;

    public interface Listener {
        void update(int bytesReadSoFar, int expectedLength, int percent);
    }


    public static byte[] toByteArray(InputStream input, CancelledListener cancelledListener, Listener listener, int expectedLength) throws IOException, InterruptedException {

        byte[] buffer = new byte[3000];
        int total = 0;
        int i = 0;
        int updatesSent = 0;


        ByteArrayOutputStream byteOut = new ByteArrayOutputStream(expectedLength);

        while ((i = input.read(buffer)) != -1) {
            byteOut.write(buffer, 0, i);
            total += i;

            if (total / 3000 > updatesSent) {
                Logger.d("read " + total + "B of " + expectedLength + " expected");
                updatesSent++;
                if (listener != null) {
                    if (total > expectedLength)     // handle if we've gone past expectedLength (eg chunking)
                        listener.update(expectedLength - 1, expectedLength, 99);
                    else {
                        listener.update(total, expectedLength, total * 100 / expectedLength);
                    }
                }
            }
            cancelledListener.ifCancelledThrow();
        }

        Logger.d("toByteArray expectedLength=" + expectedLength + ", actualLength=" + total);

        return byteOut.toByteArray();
    }


    public static String httpGet(Context cxt, String path, CancelledListener cancelledListener, Listener listener) throws IOException, InterruptedException {
        FileDetails fileDetails = httpGetBytes(cxt, path, cancelledListener, listener);

        // Convert data into string. Geocaching.com uses utf-8 pages?
        String ret = new String(fileDetails.contents, "utf-8");
        Logger.d(ret);
        return ret;
    }

    /**
     * Read a html string from HttpResponse
     * Doesn't follow redirects and throws exception if http status code isn't good
     *
     * @throws InterruptedException
     */
    public static FileDetails httpGetBytes(Context cxt, String path, CancelledListener cancelledListener, Listener listener) throws IOException, InterruptedException {

        Logger.d("enter [path=" + getSecureHost() + path + "]");

        URL url = new URL(getSecureHost() + path);

        HttpURLConnection client = HttpClientFactory.createURLConnection(url);
        client.setRequestMethod("GET");

        // Restore cookies
        // This gives us a chance for server to think we are logged in

        Map<String,String> cookies = Prefs.getCookies(cxt);
        if (cookies.size() > 0) {
            String coo = "";
            for (Map.Entry<String, String> cookie : cookies.entrySet()) {
                if (coo.length() == 0) {
                    coo = cookie.getKey() + "=" + cookie.getValue();
                } else {
                    coo += ";" + cookie.getKey() + "=" + cookie.getValue();
                }
            }
            client.setRequestProperty("Cookie", coo);
        }

        // Initiate the HTTP request

        int length = (int) client.getContentLength();
        boolean chunked = false;
        if (client.getHeaderField("Transfer-Encoding") != null && client.getHeaderField("Transfer-Encoding").equalsIgnoreCase("Chunked")) {
            chunked = true;
        }
        String contentEncoding = client.getContentEncoding(); // .getFirstHeader("Content-Encoding");
        int statusCode = client.getResponseCode();
        String filename = decodeContentDispositionHeader(client.getHeaderField("Content-Disposition"));
        Logger.d("response [length=" + length + ",chunked=" + chunked + ",contentEncoding=" + contentEncoding + ",statusCode=" + statusCode + "]");

        // Read response

        if (length == -1)
            length = ESTIMATED_CONTENT_SIZE;   // if chunking is on, we have to guess final length

        InputStream in = client.getInputStream();
        byte data[] = IOUtils.toByteArray(in, cancelledListener, listener, length);

        // Handle if response is compressed

        if (contentEncoding != null && contentEncoding.equalsIgnoreCase("gzip")) {
            Logger.d("decoding gzip");
            in = new GZIPInputStream(new ByteArrayInputStream(data));
            data = IOUtils.toByteArray(in, cancelledListener, null, length * ESTIMATED_COMPRESSION_RATIO);
        }

        Map<String,String> newcookies = extractCookies(client);
        cookies.putAll(newcookies);

        Prefs.saveCookies(cxt, cookies);

        // Check for 404 page not found, 302 object moved etc
        // We read in body, even though error, because need to see the redirect link etc
        // Can assume error page is html (rather than binary) so ok to log it etc
        if (statusCode >= 300) {
            String s = new String(data);
            Logger.d("Bad status page response body.. " + s);
            throw new HTTPStatusCodeException(client.getResponseCode(), client.getResponseMessage(), s);
        }

        Logger.d("returning " + data.length + " bytes");

        FileDetails ret = new FileDetails();
        ret.filename = filename;
        ret.contents = data;

        return ret;
    }

    public static String httpPost(Context cxt, List<Pair<String,String>> paramList, String path, boolean secure, CancelledListener cancelledListener, Listener listener) throws IOException, InterruptedException {

        Logger.d("enter [path=" + getSecureHost() + path + "]");

        URL url = new URL(getSecureHost() + path);

        HttpURLConnection client = HttpClientFactory.createURLConnection(url);

        // Restore cookies
        // This gives us a chance for server to think we are logged in

        Map<String,String> cookies = Prefs.getCookies(cxt);
        if (cookies.size() > 0) {
            String coo = "";
            for (Map.Entry<String, String> cookie : cookies.entrySet()) {
                if (coo.length() == 0) {
                    coo = cookie.getKey() + "=" + cookie.getValue();
                } else {
                    coo += ";" + cookie.getKey() + "=" + cookie.getValue();
                }
            }

            coo += ";_gali=ctl00_ContentBody_btnSignIn";

            client.setRequestProperty("Cookie", coo);
        }





        // Setup output stuff
        client.setDoOutput(true);
        client.setRequestMethod("POST");
        client.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");



        // Add form parameters
        StringBuilder form = new StringBuilder();
        boolean first = true;

        for (Pair<String,String> pair : paramList) {
            if (first)
                first = false;
            else
                form.append("&");

            form.append(URLEncoder.encode(pair.first, "UTF-8"));
            form.append("=");
            form.append(URLEncoder.encode(pair.second, "UTF-8"));
        }



        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream(), "UTF-8"));
        writer.write(form.toString());
        writer.flush();
        writer.close();


        client.connect();


        // Initiate the HTTP request

        int length = (int) client.getContentLength();
        boolean chunked = false;
        if (client.getHeaderField("Transfer-Encoding") != null && client.getHeaderField("Transfer-Encoding").equalsIgnoreCase("Chunked")) {
            chunked = true;
        }
        String contentEncoding = client.getContentEncoding(); // .getFirstHeader("Content-Encoding");
        int statusCode = client.getResponseCode();
        String filename = decodeContentDispositionHeader(client.getHeaderField("Content-Disposition"));
        Logger.d("response [length=" + length + ",chunked=" + chunked + ",contentEncoding=" + contentEncoding + ",statusCode=" + statusCode + "]");


        // Read response

        if (length == -1)
            length = ESTIMATED_CONTENT_SIZE;   // if chunking is on, we have to guess final length

        InputStream in = client.getInputStream();
        byte data[] = IOUtils.toByteArray(in, cancelledListener, listener, length);

        // Handle if response is compressed

        if (contentEncoding != null && contentEncoding.equalsIgnoreCase("gzip")) {
            Logger.d("decoding gzip");
            in = new GZIPInputStream(new ByteArrayInputStream(data));
            data = IOUtils.toByteArray(in, cancelledListener, null, length * ESTIMATED_COMPRESSION_RATIO);
        }


        Map<String,String> newcookies = extractCookies(client);
        cookies.putAll(newcookies);

        Prefs.saveCookies(cxt, cookies);

        String ret = new String(data, "utf-8");
        Logger.d(ret);
        return ret;
    }

    private static String getHost() {
        return "http://www.geocaching.com";
    }

    private static String getSecureHost() {
        return "https://www.geocaching.com";
    }

    private static String decodeContentDispositionHeader(String header) {
        if (header == null) {
            Logger.d("Unable to decode DownloadablePQ name from Content-Disposition response http headers");
            return null;
        }

        String depoSplit[] = header.split("filename=");
        String ret = depoSplit[1].replace("filename=", "").replace("\"", "").trim();

        return ret;
    }

    private static Map<String,String> extractCookies(HttpURLConnection conn) {
        Map<String,String> ret = new HashMap<>();

        String headerName=null;
        for (int i=1; (headerName = conn.getHeaderFieldKey(i)) != null; i++) {
            if (headerName.equalsIgnoreCase(SET_COOKIE)) {

                List<HttpCookie> subcookies = HttpCookie.parse(conn.getHeaderField(i));

                for (HttpCookie cookie : subcookies) {
                    ret.put(cookie.getName(), cookie.getValue());
                }
            }
        }
        return ret;
    }

}
