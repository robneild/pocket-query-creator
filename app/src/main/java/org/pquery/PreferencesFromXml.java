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

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

import org.pquery.util.AppCompatPreferenceActivity;
import org.pquery.util.Logger;
import org.pquery.util.Prefs;
import org.pquery.util.Util;

import java.util.List;

public class PreferencesFromXml extends AppCompatPreferenceActivity implements OnSharedPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String action = getIntent().getAction();

        if (action != null && action.equals("PREFS_LOGIN")) {
            addPreferencesFromResource(R.xml.login_preferences);
            setTitle(R.string.prefs_login);
        } else if (action != null && action.equals("PREFS_CREATION")) {
            addPreferencesFromResource(R.xml.creation_preferences);
            setTitle(R.string.prefs_creation);
        } else if (action != null && action.equals("PREFS_DOWNLOAD")) {
            addPreferencesFromResource(R.xml.download_preferences);
            setTitle(R.string.prefs_download);
        } else if (action != null && action.equals("PREFS_ADVANCED")) {
            addPreferencesFromResource(R.xml.advanced_preferences);
            setTitle(R.string.prefs_advanced);
        }
    }

    /**
     * Only called on Android 3.0 and above
     */
    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, Main.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Copied from StockPreferenceFragment
        Preference userDownload = findPreference(Prefs.USER_DOWNLOAD_DIR);
        if (userDownload != null)
            userDownload.setSummary(Prefs.getUserSpecifiedDownloadDir(this));
        Preference defaultDownload = findPreference(Prefs.DEFAULT_DOWNLOAD_DIR);
        if (defaultDownload != null)
            defaultDownload.setSummary(String.format(getResources().getString(R.string.summary_default_download_dir_preference), Util.getDefaultDownloadDirectory(this)));


        if (getPreferenceScreen() != null)
            // Set up a listener whenever a key changes
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (getPreferenceScreen() != null)
            // Unregister the listener whenever a key changes
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Toggle log file creation on and off as soon as changed in preferences
        if (key.equals(Prefs.DEBUG_PREFERENCE)) {
            boolean logOn = sharedPreferences.getBoolean(key, false);
            if (logOn) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.title_debug_preference)
                        .setMessage(R.string.message_debug_preference);
                builder.setPositiveButton(R.string.ok, null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
            Logger.setEnable(logOn);
        }

        if (key.equals(Prefs.USERNAME)) {
            Prefs.userNameChanged(this);
        }
    }


    protected boolean isValidFragment(String fragmentName) {
        if (StockPreferenceFragment.class.getName().equals(fragmentName))
            return true;
        return false;

    }
}
