package net.sourceforge.htmlunit.corejs.javascript.commonjs.module.provider;

import java.io.Serializable;
import java.net.URLConnection;

/**
 * The default heuristic for calculating cache expiry of URL-based resources.
 * It is simply configured with a default relative expiry, and each invocation
 * of {@link #calculateExpiry(URLConnection)} returns 
 * {@link System#currentTimeMillis()} incremented with the relative expiry. 
 * @author Attila Szegedi
 * @version $Id: DefaultUrlConnectionExpiryCalculator.java 6395 2011-05-05 17:00:20Z mguillem $
 */
public class DefaultUrlConnectionExpiryCalculator 
implements UrlConnectionExpiryCalculator, Serializable
{
    private static final long serialVersionUID = 1L;

    private final long relativeExpiry;
    
    /**
     * Creates a new default expiry calculator with one minute relative expiry.
     */
    public DefaultUrlConnectionExpiryCalculator() {
        this(60000L);
    }
    
    /**
     * Creates a new default expiry calculator with the specified relative 
     * expiry.
     * @param relativeExpiry the fixed relative expiry, in milliseconds.
     */
    public DefaultUrlConnectionExpiryCalculator(long relativeExpiry) {
        if(relativeExpiry < 0) {
            throw new IllegalArgumentException("relativeExpiry < 0");
        }
        this.relativeExpiry = relativeExpiry;
    }
    
    public long calculateExpiry(URLConnection urlConnection) {
        return System.currentTimeMillis() + relativeExpiry;
    }
}