package org.pquery.dao;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

public class PQ implements Parcelable, Serializable {
    public String name;
    public String size;
    public String age;
    public String waypoints;
    public String url;

    public PQ(Parcel in) {
        name = in.readString();
        size = in.readString();
        age = in.readString();
        waypoints = in.readString();
        url = in.readString();
    }
    public PQ() {
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
    public static final Parcelable.Creator<PQ> CREATOR = new Parcelable.Creator<PQ>() {
        public PQ createFromParcel(Parcel in) {
            return new PQ(in);
        }

        public PQ[] newArray(int size) {
            return new PQ[size];
        }
    };
}
