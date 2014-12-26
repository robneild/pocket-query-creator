package org.pquery.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;

import org.pquery.R;
import org.pquery.dao.Schedule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Dialog with a list of weekdays to change the scheduling of a PQ
 */
public class SchedulePQFragment extends DialogFragment {

    private List<String> mSelectedItems = new ArrayList<String>();
    private boolean[] selectedWeekdays;
    private Map<Integer, Schedule> schedules;
    private PQListFragment.PQClickedListener listener;

    public void setSchedules(Map<Integer, Schedule> schedules) {
        this.schedules = schedules;
        selectedWeekdays = new boolean[schedules.size()];
        int index = 0;
        for (Schedule schedule : schedules.values()) {
            selectedWeekdays[index] = schedule.isEnabled();
            if (schedule.isEnabled()) {
                mSelectedItems.add(schedule.getDay().toString());
            }
            index++;
        }
    }

    @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final String[] weekdays = getResources().getStringArray(R.array.weekdayNames);

            // Set the dialog title
            builder.setTitle(R.string.change_schedule)
                    // Specify the list array, the items to be selected by default (null for none),
                    // and the listener through which to receive callbacks when items are selected
                    .setMultiChoiceItems(weekdays, selectedWeekdays,
                            new DialogInterface.OnMultiChoiceClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which,
                                                    boolean isChecked) {
                                    Schedule schedule = schedules.get(which);
                                    String weekday = weekdays[which];
                                    if (isChecked) {
                                        // If the user checked the item, add it to the selected items
                                        mSelectedItems.add(weekday);
                                    } else if (mSelectedItems.contains(weekday)) {
                                        // Else, if the item is already in the array, remove it
                                        mSelectedItems.remove(weekday);
                                    }
                                    listener.onSchedulePQ(schedule.getHref());
                                    dialog.dismiss();
                                }
                            })

                        // Set the action buttons
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            // User clicked cancel, nothing to do
                        }
                    });

            return builder.create();
        }

    public void setPQClickedListener(PQListFragment.PQClickedListener listener) {
        this.listener = listener;
    }

}
