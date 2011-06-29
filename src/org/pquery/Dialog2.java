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
import java.util.Map;

import junit.framework.Assert;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
public class Dialog2 extends Activity implements LocationListener {

    private LocationManager locationManager;

    private LoginAsync task;

    /**
     * Show login progress
     */
    private ProgressBar bar;
    private ProgressBar progressSpinner;

    /**
     * Show result of login attempt
     */
    private TextView resultsTextView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog2);

        // Setup GPS

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Store references to controls

        bar = (ProgressBar) findViewById(R.id.progress_bar);
        progressSpinner = (ProgressBar) findViewById(R.id.progress_spinner);
        resultsTextView = (TextView) findViewById(R.id.results);
        Button cancelButton = (Button) findViewById(R.id.button_cancel);

        // Handle cancel button

        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
            }
        });

        // Get preferences

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        final String username = prefs.getString("username_preference", "");
        final String password = prefs.getString("password_preference", "");

        // Manage Login Thread
        //
        // It will continue to run over screen rotations &
        // activity destruction/creation

        task = (LoginAsync) getLastNonConfigurationInstance();

        if (task==null) {
            // No existing task so start one

            task=new LoginAsync(this, username, password);
            task.execute();
        }
        else {
            // Existing task, send it our new 'Activity' reference

            // Whilst Activity was being destroyed/created the task thread would have continued
            // Update UI for any missed events and check if task completed

            task.attach(this);
            updateProgress(task.getProgress());

            if (task.getProgress()>=100) {
                onTaskFinished(task.getResult(), task.getCookies());
            }
        }

    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        task.detach();
        return task;
    }

    private void onTaskFinished(String result, Map<String,String> cookies) {

        // Login attempt finished

        if (isFinishing()) // detect if activity has been closed behind us (back or cancel button)
            return;

        bar.setProgress(100);
        bar.setVisibility(View.INVISIBLE);
        progressSpinner.setVisibility(View.INVISIBLE);

        if (result == null) {				// no result text means success
            // Automatically move onto next page

            Assert.assertNotNull(cookies);

            Bundle bundle = new Bundle();
            QueryStore qs = new QueryStore();

            qs.cookies = cookies;
            qs.saveToBundle(bundle);

            Intent myIntent = new Intent(getApplicationContext(), Dialog3.class);
            myIntent.putExtra("QueryStore", bundle);

            startActivity(myIntent);
            finish();
        } else
            resultsTextView.setText(result);
    }

    /**
     * Update UI
     */
    void updateProgress(int progress) {
        bar.setProgress(progress);
    }

    /**
     * A task running on a thread
     * 
     * Updates UI progress bar as thread executes but needs to manage surrounding 
     * Activity being destroyed & recreated (i.e screen rotate)
     */
    static class LoginAsync extends AsyncTask<Void, Integer, String> {
        private Dialog2 activity;

        // Parameters

        private String user;
        private String pass;

        // Results

        private int progress;
        private String result;
        private Map <String,String> cookies;

        LoginAsync(Dialog2 activity, String user, String pass) {
            attach(activity);

            this.user = user;
            this.pass = pass;
        }

        /**
         * Login to geocaching.com
         * 
         * Get the login page, then posts a response
         * 
         * Returns null on success else some error text
         */
        @Override
        protected String doInBackground(Void... unused) {

            publishProgress(0);

            // Get the login screen

            Connection loginConnection = Jsoup.connect("https://www.geocaching.com/login/");
            loginConnection.timeout(10000);
            //loginConnection.request().headers().remove("Accept-Encoding"); 		// don't accept g-zip

            // Do the actual request (HTTP GET)

            Response loginResponse = null;
            try {
                loginConnection.method(Method.GET);
                loginResponse = loginConnection.execute();

            } catch (IOException e) {
                return ("Unable to retrieve Geocaching.com login page. Verify you have network access");
            }

            // Do some parsing of the response

            String loginHtml = loginResponse.body();

            publishProgress(40);

            // Retrieve and store cookies in reply

            cookies = loginResponse.cookies();

            // Check if the login page is there

            if (loginHtml.indexOf("LoginForm") == -1)
                return("Couldn't find expected form on Geocaching.com login");


            // Extract the viewState hidden form value

            String viewState = null;

            try {
                String VIEWSTATE = "name=\"__VIEWSTATE\" id=\"__VIEWSTATE\" value=\"";

                int start = loginHtml.indexOf(VIEWSTATE);
                int end = loginHtml.indexOf("\"", start + VIEWSTATE.length());

                viewState = loginHtml.substring(start + VIEWSTATE.length(), end);

            } catch (Exception e) {
                return("The Geocaching.com login page was mising some expected contents");
            }






            // Start Login POST

            Connection loginPostConnection = Jsoup.connect("https://www.geocaching.com/login/");
            loginPostConnection.timeout(10000);
            //loginPostConnection.request().headers().remove("Accept-Encoding"); 		// stop g-zip

            // Copy cookies returned from first response

            for (String key : cookies.keySet()) {
                String value = cookies.get(key);
                loginPostConnection.cookie(key, value);
            }


            // Fill in the form values

            loginPostConnection.data("__EVENTTARGET","");
            loginPostConnection.data("__EVENTARGUMENT","");
            loginPostConnection.data("__VIEWSTATE", viewState);
            loginPostConnection.data("ctl00$SiteContent$tbUsername", user);
            loginPostConnection.data("ctl00$SiteContent$tbPassword", pass);
            loginPostConnection.data("ctl00$SiteContent$cbRememberMe","on");
            loginPostConnection.data("ctl00$SiteContent$btnSignIn","on");

            publishProgress(50);

            // Do the actual request here (HTTP POST)

            Response postResponse;
            try {
                loginPostConnection.method(Method.POST);
                postResponse = loginPostConnection.execute();


            } catch (IOException e) {
                return("Exception doing post " +e);
            }

            publishProgress(90);

            loginHtml = postResponse.body();

            // Check we are now logged in

            if (loginHtml.indexOf("ctl00_SiteContent_lbMessageText") == -1)
                return("Login to Geocaching.com failed. Verify your credentials are correct");


            publishProgress(100);
            return null;
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
        protected void onPostExecute(String result) {

            this.progress = 100;
            this.result = result;

            if (activity==null) {
                // skip UI update as no attached Activity
            }	
            else {
                activity.onTaskFinished(result, cookies);
            }
        }

        void detach() {
            activity=null;
        }

        void attach(Dialog2 activity) {
            this.activity=activity;
        }


        int getProgress() {
            return progress;
        }

        String getResult() {
            return result;
        }

        Map<String, String> getCookies() {
            return cookies;
        }
    }





    // Handle GPS

    @Override
    protected void onResume() {
        super.onResume();
        GPS.requestLocationUpdates(locationManager, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    public void onLocationChanged(Location arg0) {}
    public void onProviderDisabled(String arg0) {}
    public void onProviderEnabled(String arg0) {}
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}
}
