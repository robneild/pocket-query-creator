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

import org.pquery.R;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import android.R.color;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

/**
 * Show an HTML about page
 */
public class About extends SherlockActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
		"li {margin: 5px}" +
		"</style> " +

 		"<h2>Contains</h2>" +
 		"<a href='http://www.openclipart.org/detail/89059/push-pin-icon-by-jhnri4'>Push pin icon</a> - openclipart.org<br/>" +
 		"<a href='http://www.openclipart.org/detail/5055/old-pocketwatch-by-johnny_automatic'>Old pocketwatch</a> - openclipart.org<br/>" +
 		"<a href='http://openclipart.org/detail/164221/ghost-by-arcdroid'>Ghost</a> - openclipart.org<br/>" +
 		"<a href='http://openclipart.org/detail/162601/medicina-by-maoriveros'>Medicina</a> - openclipart.org<br/>" +
 		"<a href='http://openclipart.org/detail/16813/satellite-by-ivak'>satellite</a> - openclipart.org<br/>" +
 		"<a href='http://openclipart.org/detail/44005/treasure-map-by-hextrust'>Treasure Map</a> - openclipart.org<br/>" +
 		"<a href='http://openclipart.org/detail/10941/red-+-green-ok-not-ok-icons-by-tzeeniewheenie-10941'>Red cross</a> - openclipart.org<br/>" +
 		"<a href='http://openclipart.org/detail/10940/red-+-green-ok-not-ok-icons-by-tzeeniewheenie-10940'>Green cross</a> - openclipart.org<br/>" +

	    "<a href='http://openclipart.org/detail/4152/friendly-rabbit-by-danko'>Friendly rabbit</a> - openclipart.org<br/>" +
	    "<a href='http://openclipart.org/detail/170491/gear---options---icon-by-diamonjohn-170491'>Gear - options - icon</a> - openclipart.org<br/>" +
	    "<a href='http://openclipart.org/detail/104977/help-orb-button-by-decosigner'>Help Orb Button </a> - openclipart.org<br/>" +

	    "<a href='http://openclipart.org/detail/9457/rpg-map-symbols:-mountains-by-nicubunu-9457'>Mountains</a> - openclipart.org<br/>" +
	    "<a href='http://openclipart.org/detail/11476/rpg-map-symbols:-maze-by-nicubunu'>Maze</a> - openclipart.org<br/>" +
	    "<a href='http://openclipart.org/detail/33733/info-sign-by-ernes'>Info sign</a> - openclipart.org<br/>" +
	    "<a href='http://openclipart.org/detail/1067/blue-iris-by-aurium'>Blue iris</a> - openclipart.org<br/>" +
	    "<a href='http://openclipart.org/detail/73/hammer-by-rejon'>Hammer</a> - openclipart.org<br/>" +
	    "<br>" +
	    "<a href='http://code.google.com/p/range-seek-bar'>range-seek-bar</a><br>" +
	    "<a href='http://www.bgreco.net/directorypicker/'>Android Directory Picker</a><br>"+
	    "<a href='http://www.gisgraphy.com/gisgraphoid.htm'>Gisgraphoid</a><br>" +
	    "<a href='http://code.google.com/p/google-gson/'>Google Gson</a>" +

		"<h2>Acknowledgements</h2>" +
		"The web site <a href='http://geocaching.com'>Geocaching.com</a> is owned by <a href='http://www.groundspeak.com/'>Groundspeak Inc.</a>" +
		"This application is in no way affiliated or approved by them" +

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

		"<p>Source code is available here <a href='http://code.google.com/p/pocket-query-creator/'>code.google.com/p/pocket-query-creator</a>" +
		"</font>";

        wv = (WebView) findViewById(R.id.webview1);
        wv.setBackgroundColor(getResources().getColor(color.black));
        wv.loadData(html, mimeType, encoding);


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
