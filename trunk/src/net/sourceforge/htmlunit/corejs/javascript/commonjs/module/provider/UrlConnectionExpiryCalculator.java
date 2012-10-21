package net.sourceforge.htmlunit.corejs.javascript.commonjs.module.provider;

import java.net.URLConnection;

/**
 * Implemented by objects that can be used as heuristic strategies for 
 * calculating the expiry of a cached resource in cases where the server of the
 * resource doesn't provide explicit expiry information.
 * @author Attila Szegedi
 * @version $Id: UrlConnectionExpiryCalculator.java 6395 2011-05-05 17:00:20Z mguillem $
 */
public interface UrlConnectionExpiryCalculator
{
    /**
     * Given a URL connection, returns a calculated heuristic expiry time (in
     * terms of milliseconds since epoch) for the resource.
     * @param urlConnection the URL connection for the resource
     * @return the expiry for the resource
     */
    public long calculateExpiry(URLConnection urlConnection);
}
