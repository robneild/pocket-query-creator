/* 
 * Copyright (C) 2011 Robert Neild
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.pquery;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.Connection.KeyVal;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Request;
import org.jsoup.Connection.Response;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Logs the user into geocaching.com web site
 * If login is successful 'next' button is enabled
 */
public class Dialog5 extends Activity {

	private LoginAsync task;

	/**
	 * Show login progress
	 */
	private ProgressBar bar;

	private ProgressBar progressSpinner;

	/**
	 * Show result of login attempt
	 */
	private TextView resultTextView;

	private QueryStore queryStore;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog5);

		// Store references to controls

		bar = (ProgressBar) findViewById(R.id.progress_bar);
		progressSpinner = (ProgressBar) findViewById(R.id.progress_spinner);
		resultTextView = (TextView) findViewById(R.id.results);
		Button cancelButton = (Button) findViewById(R.id.button_cancel);

		// Get parameters passed from previous wizard stage

		Bundle bundle = getIntent().getBundleExtra("QueryStore");
		Assert.assertNotNull(bundle);
		queryStore = new QueryStore(bundle);

		// Handle button

		cancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				finish();
			}
		});

		// Manage Creation thread Thread
		//
		// It will continue to run over screen rotations &
		// activity destruction/creation

		task = (LoginAsync) getLastNonConfigurationInstance();

		if (task==null) {
			// No existing task so start one

			task=new LoginAsync(this, queryStore, PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
			task.execute();
		}
		else {
			// Existing task, send it our new 'Activity' reference

			// Whilst Activity was being destroyed/created the task thread would have continued
			// Update UI for any missed events and check if task completed

			task.attach(this);
			updateProgress(task.getProgress());

			if (task.getProgress()>=100) {
				onTaskFinished(task.getErrorMessage(), task.getSuccessMessage());
			}
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		task.detach();
		return task;
	}

	/**
	 * Update UI
	 */
	private void updateProgress(int progress) {
		bar.setProgress(progress);
	}

	private void onTaskFinished(String errorMessage, String successMessage) {

		if (isFinishing()) // detect if activity has been closed behind us (back or cancel button)
			return;

		bar.setProgress(100);

		bar.setVisibility(View.INVISIBLE);
		progressSpinner.setVisibility(View.INVISIBLE);

		if (successMessage!=null) {

			Intent myIntent = new Intent(getApplicationContext(), Dialog6.class);
			myIntent.putExtra("html", successMessage);
			startActivity(myIntent);
			finish();

		} else {

			resultTextView.setText(errorMessage);

		}
	}




	/**
	 * A task running on a thread
	 * 
	 * Updates UI progress bar as thread executes but needs to manage surrounding 
	 * Activity being destroyed & recreated (i.e screen rotate)
	 */
	static class LoginAsync extends AsyncTask<Void, Integer, String> {

		private Dialog5 activity;
		private QueryStore queryStore;
		private SharedPreferences prefs;

		// Results

		private int progress;
		private String errorMessage;
		private String successMessage;

		LoginAsync(Dialog5 activity, QueryStore queryStore, SharedPreferences prefs) {
			attach(activity);

			this.queryStore = queryStore;
			this.prefs = prefs;
		}

		/**
		 * Create pocket query
		 * 
		 * Returns null on success else some error text
		 */
		@Override
		protected String doInBackground(Void... unused) {


			// Get values 

			int radius = Integer.valueOf(prefs.getString("radius_preference", "5"));		
			String max = prefs.getString("maxcaches_preference", "500");
			boolean notFound = prefs.getBoolean("not_found_preference", false);
			boolean active = prefs.getBoolean("active_preference", true);


			publishProgress(0);




			// Do GET on pocket query creation page
			// (Need to get VIEWSTATE hidden parameters)




			Connection connection = Jsoup.connect("https://www.geocaching.com/pocket/gcquery.aspx");
			connection.timeout(10000);
			//connection.request().headers().remove("Accept-Encoding"); 

			// Copy cookies from store

			for (String key : queryStore.cookies.keySet()) {
				String value = queryStore.cookies.get(key);	 
				connection.cookie(key, value); 
			}


			// Execute the GET

			Response response = null;

			try {
				connection.method(Method.GET);
				response = connection.execute();

			} catch (IOException e) {
				return("Unable to get pocket query creation page");
			}

			publishProgress(40);

			String html = response.body();

			if (html.indexOf("ctl00_divSignedIn") == -1) {
				return("Don't seem to be logged in anymore");
			}

			// TODO NEED TO CHECK
			if (html.indexOf("ctl00_litPMLevel") == -1) {
				return("You aren't a premium member. Goto Geocaching.com and upgrade");
			}

			// Retrieve and store cookies in reply

			//Map <String,String> cookiesBob = response.cookies();
			//QueryStore.cookies = cookies;

			// Copy VIEWSTATE hidden parameters

			HashMap<String, String> viewStateMap = new HashMap<String,String>();

			String VIEWSTATE = "name=\"__VIEWSTATE\" id=\"__VIEWSTATE\" value=\"";

			int start = html.indexOf(VIEWSTATE);
			int end = html.indexOf("\"", start + VIEWSTATE.length());

			viewStateMap.put("__VIEWSTATE", html.substring(start + VIEWSTATE.length(), end));


			for (int i=1; i<11; i++) {

				extractViewState(html, i);
				viewStateMap.put("__VIEWSTATE" + i, extractViewState(html, i));
			}

			publishProgress(50);






			// Do POST to create pocket query




			Connection postConnection = Jsoup.connect("https://www.geocaching.com/pocket/gcquery.aspx");
			postConnection.timeout(10000);
			//postConnection.request().headers().remove("Accept-Encoding"); 

			// Add cookies returned from previous GET

			for (String key : queryStore.cookies.keySet()) {
				String value = queryStore.cookies.get(key);	 
				postConnection.cookie(key, value); 
			}


			postConnection.data("__EVENTTARGET","");
			postConnection.data("__EVENTARGUMENT","");
			postConnection.data("__VIEWSTATEFIELDCOUNT","11");
			postConnection.data(viewStateMap);

			// Name of pocket query
			postConnection.data("ctl00$ContentBody$tbName",queryStore.name);

			postConnection.data("ctl00$ContentBody$cbDays$0","on");
			postConnection.data("ctl00$ContentBody$cbDays$1","on");
			postConnection.data("ctl00$ContentBody$cbDays$1","on");
			postConnection.data("ctl00$ContentBody$cbDays$2","on");
			postConnection.data("ctl00$ContentBody$cbDays$3","on");
			postConnection.data("ctl00$ContentBody$cbDays$4","on");
			postConnection.data("ctl00$ContentBody$cbDays$5","on");
			postConnection.data("ctl00$ContentBody$cbDays$6","on");

			// 3 = Run this query once then delete it
			postConnection.data("ctl00$ContentBody$rbRunOption","3");


			postConnection.data("ctl00$ContentBody$tbResults", max);

			postConnection.data("ctl00$ContentBody$Type","rbTypeAny");
			postConnection.data("ctl00$ContentBody$Container","rbContainerAny");

			// I haven't found yet
			if (notFound)
				postConnection.data("ctl00$ContentBody$cbOptions$0","on");

			// Is Active
			if (active)
				postConnection.data("ctl00$ContentBody$cbOptions$13","on");

			postConnection.data("ctl00$ContentBody$ddDifficulty",">=");
			postConnection.data("ctl00$ContentBody$ddDifficultyScore","1");
			postConnection.data("ctl00$ContentBody$ddTerrain",">=");
			postConnection.data("ctl00$ContentBody$ddTerrainScore","1");
			postConnection.data("ctl00$ContentBody$CountryState","rbNone");

			//postConnection.data("ctl00$ContentBody$Origin","rbOriginWpt"); // rbOriginNone");  //  rbOriginWpt");

			postConnection.data("ctl00$ContentBody$tbGC","GCXXXX");
			postConnection.data("ctl00$ContentBody$tbPostalCode","");

			postConnection.data("ctl00$ContentBody$Origin","rbOriginWpt"); // rbOriginNone");  //  rbOriginWpt");


			// 0 = decimal degrees
			postConnection.data("ctl00$ContentBody$LatLong","0"); // "1");

			if (queryStore.lat>0) {
				// North
				postConnection.data("ctl00$ContentBody$LatLong:_selectNorthSouth","1");		// 1 = North, -1 = South
				postConnection.data("ctl00$ContentBody$LatLong$_inputLatDegs", Double.toString(queryStore.lat));
			} else {
				// South
				postConnection.data("ctl00$ContentBody$LatLong:_selectNorthSouth","-1");	// 1 = North, -1 = South
				postConnection.data("ctl00$ContentBody$LatLong$_inputLatDegs", Double.toString(- queryStore.lat));
			}


			// Ignored?
			//postConnection.data("ctl00$ContentBody$LatLong$_inputLatMins","00.000");

			if (queryStore.lon>0) {
				// East
				postConnection.data("ctl00$ContentBody$LatLong:_selectEastWest","1");		// -1 = West, 1 = East
				postConnection.data("ctl00$ContentBody$LatLong$_inputLongDegs", Double.toString(queryStore.lon));
			} else {
				// West
				postConnection.data("ctl00$ContentBody$LatLong:_selectEastWest","-1");		// -1 = West, 1 = East
				postConnection.data("ctl00$ContentBody$LatLong$_inputLongDegs", Double.toString(- queryStore.lon));
			}

			// Ignored?
			//postConnection.data("ctl00$ContentBody$LatLong$_inputLongMins","00.000");

			// = decimal degrees
			postConnection.data("ctl00$ContentBody$LatLong:_currentLatLongFormat","0"); // "1");

			postConnection.data("ctl00$ContentBody$tbRadius", Integer.toString(radius));

			postConnection.data("ctl00$ContentBody$rbUnitType","mi");
			postConnection.data("ctl00$ContentBody$Placed","rbPlacedNone");
			postConnection.data("ctl00$ContentBody$ddLastPlaced","WEEK");

			postConnection.data("ctl00$ContentBody$DateTimeBegin","June/11/2011");

			postConnection.data("ctl00$ContentBody$DateTimeBegin$Month","6");
			postConnection.data("ctl00$ContentBody$DateTimeBegin$Day","11");
			postConnection.data("ctl00$ContentBody$DateTimeBegin$Year","2011");
			postConnection.data("ctl00$ContentBody$DateTimeEnd","June/18/2011");
			postConnection.data("ctl00$ContentBody$DateTimeEnd$Month","6");
			postConnection.data("ctl00$ContentBody$DateTimeEnd$Day","18");
			postConnection.data("ctl00$ContentBody$DateTimeEnd$Year","2011");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl00$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl01$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl02$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl03$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl04$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl05$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl06$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl07$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl08$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl09$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl10$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl11$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl12$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl13$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl14$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl15$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl16$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl17$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl18$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl19$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl20$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl21$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl22$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl23$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl24$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl25$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl26$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl27$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl28$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl29$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl30$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl31$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl32$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl33$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl34$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl35$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl36$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl37$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl38$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl39$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl40$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl41$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl42$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl43$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl44$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl45$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl46$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl47$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl48$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl49$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl50$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl51$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl52$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl53$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl54$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl55$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl56$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl57$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl58$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl59$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl00$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl01$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl02$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl03$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl04$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl05$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl06$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl07$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl08$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl09$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl10$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl11$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl12$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl13$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl14$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl15$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl16$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl17$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl18$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl19$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl20$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl21$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl22$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl23$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl24$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl25$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl26$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl27$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl28$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl29$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl30$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl31$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl32$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl33$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl34$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl35$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl36$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl37$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl38$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl39$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl40$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl41$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl42$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl43$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl44$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl45$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl46$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl47$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl48$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl49$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl50$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl51$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl52$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl53$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl54$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl55$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl56$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl57$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl58$hidInput","0");
			postConnection.data("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl59$hidInput","0");
			postConnection.data("ctl00$ContentBody$ddlAltEmails","b@bigbob.org.uk");
			postConnection.data("ctl00$ContentBody$ddFormats","GPX");

			postConnection.data("ctl00$ContentBody$cbZip","on");
			postConnection.data("ctl00$ContentBody$cbIncludePQNameInFileName","on");

			postConnection.data("ctl00$ContentBody$btnSubmit","Submit Information");


			Response postResponse = null;
			try {
				postConnection.method(Method.POST);
				postResponse = postConnection.execute();

			} catch (IOException e) {
				return("Error sending creation post");
			}
			publishProgress(90);

			html = postResponse.body();

			final String SUCCESS = "<p class=\"Success\">";

			int successStart = html.indexOf(SUCCESS);

			if (successStart==-1) {
				return("Creation seems to have failed");
			}

			int successEnd = html.indexOf("</p>", successStart + SUCCESS.length());

			this.successMessage = html.substring(successStart, successEnd);

			return null;		// no error message returned
		}

		private String extractViewState(String loginHtml, int i) {
			String VIEWSTATE = "name=\"__VIEWSTATE" + i + "\" id=\"__VIEWSTATE" + i + "\" value=\"";

			int start = loginHtml.indexOf(VIEWSTATE);
			int end = loginHtml.indexOf("\"", start + VIEWSTATE.length());

			return loginHtml.substring(start + VIEWSTATE.length(), end);

		}

		@Override
		protected void onProgressUpdate(Integer... progress) {

			this.progress = progress[0];

			if (activity==null) {
				// Skip UI update as no activity
			}
			else {
				activity.updateProgress(progress[0]);
			}
		}

		@Override
		protected void onPostExecute(String errorMessage) {

			this.progress = 100;
			this.errorMessage = errorMessage;

			if (activity==null) {
				// skip UI update as no attached Activity
			}	
			else {
				activity.onTaskFinished(errorMessage, successMessage);
			}
		}

		void detach() {
			activity=null;
		}

		void attach(Dialog5 activity) {
			this.activity=activity;
		}


		int getProgress() {
			return progress;
		}

		String getSuccessMessage() {
			return successMessage;
		}

		String getErrorMessage() {
			return errorMessage;
		}

	}











}
