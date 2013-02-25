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

import java.util.List;

import org.pquery.util.Logger;
import org.pquery.util.Prefs;
import org.pquery.util.Util;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;

public class PreferencesFromXml extends SherlockPreferenceActivity  implements OnSharedPreferenceChangeListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		String action = getIntent().getAction();

		if (action != null && action.equals("PREFS_LOGIN")) {
			addPreferencesFromResource(R.xml.login_preferences);
			setTitle(R.string.prefs_login);
		}
		else if (action != null && action.equals("PREFS_CREATION")) {
			addPreferencesFromResource(R.xml.creation_preferences);
			setTitle(R.string.prefs_creation);
		}
		else if (action != null && action.equals("PREFS_DOWNLOAD")) {
			addPreferencesFromResource(R.xml.download_preferences);
			setTitle(R.string.prefs_download);
		}
		else if (action != null && action.equals("PREFS_ADVANCED")) {
			addPreferencesFromResource(R.xml.advanced_preferences);
			setTitle(R.string.prefs_advanced);
		}
		else if (Build.VERSION.SDK_INT<Build.VERSION_CODES.HONEYCOMB) {
			addPreferencesFromResource(R.xml.preference_headers_legacy);
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
		if (userDownload!=null)
			userDownload.setSummary(Prefs.getUserSpecifiedDownloadDir(this));
		Preference defaultDownload = findPreference(Prefs.DEFAULT_DOWNLOAD_DIR);
		if (defaultDownload!=null)
			defaultDownload.setSummary("Output to default 'Download' directory (" + Util.getDefaultDownloadDirectory() + ")");



		if (getPreferenceScreen()!=null)
			// Set up a listener whenever a key changes
			getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (getPreferenceScreen()!=null)
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

		if (key.equals(Prefs.USERNAME)) {
			Prefs.userNameChanged(this);
		}
	}


}
