package org.pquery.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.pquery.webdriver.CancelledListener;


public class IOUtils {    

    private static final int ESTIMATED_COMPRESSION_RATIO = 5;
    private static final int ESTIMATED_CONTENT_SIZE = 48000;
    
    public interface Listener {
        public void update(int bytesReadSoFar, int expectedLength, int percent);
    }
    
//    public static byte[] toByteArray(InputStream input) throws IOException {
//        return toByteArray(input, null, -1);
//    }
        
    public static byte[] toByteArray(InputStream input, CancelledListener cancelledListener, Listener listener, int expectedLength) throws IOException, InterruptedException {
        
        byte[] buffer = new byte[3000];
        int total=0;
        int i=0;
        int updatesSent=0;
        
        
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream(expectedLength);
        
        while ((i = input.read(buffer)) != -1) {
            byteOut.write(buffer, 0, i);
            total += i;
            
            if (total/3000>updatesSent) {
                Logger.d("read " + total + "B of " + expectedLength + " expected");
                updatesSent++;
                if (listener!=null) {
                    if (total>expectedLength)     // handle if we've gone past expectedLength (eg chunking)
                        listener.update(expectedLength-1, expectedLength, 99);
                    else {
                        listener.update(total, expectedLength, total*100/expectedLength);
                    }
                }
            }
            cancelledListener.ifCancelledThrow();
        }
        
        Logger.d("toByteArray expectedLength="+expectedLength+", actualLength="+total);
        
        return byteOut.toByteArray();
    }
    
    public static String httpGet(HttpClient client, String path, CancelledListener cancelledListener, Listener listener) throws IOException, InterruptedException { 
        byte[] data =  httpGetBytes(client, path, cancelledListener, listener);
        
        // Convert data into string. Geocaching.com uses utf-8 pages?
        String ret = new String(data, "utf-8");
        Logger.d(ret);
        return ret;
    }
    
    /**
     * Read a html string from HttpResponse
     * Doesn't follow redirects and throws exception if http status code isn't good
     * @throws InterruptedException 
     */
    public static byte[] httpGetBytes(HttpClient client, String path, CancelledListener cancelledListener, Listener listener) throws IOException, InterruptedException {

        Logger.d("enter [path=" + getHost() + path +"]");
        
        HttpGet get = new HttpGet(getHost() + path);
        get.addHeader("Accept-Encoding", "gzip");
        get.addHeader("Connection", "close");
        HttpClientParams.setRedirecting(get.getParams(), false);    // don't follow redirects. geocaching redirects on errors
        
        HttpResponse response = client.execute(get);
        
        int length = (int) response.getEntity().getContentLength();
        boolean chunked = response.getEntity().isChunked();
        Header contentEncoding = response.getFirstHeader("Content-Encoding");
        int statusCode = response.getStatusLine().getStatusCode();

        Logger.d("response [length="+length+",chunked="+chunked+",contentEncoding="+contentEncoding+",statusCode="+statusCode+"]");
        
        // Read response
         
        if (length==-1)
            length=ESTIMATED_CONTENT_SIZE;   // if chunking is on, we have to guess final length
        
        InputStream in = response.getEntity().getContent();
        byte data[] = IOUtils.toByteArray(in, cancelledListener, listener, length);
        
        // Handle if response is compressed
        
        if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
        	Logger.d("decoding gzip");
        	in = new GZIPInputStream(new ByteArrayInputStream(data));
            data = IOUtils.toByteArray(in, cancelledListener, null, length*ESTIMATED_COMPRESSION_RATIO);
        }
        
        // Check for 404 page not found, 302 object moved etc
        // We read in body, even though error, because need to see the redirect link etc
        // Can assume error page is html (rather than binary) so ok to log it etc
        if (statusCode>=300) {
            String s = new String(data);
            Logger.d("Bad status page response body.. " + s);
            throw new HTTPStatusCodeException(response.getStatusLine(),s);
        }
        
        Logger.d("returning " + data.length + " bytes");
        return data;
    }
    
    public static String httpPost(HttpClient client, List<BasicNameValuePair> paramList, String path, boolean secure, CancelledListener cancelledListener, Listener listener) throws IOException, InterruptedException {
        
        HttpEntity entity = new UrlEncodedFormEntity(paramList, HTTP.UTF_8);
        
        String url;
        if (secure)
            url = getSecureHost() + path;
        else
            url = getHost() + path;
        
        Logger.d("enter [url=" + url +"]");
        Logger.d(paramList);
        
        HttpPost post = new HttpPost(url);
        post.addHeader("Accept-Encoding", "gzip");
        post.setEntity(entity);
        
        HttpResponse response = client.execute(post);
        
        int length = (int) response.getEntity().getContentLength();
        boolean chunked = response.getEntity().isChunked();
        Header contentEncoding = response.getFirstHeader("Content-Encoding");
        
        Logger.d("response [length="+length+",chunked="+chunked+",contentEncoding="+contentEncoding+"]");
        
        // Read response

        if (length==-1)
            length=ESTIMATED_CONTENT_SIZE;   // if chunking is on, we have to guess final length
        
        InputStream in = response.getEntity().getContent();
        byte data[] = IOUtils.toByteArray(in, cancelledListener, listener, length);

        // Handle if response is compressed
        
        if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
        	Logger.d("decoding gzip");
            in = new GZIPInputStream(new ByteArrayInputStream(data));
            data = IOUtils.toByteArray(in, cancelledListener, null, length*ESTIMATED_COMPRESSION_RATIO);
        }
        
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
}
