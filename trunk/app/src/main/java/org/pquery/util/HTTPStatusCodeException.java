package org.pquery.util;

import org.apache.http.StatusLine;

import java.io.IOException;

public class HTTPStatusCodeException extends IOException {

    private static final long serialVersionUID = 6392750451335427514L;

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
