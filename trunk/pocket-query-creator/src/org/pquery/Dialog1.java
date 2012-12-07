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

import org.pquery.R;
import org.pquery.util.GPS;
import org.pquery.util.Logger;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Just displays starting some info text.
 * Also engages GPS
 */
public class Dialog1 extends Activity implements LocationListener {

    private LocationManager locationManager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog1);

        Logger.d("enter");
        
        // Setup GPS
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        Button next = (Button) findViewById(R.id.button_create);

        // Next button goes to dialog2

        next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            
                Intent myIntent = new Intent(view.getContext(), CreateFiltersActivity.class);
                startActivity(myIntent);
                finish();
            }
        });
    }

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

    // Impementation of LocationListener
    
    public void onLocationChanged(Location arg0) {}
    public void onProviderDisabled(String arg0) {}
    public void onProviderEnabled(String arg0) {}
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}
}