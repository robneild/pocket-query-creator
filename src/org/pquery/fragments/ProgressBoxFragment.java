package org.pquery.fragments;

import junit.framework.Assert;

import org.pquery.R;
import org.pquery.fragments.PQListFragment.PQClickedListener;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

public class ProgressBoxFragment extends Fragment {
    
    public interface ProgressBoxFragmentListener {
        public void onProgressBoxFragmentClicked();
    }
    
    private String text;
    private ProgressBoxFragmentListener listener;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);
    }
    
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
        
        View root = inflater.inflate(R.layout.progress_box_fragment, container, false);
        TextView tv = (TextView) root.findViewById(R.id.progress_text);
        tv.setText(text);
        
        root.setClickable(true);
        root.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
               int i = 0;
                
            }
        });
        return root;
    }

    
    public void setText(String text) {
        this.text = text;
        
        if (getView() != null) {
            TextView tv = (TextView) getView().findViewById(R.id.progress_text);
            tv.setText(text);
        }
    }
}