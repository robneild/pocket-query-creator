package org.pquery.dao;

import android.net.UrlQuerySanitizer;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RepeatablePQ implements Parcelable, Serializable, PQListItem {

    private static final long serialVersionUID = -6540850109992510771L;
    public String name;
    public String waypoints;
    private HashMap<Integer, Schedule> schedules = new HashMap<Integer, Schedule>();

    public RepeatablePQ(Parcel in) {
        name = in.readString();
        waypoints = in.readString();
        schedules = in.readHashMap(ClassLoader.getSystemClassLoader());
    }

    public RepeatablePQ() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(waypoints);
        dest.writeMap(schedules);

    }

    /**
     * Implementing the Parcelable interface must also have a static field called CREATOR, which is an object
     * implementing the Parcelable.Creator interface.
     */
    public static final Creator<RepeatablePQ> CREATOR = new Creator<RepeatablePQ>() {
        public RepeatablePQ createFromParcel(Parcel in) {
            return new RepeatablePQ(in);
        }

        public RepeatablePQ[] newArray(int size) {
            return new RepeatablePQ[size];
        }
    };

    @Override
    public String getName() {
        return name;
    }

    public List<Integer> getCheckedWeekdays() {
        List<Integer> checkedWeekdays = new ArrayList<Integer>();
        for (Schedule schedule : schedules.values()) {
           if (schedule.isEnabled()) {
               checkedWeekdays.add(schedule.getDay());
           }
        }
        return checkedWeekdays;
    }

    public void addScheduleURL(String url) {
        UrlQuerySanitizer sanitizer = new UrlQuerySanitizer();
        sanitizer.setAllowUnregisteredParamaters(true);
        sanitizer.parseUrl(url);
        String day = sanitizer.getValue("d");
        String opt = sanitizer.getValue("opt");
        Schedule schedule = new Schedule(url, Integer.valueOf(day), "0".equals(opt));
        schedules.put(schedule.getDay(), schedule);
    }

    public Map<Integer, Schedule> getSchedules() {
        return schedules;
    }

    public String getCheckedWeekdaysAsText(String[] weekdayNames) {
        String weekdaysString = null;
        for (Integer dayNumber : getCheckedWeekdays()) {
            String dayName = weekdayNames[dayNumber];
            if (weekdaysString == null) {
                weekdaysString = dayName;
            } else {
                weekdaysString += ", " + dayName;
            }
        }
        if (weekdaysString == null) {
            weekdaysString = "-";
        }
        return weekdaysString;
    }
}
