package org.pquery.fragments;

import org.pquery.CreateFiltersActivity;
import org.pquery.CreateLocationActivity;
import org.pquery.CreateNameActivity;
import org.pquery.R;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.actionbarsherlock.app.SherlockListFragment;

public class CreateCategoriesFragment extends SherlockListFragment {
    
    boolean mDualPane;
    int mCurCheckPosition = 0;
    private String initialName;
    private Location initialLocation;
    
    public void setInitialName(String initialName) {
        this.initialName = initialName;
    }
    public void setInitialLocation(Location initialLocation) {
        this.initialLocation = initialLocation;
    }
    
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup vg, Bundle data) {
        return inflater.inflate(R.layout.list_fragment, null);
    }

//    @Override
//    public void onViewCreated(View arg0, Bundle arg1) {
//    super.onViewCreated(arg0, arg1);
//    setListAdapter(new ArrayAdapter<String>(getSherlockActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, getResources().getStringArray(R.array.create_categories)));
//    }

    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Populate list with our static array of titles.
        setListAdapter(new ArrayAdapter<String>(getSherlockActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, getResources().getStringArray(R.array.create_categories)));

        // Check to see if we have a frame in which to embed the details
        // fragment directly in the containing UI.
        View detailsFrame = getActivity().findViewById(R.id.details);
        mDualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;

        if (savedInstanceState != null) {
            // Restore last state for checked position.
            mCurCheckPosition = savedInstanceState.getInt("curChoice", 0);
        }

        if (mDualPane) {
            // In dual-pane mode, the list view highlights the selected item.
            getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            // Make sure our UI is in the correct state.
            showDetails(mCurCheckPosition);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("curChoice", mCurCheckPosition);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        showDetails(position);
    }

    /**
     * Helper function to show the details of a selected item, either by
     * displaying a fragment in-place in the current UI, or starting a
     * whole new activity in which it is displayed.
     */
    void showDetails(int index) {
        mCurCheckPosition = index;

        if (mDualPane) {
            // We can display everything in-place with fragments, so update
            // the list to highlight the selected item and show the data.
            getListView().setItemChecked(index, true);

            // Check what fragment is currently shown, replace if needed.
            Fragment currentFrag = getFragmentManager().findFragmentById(R.id.details);
            Fragment newFrag = null;
            
            switch(index) {
            case 0:
                if (!(currentFrag instanceof CreateFiltersFragment))
                    newFrag = new CreateFiltersFragment();
                break;
            case 1:
                if (!(currentFrag instanceof CreateNameFragment))
                    newFrag = new CreateNameFragment(initialName);
                break;
            case 2:
                if (!(currentFrag instanceof CreateLocationFragment))
                    newFrag = new CreateLocationFragment(initialLocation);
                break;
            }
            
            if (newFrag!=null) {
                // Execute a transaction, replacing any existing fragment
                // with this one inside the frame.
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.details, newFrag);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.commit();
            }

        } else {
            // Otherwise we need to launch a new activity to display
            // the dialog fragment with selected text.
            Intent intent = new Intent();
            if (index==0)
                intent.setClass(getActivity(), CreateFiltersActivity.class);
            if(index==1) {
                intent.setClass(getActivity(), CreateNameActivity.class);
                intent.putExtra("initialName", initialName);
            }
            if(index==2) {
                intent.putExtra("initialLocation", initialLocation);
                intent.setClass(getActivity(), CreateLocationActivity.class);
            }
            startActivityForResult(intent, 0);
        }
    }
    
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
    }
}