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
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import junit.framework.Assert;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.Connection.KeyVal;
import org.jsoup.Connection.Request;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Allows entering of pocket query name and radius
 */
public class Dialog3 extends Activity implements LocationListener {

    private LocationManager locationManager;

    private QueryStore queryStore;

    private EditText name;
    private EditText radius;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog3);

        // Setup GPS

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Store references to controls

        name = (EditText)findViewById(R.id.editText_name);
        radius = (EditText)findViewById(R.id.editText_radius);
        final Button nextButton = (Button) findViewById(R.id.button_next);

        name.setText(getDefaultName());
        radius.setText(getDefaultRadius());

        // Get parameters passed from previous wizard stage

        Bundle bundle = getIntent().getBundleExtra("QueryStore");
        Assert.assertNotNull(bundle);
        queryStore = new QueryStore(bundle);


        nextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                if (!validForm()) {
                    Toast.makeText(getApplicationContext(), "Enter valid values", Toast.LENGTH_LONG).show();
                    return;
                }

                queryStore.name = name.getText().toString();
                queryStore.radius = Integer.parseInt(radius.getText().toString());

                Bundle bundle = new Bundle();
                queryStore.saveToBundle(bundle);

                Intent myIntent = new Intent(view.getContext(), Dialog4.class);
                myIntent.putExtra("QueryStore", bundle);
                startActivity(myIntent);
                finish();
            }
        });



        Button cancelButton = (Button) findViewById(R.id.button_cancel);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
            }
        });

    }


    /**
     * Is current form content valid
     */
    private boolean validForm() {
        if (name.getText().toString().length()>0)
            if (radius.getText().toString().length()>0)
                return true;
        return false;
    }


    private String getDefaultName() {
        return DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT).format(new Date());
    }

    private String getDefaultRadius() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return prefs.getString("radius_preference", "5");
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
