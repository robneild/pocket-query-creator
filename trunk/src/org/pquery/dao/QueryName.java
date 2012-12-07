package org.pquery.dao;

import android.os.Parcel;
import android.os.Parcelable;

public class QueryName implements Parcelable {

    public String name;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
    }

    public static final Parcelable.Creator<QueryName> CREATOR = new Parcelable.Creator<QueryName>() {
        public QueryName createFromParcel(Parcel in) {
            return new QueryName(in);
        }
        public QueryName[] newArray(int size) {
            return new QueryName[size];
        }
    };

    public QueryName(Parcel in) {
        name = in.readString();
    }
    public QueryName(String name) {
        this.name = name;
    }
}
