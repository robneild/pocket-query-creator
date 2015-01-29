package org.pquery.service;

import android.content.Context;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;

import junit.framework.Assert;

import net.htmlparser.jericho.FormFields;
import net.htmlparser.jericho.Source;

import org.apache.http.cookie.Cookie;
import org.apache.http.message.BasicNameValuePair;
import org.pquery.QueryStore;
import org.pquery.R;
import org.pquery.dao.DownloadablePQ;
import org.pquery.filter.CacheType;
import org.pquery.filter.CacheTypeList;
import org.pquery.filter.CheckBoxesFilter;
import org.pquery.filter.ContainerType;
import org.pquery.filter.ContainerTypeList;
import org.pquery.filter.DaysToGenerateFilter;
import org.pquery.filter.OneToFiveFilter;
import org.pquery.util.GPS;
import org.pquery.util.Logger;
import org.pquery.util.Prefs;
import org.pquery.webdriver.CancelledListener;
import org.pquery.webdriver.CreateOutputDirectoryTask;
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

import java.io.File;
import java.util.List;

public class CreatePQAsync extends AsyncTask<Void, ProgressInfo, CreatePQResult> implements LocationListener, CancelledListener, ProgressListener {

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
            publishProgress(new ProgressInfo(1, res.getString((R.string.gps_wait_no_fix))));
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
            RetrievePageTask loginTask = new RetrievePageTask(retryCount, 5, 30, this, this, cxt, "/pocket/gcquery.aspx");
            Source parsedHtml = loginTask.call();

            publishProgress(new ProgressInfo(30, res.getString(R.string.processing_form)));

            GeocachingPage createPage = new GeocachingPage(parsedHtml);
            List<BasicNameValuePair> form = fillInCreateForm(createPage);


            // 30% - 60%
            SubmitFormPageTask submitTask = new SubmitFormPageTask(form, retryCount, 30, 60, this, this, cxt, "/pocket/gcquery.aspx");
            String html = submitTask.call();


            SuccessMessageParser successMessageParser = new SuccessMessageParser(html, res);


            Logger.d("[successMessage=" + successMessageParser.getSuccessMessage() + "]");

            if (!Prefs.getDownload(cxt) || successMessageParser.extractNumberPQ() == 0) {
                // We we aren't downloading or creating disabled then can finish
                // now
                return new CreatePQResult(successMessageParser.toString(res));
            }

            // 60% - 70%

            checkCancelled();
            publishProgress(new ProgressInfo(60, successMessageParser.toString(res)
                    + "<br><br>" + res.getString(R.string.waiting_for_pq_to_run)));
            Thread.sleep(30000);

            String guid = successMessageParser.extractDownloadGuid();

            DownloadablePQ pq = new DownloadablePQ();
            pq.url = "/pocket/downloadpq.ashx?g=" + guid;
            pq.name = queryStore.name;

            // Download DownloadablePQ

            CreateOutputDirectoryTask createTask = new CreateOutputDirectoryTask(retryCount, 70, 75, this, this, cxt);
            File outputDirectory = createTask.call();

            DownloadTask downloadTask = new DownloadTask(retryCount, 75, 100, this, this, cxt, pq.url, outputDirectory, Prefs.getDownloadPrefix(cxt) + pq.name + ".zip");
            File downloadedFile = downloadTask.call();

            return new CreatePQResult(new DownloadPQResult(downloadedFile));

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
        Logger.d("enter [minutesToWait=" + minutesToWait + "]");
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

    /**
     * Create pocket query
     * <p/>
     * Returns null on success else some error text
     *
     * @throws FailurePermanentException
     */
    public List<BasicNameValuePair> fillInCreateForm(GeocachingPage page) throws FailurePermanentException {

        FormFields loginForm = page.extractForm();

        FormFieldsExtra loginFormExtra = new FormFieldsExtra(loginForm);
        try {
            // Name of pocket query
            loginFormExtra.setValueChecked("ctl00$ContentBody$tbName", queryStore.name);

            // Days of week
            DaysToGenerateFilter daysToGenerateFilter = Prefs.getDaysToGenerateFilter(cxt);

            if (daysToGenerateFilter.dayOfWeek[0]) loginFormExtra.setValueChecked("ctl00$ContentBody$cbDays$0", "on");
            if (daysToGenerateFilter.dayOfWeek[1]) loginFormExtra.setValueChecked("ctl00$ContentBody$cbDays$1", "on");
            if (daysToGenerateFilter.dayOfWeek[2]) loginFormExtra.setValueChecked("ctl00$ContentBody$cbDays$2", "on");
            if (daysToGenerateFilter.dayOfWeek[3]) loginFormExtra.setValueChecked("ctl00$ContentBody$cbDays$3", "on");
            if (daysToGenerateFilter.dayOfWeek[4]) loginFormExtra.setValueChecked("ctl00$ContentBody$cbDays$4", "on");
            if (daysToGenerateFilter.dayOfWeek[5]) loginFormExtra.setValueChecked("ctl00$ContentBody$cbDays$5", "on");
            if (daysToGenerateFilter.dayOfWeek[6]) loginFormExtra.setValueChecked("ctl00$ContentBody$cbDays$6", "on");


            // 1 = Uncheck the day of the week after the query runs
            if (daysToGenerateFilter.howOftenRun == DaysToGenerateFilter.UNCHECK_DAY_AFTER_QUERY)
                loginFormExtra.setValueChecked("ctl00$ContentBody$rbRunOption", "1");

            // 2 = Run this query every week on the days checked
            if (daysToGenerateFilter.howOftenRun == DaysToGenerateFilter.RUN_EVERY_WEEK_ON_CHECKED_DAYS)
                loginFormExtra.setValueChecked("ctl00$ContentBody$rbRunOption", "2");

            // 3 = Run this query once then delete it
            if (daysToGenerateFilter.howOftenRun == DaysToGenerateFilter.RUN_ONCE_THEN_DELETE)
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

            if (checkBoxesFilter.idontown)
                loginFormExtra.setValueChecked("ctl00$ContentBody$cbOptions$2", "on");

            if (checkBoxesFilter.notBeenFound)
                loginFormExtra.setValueChecked("ctl00$ContentBody$cbOptions$9", "on");

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

            loginFormExtra.setValueChecked("ctl00$ContentBody$cbIncludePQNameInFileName", "on");
            //loginFormExtra.checkValue("ctl00$ContentBody$btnSubmit", "Submit Information");

            // delete the other form submit
            loginFormExtra.deleteValue("ctl00$ContentBody$btnDelete");


        } catch (ParseException e) {
            throw new FailurePermanentException(res.getString(R.string.failed_login_form), e.getMessage());
        }

        List<BasicNameValuePair> nameValuePairs = loginFormExtra.toNameValuePairs();

        return nameValuePairs;

    }

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

        publishProgress(new ProgressInfo(1, String.format(res.getString(R.string.gps_wait), (int)gpsLocation.getAccuracy())));
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
        publishProgress(new ProgressInfo[]{progress});
    }

    @Override
    public void ifCancelledThrow() throws InterruptedException {
        if (isCancelled())
            throw new InterruptedException();
    }


}

