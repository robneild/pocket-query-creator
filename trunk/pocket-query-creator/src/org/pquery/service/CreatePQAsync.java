package org.pquery.service;

import java.io.File;
import java.util.List;

import junit.framework.Assert;

import net.htmlparser.jericho.FormFields;
import net.htmlparser.jericho.Source;

import org.apache.http.cookie.Cookie;
import org.apache.http.message.BasicNameValuePair;
import org.pquery.QueryStore;
import org.pquery.R;
import org.pquery.dao.PQ;
import org.pquery.filter.CacheType;
import org.pquery.filter.CacheTypeList;
import org.pquery.filter.CheckBoxesFilter;
import org.pquery.filter.ContainerType;
import org.pquery.filter.ContainerTypeList;
import org.pquery.filter.OneToFiveFilter;
import org.pquery.util.GPS;
import org.pquery.util.Logger;
import org.pquery.util.Prefs;
import org.pquery.webdriver.CancelledListener;
import org.pquery.webdriver.CreateOutputFileTask;
import org.pquery.webdriver.DownloadTask;
import org.pquery.webdriver.FailurePermanentException;
import org.pquery.webdriver.ProgressInfo;
import org.pquery.webdriver.ProgressListener;
import org.pquery.webdriver.RetrievePageTask;
import org.pquery.webdriver.SubmitFormPageTask;
import org.pquery.webdriver.parser.FormFieldsExtra;
import org.pquery.webdriver.parser.GeocachingPage;
import org.pquery.webdriver.parser.ParseException;
import org.pquery.webdriver.parser.SuccessMessageParser;

import android.content.Context;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;

public class CreatePQAsync extends AsyncTask<Void, ProgressInfo, CreatePQResult> implements LocationListener, CancelledListener, ProgressListener{

	private Resources res;
	private Context cxt;

	private QueryStore queryStore;
	private LocationManager locationManager;
	private Location gpsLocation;
	private List<Cookie> cookies;
	private int retryCount;
	private boolean gpsOn;

	public CreatePQAsync(Context cxt, QueryStore queryStore, LocationManager locationManager) {
		Assert.assertNotNull(queryStore);

		this.res = cxt.getResources();
		this.cxt = cxt;
		this.queryStore = queryStore;
		this.locationManager = locationManager;
		this.cookies = Prefs.getCookies(cxt);
		this.retryCount = Prefs.getRetryCount(cxt);

		Logger.cookie("Cookies retrieve from prefs", cookies);
	}

	private void checkCancelled() throws InterruptedException {
		if (isCancelled()) {
			Logger.d("Detected a cancellation. Manually throwing InterruptedException");
			throw new InterruptedException();
		}
	}

	private void startGPS() {
		Logger.d("Turning GPS on");
		GPS.requestLocationUpdates(locationManager, this);
		gpsOn = true;
	}

	private void stopGPS() {
		Logger.d("Turning GPS off");
		GPS.stopLocationUpdate(locationManager, this);
		gpsOn = false;
	}

	/**
	 * Called on UI thread before we start
	 */
	@Override
	protected void onPreExecute() {
		// Turn on GPS if location not passed in
		// Needs to be done here, on UI thread
		if (!queryStore.haveLocation()) {
			Logger.d("Starting gps");
			publishProgress(new ProgressInfo(1, res.getString((R.string.gps_wait))));
			startGPS();
		}
	}

	//    @Override
	//    protected void onPostExecute(ResultInfo result) {
	//        super.onPostExecute(result);
	//        Prefs.saveCookies(cxt, cookies);
	//        Logger.cookie("Saving cookies to prefs", cookies);
	//    }

	@Override
	protected CreatePQResult doInBackground(Void... params) {

		Logger.d("enter");

		try {
			// If GPS on, wait for fix
			// Convert retry count into max minutes to wait for GPS (bit dodgy)

			if (gpsOn) {        // turn on in onPreExecute
				try {
					if (!waitForGPS(retryCount + 1))
						return new CreatePQResult(new FailurePermanentException(res.getString(R.string.gps_timeout)));
				} finally {
					stopGPS();
				}
			}

			Logger.d("Got location fix ok");

			// Optionally try to resolve location into a name
			doLookupLocationName();





			// Login 5 - 30%
			RetrievePageTask loginTask = new RetrievePageTask(retryCount,5, 30, this, this, cxt, "/pocket/gcquery.aspx");
			Source parsedHtml = loginTask.call();

			publishProgress(new ProgressInfo(30, "Processing form"));
			
			GeocachingPage createPage = new GeocachingPage(parsedHtml);
			List<BasicNameValuePair> form = fillInCreateForm(createPage);





			// 30% - 60%
			SubmitFormPageTask submitTask = new SubmitFormPageTask(form, retryCount, 30, 60, this, this, cxt, "/pocket/gcquery.aspx");
			String html = submitTask.call();


			SuccessMessageParser successMessageParser = new SuccessMessageParser(html);


			Logger.d("[successMessage=" + successMessageParser.successMessage + "]");

			if (!Prefs.getDownload(cxt) || Prefs.getDisabled(cxt) || successMessageParser.extractNumberPQ()==0) {
				// We we aren't downloading or creating disabled then can finish
				// now
				return new CreatePQResult(successMessageParser.toString(res));
			}

			// 60% - 70%

			checkCancelled();
			publishProgress(new ProgressInfo(60, successMessageParser.toString(res)
					+ "<br><br>Waiting 30 seconds to allow Pocket Query to run"));
			Thread.sleep(30000);

			String guid = successMessageParser.extractDownloadGuid();

			PQ pq = new PQ();
			pq.url = "/pocket/downloadpq.ashx?g=" + guid;
			pq.name = queryStore.name;

			// Download PQ

			CreateOutputFileTask createTask = new CreateOutputFileTask(retryCount, 70, 75, this, this, cxt, pq.name);
			File outputFile = createTask.call();

			DownloadTask downloadTask = new DownloadTask(retryCount, 75, 100, this, this, cxt, pq.url, outputFile);
			Integer bytesDownloaded = downloadTask.call();

			return new CreatePQResult(new DownloadPQResult(bytesDownloaded, outputFile));

		} catch (InterruptedException e) {
			// Probably user cancelled and async.stop has been called
			// Service onPostExecute won't be called so our return FailureResult
			// will be ignored
			Logger.d("Interrupted");
			return null;
		} catch (FailurePermanentException e) {
			return new CreatePQResult(e);
		}

	}

	/**
	 * Loops around waiting for queryStore location to be set
	 * Should be set by GPS callback
	 */
	private boolean waitForGPS(int minutesToWait) throws InterruptedException {
		Logger.d("enter [minutesToWait="+minutesToWait+"]");
		// Check if have GPS fix every 5 seconds
		for (int i = 0; i < minutesToWait * 12; i++) {
			checkCancelled();
			if (queryStore.haveLocation()) {
				Thread.sleep(5000); // wait a few extra seconds when have fix to
				// give a chance to get better
				return true;
			}
			Thread.sleep(5000);
		}
		Logger.e("Waiting too long");
		return false;
	}

	/**
	 * If pref is set, try to resolve location into a human name using geocoder
	 */
	private void doLookupLocationName() {
		if (Prefs.isAutoName(cxt)) {
			Logger.d("Doing lookup");
			String name = GPS.getLocality(cxt, queryStore.lat, queryStore.lon);
			if (name != null) {
				queryStore.name = name;
				Logger.d("Resolved location to name ok [" + name + "]");
			}
		}
	}

	//  public Failure doLogin() throws FailureDontRetry {
	//
	//      Logger.d("enter");
	//
	//      // Get preferences
	//
	//      String username = Prefs.getUsername(cxt);
	//      String password = Prefs.getPassword(cxt);
	//
	//      String html = "";
	//      DefaultHttpClient client = null;
	//
	//      try {
	//          publishProgress(new ProgressInfo(5, res.getString(R.string.login_detect)));
	//
	//          client = new DefaultHttpClient();
	//
	//          for (Cookie c : cookies) {
	//              Logger.d("restored cookie " + c);
	//              client.getCookieStore().addCookie(c);
	//          }
	//
	//          // Get the pocket query creation page
	//          // and read the response. Need to detect if logged in or no
	//
	//          try {
	//              html = IOUtils.httpGet(client, "pocket/gcquery.aspx", new Listener() {
	//
	//                  @Override
	//                  public void update(int bytesReadSoFar, int expectedLength) {
	//                      publishProgress(new ProgressInfo((int) 5 + (bytesReadSoFar * 12 / expectedLength), res
	//                              .getString(R.string.login_detect))); // 5-17%
	//                  }
	//              });
	//
	//              // Retrieve and store cookies in reply
	//
	//              cookies = client.getCookieStore().getCookies();
	//
	//          } catch (IOException e) {
	//              Logger.e("Exception downloading login page", e);
	//              return new Failure(res.getString(R.string.login_download_fail), e);
	//          }
	//
	//          // Parse the response
	//
	//          Parser parse = new Parser(html);
	//          queryStore.viewStateMap = parse.extractViewState();
	//
	//          // Check for a completely wrong page returned that doesn't mention
	//          // geocaching
	//          // Likely to be a wifi login page
	//          if (!parse.detectGeocachingCom()) {
	//              return new Failure(res.getString(R.string.login_page_wrong));
	//          }
	//
	//          // Check the response. Detecting login and premium state
	//          if (parse.isLoggedIn()) {
	//              if (!parse.isPremium())
	//                  throw new FailureDontRetry("You aren't a premium member. Goto Geocaching.com and upgrade");
	//
	//              Logger.d("Detected already logged in");
	//              return null; // All ok. Cookies must be ok and already logged in
	//          }
	//
	//          publishProgress(new ProgressInfo(18, res.getString(R.string.login_geocaching_com)));
	//
	//          // Extract an extra field that the un-logged in pocket query page
	//          // seems to have
	//
	//          String PREVIOUS_PAGE = "name=\"__PREVIOUSPAGE\" id=\"__PREVIOUSPAGE\" value=\"";
	//
	//          int start = html.indexOf(PREVIOUS_PAGE);
	//          int end = html.indexOf("\"", start + PREVIOUS_PAGE.length());
	//
	//          queryStore.viewStateMap.put("__PREVIOUSPAGE", html.substring(start + PREVIOUS_PAGE.length(), end));
	//
	//          // We need to login
	//          // Create the POST
	//
	//          List<BasicNameValuePair> paramList = new ArrayList<BasicNameValuePair>();
	//
	//          paramList.add(new BasicNameValuePair("__EVENTTARGET", ""));
	//          paramList.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
	//          paramList.add(new BasicNameValuePair("__VIEWSTATEFIELDCOUNT", "" + queryStore.viewStateMap.size()));
	//
	//          for (Map.Entry<String, String> entry : queryStore.viewStateMap.entrySet()) {
	//              paramList.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
	//          }
	//
	//          // Fill in the form values
	//
	//          paramList.add(new BasicNameValuePair("ctl00$tbUsername", username));
	//          paramList.add(new BasicNameValuePair("ctl00$tbPassword", password));
	//          paramList.add(new BasicNameValuePair("ctl00$cbRememberMe", "on"));
	//          paramList.add(new BasicNameValuePair("ctl00$btnSignIn", "Sign In"));
	//
	//          try {
	//              html = IOUtils.httpPost(client, paramList, "login/default.aspx?redir=%2fpocket%2fgcquery.aspx%3f",
	//                      true, new Listener() {
	//
	//                          @Override
	//                          public void update(int bytesReadSoFar, int expectedLength) {
	//                              publishProgress(new ProgressInfo((int) (18 + (bytesReadSoFar * 12 / expectedLength)),
	//                                      res.getString(R.string.login_geocaching_com))); // 18-30%
	//                          }
	//                      });
	//
	//              // Retrieve and store cookies in reply
	//              cookies = client.getCookieStore().getCookies();
	//
	//          } catch (IOException e) {
	//              return new Failure("Unable to submit login form", e);
	//          }
	//
	//          // Parse response to check we are now logged in
	//          parse = new Parser(html);
	//
	//          if (parse.atLoginPage() || !parse.isLoggedIn()) {
	//              throw new FailureDontRetry(res.getString(R.string.bad_credentials));
	//          }
	//
	//          if (!parse.isPremium())
	//              throw new FailureDontRetry(res.getString(R.string.not_premium));
	//
	//          // Store page state for later use when we create a pocket query
	//
	//          queryStore.viewStateMap = parse.extractViewState();
	//          return null;
	//
	//      } catch (ParseException e) {
	//          return new Failure(res.getString(R.string.error_parsing), e);
	//      } finally {
	//          // Shutdown
	//          if (client != null && client.getConnectionManager() != null)
	//              client.getConnectionManager().shutdown();
	//      }
	//  }

	/**
	 * Create pocket query
	 * 
	 * Returns null on success else some error text
	 * @throws FailurePermanentException 
	 * 
	 * @throws FailureDontRetry
	 */
	public List<BasicNameValuePair> fillInCreateForm(GeocachingPage page) throws FailurePermanentException {

		FormFields loginForm = page.extractForm();

		FormFieldsExtra loginFormExtra = new FormFieldsExtra(loginForm);
		try {
			// Name of pocket query
			loginFormExtra.setValueChecked("ctl00$ContentBody$tbName", queryStore.name);

			// Turn of all days of week (if not creating disabled)
			if (!Prefs.getDisabled(cxt)) {
				loginFormExtra.setValueChecked("ctl00$ContentBody$cbDays$0", "on");
				loginFormExtra.setValueChecked("ctl00$ContentBody$cbDays$1", "on");
				loginFormExtra.setValueChecked("ctl00$ContentBody$cbDays$2", "on");
				loginFormExtra.setValueChecked("ctl00$ContentBody$cbDays$3", "on");
				loginFormExtra.setValueChecked("ctl00$ContentBody$cbDays$4", "on");
				loginFormExtra.setValueChecked("ctl00$ContentBody$cbDays$5", "on");
				loginFormExtra.setValueChecked("ctl00$ContentBody$cbDays$6", "on");
			}

			// 3 = Run this query once then delete it
			loginFormExtra.setValueChecked("ctl00$ContentBody$rbRunOption", "3");

			loginFormExtra.setValueChecked("ctl00$ContentBody$tbResults", Prefs.getMaxCaches(cxt));

			// Cache type filter
			CacheTypeList cacheTypeList = Prefs.getCacheTypeFilter(cxt);

			if (cacheTypeList.isAll())
				loginFormExtra.setValueChecked("ctl00$ContentBody$Type", "rbTypeAny");
			else {
				loginFormExtra.setValueChecked("ctl00$ContentBody$Type", "rbTypeSelect");

				for (CacheType cache : cacheTypeList) {
					loginFormExtra.setValueChecked("ctl00$ContentBody$cbTaxonomy$" + cache.ordinal(), "on");
				}
			}

			// Container type filter
			ContainerTypeList containerTypeList = Prefs.getContainerTypeFilter(cxt);

			if (containerTypeList.isAll())
				loginFormExtra.setValueChecked("ctl00$ContentBody$Container", "rbContainerAny");
			else {

				loginFormExtra.setValueChecked("ctl00$ContentBody$Container", "rbContainerSelect");

				for (ContainerType container : containerTypeList) {
					loginFormExtra.setValueChecked("ctl00$ContentBody$cbContainers$" + container.ordinal(), "on");
				}
			}

			CheckBoxesFilter checkBoxesFilter = Prefs.getCheckBoxesFilter(cxt);

			// I haven't found yet
			if (checkBoxesFilter.notFound)
				loginFormExtra.setValueChecked("ctl00$ContentBody$cbOptions$0", "on");

			// Is Active
			if (checkBoxesFilter.enabled)
				loginFormExtra.setValueChecked("ctl00$ContentBody$cbOptions$13", "on");

			// Not on ignore list
			if (checkBoxesFilter.notOnIgnore)
				loginFormExtra.setValueChecked("ctl00$ContentBody$cbOptions$6", "on");

			// Has a travel bug
			if (checkBoxesFilter.travelBug)
				loginFormExtra.setValueChecked("ctl00$ContentBody$cbOptions$10", "on");

			// Not on ignore list
			if (checkBoxesFilter.found7days)
				loginFormExtra.setValueChecked("ctl00$ContentBody$cbOptions$8", "on");

			OneToFiveFilter difficultyFilter = Prefs.getDifficultyFilter(cxt);
			if (difficultyFilter.isAll()) {
				// Just put some default values in
				loginFormExtra.setValueChecked("ctl00$ContentBody$ddDifficulty", ">=");
				loginFormExtra.setValueChecked("ctl00$ContentBody$ddDifficultyScore", "1");
			} else {
				loginFormExtra.setValueChecked("ctl00$ContentBody$cbDifficulty", "on");
				loginFormExtra.setValueChecked("ctl00$ContentBody$ddDifficulty", difficultyFilter.up ? ">=" : "<=");
				loginFormExtra.setValueChecked("ctl00$ContentBody$ddDifficultyScore", "" + difficultyFilter.value);
			}

			OneToFiveFilter terrainFilter = Prefs.getTerrainFilter(cxt);
			if (terrainFilter.isAll()) {
				// Just put some default values in
				loginFormExtra.setValueChecked("ctl00$ContentBody$ddTerrain", ">=");
				loginFormExtra.setValueChecked("ctl00$ContentBody$ddTerrainScore", "1");
			} else {
				loginFormExtra.setValueChecked("ctl00$ContentBody$cbTerrain", "on");
				loginFormExtra.setValueChecked("ctl00$ContentBody$ddTerrain", terrainFilter.up ? ">=" : "<=");
				loginFormExtra.setValueChecked("ctl00$ContentBody$ddTerrainScore", "" + terrainFilter.value);
			}

			loginFormExtra.setValueChecked("ctl00$ContentBody$CountryState", "rbNone");

			// paramList.add(new
			// BasicNameValuePair("ctl00$ContentBody$Origin","rbOriginWpt"); //
			// rbOriginNone");  //  rbOriginWpt");

			loginFormExtra.setValueChecked("ctl00$ContentBody$Origin", "rbOriginWpt"); // rbOriginNone");  //  rbOriginWpt");

			loginFormExtra.setValueChecked("ctl00$ContentBody$tbGC", "GCXXXX");
			loginFormExtra.setValueChecked("ctl00$ContentBody$tbPostalCode", "");


			// 0 = decimal degrees
			loginFormExtra.setValueChecked("ctl00$ContentBody$LatLong", "0"); // "1");

			// The jericho parser doesn't know that these shouldn't be submitted when
			// 'Decimal degrees' is selected

			loginFormExtra.deleteValue("ctl00$ContentBody$LatLong$_inputLatMins");
			loginFormExtra.deleteValue("ctl00$ContentBody$LatLong$_inputLongMins");

			if (queryStore.lat > 0) {
				// North
				loginFormExtra.setValueChecked("ctl00$ContentBody$LatLong:_selectNorthSouth", "1"); // 1
				// =
				// North,
				// -1
				// =
				// South
				loginFormExtra.setValueChecked("ctl00$ContentBody$LatLong$_inputLatDegs", Double
						.toString(queryStore.lat));
			} else {
				// South
				loginFormExtra.setValueChecked("ctl00$ContentBody$LatLong:_selectNorthSouth", "-1"); // 1
				// =
				// North,
				// -1
				// =
				// South
				loginFormExtra.setValueChecked("ctl00$ContentBody$LatLong$_inputLatDegs", Double
						.toString(-queryStore.lat));
			}

			// Ignored?
			// paramList.add(new
			// BasicNameValuePair("ctl00$ContentBody$LatLong$_inputLatMins","00.000");

			if (queryStore.lon > 0) {
				// East
				loginFormExtra.setValueChecked("ctl00$ContentBody$LatLong:_selectEastWest", "1"); // -1
				// =
				// West,
				// 1
				// =
				// East
				loginFormExtra.setValueChecked("ctl00$ContentBody$LatLong$_inputLongDegs", Double
						.toString(queryStore.lon));
			} else {
				// West
				loginFormExtra.setValueChecked("ctl00$ContentBody$LatLong:_selectEastWest", "-1"); // -1
				// =
				// West,
				// 1
				// =
				// East
				loginFormExtra.setValueChecked("ctl00$ContentBody$LatLong$_inputLongDegs", Double
						.toString(-queryStore.lon));
			}

			// Ignored?
			// paramList.add(new
			// BasicNameValuePair("ctl00$ContentBody$LatLong$_inputLongMins","00.000");

			// = decimal degrees
			loginFormExtra.setValueChecked("ctl00$ContentBody$LatLong:_currentLatLongFormat", "0"); // "1");

			loginFormExtra.setValueChecked("ctl00$ContentBody$tbRadius", Prefs.getDefaultRadius(cxt));

			if (Prefs.isMetric(cxt))
				loginFormExtra.setValueChecked("ctl00$ContentBody$rbUnitType", "km");
			else
				loginFormExtra.setValueChecked("ctl00$ContentBody$rbUnitType", "mi");

			loginFormExtra.setValueChecked("ctl00$ContentBody$Placed", "rbPlacedNone");
			loginFormExtra.setValueChecked("ctl00$ContentBody$ddLastPlaced", "WEEK");

			loginFormExtra.setValueChecked("ctl00$ContentBody$DateTimeBegin", "June/11/2011");

			loginFormExtra.setValueChecked("ctl00$ContentBody$DateTimeBegin$Month", "6");
			loginFormExtra.setValueChecked("ctl00$ContentBody$DateTimeBegin$Day", "11");
			loginFormExtra.setValueChecked("ctl00$ContentBody$DateTimeBegin$Year", "2011");
			loginFormExtra.setValueChecked("ctl00$ContentBody$DateTimeEnd", "June/18/2011");
			loginFormExtra.setValueChecked("ctl00$ContentBody$DateTimeEnd$Month", "6");
			loginFormExtra.setValueChecked("ctl00$ContentBody$DateTimeEnd$Day", "18");
			loginFormExtra.setValueChecked("ctl00$ContentBody$DateTimeEnd$Year", "2011");



			//loginFormExtra.setValueChecked("ctl00$ContentBody$ddlAltEmails", "b@bigbob.org.uk"));
			loginFormExtra.setValueChecked("ctl00$ContentBody$ddFormats", "GPX");

			if (Prefs.isZip(cxt))
				loginFormExtra.setValueChecked("ctl00$ContentBody$cbZip", "on");

			loginFormExtra.setValueChecked("ctl00$ContentBody$cbIncludePQNameInFileName", "on");
			//loginFormExtra.checkValue("ctl00$ContentBody$btnSubmit", "Submit Information");

			// delete the other form submit
			loginFormExtra.deleteValue("ctl00$ContentBody$btnDelete");


		}
		catch (ParseException e) {
			throw new FailurePermanentException("Failed to fill in login form", e.getMessage());
		}

		List<BasicNameValuePair> nameValuePairs = loginFormExtra.toNameValuePairs();

		return nameValuePairs;

	}



	//  public Failure fillInCreateForm() throws FailureDontRetry {
	//
	//      DefaultHttpClient client = new DefaultHttpClient();
	//
	//      for (Cookie c : cookies) { // todo not sagfe
	//          Logger.d("restored cookie " + c);
	//          client.getCookieStore().addCookie(c);
	//      }
	//
	//      String html;
	//
	//      // Get values
	//
	//      String max = prefs.getString("maxcaches_preference", "500");
	//      CheckBoxesFilter checkBoxesFilter = Prefs.getCheckBoxesFilter(cxt);
	//      boolean metric = prefs.getBoolean("metric_preference", false);
	//
	//      Map<String, String> viewStateMap = queryStore.viewStateMap;
	//
	//      publishProgress(new ProgressInfo(30, res.getString(R.string.creating)));
	//
	//      // Do POST to create pocket query
	//
	//      List<BasicNameValuePair> paramList = new ArrayList<BasicNameValuePair>();
	//
	//      paramList.add(new BasicNameValuePair("__EVENTTARGET", ""));
	//      paramList.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
	//      paramList.add(new BasicNameValuePair("__VIEWSTATEFIELDCOUNT", "" + viewStateMap.size()));
	//
	//      for (Map.Entry<String, String> entry : viewStateMap.entrySet()) {
	//          paramList.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
	//      }
	//
	//      // Name of pocket query
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$tbName", queryStore.name));
	//
	//      if (!Prefs.getDisabled(cxt)) {
	//          paramList.add(new BasicNameValuePair("ctl00$ContentBody$cbDays$0", "on"));
	//          paramList.add(new BasicNameValuePair("ctl00$ContentBody$cbDays$1", "on"));
	//          paramList.add(new BasicNameValuePair("ctl00$ContentBody$cbDays$2", "on"));
	//          paramList.add(new BasicNameValuePair("ctl00$ContentBody$cbDays$3", "on"));
	//          paramList.add(new BasicNameValuePair("ctl00$ContentBody$cbDays$4", "on"));
	//          paramList.add(new BasicNameValuePair("ctl00$ContentBody$cbDays$5", "on"));
	//          paramList.add(new BasicNameValuePair("ctl00$ContentBody$cbDays$6", "on"));
	//      }
	//
	//      // 3 = Run this query once then delete it
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$rbRunOption", "3"));
	//
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$tbResults", max));
	//
	//      // Cache type filter
	//      CacheTypeList cacheTypeList = Prefs.getCacheTypeFilter(cxt);
	//
	//      if (cacheTypeList.isAll())
	//          paramList.add(new BasicNameValuePair("ctl00$ContentBody$Type", "rbTypeAny"));
	//      else {
	//          paramList.add(new BasicNameValuePair("ctl00$ContentBody$Type", "rbTypeSelect"));
	//
	//          for (CacheType cache : cacheTypeList) {
	//              paramList.add(new BasicNameValuePair("ctl00$ContentBody$cbTaxonomy$" + cache.ordinal(), "on"));
	//          }
	//      }
	//
	//      // Container type filter
	//      ContainerTypeList containerTypeList = Prefs.getContainerTypeFilter(cxt);
	//
	//      if (containerTypeList.isAll())
	//          paramList.add(new BasicNameValuePair("ctl00$ContentBody$Container", "rbContainerAny"));
	//      else {
	//          paramList.add(new BasicNameValuePair("ctl00$ContentBody$Container", "rbContainerSelect"));
	//
	//          for (ContainerType container : containerTypeList) {
	//              paramList.add(new BasicNameValuePair("ctl00$ContentBody$cbContainers$" + container.ordinal(), "on"));
	//          }
	//      }
	//
	//      // I haven't found yet
	//      if (checkBoxesFilter.notFound)
	//          paramList.add(new BasicNameValuePair("ctl00$ContentBody$cbOptions$0", "on"));
	//
	//      // Is Active
	//      if (checkBoxesFilter.enabled)
	//          paramList.add(new BasicNameValuePair("ctl00$ContentBody$cbOptions$13", "on"));
	//
	//      // Not on ignore list
	//      if (checkBoxesFilter.notOnIgnore)
	//          paramList.add(new BasicNameValuePair("ctl00$ContentBody$cbOptions$6", "on"));
	//
	//      // Has a travel bug
	//      if (checkBoxesFilter.travelBug)
	//          paramList.add(new BasicNameValuePair("ctl00$ContentBody$cbOptions$10", "on"));
	//
	//      // Not on ignore list
	//      if (checkBoxesFilter.found7days)
	//          paramList.add(new BasicNameValuePair("ctl00$ContentBody$cbOptions$8", "on"));
	//
	//      OneToFiveFilter difficultyFilter = Prefs.getDifficultyFilter(cxt);
	//      if (difficultyFilter.isAll()) {
	//          // Just put some default values in
	//          paramList.add(new BasicNameValuePair("ctl00$ContentBody$ddDifficulty", ">="));
	//          paramList.add(new BasicNameValuePair("ctl00$ContentBody$ddDifficultyScore", "1"));
	//      } else {
	//          paramList.add(new BasicNameValuePair("ctl00$ContentBody$cbDifficulty", "on"));
	//          paramList.add(new BasicNameValuePair("ctl00$ContentBody$ddDifficulty", difficultyFilter.up ? ">=" : "<="));
	//          paramList.add(new BasicNameValuePair("ctl00$ContentBody$ddDifficultyScore", "" + difficultyFilter.value));
	//      }
	//
	//      OneToFiveFilter terrainFilter = Prefs.getTerrainFilter(cxt);
	//      if (terrainFilter.isAll()) {
	//          // Just put some default values in
	//          paramList.add(new BasicNameValuePair("ctl00$ContentBody$ddTerrain", ">="));
	//          paramList.add(new BasicNameValuePair("ctl00$ContentBody$ddTerrainScore", "1"));
	//      } else {
	//          paramList.add(new BasicNameValuePair("ctl00$ContentBody$cbTerrain", "on"));
	//          paramList.add(new BasicNameValuePair("ctl00$ContentBody$ddTerrain", terrainFilter.up ? ">=" : "<="));
	//          paramList.add(new BasicNameValuePair("ctl00$ContentBody$ddTerrainScore", "" + terrainFilter.value));
	//      }
	//
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$CountryState", "rbNone"));
	//
	//      // paramList.add(new
	//      // BasicNameValuePair("ctl00$ContentBody$Origin","rbOriginWpt"); //
	//      // rbOriginNone");  //  rbOriginWpt");
	//
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$tbGC", "GCXXXX"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$tbPostalCode", ""));
	//
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$Origin", "rbOriginWpt")); // rbOriginNone");  //  rbOriginWpt");
	//
	//      // 0 = decimal degrees
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$LatLong", "0")); // "1");
	//
	//      if (queryStore.lat > 0) {
	//          // North
	//          paramList.add(new BasicNameValuePair("ctl00$ContentBody$LatLong:_selectNorthSouth", "1")); // 1
	//                                                                                                     // =
	//                                                                                                     // North,
	//                                                                                                     // -1
	//                                                                                                     // =
	//                                                                                                     // South
	//          paramList.add(new BasicNameValuePair("ctl00$ContentBody$LatLong$_inputLatDegs", Double
	//                  .toString(queryStore.lat)));
	//      } else {
	//          // South
	//          paramList.add(new BasicNameValuePair("ctl00$ContentBody$LatLong:_selectNorthSouth", "-1")); // 1
	//                                                                                                      // =
	//                                                                                                      // North,
	//                                                                                                      // -1
	//                                                                                                      // =
	//                                                                                                      // South
	//          paramList.add(new BasicNameValuePair("ctl00$ContentBody$LatLong$_inputLatDegs", Double
	//                  .toString(-queryStore.lat)));
	//      }
	//
	//      // Ignored?
	//      // paramList.add(new
	//      // BasicNameValuePair("ctl00$ContentBody$LatLong$_inputLatMins","00.000");
	//
	//      if (queryStore.lon > 0) {
	//          // East
	//          paramList.add(new BasicNameValuePair("ctl00$ContentBody$LatLong:_selectEastWest", "1")); // -1
	//                                                                                                   // =
	//                                                                                                   // West,
	//                                                                                                   // 1
	//                                                                                                   // =
	//                                                                                                   // East
	//          paramList.add(new BasicNameValuePair("ctl00$ContentBody$LatLong$_inputLongDegs", Double
	//                  .toString(queryStore.lon)));
	//      } else {
	//          // West
	//          paramList.add(new BasicNameValuePair("ctl00$ContentBody$LatLong:_selectEastWest", "-1")); // -1
	//                                                                                                    // =
	//                                                                                                    // West,
	//                                                                                                    // 1
	//                                                                                                    // =
	//                                                                                                    // East
	//          paramList.add(new BasicNameValuePair("ctl00$ContentBody$LatLong$_inputLongDegs", Double
	//                  .toString(-queryStore.lon)));
	//      }
	//
	//      // Ignored?
	//      // paramList.add(new
	//      // BasicNameValuePair("ctl00$ContentBody$LatLong$_inputLongMins","00.000");
	//
	//      // = decimal degrees
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$LatLong:_currentLatLongFormat", "0")); // "1");
	//
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$tbRadius", Integer.toString(queryStore.radius)));
	//
	//      if (metric)
	//          paramList.add(new BasicNameValuePair("ctl00$ContentBody$rbUnitType", "km"));
	//      else
	//          paramList.add(new BasicNameValuePair("ctl00$ContentBody$rbUnitType", "mi"));
	//
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$Placed", "rbPlacedNone"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ddLastPlaced", "WEEK"));
	//
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$DateTimeBegin", "June/11/2011"));
	//
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$DateTimeBegin$Month", "6"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$DateTimeBegin$Day", "11"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$DateTimeBegin$Year", "2011"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$DateTimeEnd", "June/18/2011"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$DateTimeEnd$Month", "6"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$DateTimeEnd$Day", "18"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$DateTimeEnd$Year", "2011"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl00$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl01$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl02$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl03$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl04$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl05$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl06$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl07$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl08$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl09$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl10$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl11$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl12$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl13$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl14$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl15$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl16$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl17$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl18$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl19$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl20$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl21$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl22$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl23$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl24$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl25$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl26$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl27$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl28$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl29$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl30$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl31$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl32$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl33$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl34$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl35$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl36$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl37$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl38$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl39$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl40$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl41$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl42$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl43$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl44$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl45$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl46$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl47$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl48$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl49$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl50$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl51$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl52$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl53$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl54$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl55$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl56$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl57$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl58$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl59$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl60$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl61$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl62$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl63$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrInclude$dtlAttributeIcons$ctl64$hidInput", "0"));
	//
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl00$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl01$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl02$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl03$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl04$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl05$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl06$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl07$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl08$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl09$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl10$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl11$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl12$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl13$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl14$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl15$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl16$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl17$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl18$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl19$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl20$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl21$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl22$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl23$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl24$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl25$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl26$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl27$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl28$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl29$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl30$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl31$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl32$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl33$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl34$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl35$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl36$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl37$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl38$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl39$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl40$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl41$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl42$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl43$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl44$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl45$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl46$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl47$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl48$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl49$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl50$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl51$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl52$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl53$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl54$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl55$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl56$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl57$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl58$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl59$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl60$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl61$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl62$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl63$hidInput", "0"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ctlAttrExclude$dtlAttributeIcons$ctl64$hidInput", "0"));
	//
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ddlAltEmails", "b@bigbob.org.uk"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$ddFormats", "GPX"));
	//
	//      if (Prefs.isZip(cxt))
	//          paramList.add(new BasicNameValuePair("ctl00$ContentBody$cbZip", "on"));
	//
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$cbIncludePQNameInFileName", "on"));
	//      paramList.add(new BasicNameValuePair("ctl00$ContentBody$btnSubmit", "Submit Information"));
	//
	//      try {
	//
	//          html = IOUtils.httpPost(client, paramList, "pocket/gcquery.aspx", false, new Listener() {
	//              @Override
	//              public void update(int bytesReadSoFar, int expectedLength) {
	//                  publishProgress(new ProgressInfo((int) (30 + (bytesReadSoFar * 30 / expectedLength)), res
	//                          .getString(R.string.creating))); // 30 - 60%
	//              }
	//          });
	//
	//          // Retrieve and store cookies in reply
	//          cookies = client.getCookieStore().getCookies();
	//
	//      } catch (IOException e) {
	//          return new Failure("Problem submitting Query creation page", e);
	//      }
	//
	//      // Parse POST html response
	//      // Look for success message inside <p class="Success">
	//
	//      this.successMessageParser = new SuccessMessageParser(html);
	//
	//      if (successMessageParser.extractNumberPQ() == 0)
	//          throw new FailureDontRetry(res.getString(R.string.creation_zero_pqs));
	//
	//      // Shutdown
	//      client.getConnectionManager().shutdown();
	//
	//      return null; // no error message returned
	//  }



	//  private Failure openOutputFile() {
	//      if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
	//          return new Failure(res.getString(R.string.external_storage_unavailable));
	//      }
	//
	//      boolean error = false;
	//
	//      String dir = Util.getDefaultDownloadDirectory();
	//      if (!Prefs.isDefaultDownloadDir(cxt))
	//          dir = Prefs.getUserSpecifiedDownloadDir(cxt);
	//
	//      outputFile = Util.getUniqueFile(dir, Prefs.getDownloadPrefix(cxt) + queryStore.name, (Prefs.isZip(cxt) ? "zip"
	//              : ""));
	//      try {
	//          if (outputFile.exists())
	//              error = true;
	//          new File(outputFile.getParent()).mkdirs();
	//          if (!outputFile.createNewFile())
	//              error = true;
	//      } catch (IOException e) {
	//          return new Failure(res.getString(R.string.file_creation_error), e);
	//      }
	//      if (error)
	//          return new Failure(res.getString(R.string.file_creation_error), outputFile.getAbsolutePath());
	//      return null;
	//  }

	//  private Failure download(String path) throws FailureDontRetry {
	//      Logger.d("enter");
	//
	//      byte[] pq = null;
	//      DefaultHttpClient client = new DefaultHttpClient();
	//
	//      for (Cookie c : cookies) {
	//          Logger.d("restored cookie " + c);
	//          client.getCookieStore().addCookie(c);
	//      }
	//
	//      // Get the pocket query creation page
	//      // and read the response. Need to detect if logged in or no
	//
	//      try {
	//          pq = IOUtils.httpGetBytes(client, path, new Listener() {
	//
	//              @Override
	//              public void update(int bytesReadSoFar, int expectedLength) {
	//                  publishProgress(new ProgressInfo((int) 70 + (bytesReadSoFar * 30 / expectedLength), res
	//                          .getString(R.string.download_in_progress)
	//                          + " "
	//                          + Util.humanDownloadCounter(bytesReadSoFar, expectedLength))); // 70-100%
	//              }
	//          });
	//
	//          // Retrieve and store cookies in reply
	//          cookies = client.getCookieStore().getCookies();
	//
	//      } catch (HTTPStatusCodeException e) {
	//          // When PQ not run, we get back 302 redirect to <a href="/pocket/">
	//          if (e.code == HttpStatus.SC_MOVED_TEMPORARILY && e.body.indexOf("<a href=\"/pocket/\">") != -1)
	//              return new Failure(res.getString(R.string.download_not_ready));
	//          // Treat any other status code as error
	//          return new Failure(res.getString(R.string.download_failed), e);
	//      } catch (IOException e) {
	//          return new Failure(res.getString(R.string.download_failed), e);
	//      }
	//
	//      // Write to output file
	//      try {
	//          Logger.d("Going to write to file");
	//          FileOutputStream fout = new FileOutputStream(outputFile);
	//          fout.write(pq);
	//          fout.close();
	//          Logger.d("Written to file ok");
	//
	//      } catch (IOException e) {
	//          throw new FailureDontRetry("Unable to write to output file");
	//      }
	//
	//      return null;
	//  }

	@Override
	public void onLocationChanged(Location location) {
		Logger.d("Got fix [accuracy=" + location.getAccuracy() + "]");

		if (location.getProvider().equals(LocationManager.NETWORK_PROVIDER) && this.gpsLocation != null
				&& this.gpsLocation.getProvider().equals(LocationManager.GPS_PROVIDER)) {
			// don't over write GPS with network provider
		} else {
			this.gpsLocation = location;
		}

		if (gpsLocation.getAccuracy() < Prefs.getLocationAccuracy(cxt)) {
			Logger.d("Fix is accurate enough. Saving it [accuracy=" + gpsLocation.getAccuracy() + ", requiredAccuracy="
					+ Prefs.getLocationAccuracy(cxt));
			queryStore.lat = gpsLocation.getLatitude();
			queryStore.lon = gpsLocation.getLongitude();
		}

		publishProgress(new ProgressInfo(1, res.getString((R.string.gps_wait)) + " (accuracy "
				+ (int) gpsLocation.getAccuracy() + "m)"));
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}



	@Override
	public void progressReport(ProgressInfo progress) {
		publishProgress(new ProgressInfo[] { progress });
	}

	@Override
	public void ifCancelledThrow() throws InterruptedException {
		if (isCancelled())
			throw new InterruptedException();
	}


}

