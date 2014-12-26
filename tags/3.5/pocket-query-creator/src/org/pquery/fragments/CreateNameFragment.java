//package org.pquery.fragments;
//
//import org.pquery.AutoSetNameDialog;
//import org.pquery.CreateSettingsChangedListener;
//import org.pquery.R;
//import org.pquery.AutoSetNameDialog.AutoSetNameDialogListener;
//import org.pquery.dao.QueryName;
//import org.pquery.util.GPS;
//import org.pquery.util.Logger;
//import org.pquery.util.Prefs;
//
//import com.actionbarsherlock.app.SherlockFragment;
//
//import android.app.Activity;
//import android.content.Context;
//import android.location.Location;
//import android.location.LocationListener;
//import android.location.LocationManager;
//import android.os.Bundle;
//import android.support.v4.app.DialogFragment;
//import android.text.Editable;
//import android.text.TextWatcher;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.CheckBox;
//import android.widget.CompoundButton;
//import android.widget.EditText;
//import android.widget.TextView;
//import android.widget.CompoundButton.OnCheckedChangeListener;
//
//public class CreateNameFragment extends SherlockFragment  implements LocationListener, AutoSetNameDialogListener {
//
//    private LocationManager locationManager;
//
//    private EditText name;
//    private CheckBox autoName;
//    private EditText radius;
//    private Button autoNameButton;
//    private Location location = new Location("rob");
//    
//    private CreateSettingsChangedListener listener;
//    private String initialName;
//    
//    public CreateNameFragment() {
//    }
//    
//    public CreateNameFragment(String initialName) {
//        this.initialName = initialName;
//    }
//    
//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//        try {
//            listener = (CreateSettingsChangedListener) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString() + " must implement CreateSettingsChangedListener");
//        }
//    }
//    
//    @Override
//    public void onDetach() {
//        super.onDetach();
//        listener = null;
//    }
//    
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        super.onCreateView(inflater, container, savedInstanceState);
//        
//        View view = inflater.inflate(R.layout.create_name_fragment, null);
//
//        Logger.d("enter");
//        
//        // Setup GPS
//
//        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
//
//        // Store references to controls
//
//        TextView radiusText = (TextView) view.findViewById(R.id.text_radius);
//        name = (EditText)view.findViewById(R.id.editText_name);
//        radius = (EditText)view.findViewById(R.id.editText_radius);
//        autoName = (CheckBox) view.findViewById(R.id.checkBox_autoname);
//        autoNameButton = (Button) view.findViewById(R.id.button_autoname);
//        
//        name.setText(initialName);
//        radius.setText(Prefs.getDefaultRadius(getActivity()));
//
//        if (Prefs.isMetric(getActivity()))
//           radiusText.setText(radiusText.getText() + " (km)");
//        else
//            radiusText.setText(radiusText.getText() + " (miles)");
//  
//        // TODO check geocoder is available
//        autoName.setChecked(Prefs.isAutoName(getActivity()));
//        
//        // Get parameters passed from previous wizard stage
//
////        Bundle bundle = getIntent().getBundleExtra("QueryStore");
////        Assert.assertNotNull(bundle);
////        queryStore = new QueryStore(bundle);
//        
//        autoName.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                Prefs.saveAutoName(getActivity(), isChecked);   
//            }
//        });
//        
//        radius.addTextChangedListener(new TextWatcher() {
//            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
//            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//            @Override
//            public void afterTextChanged(Editable s) {
//                if (s.length()>0)
//                    Prefs.saveDefaultRadius(getActivity(), s.toString());
//            }
//        });
//        
//        name.addTextChangedListener(new TextWatcher() {
//            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
//            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//            @Override
//            public void afterTextChanged(Editable s) {
//                if (s.length()>0) {
//                    Bundle bundle = new Bundle();
//                    bundle.putParcelable("queryName", new QueryName(s.toString()));
//                    listener.onSettingsChange(bundle);
//                }  
//            }
//        });
//       
//        autoNameButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showDialog();
//            }
//        });
//
//        return view;
//    }
//    
//
//    void showDialog() {
//        DialogFragment newFragment = AutoSetNameDialog.newInstance(location.getLatitude(), location.getLongitude());
//        newFragment.setTargetFragment(this, 0);
//        newFragment.show(getFragmentManager(), "dialog");
//    }
//
//    /**
//     * Callback for when locality lookup done
//     */
//    @Override
//    public void onAutoSetSuccess(String locality) {
//       name.setText(locality);
//       autoName.setChecked(false);
//    }
//    
//    
//    // Handle GPS callbacks
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        GPS.requestLocationUpdates(locationManager, this);
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        GPS.stopLocationUpdate(locationManager, this);
//    }
//
//    public void onLocationChanged(Location location) {
//        this.location = location;
//    }
//    public void onProviderDisabled(String arg0) {}
//    public void onProviderEnabled(String arg0) {}
//    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}
//
//
//}