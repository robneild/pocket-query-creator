/*
 * Copyright (c) 2002-2012 Gargoyle Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gargoylesoftware.htmlunit;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.gargoylesoftware.htmlunit.DefaultPageCreator.PageType;
import com.gargoylesoftware.htmlunit.util.EncodingSniffer;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.gargoylesoftware.htmlunit.xml.XmlUtil;

/**
 * A response from a web server.
 *
 * @version $Revision: 6842 $
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * @author Brad Clarke
 * @author Noboru Sinohara
 * @author Marc Guillemot
 * @author Ahmed Ashour
 * @author Ronald Brill
 */
public class WebResponse implements Serializable {

    private static final Log LOG = LogFactory.getLog(WebResponse.class);

    private long loadTime_;
    private WebResponseData responseData_;
    private WebRequest request_;

    /**
     * Constructs with all data.
     *
     * @param responseData      Data that was send back
     * @param url               Where this response came from
     * @param requestMethod     the method used to get this response
     * @param loadTime          How long the response took to be sent
     */
    public WebResponse(final WebResponseData responseData, final URL url,
            final HttpMethod requestMethod, final long loadTime) {
        this(responseData, new WebRequest(url, requestMethod), loadTime);
    }

    /**
     * Constructs with all data.
     *
     * @param responseData      Data that was send back
     * @param request           the request used to get this response
     * @param loadTime          How long the response took to be sent
     */
    public WebResponse(final WebResponseData responseData,
            final WebRequest request, final long loadTime) {
        responseData_ = responseData;
        request_ = request;
        loadTime_ = loadTime;
    }

    /**
     * Returns the request used to load this response.
     * @return the request used to load this response
     */
    public WebRequest getWebRequest() {
        return request_;
    }

    /**
     * Returns the response headers as a list of {@link NameValuePair}s.
     * @return the response headers as a list of {@link NameValuePair}s
     */
    public List<NameValuePair> getResponseHeaders() {
        return responseData_.getResponseHeaders();
    }

    /**
     * Returns the value of the specified response header.
     * @param headerName the name of the header whose value is to be returned
     * @return the header value, <code>null</code> if no response header exists with this name
     */
    public String getResponseHeaderValue(final String headerName) {
        for (final NameValuePair pair : responseData_.getResponseHeaders()) {
            if (pair.getName().equalsIgnoreCase(headerName)) {
                return pair.getValue();
            }
        }
        return null;
    }

    /**
     * Returns the status code that was returned by the server.
     * @return the status code that was returned by the server
     */
    public int getStatusCode() {
        return responseData_.getStatusCode();
    }

    /**
     * Returns the status message that was returned from the server.
     * @return the status message that was returned from the server
     */
    public String getStatusMessage() {
        return responseData_.getStatusMessage();
    }

    /**
     * Returns the content type returned from the server, e.g. "text/html".
     * @return the content type returned from the server, e.g. "text/html"
     */
    public String getContentType() {
        final String contentTypeHeader = getResponseHeaderValue("content-type");
        if (contentTypeHeader == null) {
            // Not technically legal but some servers don't return a content-type
            return "";
        }
        final int index = contentTypeHeader.indexOf(';');
        if (index == -1) {
            return contentTypeHeader;
        }
        return contentTypeHeader.substring(0, index);
    }

    /**
     * Returns the content charset specified explicitly in the header or in the content,
     * or <tt>null</tt> if none was specified.
     * @return the content charset specified explicitly in the header or in the content,
     *         or <tt>null</tt> if none was specified
     */
    public String getContentCharsetOrNull() {
        try {
            return EncodingSniffer.sniffEncoding(getResponseHeaders(), getContentAsStream());
        }
        catch (final IOException e) {
            LOG.warn("Error trying to sniff encoding.", e);
            return null;
        }
    }

    /**
     * Returns the content charset for this response, even if no charset was specified explicitly.
     * This method always returns a valid charset. This method first checks the "Content-Type"
     * header; if not found, it checks the request charset; as a last resort, this method
     * returns {@link TextUtil#DEFAULT_CHARSET}.
     * If no charset is defined for an xml respose, then UTF-8 is used ({@link "http://www.w3.org/TR/xml/#charencoding"}
     * @return the content charset for this response
     */
    public String getContentCharset() {
        String charset = getContentCharsetOrNull();
        if (charset == null) {
            final String contentType = getContentType();

            // xml pages are using a different content type
            if (null != contentType
                && PageType.XML == DefaultPageCreator.determinePageType(contentType)) {
                return XmlUtil.DEFAULT_CHARSET;
            }

            charset = getWebRequest().getCharset();
        }
        if (charset == null) {
            charset = TextUtil.DEFAULT_CHARSET;
        }
        return charset;
    }

    /**
     * Returns the response content as a string, using the charset/encoding specified in the server response.
     * @return the response content as a string, using the charset/encoding specified in the server response
     */
    public String getContentAsString() {
        return getContentAsString(getContentCharset());
    }

    /**
     * Returns the response content as a string, using the specified charset/encoding,
     * rather than the charset/encoding specified in the server response. If the specified
     * charset/encoding is not supported then the default system encoding is used.
     * @param encoding the charset/encoding to use to convert the response content into a string
     * @return the response content as a string
     */
    public String getContentAsString(final String encoding) {
        final InputStream in = responseData_.getInputStream();
        if (null == in) {
            return null;
        }

        try {
            // first verify the charset because we can't read the
            // input stream twice
            try {
                Charset.forName(encoding);
            }
            catch (final Exception e) {
                LOG.warn("Attempted to use unsupported encoding '"
                        + encoding + "'; using default system encoding.");
                return IOUtils.toString(in);
            }

            return IOUtils.toString(in, encoding);
        }
        catch (final IOException e) {
            LOG.warn(e);
            return null;
        }
    }

    /**
     * Returns the response content as an input stream.
     * @return the response content as an input stream
     */
    public InputStream getContentAsStream() {
        return responseData_.getInputStream();
    }

    /**
     * Returns the time it took to load this web response, in milliseconds.
     * @return the time it took to load this web response, in milliseconds
     */
    public long getLoadTime() {
        return loadTime_;
    }
}
