package org.pquery;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import org.pquery.util.Prefs;
import org.pquery.util.Util;

@TargetApi(11)
public class StockPreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int res = getActivity().getResources().getIdentifier(getArguments().getString("resource"), "xml", getActivity().getPackageName());

        addPreferencesFromResource(res);
    }

    @Override
    public void onResume() {
        super.onResume();

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener((SharedPreferences.OnSharedPreferenceChangeListener) getActivity());

        // Copied from PreferencesFromXml
        Preference userDownload = findPreference(Prefs.USER_DOWNLOAD_DIR);
        if (userDownload != null)
            userDownload.setSummary(Prefs.getUserSpecifiedDownloadDir(getActivity()));
        Preference defaultDownload = findPreference(Prefs.DEFAULT_DOWNLOAD_DIR);
        if (defaultDownload != null)
            defaultDownload.setSummary(Util.getDefaultDownloadDirectory(this.getActivity()));
    }

    @Override
    public void onPause() {
        super.onPause();

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener((SharedPreferences.OnSharedPreferenceChangeListener) getActivity());

    }

}



