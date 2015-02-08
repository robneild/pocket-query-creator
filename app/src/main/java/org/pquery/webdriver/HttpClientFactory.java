package org.pquery.webdriver;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

/**
 * Created by rob on 08/02/15.
 */
public class HttpClientFactory {

    public static DefaultHttpClient createHttpClient() {

        // Set timeout
        // I have seen some random failures where the network access just seems to lock-up for over a minute
        // I can't reproduce it but hopefully this will make it error and allow user to manually retry
        final HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
        HttpConnectionParams.setSoTimeout(httpParams, 10000);

        return new DefaultHttpClient(httpParams);
    }
}
