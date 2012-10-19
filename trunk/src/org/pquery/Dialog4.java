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

import java.text.DateFormat;
import java.util.Date;

import junit.framework.Assert;

import org.pquery.AutoSetNameDialog.AutoSetNameDialogListener;
import org.pquery.R;
import org.pquery.util.GPS;
import org.pquery.util.Logger;
import org.pquery.util.Prefs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Allows entering of pocket query name and radius
 */
public class Dialog4 extends FragmentActivity  implements LocationListener, AutoSetNameDialogListener {

    private LocationManager locationManager;

    private QueryStore queryStore;

    private EditText name;
    private CheckBox autoName;
    private EditText radius;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog4);

        Logger.d("enter");
        
        final Context cxt = getApplicationContext();
        
        // Setup GPS

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Store references to controls

        TextView radiusText = (TextView) findViewById(R.id.text_radius);
        name = (EditText)findViewById(R.id.editText_name);
        radius = (EditText)findViewById(R.id.editText_radius);
        autoName = (CheckBox) findViewById(R.id.checkBox_autoname);
        final Button nextButton = (Button) findViewById(R.id.button_next);

        name.setText(getDefaultName());
        radius.setText(Prefs.getDefaultRadius(cxt));

        if (Prefs.isMetric(cxt))
           radiusText.setText(radiusText.getText() + " (km)");
        else
            radiusText.setText(radiusText.getText() + " (miles)");
  
        // TODO check geocoder is available
        autoName.setChecked(Prefs.isAutoName(this));
        
        // Get parameters passed from previous wizard stage

        Bundle bundle = getIntent().getBundleExtra("QueryStore");
        Assert.assertNotNull(bundle);
        queryStore = new QueryStore(bundle);
        
        autoName.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Prefs.saveAutoName(cxt, isChecked);   
            }
        });
        
        // Handle next button
        // Goes onto next stage of wizard
        
        nextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                
                // Only go to next wizard page is some form values have been entered
                
                if (!validForm()) {
                    Toast.makeText(getApplicationContext(), "Enter valid values", Toast.LENGTH_LONG).show();
                    return;
                }
                
                // Save preferences
                
                Prefs.saveDefaultRadius(cxt, radius.getText().toString());
                
                // Go onto next wizard page; pass current values in QueryStore
                
                queryStore.name = name.getText().toString();
                queryStore.radius = Integer.parseInt(radius.getText().toString());

                                
                // All info collected. Kick off creation service
                
                Bundle bundle = new Bundle();
                queryStore.saveToBundle(bundle);

                
                Intent myIntent = new Intent(view.getContext(), CreationService.class);
                myIntent.putExtra("QueryStore", bundle);
                getApplicationContext().startService(myIntent);
                
                
                finish();
            }
        });

        // Handle cancel button
        // Just closes the activity

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

    /**
     * Create a starting point for pocket query name
     * Should be file name safe
     */
    private String getDefaultName() {
        String ret = DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT).format(new Date());
        ret = ret.replaceAll(":", ".");
        ret = ret.replaceAll("/", "-");
        return ret;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.dialog4, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.start_autoname:
                showDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    

    void showDialog() {
        DialogFragment newFragment = AutoSetNameDialog.newInstance(queryStore.lat, queryStore.lon);
        newFragment.show(getSupportFragmentManager(), "dialog");
    }

    /**
     * Callback for when locality lookup done
     */
    @Override
    public void onAutoSetSuccess(String locality) {
       name.setText(locality);
       autoName.setChecked(false);
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
        GPS.stopLocationUpdate(locationManager, this);
    }

    public void onLocationChanged(Location arg0) {}
    public void onProviderDisabled(String arg0) {}
    public void onProviderEnabled(String arg0) {}
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}


}
