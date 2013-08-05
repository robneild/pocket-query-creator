package org.pquery.fragments;

import org.pquery.R;
import org.pquery.dao.PQ;
import org.pquery.util.Logger;

import android.R.color;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Fragment that shows a list of Pocket Query that the user can download
 * 
 * It shows some help when first opened
 */
public class PQListFragment extends ListFragment {

	public interface PQClickedListener {
		public void onPQClicked(PQ pq);
	}

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

		if (pqs == null) {

			// We have don't have a pocket query list to display
			// so show some help instead
			
			WebView wv = ((WebView) getListView().getEmptyView());
			
			String html2 = "<html>" +
					"<table style='height:100%;width:100%;'>" +
					"<tr>" +
					"<td align='center' valign='center'>" +
					"<font color='grey'>" +
					"This app allows the easy creation of Pocket Queries<p>" +
					"It requires a premium geocaching.com account<p>" +
					"Press <img style='vertical-align: middle' width='20px' src='content_new.png'> to create new pocket query<br>"+
					"Press <img style='vertical-align: middle' width='20px' src='navigation_refresh.png'> to download existing pocket query" +
					"<p>" +
					"Press <img style='vertical-align: middle' width='20px' src='action_help.png'> to get more help" +
					"</font>" +
	 				"</td>" +
					"</tr>" +
					"</table>" +
					"</html>";
			
	        wv.setBackgroundColor(getResources().getColor(color.black));
	        wv.loadDataWithBaseURL("file:///android_asset/", html2, "text/html", "utf-8", "");
			
			
			setListAdapter(new IconicAdapter(getActivity(), new PQ[0]));		// have to set empty list so help is displayed
			
		} else {
			
			// If list is empty, display a message
			// else display list
			
			((WebView) getListView().getEmptyView()).loadDataWithBaseURL("file:///android_asset/","<html><table style='height:100%;width:100%;'><tr><td align='center'><font color='white'>No downloadable PQ</font></td></tr></table></html>", "text/html", "utf-8", ""); 
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
