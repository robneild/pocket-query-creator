package org.pquery.dao;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.Map;

public class DownloadablePQ implements Parcelable, Serializable, PQListItem {

    private static final long serialVersionUID = -6540850109992510771L;
    public String name;
    public String size;
    public String age;
    public String waypoints;
    public String url;

    public DownloadablePQ(Parcel in) {
        name = in.readString();
        size = in.readString();
        age = in.readString();
        waypoints = in.readString();
        url = in.readString();
    }

    public DownloadablePQ() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(size);
        dest.writeString(age);
        dest.writeString(waypoints);
        dest.writeString(url);
    }

    /**
     * Implementing the Parcelable interface must also have a static field called CREATOR, which is an object
     * implementing the Parcelable.Creator interface.
     */
    public static final Parcelable.Creator<DownloadablePQ> CREATOR = new Parcelable.Creator<DownloadablePQ>() {
        public DownloadablePQ createFromParcel(Parcel in) {
            return new DownloadablePQ(in);
        }

        public DownloadablePQ[] newArray(int size) {
            return new DownloadablePQ[size];
        }
    };

    @Override
    public String getName() {
        return name;
    }

}
