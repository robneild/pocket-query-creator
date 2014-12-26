package org.pquery.dao;

/**
 * A Schedule for a PocketQuery for a day
 */
public class Schedule {
    private final String href;
    private final Integer day;
    private final boolean enabled;

    public Schedule(String href, Integer day, boolean enabled) {
        this.href = href;
        this.day = day;
        this.enabled = enabled;
    }

    public String getHref() {
        return href;
    }

    public Integer getDay() {
        return day;
    }

    public boolean isEnabled() {
        return enabled;
    }

}
