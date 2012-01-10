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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import junit.framework.Assert;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.pquery.IOUtils.Listener;

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

    private boolean debug;
    
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
        debug = prefs.getBoolean("debug_preference", false);
        
        // Manage Login Thread
        //
        // It will continue to run over screen rotations &
        // activity destruction/creation

        task = (LoginAsync) getLastNonConfigurationInstance();

        if (task==null) {
            // No existing task so start one

            task=new LoginAsync(this, username, password,debug);
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

    private void onTaskFinished(String result, List<Cookie> cookies) {

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
            QueryStore qs = new QueryStore(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

            qs.cookies = cookies;
            qs.saveToBundle(bundle);
            qs.debug = debug;
            
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
        private boolean debug;
        
        // Results

        private int progress;
        private String result;
        private List <Cookie> cookies;

        LoginAsync(Dialog2 activity, String user, String pass, boolean debug) {
            attach(activity);

            this.user = user;
            this.pass = pass;
            this.debug = debug;
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

            String html;
            
            publishProgress(0);

            DefaultHttpClient client = new DefaultHttpClient();

            // Get the login screen
            // and read the response
            
            try {
                html = IOUtils.httpGet(client, "login/", new Listener() {
                    
                    @Override
                    public void update(int bytesReadSoFar, int expectedLength) {
                        publishProgress((int)(bytesReadSoFar*40/expectedLength));
                    }
                });

                // Retrieve and store cookies in reply

                cookies = client.getCookieStore().getCookies();
                
            } catch (Exception e) {
                return "Couldn't download login page " + (debug?e:"");
            }
            
            // Parse the response            
            
            publishProgress(40);

            // Check if the login page is there

            if (html.indexOf("LoginForm") == -1)
                return("Couldn't find expected form on Geocaching.com login");

            // Extract the viewState hidden form value

            String viewState = null;

            try {
                String VIEWSTATE = "name=\"__VIEWSTATE\" id=\"__VIEWSTATE\" value=\"";

                int start = html.indexOf(VIEWSTATE);
                int end = html.indexOf("\"", start + VIEWSTATE.length());

                viewState = html.substring(start + VIEWSTATE.length(), end);

            } catch (Exception e) {
                return("The Geocaching.com login page was mising some expected contents");
            }



            
            // Create the Login POST


            // Fill in the form values
            
            List <NameValuePair> paramList = new ArrayList <NameValuePair>();

            paramList.add(new BasicNameValuePair("__EVENTTARGET",""));
            paramList.add(new BasicNameValuePair("__EVENTARGUMENT",""));
            paramList.add(new BasicNameValuePair("__VIEWSTATE", viewState));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$tbUsername", user));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$tbPassword", pass));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$cbRememberMe","on"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$btnSignIn","on"));

            publishProgress(50);


            try {
                html = IOUtils.httpPost(client, new UrlEncodedFormEntity(paramList, HTTP.UTF_8), "login/", new Listener() {
                    
                    @Override
                    public void update(int bytesReadSoFar, int expectedLength) {
                        publishProgress((int) (50 + (bytesReadSoFar*40/expectedLength)));       // 50-90%
                    }
                });
                
                // Retrieve and store cookies in reply

                cookies = client.getCookieStore().getCookies();
                
            } catch (IOException e) {
                return("Unable to submit login form " + (debug?e:""));
            }

            publishProgress(90);
            
            // Parse response to check we are now logged in

            if (html.indexOf("ctl00_ContentBody_lbMessageText") == -1) {
                String ret = "Login to Geocaching.com failed. Verify your credentials are correct";
                if (debug) {
                    Util.writeBadHTMLResponse("Dialog2::ctl00_ContentBody_lbMessageText", html);
                    ret += ". DEBUG response saved to " + Util.STORE_DIR;
                }
                return(ret);
            }
            
            // Shutdown
            client.getConnectionManager().shutdown();  
            
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

        List<Cookie> getCookies() {
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
