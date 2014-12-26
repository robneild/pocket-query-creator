package org.pquery.filter;

import android.content.res.Resources;

import org.pquery.R;

public class CheckBoxesFilter {

    public boolean enabled;
    public boolean notOnIgnore;
    public boolean notFound;
    public boolean found7days;
    public boolean travelBug;
    public boolean idontown;
    public boolean notBeenFound;


    public CheckBoxesFilter() {
    }

    public CheckBoxesFilter(boolean[] selections) {
        enabled = selections[0];
        notOnIgnore = selections[1];
        notFound = selections[2];
        found7days = selections[3];
        travelBug = selections[4];
        idontown = selections[5];
        notBeenFound = selections[6];
    }

    public String toLocalisedString(Resources res) {
        StringBuffer ret = new StringBuffer();

        if (enabled)
            ret.append(res.getString(R.string.filter_enabled) + ", ");
        if (notOnIgnore)
            ret.append(res.getString(R.string.filter_ignorelist) + ", ");
        if (notFound)
            ret.append(res.getString(R.string.filter_notfound) + ", ");
        if (found7days)
            ret.append(res.getString(R.string.filter_found7days) + ", ");
        if (travelBug)
            ret.append(res.getString(R.string.filter_travelbug) + ", ");
        if (idontown)
            ret.append(res.getString(R.string.filter_idontown) + ", ");
        if (notBeenFound)
            ret.append(res.getString(R.string.filter_notbeenfound) + ", ");
        // Knock off ending ', '
        if (ret.length() > 0)
            ret.setLength(ret.length() - 2);

        return ret.toString();
    }

    public String[] getOptions(Resources res) {
        String[] ret = new String[7];
        ret[0] = res.getString(R.string.filter_enabled);
        ret[1] = res.getString(R.string.filter_ignorelist);
        ret[2] = res.getString(R.string.filter_notfound);
        ret[3] = res.getString(R.string.filter_found7days);
        ret[4] = res.getString(R.string.filter_travelbug);
        ret[5] = res.getString(R.string.filter_idontown);
        ret[6] = res.getString(R.string.filter_notbeenfound);
        return ret;
    }

    public boolean[] getAsBooleanArray() {
        boolean[] ret = new boolean[7];
        ret[0] = enabled;
        ret[1] = notOnIgnore;
        ret[2] = notFound;
        ret[3] = found7days;
        ret[4] = travelBug;
        ret[5] = idontown;
        ret[6] = notBeenFound;
        return ret;
    }
}
