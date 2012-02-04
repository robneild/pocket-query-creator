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

import static org.pquery.Util.APPNAME;

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
import java.util.HashMap;
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
import org.apache.http.client.params.ClientPNames;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.pquery.IOUtils.Listener;
import org.pquery.util.Parser.ParseException;
import org.pquery.util.Prefs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import org.pquery.util.Parser;

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
    private TextView progressText;
    
    /**
     * Show result of login attempt
     */
    private TextView resultsTextView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog2);

        Context cxt = getApplicationContext();
        
        // Setup GPS

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Store references to controls

        bar = (ProgressBar) findViewById(R.id.progress_bar);
        progressSpinner = (ProgressBar) findViewById(R.id.progress_spinner);
        progressText = (TextView) findViewById(R.id.progress_text);
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

        final String username = Prefs.getUsername(cxt);
        final String password = Prefs.getPassword(cxt);
        debug = prefs.getBoolean("debug_preference", false);

        // Manage Login Thread
        //
        // It will continue to run over screen rotations &
        // activity destruction/creation

        task = (LoginAsync) getLastNonConfigurationInstance();

        if (task==null) {
            // No existing task so start one

            task=new LoginAsync(this, Prefs.getCookies(cxt), username, password,debug, getResources());
            task.execute();
        }
        else {
            // Existing task, send it our new 'Activity' reference

            // Whilst Activity was being destroyed/created the task thread would have continued
            // Update UI for any missed events and check if task completed

            task.attach(this);
            updateProgress(task.getProgress());

            if (task.getProgress().percent>=100) {
                onTaskFinished(task.getResult(), task.getCookies(), task.getViewStateMap());
            }
        }

    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        task.detach();
        return task;
    }

    private void onTaskFinished(String result, List<Cookie> cookies, Map<String,String> viewStateMap) {

        // Login attempt finished

        if (isFinishing()) // detect if activity has been closed behind us (back or cancel button)
            return;

        bar.setProgress(100);
        bar.setVisibility(View.INVISIBLE);
        progressSpinner.setVisibility(View.INVISIBLE);

        if (result == null) {				// no result text means success
            // Automatically move onto next page

            // Save cookies in to preferences
            // Allows us to remain logged in so subsequent pocket query creations can be quicker
            
            Assert.assertNotNull(cookies);
            
            Prefs.saveCookies(getApplicationContext(), cookies);

            // Save other values into Bundle to pass onto next stages of wizard
            
            Bundle bundle = new Bundle();
            QueryStore qs = new QueryStore(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
            
            qs.viewStateMap = viewStateMap;  // hidden values on page. Allow to just submit creation page later
            qs.debug = debug;
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
    void updateProgress(ProgressInfo progress) {
        bar.setProgress(progress.percent);
        if (progress.resId != null)
            progressText.setText(progress.resId);       
    }

    
    static class ProgressInfo {
        public int percent;
        public Integer resId;
        ProgressInfo(int percent) {
            this.percent = percent;
        }
        ProgressInfo(int percent, int resId) {
            this(percent);
            this.resId = resId;
        }
    }
    
    
    /**
     * A task running on a thread
     * 
     * Updates UI progress bar as thread executes but needs to manage surrounding 
     * Activity being destroyed & recreated (i.e screen rotate)
     * 
     * Void = doBackground arguments
     * String = publisProgress arguments
     * String = result
     */
    static class LoginAsync extends AsyncTask<Void, ProgressInfo, String> {
        
        private Dialog2 activity;
        private Resources res;
        
        // Parameters

        private String user;
        private String pass;
        private boolean debug;

        // Results

        private ProgressInfo progress;
        private String result;
        private List <Cookie> cookies;
        private Map<String,String> viewStateMap;
        
        LoginAsync(Dialog2 activity, List<Cookie> cookies, String user, String pass, boolean debug, Resources res) {
            attach(activity);
            this.res = res;
            this.user = user;
            this.pass = pass;
            this.debug = debug;
            this.cookies = cookies;
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
            String html = "";
            
            DefaultHttpClient client = null;
            
            try {
                publishProgress(new ProgressInfo(0));

                client = new DefaultHttpClient();
   
                for (Cookie c: cookies) {
                    Log.v(APPNAME, "Dialog2 restored cookie "+c);
                    client.getCookieStore().addCookie(c);
                }

                // Get the pocket query creation page
                // and read the response. Need to detect if logged in or no

                try {
                    html = IOUtils.httpGet(client, "pocket/gcquery.aspx", new Listener() {

                        @Override
                        public void update(int bytesReadSoFar, int expectedLength) {
                            publishProgress(new ProgressInfo((int)(bytesReadSoFar*90/expectedLength)));     // 0-90%
                        }
                    });

                    // Retrieve and store cookies in reply

                    cookies = client.getCookieStore().getCookies();

                } catch (IOException e) {
                    return "Couldn't download login page " + (debug?e:"");
                }
                

                // Parse the response
                
                publishProgress(new ProgressInfo(90));
                Parser parse = new Parser(html);
                viewStateMap = parse.extractViewState();
                

                // Check the response. Detecting login and premium state

                if (parse.isLoggedIn()) {
                    if (!parse.isPremium()) 
                        return "You aren't a premium member. Goto Geocaching.com and upgrade" ;

                    return null; // All ok. Cookies must be ok and already logged in
                }

                publishProgress(new ProgressInfo(0,R.string.login_geocaching_com));     // 0%
                
                // Extract an extra field that the un-logged in pocket query page seems to have

                String PREVIOUS_PAGE = "name=\"__PREVIOUSPAGE\" id=\"__PREVIOUSPAGE\" value=\"";

                int start = html.indexOf(PREVIOUS_PAGE);
                int end = html.indexOf("\"", start + PREVIOUS_PAGE.length());

                viewStateMap.put("__PREVIOUSPAGE", html.substring(start + PREVIOUS_PAGE.length(), end));

                
                // We need to login
                // Create the POST

                List <NameValuePair> paramList = new ArrayList <NameValuePair>();

                paramList.add(new BasicNameValuePair("__EVENTTARGET",""));
                paramList.add(new BasicNameValuePair("__EVENTARGUMENT",""));
                paramList.add(new BasicNameValuePair("__VIEWSTATEFIELDCOUNT", ""+viewStateMap.size()));

                for (Map.Entry <String,String> entry: viewStateMap.entrySet()) {
                    paramList.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }

                // Fill in the form values


                paramList.add(new BasicNameValuePair("ctl00$tbUsername", user));
                paramList.add(new BasicNameValuePair("ctl00$tbPassword", pass));
                paramList.add(new BasicNameValuePair("ctl00$cbRememberMe","on"));
                paramList.add(new BasicNameValuePair("ctl00$btnSignIn","Sign In"));

                try {
                    html = IOUtils.httpPost(client, new UrlEncodedFormEntity(paramList, HTTP.UTF_8), "login/default.aspx?redir=%2fpocket%2fgcquery.aspx%3f", true, new Listener() {

                        @Override
                        public void update(int bytesReadSoFar, int expectedLength) {
                            publishProgress(new ProgressInfo((int) (10 + (bytesReadSoFar*80/expectedLength))));       // 10 - 90%
                        }
                    });

                    // Retrieve and store cookies in reply

                    cookies = client.getCookieStore().getCookies();

                } catch (IOException e) {
                    return("Unable to submit login form " + (debug?e:""));
                }

                // Parse response to check we are now logged in
                
                publishProgress(new ProgressInfo(90));
                parse = new Parser(html);

                if (parse.atLoginPage() || !parse.isLoggedIn()) {
                    String ret = res.getString(R.string.bad_credentials);
                    if (debug) {
                        Util.writeBadHTMLResponse("Dialog2 login failed", html);
                        ret += ". DEBUG response saved to " + Util.STORE_DIR;
                    }
                    return(ret);
                }

                if (!parse.isPremium()) 
                    return res.getString(R.string.not_premium);
                
                
                // Store page state for later use when we create a pocket query

                viewStateMap = parse.extractViewState();

                publishProgress(new ProgressInfo(100));
                return null;

            } catch (ParseException e) {
                String ret = e.getMessage();
                if (debug) {
                    Util.writeBadHTMLResponse("Dialog2 parseException=" + e.getMessage(), html);
                    ret += ". DEBUG response saved to " + Util.STORE_DIR;
                } 
                return ret;
            } 
            finally {
                // Shutdown
                if (client!=null && client.getConnectionManager()!=null)
                    client.getConnectionManager().shutdown();  
            }
        }


        @Override
        protected void onProgressUpdate(ProgressInfo... progress) {

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

            this.progress = new ProgressInfo(100);
            this.result = result;

            if (activity==null) {
                // skip UI update as no attached Activity
            }	
            else {
                activity.onTaskFinished(result, cookies, viewStateMap);
            }
        }

        void detach() {
            activity=null;
        }

        void attach(Dialog2 activity) {
            this.activity=activity;
        }


        ProgressInfo getProgress() {
            return progress;
        }

        String getResult() {
            return result;
        }

        List<Cookie> getCookies() {
            return cookies;
        }
        Map<String, String> getViewStateMap() {
            return viewStateMap;
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