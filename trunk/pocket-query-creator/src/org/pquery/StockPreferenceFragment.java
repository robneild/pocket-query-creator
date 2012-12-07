package org.pquery;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;

@TargetApi(11)
public class StockPreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int res= getActivity().getResources().getIdentifier(getArguments().getString("resource"), "xml",getActivity().getPackageName());
       
        addPreferencesFromResource(res);
    }
    
    @Override
    public void onResume() {
        super.onResume();

            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener((SharedPreferences.OnSharedPreferenceChangeListener)getActivity());

    }
    
    @Override
    public void onPause() {
        super.onPause();

            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener((SharedPreferences.OnSharedPreferenceChangeListener)getActivity());

    }
    
}



