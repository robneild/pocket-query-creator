package org.pquery;

import org.pquery.util.GPS;
import org.pquery.util.Logger;
import org.pquery.util.Prefs;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.LinearLayout.LayoutParams;

public class AutoSetNameDialog extends DialogFragment implements LocationListener {

    /** the async task the does the lookup */
    private LookupLocationTask lookupLocationTask;
    private LocationManager locationManager;
    
    /** A 'good' gps fix */
    private LocationFix okLocation;
    
    /** uses whilst searching for a good gps fix */
    private Location gpsSearch;
    
    /**
     * a lat and lon of 0 are considered missing
     */
    public static AutoSetNameDialog newInstance(double lat, double lon) {
        AutoSetNameDialog frag = new AutoSetNameDialog();
        Bundle args = new Bundle();
        args.putDouble("lat", lat);
        args.putDouble("lon", lon);
        frag.setArguments(args);
        return frag;
    }

    public interface AutoSetNameDialogListener {
        public void onAutoSetSuccess(String locality);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        ProgressDialog d = new ProgressDialog(getActivity());
        d.setIndeterminate(true);
        d.setCancelable(true);
        
        this.lookupLocationTask = new LookupLocationTask();
        this.locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        // There are 2 possibilities
        //
        // We have already got a good GPS fix (either passed in, or found before
        // a rotation). Start ASync task
        // 
        // We don't have GPS fix. Start GPS. When we get good enough fix the
        // GPS callback will stat ASync task
        
        if (savedInstanceState!=null && savedInstanceState.getDouble("lat") != 0) {
            // GPS fix ok
            okLocation = new LocationFix(
                    savedInstanceState.getDouble("lat"),
                    savedInstanceState.getDouble("lon"));
            
            lookupLocationTask.execute(new LocationFix[] { okLocation });
        } else {
            // No GPS fix
            GPS.requestLocationUpdates(locationManager, this);
        }

        setStyle(STYLE_NO_FRAME, 0);
        return;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog progressDialog = new ProgressDialog(getActivity());

        progressDialog.setMessage("Working");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(true);
        
        return progressDialog;
    }
    
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//            Bundle savedInstanceState) {
//        super.onCreateView(inflater, container, savedInstanceState);
//        
//        //LayoutParams p = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
//        //container.setLayoutParams(p);
//        
//        View v = inflater.inflate(R.layout.autosetnamedialog, container, false);
//        
////        ProgressBar spinner = new ProgressBar(getActivity());
////        
////        LinearLayout ll = new LinearLayout(getActivity());
////        ll.setBackgroundColor(0xAA000000);
////        ll.addView(spinner);
//        
//        return v;
//    }
//    
    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);

        if (okLocation!=null) {
            bundle.putDouble("lat", okLocation.lat);
            bundle.putDouble("lon", okLocation.lon);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        lookupLocationTask.cancel(true);
        GPS.stopLocationUpdate(locationManager, this);
    }

    
    
    private class LocationFix {
        public double lat;
        public double lon;

        LocationFix(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }
    }

    private class LookupLocationTask extends AsyncTask<LocationFix, Void, String> {

        @Override
        protected void onPostExecute(String result) {
            if (result!=null)
                ((AutoSetNameDialogListener) getActivity()).onAutoSetSuccess(result);
            dismiss();
        }

        @Override
        protected String doInBackground(LocationFix... params) {
            String locality = GPS.getLocality(getActivity(), params[0].lat, params[0].lon);
            return locality;
        }
    }

    
    @Override
    public void onLocationChanged(android.location.Location location) {
        
        if (location.getProvider().equals(LocationManager.NETWORK_PROVIDER) && this.gpsSearch != null
                && this.gpsSearch.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            // don't over write GPS with network provider
        } else {
            this.gpsSearch = location;
        }

        if (gpsSearch.getAccuracy() < Prefs.getLocationAccuracy(getActivity())) {
            if (okLocation == null)
            {
            Logger.d("Fix is accurate enough. Saving it [accuracy=" + gpsSearch.getAccuracy() + ", requiredAccuracy="
                    + Prefs.getLocationAccuracy(getActivity()));
            
            // We have a good enough GPS fix
            // Store it (to persist over rotation) then
            // stop GPS and start ASync task
            
            okLocation = new LocationFix( gpsSearch.getLatitude(), gpsSearch.getLongitude());
            GPS.stopLocationUpdate(locationManager, this);
            lookupLocationTask.execute(new LocationFix[] { okLocation });
            }
        }

    }

    @Override
    public void onProviderDisabled(String arg0) {
    }

    @Override
    public void onProviderEnabled(String arg0) {
    }

    @Override
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
    }

}
/*
 * public class AutoSetNameDialog extends ProgressDialog {
 * 
 * private AutoSetNameDialogListener autoSetNameDialogListener; private
 * LookupLocationTask lookupLocationTask; private Location location;
 * 
 * public interface AutoSetNameDialogListener { public void
 * onAutoSetSuccess(String locality); }
 * 
 * public AutoSetNameDialog(Context context, double lat, double lon,
 * AutoSetNameDialogListener listener) { super(context); setListener(listener);
 * location = new Location(lat, lon); }
 * 
 * public AutoSetNameDialog(Context context, int theme, double lat, double lon,
 * AutoSetNameDialogListener listener) { super(context, theme);
 * setListener(listener); location = new Location(lat, lon); }
 * 
 * private void setListener(AutoSetNameDialogListener listener) { if
 * (listener==null) throw new IllegalArgumentException("listener");
 * this.autoSetNameDialogListener = listener; }
 * 
 * @Override protected void onCreate(Bundle savedInstanceState) {
 * super.onCreate(savedInstanceState);
 * 
 * setTitle("Indeterminate"); setMessage("Please wait while loading...");
 * setIndeterminate(true); setCancelable(true);
 * 
 * lookupLocationTask = new LookupLocationTask(); lookupLocationTask.execute(new
 * Location[] { location }); }
 * 
 * 
 * 
 * private class Location { public double lat; public double lon;
 * Location(double lat, double lon) { this.lat = lat; this.lon = lon; } }
 * 
 * private class LookupLocationTask extends AsyncTask<Location, Void, String> {
 * 
 * @Override protected void onPostExecute(String result) {
 * autoSetNameDialogListener.onAutoSetSuccess(result);
 * AutoSetNameDialog.this.dismiss(); }
 * 
 * @Override protected String doInBackground(Location... params) { String
 * locality = GPS.getLocality(getContext(), params[0].lat, params[1].lon);
 * return locality; } } }
 */
