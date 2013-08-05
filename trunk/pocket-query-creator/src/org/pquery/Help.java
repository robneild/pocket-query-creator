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

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import android.R.color;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

/**
 * Show an HTML about page
 */
public class Help extends SherlockActivity {

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.about);

		final String mimeType = "text/html";
		final String encoding = "utf-8";

		WebView wv;

		String html = "<font color='#ffffff'>" + 

		"<style type='text/css'>" +
		
		"a:link {color: #0066FF; text-decoration: underline; }" +
		"a:active {color: #0066FF; text-decoration: underline; }" +
		"a:visited {color: #0066FF; text-decoration: underline; }" +
		"a:hover {color: #0066FF; text-decoration: underline; }" +
		"h2 {margin-top:25px;font-size: 18px;}" +
		"li {margin: 5px}" +
		"body {font: 15px/1.3em 'Helvetica Neue','HelveticaNeue',Helvetica,sans-serif }" +
		"</style> " +
		
		"This app allows the quick creation and downloading of " +
		"<a href='http://www.geocaching.com/pocket/'>Pocket Queries</a>. " +
		"They are provided to premium members of the website " +
		"<a href='http://www.geocaching.com'>Geocaching.com</a>." +
		"<p>This app does nothing that can't be done at the Geocaching.com website. It just acts as a convienience." +

		"<p>You can't go geocaching with just this app. Try <a href='market://search?q=pname:com.google.code.geobeagle'>geobeagle</a>"+
		" or <a href='market://search?q=pname:com.groundspeak.geocaching'>the official client</a>" +

		"<br><h2>Howto Create a Pocket Query</h2>" +

		"<ul><li>Ensure your geocaching.com credentials have been entered into settings page" +
		"<li>Press <img style='vertical-align: middle' width='20px' src='content_new.png'>" +
		"<li>Tailor creation options" +
		"<li>Press <img style='vertical-align: middle' width='20px' src='content_new.png'> again to create and download the Pocket Query" +
		"</ul>" +
		
		"<h2>Howto Download a Pocket Query</h2>" +

		"<ul><li>Press <img style='vertical-align: middle' width='20px' src='navigation_refresh.png'>" +
		"<li>A list will be shown of Pocket Query that are ready to download" +
		"<li>Select a Pocket Query then <img style='vertical-align: middle' width='20px' src='av_download.png'> to download it" +
		"</ul>" +
		
		"<h2>Working with c:geo</h2>" +

		"c:geo can import zipped pocket queries if they are named a certain way and placed in the correct location" +
		"<ul><li>name - the pocket query zip file must start with numbers. Enable 'c:geo compatability' on the Download settings page to ensure this is always the case" +
		"<li>location - the pocket query is best put into the c:geo 'gpx' import directory (configurable)" +
		"</ul>" +
		
		"<h2>Hints</h2>" +
		"<ul><li>Once created, Pocket Queries can take a while to run" +
		"<li>There is no creation limit, but a maximum of 5 pocket queries will run in any 24 hour period" +
		"<li>The query results will be sent to the email address in your Geocaching.com profile" +
		"<li>The process to create the Pocket Query can take minutes on slow connections" +
		"</ul>" +
		"<h2>FAQ</h2>" +
		"Q. What can I do with the zip files?<br>" +
		"<i>A. This app can't use them. You need another app to use them, like GeoBeagle</i><p>" +
		"Q. Can I extract the PQ attachment from emails?<br>" +
		"<i>A. Use an email client that can (K-9 Mail) or use this app to download the PQ</i><p>" +
		"Q. Can I clear the list of downloadable PQs?<br>" +
		"<i>A. Not using this app. You can delete them at geocaching.com</i>" +
		"</font>";

		wv = (WebView) findViewById(R.id.webview1);
		wv.setBackgroundColor(getResources().getColor(color.black));
		wv.loadDataWithBaseURL("file:///android_asset/", html, mimeType, encoding, "");
		//wv.loadData(html, mimeType, encoding);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent intent = new Intent(this, Main.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
