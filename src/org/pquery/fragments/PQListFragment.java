package org.pquery.fragments;

import junit.framework.Assert;

import org.pquery.R;
import org.pquery.dao.PQ;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class PQListFragment extends ListFragment {
    
    public interface PQClickedListener {
        public void onPQClicked(PQ pq);
    }
    
    private PQ[] pqs;
    private PQClickedListener listener;
    
//  @Override
//  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) { 
//      View view = inflater.inflate(R.layout.test_fragment, null);
//      return view;
//  }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (PQClickedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement PQListFragment");
        }
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        if (savedInstanceState != null) {
            PQ [] pqs = (PQ[]) savedInstanceState.getParcelableArray("pqs");
            updateList(pqs);
        }
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.pq_list_fragment, null);
      return view;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        PQ pq = (PQ) l.getItemAtPosition((int) id);
        listener.onPQClicked(pq);
    }
    
//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//        
//        ListView bob1 = getListView();
//        
//        View bob2 = getListView().getEmptyView();
//        
//        //((TextView) getListView().getEmptyView()).setText( "the empty message" );
//    }

    public void updateList(PQ[] pqs) {
        this.pqs = pqs;

        if (pqs==null) {
            ((TextView) getListView().getEmptyView()).setText("");
            setListAdapter(new IconicAdapter(getActivity(), new PQ[0]));
        } else {
            ((TextView) getListView().getEmptyView()).setText( "No downloadable PQ" );        
            setListAdapter(new IconicAdapter(getActivity(), pqs));
        }
    }
    
    private class IconicAdapter extends ArrayAdapter<PQ> {

        Activity context;

        IconicAdapter(Activity context, PQ[] pqs) {
            super(context, android.R.id.list, pqs);
            this.context = context;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();

            View row = inflater.inflate(R.layout.pq_list_row, parent, false);
            PQ pq = getItem(position);
            
            TextView name = (TextView) row.findViewById(R.id.name);
            name.setText(pq.name);
            
            TextView size = (TextView) row.findViewById(R.id.size);
            size.setText(pq.size);
            
            TextView waypoints = (TextView) row.findViewById(R.id.waypoints);
            waypoints.setText(pq.waypoints);
            
            TextView generated = (TextView) row.findViewById(R.id.generated);
            generated.setText(pq.age);
            
//
//            ImageView icon = (ImageView) row.findViewById(R.id.icon);
//
//            switch (getItem(position)) {
//            case R.string.create_new_query:
//                icon.setImageResource(R.drawable.hammer);
//                break;
//            case R.string.creation_pending:
//                icon.setImageResource(R.drawable.eye);
//                break;
//            case R.string.settings:
//                icon.setImageResource(R.drawable.settings);
//                break;
//            case R.string.help:
//                icon.setImageResource(R.drawable.help);
//                break;
//            case R.string.about:
//                icon.setImageResource(R.drawable.about);
//                break;
//            }

            return row;
        }
    }
    
    


//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//        
//        setListAdapter(new IconicAdapter(getActivity(), new PQ[]{}));
//    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        outState.putParcelableArray("pqs", pqs);
    }
}
