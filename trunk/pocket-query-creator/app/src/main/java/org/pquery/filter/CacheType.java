package org.pquery.filter;

import org.pquery.R;

/**
 * Order is important and corresponds with order on the web page
 */
public enum CacheType {

    TRADITIONAL(R.string.type_traditional),
    MULTI(R.string.type_multi),
    VIRTUAL(R.string.type_virtual),
    LETTERBOX_HYBRID(R.string.type_letterbox),
    EVENT(R.string.type_event),
    UNKNOWN(R.string.type_unknown),
    APE(R.string.type_project),
    WEBCAM(R.string.type_webcam),
    EARTHCACHE(R.string.type_earthcache),
    ADVENTURES(R.string.type_adventures),
    WHERIGO(R.string.type_wherigo);

    private int resId;

    CacheType(int resId) {
        this.resId = resId;
    }

    public int getResourceId() {
        return resId;
    }

}
