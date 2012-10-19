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

import android.R.color;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

/**
 * Show an HTML about page
 */
public class Help extends Activity {

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

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
		"li {margin: 5px}" +
		"</style> " +
		
		"This app allows the quick creation of " +
		"<a href='http://www.geocaching.com/pocket/'>Pocket Queries</a>. " +
		"They are provided to premium members of the website " +
		"<a href='http://www.geocaching.com'>Geocaching.com</a>." +
		"<br>This app does nothing that can't be done at the Geocaching.com website. It just acts as a convienience." +
	
		"<p>You can't go geocaching with just this app. Try <a href='market://search?q=pname:com.google.code.geobeagle'>geobeagle</a>"+
		" or <a href='market://search?q=pname:com.groundspeak.geocaching'>the official client</a>" +
		
		"<p>Don't hesitate to <a href='mailto:s1@bigbob.org.uk?subject=PocketQueryCreator'>contact me</a> with comments or errors" +
		
		"<br><br><h2>Hints</h2>" +
		"<ul><li>Once created, Pocket Queries can take a while to run" +
		"<li>There is no creation limit, but a maximum of 5 pocket queries will run in any 24 hour period" +
		"<li>The query results will be sent to the email address in your Geocaching.com profile" +
		"<li>The process to create the Pocket Query can take minutes on slow connections" +
		"</ul>" +
		"</font>";
		
        wv = (WebView) findViewById(R.id.webview1);
        wv.setBackgroundColor(getResources().getColor(color.black));
        wv.loadData(html, mimeType, encoding);

	}
}
