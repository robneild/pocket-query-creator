package org.pquery.webdriver;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpClientFactory {

//    public static DefaultHttpClient createHttpClient() {
//
//        // Set timeout
//        // I have seen some random failures where the network access just seems to lock-up for over a minute
//        // I can't reproduce it but hopefully this will make it error and allow user to manually retry
//        final HttpParams httpParams = new BasicHttpParams();
//        HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
//        HttpConnectionParams.setSoTimeout(httpParams, 10000);
//
//        return new DefaultHttpClient(httpParams);
//    }

    public static HttpURLConnection createURLConnection(URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // Set timeout
        // I have seen some random failures where the network access just seems to lock-up
        // ... for minutes
        // I can't reproduce it but hopefully this will make it error out and allow user to manually
        // retry
        conn.setReadTimeout(20000);         // 20 seconds
        conn.setConnectTimeout(20000);      // 20 seconds

        conn.setRequestProperty("Accept-Encoding", "gzip");
        conn.setRequestProperty("Host", "www.geocaching.com");

        // Makes our job a bit easier trying to work out what is going on with login problems etc.
        conn.setInstanceFollowRedirects(false);

        return conn;
    }
}
