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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.pquery.service.PQService;
import org.pquery.util.IOUtils;
import org.pquery.util.Logger;
import org.pquery.util.Prefs;

import com.gisgraphy.domain.valueobject.StreetSearchResultsDto;
import com.google.gson.Gson;

import android.app.Activity;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class PocketQuery extends ListActivity {

	/** Messenger for communicating with service. */
	private Messenger messengerOut;
	
//
//	private static final Integer[] OPTIONS_CREATING = new Integer[] { R.string.creation_pending, R.string.settings,
//			R.string.about };
//	private static final Integer[] OPTIONS = new Integer[] { R.string.create_new_query, R.string.settings,
//			R.string.about };
//	private static final int[] OPTIONS_BAD_HTML_RESPONSE = new int[] { R.string.create_new_query, R.string.settings,
//			R.string.about, R.string.view_bad_response, R.string.email_bad_response };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Logger.setEnable(Prefs.getDebug(getApplicationContext()));
		
		String bob = "{\"numFound\":50,\"QTime\":252,\"result\":[{\"name\":\"Newton Street\",\"distance\":36.3294330748716,\"gid\":106603218,\"openstreetmapId\":16978709,\"streetType\":\"RESIDENTIAL\",\"oneWay\":false,\"countryCode\":\"US\",\"length\":601.901637459321,\"lat\":39.83543539626998,\"lng\":-105.03742540092145,\"isIn\":\"Westminster\"},{\"name\":\"West 77th Avenue\",\"distance\":37.8060462099577,\"gid\":119753435,\"openstreetmapId\":16972586,\"streetType\":\"RESIDENTIAL\",\"oneWay\":false,\"countryCode\":\"US\",\"length\":533.566536859547,\"lat\":39.83633797787614,\"lng\":-105.03743049997163,\"isIn\":\"Westminster\"},{\"name\":\"Meade Street\",\"distance\":96.2865779913116,\"gid\":106576997,\"openstreetmapId\":16975922,\"streetType\":\"RESIDENTIAL\",\"oneWay\":false,\"countryCode\":\"US\",\"length\":529.223829706705,\"lat\":39.83691930475007,\"lng\":-105.0358697923325,\"isIn\":\"Westminster\"},{\"name\":\"West 76th Avenue\",\"distance\":162.248276928221,\"gid\":141128889,\"openstreetmapId\":87332518,\"streetType\":\"RESIDENTIAL\",\"oneWay\":false,\"countryCode\":\"US\",\"length\":2281.26989410674,\"lat\":39.83453648264977,\"lng\":-105.03857538150032,\"isIn\":\"Westminster\"},{\"name\":\"Osceola Street\",\"distance\":170.393217830008,\"gid\":119733942,\"openstreetmapId\":16968438,\"streetType\":\"RESIDENTIAL\",\"oneWay\":false,\"countryCode\":\"US\",\"length\":624.767148578469,\"lat\":39.83735494853388,\"lng\":-105.03897021628774,\"isIn\":\"Westminster\"},{\"name\":\"Meade Way\",\"distance\":170.948974478088,\"gid\":106569361,\"openstreetmapId\":16975172,\"streetType\":\"RESIDENTIAL\",\"oneWay\":false,\"countryCode\":\"US\",\"length\":141.848495967086,\"lat\":39.833588645953476,\"lng\":-105.03630364443788,\"isIn\":\"Harris Park\"},{\"name\":\"Wilson Court\",\"distance\":215.022213118766,\"gid\":106566260,\"openstreetmapId\":16973881,\"streetType\":\"RESIDENTIAL\",\"oneWay\":false,\"countryCode\":\"US\",\"length\":606.194080271035,\"lat\":39.83180779391467,\"lng\":-105.0353548324345,\"isIn\":\"Harris Park\"},{\"name\":\"Lowell Boulevard\",\"distance\":230.050096618924,\"gid\":101539901,\"openstreetmapId\":37356579,\"streetType\":\"TERTIARY\",\"oneWay\":false,\"countryCode\":\"US\",\"length\":6815.50298905347,\"lat\":39.854518940562755,\"lng\":-105.0343592,\"isIn\":\"Homestead Heights\"},{\"name\":\"Osceola Street\",\"distance\":235.181052332335,\"gid\":119733952,\"openstreetmapId\":16968464,\"streetType\":\"RESIDENTIAL\",\"oneWay\":false,\"countryCode\":\"US\",\"length\":201.40880633239,\"lat\":39.83362935,\"lng\":-105.038995,\"isIn\":\"Harris Park\"},{\"name\":\"Maria Street\",\"distance\":238.722692346446,\"gid\":146183339,\"openstreetmapId\":16963425,\"streetType\":\"RESIDENTIAL\",\"oneWay\":false,\"countryCode\":\"US\",\"length\":301.116292775805,\"lat\":39.83963342770102,\"lng\":-105.03752696407791,\"isIn\":\"Westminster\"},{\"name\":\"Maria Street\",\"distance\":238.722692346446,\"gid\":104919999,\"openstreetmapId\":16963425,\"streetType\":\"RESIDENTIAL\",\"oneWay\":false,\"countryCode\":\"US\",\"length\":301.116292775805,\"lat\":39.83963342770102,\"lng\":-105.03752696407791,\"isIn\":\"Westminster\"},{\"name\":\"Mc Cella Court\",\"distance\":250.382216381217,\"gid\":146184679,\"openstreetmapId\":16963853,\"streetType\":\"RESIDENTIAL\",\"oneWay\":false,\"countryCode\":\"US\",\"length\":146.288542740383,\"lat\":39.839005860556334,\"lng\":-105.03813043682833,\"isIn\":\"Westminster\"},{\"name\":\"Mc Cella Court\",\"distance\":250.382216381217,\"gid\":104920392,\"openstreetmapId\":16963853,\"streetType\":\"RESIDENTIAL\",\"oneWay\":false,\"countryCode\":\"US\",\"length\":146.288542740383,\"lat\":39.839005860556334,\"lng\":-105.03813043682833,\"isIn\":\"Westminster\"},{\"name\":\"Bradburn Boulevard\",\"distance\":303.323409807335,\"gid\":106602511,\"openstreetmapId\":16977948,\"streetType\":\"RESIDENTIAL\",\"oneWay\":false,\"countryCode\":\"US\",\"length\":1613.92986674489,\"lat\":39.83454381659219,\"lng\":-105.040555,\"isIn\":\"Harris Park\"},{\"distance\":311.459214332218,\"gid\":141128890,\"openstreetmapId\":87332521,\"streetType\":\"FOOTWAY\",\"oneWay\":false,\"countryCode\":\"US\",\"length\":46.1276568636799,\"lat\":39.83429721538374,\"lng\":-105.03402992689894,\"isIn\":\"Westminster\"},{\"name\":\"West 77th Place\",\"distance\":315.904469020059,\"gid\":106565935,\"openstreetmapId\":16973488,\"streetType\":\"RESIDENTIAL\",\"oneWay\":false,\"countryCode\":\"US\",\"length\":167.518878778475,\"lat\":39.836793,\"lng\":-105.04154964079349,\"isIn\":\"Hidden Creek Park\"},{\"distance\":319.916045684511,\"gid\":141128891,\"openstreetmapId\":87332522,\"streetType\":\"FOOTWAY\",\"oneWay\":false,\"countryCode\":\"US\",\"length\":12.0878616860677,\"lat\":39.834304065978834,\"lng\":-105.0337569336683,\"isIn\":\"Westminster\"},{\"name\":\"Turnpike Drive\",\"distance\":328.726869118765,\"gid\":119742072,\"openstreetmapId\":16969982,\"streetType\":\"RESIDENTIAL\",\"oneWay\":false,\"countryCode\":\"US\",\"length\":564.169430689873,\"lat\":39.83982669916193,\"lng\":-105.03684307349621,\"isIn\":\"Westminster\"},{\"name\":\"King Street\",\"distance\":328.875033016736,\"gid\":106577153,\"openstreetmapId\":16976086,\"streetType\":\"RESIDENTIAL\",\"oneWay\":false,\"countryCode\":\"US\",\"length\":235.391131630421,\"lat\":39.83561455,\"lng\":-105.03315305000001,\"isIn\":\"Westminster\"},{\"name\":\"West 77th Place\",\"distance\":337.638803349772,\"gid\":106565933,\"openstreetmapId\":16973485,\"streetType\":\"RESIDENTIAL\",\"oneWay\":false,\"countryCode\":\"US\",\"length\":111.760199951475,\"lat\":39.837265565623305,\"lng\":-105.03286623690238,\"isIn\":\"Westminster\"},{\"distance\":341.141442963996,\"gid\":137203762,\"openstreetmapId\":87332516,\"streetType\":\"FOOTWAY\",\"oneWay\":false,\"countryCode\":\"US\",\"length\":47.7633656305911,\"lat\":39.83430556246034,\"lng\":-105.03344727483474,\"isIn\":\"Westminster\"},{\"name\":\"Turnpike Drive\",\"distance\":354.45184004779,\"gid\":119742070,\"openstreetmapId\":16969980,\"streetType\":\"RESIDENTIAL\",\"oneWay\":false,\"countryCode\":\"US\",\"length\":1095.86913070988,\"lat\":39.83658591151863,\"lng\":-105.0278442335032,\"isIn\":\"Westminster\"},{\"distance\":354.947040314687,\"gid\":141128886,\"openstreetmapId\":87332517,\"streetType\":\"FOOTWAY\",\"oneWay\":false,\"countryCode\":\"US\",\"length\":67.5052044493065,\"lat\":39.83405637323205,\"lng\":-105.03322312690949,\"isIn\":\"Westminster\"},{\"name\":\"West 75th Avenue\",\"distance\":363.655947602489,\"gid\":106569542,\"openstreetmapId\":16975368,\"streetType\":\"RESIDENTIAL\",\"oneWay\":false,\"countryCode\":\"US\",\"length\":931.167423152406,\"lat\":39.83272202592744,\"lng\":-105.0397695000568,\"isIn\":\"Harris Park\"},{\"name\":\"Newton Way\",\"distance\":364.523535494889,\"gid\":119752813,\"openstreetmapId\":16971981,\"streetType\":\"RESIDENTIAL\",\"oneWay\":false,\"countryCode\":\"US\",\"length\":141.796893133834,\"lat\":39.83197635389033,\"lng\":-105.03703930394362,\"isIn\":\"Harris Park\"},{\"name\":\"Newton Street\",\"distance\":370.261828632225,\"gid\":106603186,\"openstreetmapId\":16978689,\"streetType\":\"RESIDENTIAL\",\"oneWay\":false,\"countryCode\":\"US\",\"length\":404.924359526038,\"lat\":39.83096273217169,\"lng\":-105.03666728209846,\"isIn\":\"Harris Park\"},{\"name\":\"Orchard Court\",\"distance\":378.11321343763,\"gid\":146199236,\"openstreetmapId\":16964235,\"streetType\":\"RESIDENTIAL\",\"oneWay\":false,\"countryCode\":\"US\",\"length\":404.087286583141,\"lat\":39.830912999999995,\"lng\":-105.038213,\"isIn\":\"Harris Park\"},{\"name\":\"Orchard Court\",\"distance\":378.11321343763,\"gid\":104920730,\"openstreetmapId\":16964235,\"streetType\":\"RESIDENTIAL\",\"oneWay\":false,\"countryCode\":\"US\",\"length\":404.087286583141,\"lat\":39.830912999999995,\"lng\":-105.038213,\"isIn\":\"Harris Park\"},{\"name\":\"Denver Boulder Turnpike\",\"distance\":393.298644091346,\"gid\":121553040,\"openstreetmapId\":39994874,\"streetType\":\"MOTORWAY\",\"oneWay\":false,\"countryCode\":\"US\",\"length\":42.6681882195882,\"lat\":39.83886375,\"lng\":-105.03425419999999,\"isIn\":\"Westminster\"},{\"name\":\"Perry Place\",\"distance\":397.951848369869,\"gid\":106609150,\"openstreetmapId\":16979562,\"streetType\":\"RESIDENTIAL\",\"oneWay\":false,\"countryCode\":\"US\",\"length\":151.318622964413,\"lat\":39.8374734009995,\"lng\":-105.04162065680337,\"isIn\":\"Hidden Creek Park\"},{\"name\":\"Denver Boulder Turnpike\",\"distance\":398.822101983356,\"gid\":121553039,\"openstreetmapId\":39994873,\"streetType\":\"MOTORWAY\",\"oneWay\":false,\"countryCode\":\"US\",\"length\":2550.42531833867,\"lat\":39.84700337516864,\"lng\":-105.04465949557078,\"isIn\":\"Hidden Creek Park\"},{\"name\":\"Denver Boulder Turnpike\",\"distance\":399.21061928888,\"gid\":111174812,\"openstreetmapId\":16968708,\"streetType\":\"MOTORWAY\",\"oneWay\":false,\"countryCode\":\"US\",\"length\":4155.89675141663,\"lat\":39.83200803852257,\"lng\":-105.01102434593933,\"isIn\":\"Fairview\"},{\"name\":\"Denver Boulder Turnpike\",\"distance\":407.472487070849,\"gid\":121553032,\"openstreetmapId\":39994871,\"streetType\":\"MOTORWAY\",\"oneWay\":false,\"countryCode\":\"US\",\"length\":39.6624382229874,\"lat\":39.839033400000005,\"lng\":-105.0342914,\"isIn\":\"Westminster\"},{\"name\":\"Denver Boulder Turnpike\",\"distance\":411.929665434682,\"gid\":138726761,\"openstreetmapId\":118357033,\"streetType\":\"MOTORWAY\",\"oneWay\":false,\"countryCode\":\"US\",\"length\":2956.71997612823,\"lat\":39.83422238521825,\"lng\":-105.01789291049698,\"isIn\":\"Fairview\"},{\"name\":\"Denver Boulder Turnpike\",\"distance\":416.087166360085,\"gid\":121553038,\"openstreetmapId\":39994872,\"streetType\":\"MOTORWAY\",\"oneWay\":false,\"countryCode\":\"US\",\"length\":2435.5264522607,\"lat\":39.84679130850932,\"lng\":-105.04425301759433,\"isIn\":\"Hidden Creek Park\"},{\"name\":\"Knox Court\",\"distance\":418.323823917304,\"gid\":106589137,\"openstreetmapId\":16977642,\"streetType\":\"RESIDENTIAL\",\"oneWay\":false,\"countryCode\":\"US\",\"length\":368.523592921472,\"lat\":39.836206849971404,\"lng\":-105.03210172373953,\"isIn\":\"Westminster\"},{\"name\":\"Osceola Street\",\"distance\":418.625766062279,\"gid\":119733968,\"openstreetmapId\":16968470,\"streetType\":\"RESIDENTIAL\",\"oneWay\":false,\"countryCode\":\"US\",\"length\":202.241629047725,\"lat\":39.8318134,\"lng\":-105.03941950000001,\"isIn\":\"Harris Park\"},{\"name\":\"West 75th Place\",\"distance\":420.494421795799,\"gid\":106602357,\"openstreetmapId\":16977777,\"streetType\":\"RESIDENTIAL\",\"oneWay\":false,\"countryCode\":\"US\",\"length\":88.1241865185316,\"lat\":39.833833,\"lng\":-105.032453,\"isIn\":\"Westminster\"},{\"name\":\"King Street\",\"distance\":420.494421795799,\"gid\":106577154,\"openstreetmapId\":16976087,\"streetType\":\"RESIDENTIAL\",\"oneWay\":false,\"countryCode\":\"US\",\"length\":117.216067693355,\"lat\":39.833302,\"lng\":-105.0329805,\"isIn\":\"Harris Park\"},{\"name\":\"West 75th Avenue\",\"distance\":425.890595169112,\"gid\":106569541,\"openstreetmapId\":16975367,\"streetType\":\"RESIDENTIAL\",\"oneWay\":false,\"countryCode\":\"US\",\"length\":161.825508030565,\"lat\":39.832773869908905,\"lng\":-105.03336634267458,\"isIn\":\"Harris Park\"},{\"name\":\"Quitman Street\",\"distance\":437.167806866312,\"gid\":104949359,\"openstreetmapId\":16965348,\"streetType\":\"RESIDENTIAL\",\"oneWay\":false,\"countryCode\":\"US\",\"length\":251.080943667231,\"lat\":39.835664,\"lng\":-105.0421245,\"isIn\":\"Hidden Creek Park\"},{\"name\":\"Quitman Street\",\"distance\":437.167806866312,\"gid\":146206608,\"openstreetmapId\":16965348,\"streetType\":\"RESIDENTIAL\",\"oneWay\":false,\"countryCode\":\"US\",\"length\":251.080943667231,\"lat\":39.835664,\"lng\":-105.0421245,\"isIn\":\"Hidden Creek Park\"},{\"name\":\"Knox Court\",\"distance\":462.076329277001,\"gid\":106589145,\"openstreetmapId\":16977645,\"streetType\":\"RESIDENTIAL\",\"oneWay\":false,\"countryCode\":\"US\",\"length\":345.113827384838,\"lat\":39.83291487030559,\"lng\":-105.03151854902629,\"isIn\":\"Harris Park\"},{\"name\":\"Quitman Street\",\"distance\":464.91597301777,\"gid\":146206600,\"openstreetmapId\":16965338,\"streetType\":\"RESIDENTIAL\",\"oneWay\":false,\"countryCode\":\"US\",\"length\":249.524228021499,\"lat\":39.83341092761972,\"lng\":-105.042209966926,\"isIn\":\"Harris Park\"},{\"name\":\"Quitman Street\",\"distance\":464.91597301777,\"gid\":104949351,\"openstreetmapId\":16965338,\"streetType\":\"RESIDENTIAL\",\"oneWay\":false,\"countryCode\":\"US\",\"length\":249.524228021499,\"lat\":39.83341092761972,\"lng\":-105.042209966926,\"isIn\":\"Harris Park\"},{\"name\":\"Meade Street\",\"distance\":469.357893249776,\"gid\":106577001,\"openstreetmapId\":16975926,\"streetType\":\"RESIDENTIAL\",\"oneWay\":false,\"countryCode\":\"US\",\"length\":189.411427541269,\"lat\":39.84095312584505,\"lng\":-105.0357890723592,\"isIn\":\"Westminster\"},{\"name\":\"Quitman Street\",\"distance\":471.749343858084,\"gid\":146206607,\"openstreetmapId\":16965347,\"streetType\":\"RESIDENTIAL\",\"oneWay\":false,\"countryCode\":\"US\",\"length\":148.320494445618,\"lat\":39.837470509485016,\"lng\":-105.0425728897227,\"isIn\":\"Hidden Creek Park\"},{\"name\":\"Quitman Street\",\"distance\":471.749343858084,\"gid\":104949358,\"openstreetmapId\":16965347,\"streetType\":\"RESIDENTIAL\",\"oneWay\":false,\"countryCode\":\"US\",\"length\":148.320494445618,\"lat\":39.837470509485016,\"lng\":-105.0425728897227,\"isIn\":\"Hidden Creek Park\"},{\"name\":\"Quitman Street\",\"distance\":494.706805006784,\"gid\":146206604,\"openstreetmapId\":16965345,\"streetType\":\"RESIDENTIAL\",\"oneWay\":false,\"countryCode\":\"US\",\"length\":405.528536683426,\"lat\":39.83997353657685,\"lng\":-105.04206036153033,\"isIn\":\"Hidden Creek Park\"},{\"name\":\"Quitman Street\",\"distance\":494.706805006784,\"gid\":104949355,\"openstreetmapId\":16965345,\"streetType\":\"RESIDENTIAL\",\"oneWay\":false,\"countryCode\":\"US\",\"length\":405.528536683426,\"lat\":39.83997353657685,\"lng\":-105.04206036153033,\"isIn\":\"Hidden Creek Park\"}]}";
		Object returnObjects = new Gson().fromJson(bob, StreetSearchResultsDto.class);
		 
		//setContentView(R.layout.list_item);
		getListView().setTextFilterEnabled(true);
		
		// List listener

		getListView().setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				if (position == 0) {

					if (messengerOut == null) {
						SharedPreferences prefs = PreferenceManager
								.getDefaultSharedPreferences(getApplicationContext());
						String username = prefs.getString("username_preference", "");
						String password = prefs.getString("password_preference", "");

						if (username != null && username.length() > 0 && password != null && password.length() > 0) {
						    startActivity(new Intent(view.getContext(), Dialog1.class));
						} else {
							Toast.makeText(getApplicationContext(), "Enter your credentials on the settings page",
									Toast.LENGTH_LONG).show();
						}
					} else {

						//startActivity(new Intent(view.getContext(), Detail.class));

					}
				}

				if (position == 1) {
					Intent myIntent = new Intent(getApplicationContext(), PreferencesFromXml.class);
					startActivity(myIntent);
				}


                if (position == 2) {
                    Intent myIntent = new Intent(getApplicationContext(), Help.class);
                    startActivity(myIntent);
                }
                
				if (position == 3) {
					Intent myIntent = new Intent(getApplicationContext(), About.class);
					startActivity(myIntent);
				}

				if (position == 4) {
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setDataAndType(Uri.fromFile(Logger.getLogFileName()), "text/html");
					startActivity(intent);
				}

				if (position == 5) {

					Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

					String[] recipients = new String[] { "s1@bigbob.org.uk", "", };

					emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, recipients);
					emailIntent.putExtra(android.content.Intent.EXTRA_STREAM,
							Uri.fromFile(Logger.getLogFileName()));
					emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "PocketQueryCreator log file");
					emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "log file");
					emailIntent.setType("text/html");
					startActivity(Intent.createChooser(emailIntent, "Send mail..."));
				}

			}
		});
	}

	@Override
	protected void onStart() {
	    super.onStart();
		doBindService();
		updateList();
	}
	
	@Override
	protected void onStop() {
	    super.onStop();
		doUnbindService();
	}

	private String[] getLocalizedArray(int[] resourceArray) {
		String[] ret = new String[resourceArray.length];
		for (int i = 0; i < resourceArray.length; i++) {
			ret[i] = getResources().getString(resourceArray[i]);
		}
		return ret;
	}

	private boolean isServiceBound;

	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder service) {
			Logger.d("enter");
			messengerOut = new Messenger(service);
			updateList();
			
			//Toast.makeText(getApplicationContext(), "Connect", Toast.LENGTH_LONG).show();
		}

		public void onServiceDisconnected(ComponentName className) {
			Logger.d("enter");
			messengerOut = null;
			updateList();
			
			//Toast.makeText(getApplicationContext(), "Disconnect", Toast.LENGTH_LONG).show();
		}
	};

	void doBindService() {
		isServiceBound = bindService(new Intent(getApplicationContext(), PQService.class), mConnection, 0);
		Logger.d("[isServiceBound=" + isServiceBound + "]");
	}

	void doUnbindService() {
		Logger.d("enter");
		if (isServiceBound) {
			unbindService(mConnection);
			isServiceBound = false;
			messengerOut = null;
		}
	}

	private void updateList() {
		Logger.d("[messengerOut="+messengerOut+"]");
		ArrayList <Integer> list = new ArrayList<Integer>();
		if (messengerOut == null)
			list.add(R.string.create_new_query);
		else
			list.add(R.string.creation_pending);
		
		list.add(R.string.settings);
		list.add(R.string.help);
		list.add(R.string.about);
		
		if (Prefs.getDebug(getApplicationContext())) {
			list.add(R.string.view_bad_response);
			list.add(R.string.email_bad_response);
		}
		
		setListAdapter(new IconicAdapter(this, list.toArray(new Integer[0])));
	}

	class IconicAdapter extends ArrayAdapter<Integer> {

		Activity context;

		IconicAdapter(Activity context, Integer[] itemList) {
			super(context, R.layout.main, itemList);
			this.context = context;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = context.getLayoutInflater();

			View row = inflater.inflate(R.layout.list_row, parent, false);

			TextView label = (TextView) row.findViewById(R.id.label);
			label.setText(getItem(position));

			ImageView icon = (ImageView) row.findViewById(R.id.icon);

			switch (getItem(position)) {
			case R.string.create_new_query:
				icon.setImageResource(R.drawable.hammer);
				break;
			case R.string.creation_pending:
				icon.setImageResource(R.drawable.eye);
				break;
			case R.string.settings:
				icon.setImageResource(R.drawable.settings);
				break;
			case R.string.help:
				icon.setImageResource(R.drawable.help);
				break;
			case R.string.about:
				icon.setImageResource(R.drawable.about);
				break;
			}

			return (row);
		}
	}
}
