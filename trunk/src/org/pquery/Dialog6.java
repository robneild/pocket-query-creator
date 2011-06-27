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
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

/**
 * Show an HTML about page
 */
public class Dialog6 extends Activity {

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		setContentView(R.layout.dialog6);

		
		Button cancelButton = (Button) findViewById(R.id.button_ok);

		cancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				finish();
			}
		});
		
		
		final String mimeType = "text/html";
		final String encoding = "utf-8";

		WebView wv;

		String html = "<font color='#ffffff'>" + 
		
		"<style type='text/css'>" +
		"a:link {color: #0066FF; text-decoration: underline; }" +
		"a:active {color: #0066FF; text-decoration: underline; }" +
		"a:visited {color: #0066FF; text-decoration: underline; }" +
		"a:hover {color: #0066FF; text-decoration: underline; }" +
		"</style>";
		
		html += "<h3>Geocaching.com creation message</h3>";
		html +=	getIntent().getExtras().getString("html");
		
		wv = (WebView) findViewById(R.id.webview1);
		wv.setBackgroundColor(0);
		wv.loadData(html, mimeType, encoding);

	}
}
