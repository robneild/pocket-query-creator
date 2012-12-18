package org.pquery.fragments;

import junit.framework.Assert;

import org.pquery.R;
import org.pquery.dao.PQ;
import org.pquery.util.Logger;

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class PQListFragment extends ListFragment {

	public interface PQClickedListener {
		public void onPQClicked(PQ pq);
	}

	private PQ[] pqs;
	private PQClickedListener listener;

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
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Logger.d("enter");

		outState.putParcelableArray("pqs", pqs);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Logger.d("enter");

		if (savedInstanceState != null) {
			try {
				Parcelable[] parcel = savedInstanceState.getParcelableArray("pqs");
				PQ [] pqs = null;
				if (parcel!=null) {
					Logger.d("parcelable info [parcel_length=" + parcel.length +"]");
					pqs = new PQ[parcel.length];
					for (int i=0; i<parcel.length; i++) {
						Logger.d("working on = " + i + parcel[i].getClass().getName());
						Logger.d("instanceof =" + (parcel[i] instanceof PQ));
						pqs[i] = (PQ) parcel[i];
					}
				}
				updateList(pqs);
			} catch (ClassCastException e) {
				Logger.e("ClassCaseException " + e);
			}
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


	/**
	 * Change list of Pocket Query
	 * 
	 * Called from Activity
	 * 
	 * @param pqs - null = list unknown
	 * 				empty array = user has no downloadable PQ
	 */
	public void updateList(PQ[] pqs) {
		this.pqs = pqs;

		if (pqs == null) {
			// Want to show an empty screen
			((TextView) getListView().getEmptyView()).setText("");
			setListAdapter(new IconicAdapter(getActivity(), new PQ[0]));
		} else {
			// If list is empty, display a message
			// else display list
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

			return row;
		}
	}


}
