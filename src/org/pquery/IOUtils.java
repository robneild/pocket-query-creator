package org.pquery;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import junit.framework.Assert;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.protocol.HTTP;

import android.util.Log;
import static org.pquery.Util.*;


public class IOUtils {    

    public interface Listener {
        public void update(int bytesReadSoFar, int expectedLength);
    }
    
    public static byte[] toByteArray(InputStream input) throws IOException {
        return toByteArray(input, null, -1);
    }
        
    public static byte[] toByteArray(InputStream input, Listener listener, int expectedLength) throws IOException {
        
        byte[] buffer = new byte[1024];
        int total=0;
        int i=0;
        int updatesSent=0;
        
        
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        
        while ((i = input.read(buffer)) != -1) {
            byteOut.write(buffer, 0, i);
            total += i;
            
            if (total/512>updatesSent) {
                updatesSent++;
                if (listener!=null) {
                    if (total>expectedLength)     // handle if we've gone past expectedLength (eg chunking)
                        listener.update(expectedLength-1, expectedLength);
                    else
                        listener.update(total, expectedLength);
                }
            }
        }
        
        return byteOut.toByteArray();
    }
    
    /**
     * Read a html string from HttpResponse
     * @throws IOException 
     */
    public static String httpGet(HttpClient client, String path, Listener listener) throws IOException {

        Log.v(APPNAME, "httpGet enter");
        
        HttpGet get = new HttpGet(getGeocachingURL() + path);
        get.addHeader("Accept-Encoding", "gzip");
        HttpResponse response = client.execute(get);
        
        int length = (int) response.getEntity().getContentLength();
        boolean chunked = response.getEntity().isChunked();
        Header contentEncoding = response.getFirstHeader("Content-Encoding");
        
        Log.v(APPNAME, "httpGet response [length="+length+",chunked="+chunked+",contentEncoding="+contentEncoding+"]");
        
        // Read response
         
        if (length==-1)
            length=10000;   // if chunking is on, we have to guess final length
        
        InputStream in = response.getEntity().getContent();
        byte data[] = IOUtils.toByteArray(in, listener, length);
        
        // Handle if response is compressed
        
        if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
            in = new GZIPInputStream(new ByteArrayInputStream(data));
            data = IOUtils.toByteArray(in);
        }
        
        Log.v(APPNAME, "httpGet exit [read_length="+data.length+"]");
        
        return new String(data, "utf-8");
    }
    
    public static String httpPost(HttpClient client, HttpEntity entity, String path, Listener listener) throws IOException {
        
        Log.v(APPNAME, "httpPost enter");
        
        HttpPost post = new HttpPost(getGeocachingURL() + path);
        post.addHeader("Accept-Encoding", "gzip");
        post.setEntity(entity);
        HttpResponse response = client.execute(post);
        
        int length = (int) response.getEntity().getContentLength();
        boolean chunked = response.getEntity().isChunked();
        Header contentEncoding = response.getFirstHeader("Content-Encoding");
        
        Log.v(APPNAME, "httpPost response [length="+length+",chunked="+chunked+",contentEncoding="+contentEncoding+"]");
        
        // Read response

        if (length==-1)
            length=40000;   // if chunking is on, we have to guess final length
        
        InputStream in = response.getEntity().getContent();
        byte data[] = IOUtils.toByteArray(in, listener, length);

        // Handle if response is compressed
        
        if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
            in = new GZIPInputStream(new ByteArrayInputStream(data));
            data = IOUtils.toByteArray(in);
        }
        
        Log.v(APPNAME, "httpPost exit [read_length="+data.length+"]");
        
        return new String(data, "utf-8");
    }
    
    private static String getGeocachingURL() {
        return "https://www.geocaching.com/";
    }
}