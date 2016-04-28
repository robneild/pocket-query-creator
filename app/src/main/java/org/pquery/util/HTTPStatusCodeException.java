package org.pquery.util;

import java.io.IOException;

public class HTTPStatusCodeException extends IOException {

    private static final long serialVersionUID = 6392750451335427514L;

    public int code;
    public String reason;
    public String body;

    public HTTPStatusCodeException(int statusCode, String statusMessage, String body) {
        super("Geocaching.com returned " + statusCode + " " + statusMessage);
        code = statusCode;
        reason = statusMessage;
        this.body = body;
    }
}
