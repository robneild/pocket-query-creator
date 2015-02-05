package org.pquery.filter;


import android.content.res.Resources;
import android.text.TextUtils;

import org.pquery.R;
import org.pquery.util.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represent the options in the web page section 'Days to Generate'
 */
public class DaysToGenerateFilter {

    public boolean[] dayOfWeek = new boolean[7];

    public final static int UNCHECK_DAY_AFTER_QUERY = 0;
    public final static int RUN_EVERY_WEEK_ON_CHECKED_DAYS = 1;
    public final static int RUN_ONCE_THEN_DELETE = 2;

    public int howOftenRun;

    /**
     * Recreate from the string we get back from previously storing it in preferences
     */
    public DaysToGenerateFilter(String serializedString) {
        if (serializedString != null) {
            try {
                deSerializeIntoString(serializedString);
                return;

            } catch (IOException e) {
                Logger.e("Unable to restore days_to_generate filter", e);
            }
        }

        // There was no saved state, or failed to restore state so
        // setup the initial state of this filter
        howOftenRun = RUN_ONCE_THEN_DELETE;
        for (int i=0; i<=6; i++) {
            dayOfWeek[i] = true;
        }
    }

    /**
     * Convert object into a string such that it can be stored easily in preferences
     */
    public String serializeIntoString() {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(os);

            dos.writeInt(howOftenRun);

            for (int i = 0; i < 7; i++) {
                dos.writeBoolean(dayOfWeek[i]);
            }

            return new String(os.toString());

        } catch (IOException e) {
            Logger.e("Unable to serialize days to generate filter", e);
        }
        return null;
    }

    private void deSerializeIntoString(String in) throws IOException {
        ByteArrayInputStream os = new ByteArrayInputStream(in.getBytes());
        DataInputStream dos = new DataInputStream(os);

        howOftenRun = dos.readInt();

        for (int i=0; i<7; i++) {
            dayOfWeek[i] = dos.readBoolean();
        }
    }

    /**
     * Try to create a human description of this filter to shown as summary in filter list
     */
    public String toLocalisedString(Resources res) {

        String ret = "";

        // Start with general strategy

        if (howOftenRun == RUN_ONCE_THEN_DELETE) {
            ret += res.getString(R.string.daystogenerate_runqueryonce);
        }

        if (howOftenRun == UNCHECK_DAY_AFTER_QUERY)
            ret += res.getString(R.string.daystogenerate_uncheckDayOfWeekAfterQuery);

        if (howOftenRun == RUN_EVERY_WEEK_ON_CHECKED_DAYS)
            ret += res.getString(R.string.daystogenerate_runQueryEveryWeekOnCheckedDays);


        // Add short list of selected days

        List days = new ArrayList<String>();
        String [] shortWeekDayNames = res.getStringArray(R.array.shortWeekdayNames);

        for (int i=0; i<=6; i++) {
            if (dayOfWeek[i])
                days.add(shortWeekDayNames[i]);
        }

        // If "Run once" is being used, and all days selected, then don't bother adding the days
        // This is the most common used by 99.9% of people
        if (howOftenRun == RUN_ONCE_THEN_DELETE && days.size() == 7)
            return ret;

        ret += " (" + TextUtils.join(", ", days.toArray()) + ")";

        return ret;
    }

}
