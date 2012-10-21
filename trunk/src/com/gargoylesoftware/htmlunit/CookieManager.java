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

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.set.ListOrderedSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecRegistry;
import org.apache.http.impl.client.DefaultHttpClient;

import com.gargoylesoftware.htmlunit.util.Cookie;

/**
 * Manages cookies for a {@link WebClient}. This class is thread-safe.
 *
 * @version $Revision: 6820 $
 * @author Daniel Gredler
 * @author Ahmed Ashour
 * @author Nicolas Belisle
 */
public class CookieManager implements Serializable {

    /**
     * HtmlUnit's cookie policy is to be browser-compatible. Code which requires access to
     * HtmlUnit's cookie policy should use this constant, rather than making assumptions and using
     * one of the HttpClient {@link CookiePolicy} constants directly.
     */
    public static final String HTMLUNIT_COOKIE_POLICY = CookiePolicy.BROWSER_COMPATIBILITY;

    /** Whether or not cookies are enabled. */
    private boolean cookiesEnabled_;

    /** The cookies added to this cookie manager. */
    @SuppressWarnings("unchecked")
    private final Set<Cookie> cookies_ = new ListOrderedSet();

    /** The cookies spec registry */
    private final transient CookieSpecRegistry registry_ = new DefaultHttpClient().getCookieSpecs();

    /**
     * Creates a new instance.
     */
    public CookieManager() {
        cookiesEnabled_ = true;
    }

    /**
     * Enables/disables cookie support. Cookies are enabled by default.
     * @param enabled <tt>true</tt> to enable cookie support, <tt>false</tt> otherwise
     */
    public synchronized void setCookiesEnabled(final boolean enabled) {
        cookiesEnabled_ = enabled;
    }

    /**
     * Returns <tt>true</tt> if cookies are enabled. Cookies are enabled by default.
     * @return <tt>true</tt> if cookies are enabled, <tt>false</tt> otherwise
     */
    public synchronized boolean isCookiesEnabled() {
        return cookiesEnabled_;
    }

    /**
     * Returns the currently configured cookies, in an unmodifiable set.
     * @return the currently configured cookies, in an unmodifiable set
     */
    public synchronized Set<Cookie> getCookies() {
        return Collections.unmodifiableSet(cookies_);
    }

    /**
     * Returns the currently configured cookies applicable to the specified URL, in an unmodifiable set.
     * @param url the URL on which to filter the returned cookies
     * @return the currently configured cookies applicable to the specified URL, in an unmodifiable set
     */
    public synchronized Set<Cookie> getCookies(final URL url) {
        final String host = url.getHost();
        final String path = url.getPath();
        final String protocol = url.getProtocol();
        final boolean secure = "https".equals(protocol);

        // URLs like "about:blank" don't have cookies and we need to catch these
        // cases here before HttpClient complains
        if (host.length() == 0) {
            return Collections.emptySet();
        }

        final int port = getPort(url);

        // discard expired cookies
        clearExpired(new Date());

        final CookieSpec spec = registry_.getCookieSpec(HTMLUNIT_COOKIE_POLICY);
        final org.apache.http.cookie.Cookie[] all = Cookie.toHttpClient(cookies_);
        final CookieOrigin cookieOrigin = new CookieOrigin(host, port, path, secure);
        final List<org.apache.http.cookie.Cookie> matches = new ArrayList<org.apache.http.cookie.Cookie>();
        for (final org.apache.http.cookie.Cookie cookie : all) {
            if (spec.match(cookie, cookieOrigin)) {
                matches.add(cookie);
            }
        }

        final Set<Cookie> cookies = new LinkedHashSet<Cookie>();
        cookies.addAll(Cookie.fromHttpClient(matches));
        return Collections.unmodifiableSet(cookies);
    }

    /**
     * Clears all cookies that have expired before supplied date.
     * @param date the date to use for comparison when clearing expired cookies
     * @return whether any cookies were found expired, and were cleared
     */
    public synchronized boolean clearExpired(final Date date) {
        if (date == null) {
            return false;
        }

        boolean foundExpired = false;
        for (final Iterator<Cookie> iter = cookies_.iterator(); iter.hasNext();) {
            final Cookie cookie = iter.next();
            if (cookie.getExpires() != null && date.after(cookie.getExpires())) {
                iter.remove();
                foundExpired = true;
            }
        }
        return foundExpired;
    }

    /**
     * Gets the port of the URL.
     * This functionality is implemented here as protected method to allow subclass to change it
     * as workaround to <a href="http://code.google.com/p/googleappengine/issues/detail?id=4784>
     * Google App Engine bug 4784</a>.
     * @param url the URL
     * @return the port use to connect the server
     */
    protected int getPort(final URL url) {
        if (url.getPort() != -1) {
            return url.getPort();
        }
        return url.getDefaultPort();
    }

    /**
     * Returns the currently configured cookie with the specified name, or <tt>null</tt> if one does not exist.
     * @param name the name of the cookie to return
     * @return the currently configured cookie with the specified name, or <tt>null</tt> if one does not exist
     */
    public synchronized Cookie getCookie(final String name) {
        for (Cookie cookie : cookies_) {
            if (StringUtils.equals(cookie.getName(), name)) {
                return cookie;
            }
        }
        return null;
    }

    /**
     * Adds the specified cookie.
     * @param cookie the cookie to add
     */
    public synchronized void addCookie(final Cookie cookie) {
        cookies_.remove(cookie);

        // don't add expired cookie
        if (cookie.getExpires() == null || cookie.getExpires().after(new Date())) {
            cookies_.add(cookie);
        }
    }

    /**
     * Removes the specified cookie.
     * @param cookie the cookie to remove
     */
    public synchronized void removeCookie(final Cookie cookie) {
        cookies_.remove(cookie);
    }

    /**
     * Removes all cookies.
     */
    public synchronized void clearCookies() {
        cookies_.clear();
    }

}
