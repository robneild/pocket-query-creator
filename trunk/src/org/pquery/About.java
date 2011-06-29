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

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

/**
 * Show an HTML about page
 */
public class About extends Activity {

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
		
		"<br><br><h2>Hints</h2>" +
		"<ul><li>Once created, Pocket Queries can take a while to run" +
		"<li>There is no creation limit, but a maximum of 5 pocket queries will run in any 24 hour period" +
		"<li>The query results will be sent to the email address in your Geocaching.com profile" +
		"<li>The process to create the Pocket Query can take minutes on slow connections" +
		"</ul>" +
		
 		"<br><br><h2>Contains</h2>" +
		"<a href='http://jsoup.org'>jsoup</a> - Java HTML Parser<br/>" +
		"<a href='http://www.openclipart.org/detail/89059/push-pin-icon-by-jhnri4'>Push pin icon</a> - openclipart.org<br/>" +
		"<a href='http://www.openclipart.org/detail/5055/old-pocketwatch-by-johnny_automatic'>Old pocketwatch</a> - openclipart.org<br/>" +
		
		"<h2>Acknowledgements</h2>" +
		"The web site <a href='http://geocaching.com'>Geocaching.com</a> is owned by <a href='http://www.groundspeak.com/'>Groundspeak Inc.</a>" +
		
		"<h2>GPLv3 License</h2>" +
		
		"<p>This program is Copyright (C) 2011 Robert Neild<p>" +

		"This program is free software: you can redistribute it and/or modify " +
		"it under the terms of the GNU General Public License as published by " +
		"the Free Software Foundation, either version 3 of the License, or " +
		"(at your option) any later version.<p>" +

		"This program is distributed in the hope that it will be useful, " +
		"but WITHOUT ANY WARRANTY; without even the implied warranty of " + 
		"MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the " +
		"GNU General Public License for more details. " +

		"You should have received a copy of the GNU General Public License " +
		"along with this program.  If not, see " +
		"<a href='http://www.gnu.org/licenses/'>http://www.gnu.org/licenses/</a>." +
		
		"<p>Source code is available here <a href='http://code.google.com/p/pocket-query-creator/'>http://code.google.com/p/pocket-query-creator</a>" +
		"</font>";
		
		wv = (WebView) findViewById(R.id.webview1);
		wv.setBackgroundColor(0);
		wv.loadData(html, mimeType, encoding);

	}
}
