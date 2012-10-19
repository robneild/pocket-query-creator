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

import org.pquery.util.Logger;
import org.pquery.util.Prefs;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class PreferencesFromXml extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from XML
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Preference userDownload = findPreference(Prefs.USER_DOWNLOAD_DIR);
        userDownload.setSummary(Prefs.getUserSpecifiedDownloadDir(this));
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
    
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    	// Toggle log file creation on and off as soon as changed in preferences
        if (key.equals(Prefs.DEBUG_PREFERENCE)) {
            boolean logOn = sharedPreferences.getBoolean(key, false);
            if (logOn)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
             builder.setTitle("Debug logs")
                    .setMessage("Logs into LogCat and (if directory available) 'sdcard/Android/data/org.pquery/files/log.html'. Warning - logs may contain some personal info like your username, although your password should be masked. Cookies will be logged which could potentially be replayed by someone to log into geocaching.com as you");
             builder.setPositiveButton(R.string.ok, null);
             AlertDialog dialog = builder.create();
             dialog.show();
            }
        	Logger.setEnable(logOn);
        }
    }
    
}
