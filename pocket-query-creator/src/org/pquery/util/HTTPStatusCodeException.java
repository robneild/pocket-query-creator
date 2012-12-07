package org.pquery.util;

import java.io.IOException;

import org.apache.http.StatusLine;

public class HTTPStatusCodeException extends IOException {

    public int code;
    public String reason;
    public String body;
    
    public HTTPStatusCodeException(StatusLine status, String body) {
        super("Geocaching.com returned " + status.getStatusCode() + " " + status.getReasonPhrase());
        code = status.getStatusCode();
        reason = status.getReasonPhrase();
        this.body = body;
    }
}
