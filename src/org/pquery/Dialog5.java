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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.pquery.IOUtils.Listener;

import junit.framework.Assert;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import static org.pquery.Util.*;

/**
 * Actually creates the pocket query
 * 
 * GETS the creation page, then sends a POST to create
 */
public class Dialog5 extends Activity {

    // Creation takes long time. Use thread
    private CreationAsync task;

    // Show login progress
    private ProgressBar bar;
    private ProgressBar progressSpinner;

    // Show result of login attempt
    private TextView resultTextView;

    // Stores values setup in previous screen
    private QueryStore queryStore;

    /**
     * Called on page creation and screen rotation etc.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog5);

        // Store references to controls

        bar = (ProgressBar) findViewById(R.id.progress_bar);
        progressSpinner = (ProgressBar) findViewById(R.id.progress_spinner);
        resultTextView = (TextView) findViewById(R.id.results);
        Button cancelButton = (Button) findViewById(R.id.button_cancel);

        // Extract params passed from previous screens

        Bundle bundle = getIntent().getBundleExtra("QueryStore");
        Assert.assertNotNull(bundle);
        queryStore = new QueryStore(bundle);

        // Handle cancel button

        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
            }
        });

        // Manage Creation thread Thread
        //
        // IMPORANT: Continues to run over screen rotations & activity destruction/creation

        task = (CreationAsync) getLastNonConfigurationInstance();

        if (task==null) {
            // No existing task so start one

            task=new CreationAsync(this, queryStore, PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
            task.execute();
        }
        else {
            // Existing task already running. Send it our new 'Activity' reference

            // Whilst Activity was destroyed/created task thread continued
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
     * Update UI progress bar
     */
    private void updateProgress(int progress) {
        bar.setProgress(progress);
    }

    /**
     * Update GUI etc. after ASync task has finished
     */
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
     * A background task running on a thread
     * 
     * Updates UI progress bar as thread executes but needs to manage surrounding 
     * Activity being destroyed & recreated (i.e screen rotate)
     */
    static class CreationAsync extends AsyncTask<Void, Integer, String> {

        private Dialog5 activity;
        private QueryStore queryStore;
        private SharedPreferences prefs;

        // Results

        private int progress;
        private String errorMessage;
        private String successMessage;
        private boolean debug;
        
        CreationAsync(Dialog5 activity, QueryStore queryStore, SharedPreferences prefs) {
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

            DefaultHttpClient client = new DefaultHttpClient();
            String html;

            // Get values 

            int radius = Integer.valueOf(prefs.getString("radius_preference", "5"));		
            String max = prefs.getString("maxcaches_preference", "500");
            boolean notFound = prefs.getBoolean("not_found_preference", false);
            boolean active = prefs.getBoolean("active_preference", true);
            boolean disabled = prefs.getBoolean("disabled_preference", false);
            boolean metric = prefs.getBoolean("metric_preference", false);
            boolean zip =  prefs.getBoolean("zip_preference", true);
            debug = prefs.getBoolean("debug_preference", false);
            
            publishProgress(0);


            try {
                // Retrieve and set cookies from store

                for (Cookie c: queryStore.cookies) {
                    Log.v(APPNAME, "Dialog5 restored cookie "+c);
                    client.getCookieStore().addCookie(c);
                }
                
                html = IOUtils.httpGet(client, "pocket/gcquery.aspx", new Listener() {

                    @Override
                    public void update(int bytesReadSoFar, int expectedLength) {
                        publishProgress((int)(bytesReadSoFar*40/expectedLength));       //0-40%
                    }
                });

            } catch (Exception e) {
                return "Problem geting Query creation page " + (debug?e:"");
            }



            // Parse reply

            publishProgress(40);

            if (html.indexOf("ctl00_divSignedIn") == -1) {
                return("Don't seem to be logged in anymore");
            }

            // TODO NEED TO CHECK
            if (html.indexOf("ctl00_litPMLevel") == -1) {
                String ret = "You aren't a premium member. Goto Geocaching.com and upgrade";
                if (debug) {
                    Util.writeBadHTMLResponse("Dialog5::ctl00_litPMLevel", html);
                    ret += ". DEBUG response saved to " + Util.STORE_DIR;
                }
                return(ret);
            }

            // Extract VIEWSTATE hidden fields
            
            // __VIEWSTATE , __VIEWSTATE1, __VIEWSTATE2
            // __VIEWSTATEFIELDCOUNT 3
            
            // Manually extract the first __VIEWSTATE
            
            HashMap<String, String> viewStateMap = new HashMap<String,String>();

            String VIEWSTATE = "name=\"__VIEWSTATE\" id=\"__VIEWSTATE\" value=\"";

            int start = html.indexOf(VIEWSTATE);
            int end = html.indexOf("\"", start + VIEWSTATE.length());

            viewStateMap.put("__VIEWSTATE", html.substring(start + VIEWSTATE.length(), end));

            // Loop around extracting __VIEWSTATE1 etc...

            int i=1;
            String viewState;
            
            while((viewState=extractViewState(html, i)) != null) {
                Log.v(APPNAME, "Dialog5 extracted viewstate "+i);
                viewStateMap.put("__VIEWSTATE" + i, viewState);
                i++;
            }

            publishProgress(50);




            // Do POST to create pocket query


            List <NameValuePair> paramList = new ArrayList <NameValuePair>();

            paramList.add(new BasicNameValuePair("__EVENTTARGET",""));
            paramList.add(new BasicNameValuePair("__EVENTARGUMENT",""));
            paramList.add(new BasicNameValuePair("__VIEWSTATEFIELDCOUNT", ""+viewStateMap.size()));

            for (Map.Entry <String,String> entry: viewStateMap.entrySet()) {
                paramList.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }

            // Name of pocket query
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$tbName",queryStore.name));

            if (!disabled) {
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$cbDays$0","on"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$cbDays$1","on"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$cbDays$2","on"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$cbDays$3","on"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$cbDays$4","on"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$cbDays$5","on"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$cbDays$6","on"));
            }
            
            // 3 = Run this query once then delete it
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$rbRunOption","3"));


            paramList.add(new BasicNameValuePair("ctl00$ContentBody$tbResults", max));

            // Cache type filter

            if (queryStore.cacheTypeList.isAll())
                paramList.add(new BasicNameValuePair("ctl00$ContentBody$Type","rbTypeAny"));
            else {
                paramList.add(new BasicNameValuePair("ctl00$ContentBody$Type","rbTypeSelect"));
                
                for (CacheType cache : queryStore.cacheTypeList) {
                    paramList.add(new BasicNameValuePair("ctl00$ContentBody$cbTaxonomy$"+cache.ordinal(), "on"));
                }
            }
            
            // Container type filter
            
            if (queryStore.containerTypeList.isAll())            
                paramList.add(new BasicNameValuePair("ctl00$ContentBody$Container","rbContainerAny"));
            else {
                paramList.add(new BasicNameValuePair("ctl00$ContentBody$Container","rbContainerSelect"));
                
                for (ContainerType container : queryStore.containerTypeList) {
                    paramList.add(new BasicNameValuePair("ctl00$ContentBody$cbContainers$"+container.ordinal(), "on"));
                }
            }
            
            // I haven't found yet
            if (notFound)
                paramList.add(new BasicNameValuePair("ctl00$ContentBody$cbOptions$0","on"));

            // Is Active
            if (active)
                paramList.add(new BasicNameValuePair("ctl00$ContentBody$cbOptions$13","on"));

            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ddDifficulty",">="));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ddDifficultyScore","1"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ddTerrain",">="));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ddTerrainScore","1"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$CountryState","rbNone"));

            //paramList.add(new BasicNameValuePair("ctl00$ContentBody$Origin","rbOriginWpt"); // rbOriginNone");  //  rbOriginWpt");

            paramList.add(new BasicNameValuePair("ctl00$ContentBody$tbGC","GCXXXX"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$tbPostalCode",""));

            paramList.add(new BasicNameValuePair("ctl00$ContentBody$Origin","rbOriginWpt")); // rbOriginNone");  //  rbOriginWpt");


            // 0 = decimal degrees
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$LatLong","0")); // "1");

            if (queryStore.lat>0) {
                // North
                paramList.add(new BasicNameValuePair("ctl00$ContentBody$LatLong:_selectNorthSouth","1"));		// 1 = North, -1 = South
                paramList.add(new BasicNameValuePair("ctl00$ContentBody$LatLong$_inputLatDegs", Double.toString(queryStore.lat)));
            } else {
                // South
                paramList.add(new BasicNameValuePair("ctl00$ContentBody$LatLong:_selectNorthSouth","-1"));	// 1 = North, -1 = South
                paramList.add(new BasicNameValuePair("ctl00$ContentBody$LatLong$_inputLatDegs", Double.toString(- queryStore.lat)));
            }


            // Ignored?
            //paramList.add(new BasicNameValuePair("ctl00$ContentBody$LatLong$_inputLatMins","00.000");

            if (queryStore.lon>0) {
                // East
                paramList.add(new BasicNameValuePair("ctl00$ContentBody$LatLong:_selectEastWest","1"));		// -1 = West, 1 = East
                paramList.add(new BasicNameValuePair("ctl00$ContentBody$LatLong$_inputLongDegs", Double.toString(queryStore.lon)));
            } else {
                // West
                paramList.add(new BasicNameValuePair("ctl00$ContentBody$LatLong:_selectEastWest","-1"));		// -1 = West, 1 = East
                paramList.add(new BasicNameValuePair("ctl00$ContentBody$LatLong$_inputLongDegs", Double.toString(- queryStore.lon)));
            }

            // Ignored?
            //paramList.add(new BasicNameValuePair("ctl00$ContentBody$LatLong$_inputLongMins","00.000");

            // = decimal degrees
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$LatLong:_currentLatLongFormat","0")); // "1");

            paramList.add(new BasicNameValuePair("ctl00$ContentBody$tbRadius", Integer.toString(queryStore.radius)));

            
            if (metric)
                paramList.add(new BasicNameValuePair("ctl00$ContentBody$rbUnitType","km"));
            else
                paramList.add(new BasicNameValuePair("ctl00$ContentBody$rbUnitType","mi"));
            
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$Placed","rbPlacedNone"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ddLastPlaced","WEEK"));

            paramList.add(new BasicNameValuePair("ctl00$ContentBody$DateTimeBegin","June/11/2011"));

            paramList.add(new BasicNameValuePair("ctl00$ContentBody$DateTimeBegin$Month","6"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$DateTimeBegin$Day","11"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$DateTimeBegin$Year","2011"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$DateTimeEnd","June/18/2011"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$DateTimeEnd$Month","6"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$DateTimeEnd$Day","18"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$DateTimeEnd$Year","2011"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl00$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl01$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl02$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl03$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl04$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl05$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl06$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl07$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl08$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl09$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl10$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl11$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl12$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl13$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl14$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl15$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl16$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl17$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl18$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl19$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl20$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl21$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl22$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl23$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl24$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl25$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl26$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl27$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl28$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl29$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl30$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl31$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl32$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl33$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl34$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl35$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl36$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl37$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl38$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl39$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl40$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl41$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl42$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl43$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl44$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl45$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl46$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl47$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl48$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl49$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl50$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl51$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl52$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl53$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl54$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl55$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl56$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl57$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl58$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl59$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl60$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl61$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl62$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl63$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl64$hidInput","0"));
            
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl00$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl01$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl02$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl03$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl04$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl05$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl06$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl07$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl08$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl09$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl10$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl11$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl12$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl13$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl14$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl15$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl16$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl17$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl18$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl19$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl20$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl21$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl22$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl23$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl24$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl25$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl26$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl27$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl28$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl29$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl30$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl31$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl32$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl33$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl34$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl35$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl36$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl37$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl38$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl39$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl40$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl41$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl42$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl43$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl44$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl45$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl46$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl47$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl48$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl49$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl50$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl51$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl52$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl53$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl54$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl55$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl56$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl57$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl58$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl59$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl60$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl61$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl62$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl63$hidInput","0"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl64$hidInput","0"));
            
            
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ddlAltEmails","b@bigbob.org.uk"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$ddFormats","GPX"));

            if (zip)
                paramList.add(new BasicNameValuePair("ctl00$ContentBody$cbZip","on"));
            
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$cbIncludePQNameInFileName","on"));
            paramList.add(new BasicNameValuePair("ctl00$ContentBody$btnSubmit","Submit Information"));


            try {

                html = IOUtils.httpPost(client, new UrlEncodedFormEntity(paramList, HTTP.UTF_8), "pocket/gcquery.aspx", new Listener() {
                    @Override
                    public void update(int bytesReadSoFar, int expectedLength) {
                        publishProgress((int) (50 + (bytesReadSoFar*40/expectedLength)));       //50-90%
                    }
                });

            } catch (IOException e) {
                return "Problem submitting Query creation page "  + (debug?e:"");
            }

            
            // Parse POST html response
            // Look for success message inside <p class="Success">
            
            publishProgress(90);

            final String SUCCESS = "<p class=\"Success\">";
            
            int successStart = html.indexOf(SUCCESS);                                   // start

            if (successStart==-1) {
                return "Creation seems to have failed";
            }

            int successEnd = html.indexOf("</p>", successStart + SUCCESS.length());     // end
            this.successMessage = html.substring(successStart, successEnd);             // extract

            
            // Shutdown
            client.getConnectionManager().shutdown();
            
            publishProgress(100);
            return null;		// no error message returned
        }

        private String extractViewState(String loginHtml, int i) {
            String VIEWSTATE = "name=\"__VIEWSTATE" + i + "\" id=\"__VIEWSTATE" + i + "\" value=\"";

            int start = loginHtml.indexOf(VIEWSTATE);
            int end = loginHtml.indexOf("\"", start + VIEWSTATE.length());

            if (start==-1 || end==-1)
                return null;        // not found
            
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
