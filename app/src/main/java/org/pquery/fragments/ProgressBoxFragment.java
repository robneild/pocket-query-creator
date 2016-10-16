package org.pquery.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.pquery.R;

/**
 * Fragment displays box containing service progress text
 * User can click on it to cancel service
 */
public class ProgressBoxFragment extends Fragment {

    public interface ProgressBoxFragmentListener {
        public void onProgressBoxFragmentClicked();
    }

    private String htmlText = "";
    private ProgressBoxFragmentListener listener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (ProgressBoxFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ProgressBoxFragmentListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.progress_box_fragment, container, false);
        TextView tv = (TextView) view.findViewById(R.id.progress_text);
        tv.setText(Html.fromHtml(htmlText));

        view.setClickable(true);
        view.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                listener.onProgressBoxFragmentClicked();
            }
        });
        return view;
    }

    /**
     * Update text as to what service is doing now
     */
    public void setText(String htmlText) {
        this.htmlText = htmlText;

        if (getView() != null) {
            TextView tv = (TextView) getView().findViewById(R.id.progress_text);
            tv.setText(Html.fromHtml(htmlText));
        }
    }
}