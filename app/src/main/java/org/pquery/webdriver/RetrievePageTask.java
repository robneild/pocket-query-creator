package org.pquery.webdriver;

import android.content.Context;
import android.util.Pair;

import net.htmlparser.jericho.FormFields;
import net.htmlparser.jericho.Source;

import org.pquery.R;
import org.pquery.util.IOUtils;
import org.pquery.util.IOUtils.Listener;
import org.pquery.util.Logger;
import org.pquery.util.Prefs;
import org.pquery.util.Util;
import org.pquery.webdriver.parser.FormFieldsExtra;
import org.pquery.webdriver.parser.GeocachingPage;
import org.pquery.webdriver.parser.ParseException;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

public class RetrievePageTask extends RetriableTask<Source> {

    private Context cxt;
    private String urlPath;

    public RetrievePageTask(int numberOfRetries, int fromPercent, int toPercent, ProgressListener progressListener, CancelledListener cancelledListener, Context cxt, String urlPath) {
        super(numberOfRetries, fromPercent, toPercent, progressListener, cancelledListener, cxt.getResources());
        this.cxt = cxt;
        this.urlPath = urlPath;
    }

    @Override
    protected Source task() throws FailureException, FailurePermanentException, InterruptedException {

        Logger.d("enter");

        // Get preferences

        String username = Prefs.getUsername(cxt);
        String password = Prefs.getPassword(cxt);

        String html = "";

        try {
            // Initialize to 0%

            progressReport(0, res.getString(R.string.retrieving_page), res.getString(R.string.requesting));



            //
            // Get the target page
            // Not sure if logged in yet, but try and get it any way. If not logged in it should
            // contain the login form anyway, which is handy

            // 0 - 50%

            try {

                    html = IOUtils.httpGet(cxt, urlPath, cancelledListener, new Listener() {

                        @Override
                        public void update(int bytesReadSoFar, int expectedLength, int percent0to100) {
                            progressReport(
                                    percent0to100 / 2,    // convert to 0-50%
                                    res.getString(R.string.retrieve_page),
                                    Util.humanDownloadCounter(bytesReadSoFar, expectedLength));
                        }
                    });


            } catch (IOException e) {
                Logger.e("Exception downloading login page", e);
                throw new FailureException(res.getString(R.string.login_download_fail), e);
            }

            // Parse the response
            // Check for some common problems and detect if already logged in or not
            // Can take a long time an old CPU but good way to update progress

            // 70%

            progressReport(70, res.getString(R.string.parsing), "");
            GeocachingPage pageParser = new GeocachingPage(html);

            ifCancelledThrow();


            // Check for a completely wrong page returned that doesn't mention
            // Geocaching in the title
            // Likely to be a wifi login page

            if (!pageParser.isGeocachingPage())
                throw new FailurePermanentException(res.getString(R.string.not_geocaching_page));


            // Check the response. Detecting login and premium state
            if (pageParser.isLoggedIn()) {
                if (!pageParser.isPremium())
                    throw new FailurePermanentException(res.getString(R.string.not_premium));

                // This is Good. User already logged in and proper
                // page retrieved. Cookies must be good

                Logger.d("Detected already logged in");
                return pageParser.parsedHtml;
            }







            //
            // So we now need to GET the login form and POST the credentials
            //
            // - extract form values from the previous page
            // - fill in text boxes with username/password
            // - cookies from original request will be used

            // Fill in the form values

            // 0% - 50%

            try {
                html = IOUtils.httpGet(cxt, "/account/login?returnUrl=" + URLEncoder.encode(urlPath), cancelledListener, new Listener() {

                    @Override
                    public void update(int bytesReadSoFar, int expectedLength, int percent0to100) {
                        progressReport(
                                percent0to100 / 2,
                                res.getString(R.string.login_geocaching_com),
                                Util.humanDownloadCounter(bytesReadSoFar, expectedLength)); // 18-30%
                    }
                });
            } catch (IOException e) {
                throw new FailureException(res.getString(R.string.unable_to_get_login), e);
            }
            pageParser = new GeocachingPage(html);
            FormFields loginForm = pageParser.extractForm();

            FormFieldsExtra loginFormExtra = new FormFieldsExtra(loginForm);
            try {
                loginFormExtra.setValueChecked("Username", username);
                loginFormExtra.setValueChecked("Password", password);

                //loginFormExtra.setValueChecked("ctl00$ContentBody$cbRememberMe", "on");
                //loginFormExtra.checkValue("ctl00$ContentBody$btnSignIn");

            } catch (ParseException e) {
                throw new FailurePermanentException(res.getString(R.string.failed_login_form), e.getMessage());
            }

            List<Pair<String,String>> nameValuePairs = loginFormExtra.toNameValuePairs();
            nameValuePairs = nameValuePairs.subList(1, nameValuePairs.size());

            progressReport(0, res.getString(R.string.login_geocaching_com), res.getString(R.string.requesting));

            try {
                html = IOUtils.httpPost(cxt, nameValuePairs, "/account/login?returnUrl=" + URLEncoder.encode(urlPath),
                        true, cancelledListener, new Listener() {

                            @Override
                            public void update(int bytesReadSoFar, int expectedLength, int percent0to100) {
                                progressReport(
                                        percent0to100 / 2,
                                        res.getString(R.string.login_geocaching_com),
                                        Util.humanDownloadCounter(bytesReadSoFar, expectedLength)); // 18-30%
                            }
                        });

            } catch (IOException e) {
                throw new FailureException(res.getString(R.string.unable_to_submit_login), e);
            }

            // Parse response to verify we are now logged in
            //

            progressReport(70, res.getString(R.string.parsing), "");

            pageParser = new GeocachingPage(html);
            ifCancelledThrow();

            if (!pageParser.isLoggedIn()) {
                throw new FailurePermanentException(res.getString(R.string.bad_credentials));
            }








            //
            // Retrieve page for a second time
            // We are now logged in, so should be ok
            //


            progressReport(0, res.getString(R.string.retrieving_page), res.getString(R.string.requesting));

            //
            // Get the target page
            // Not sure if logged in yet, but try and get it any way. If not logged in it should
            // contain the login form anyway, which is handy

            // 0 - 50%

            try {

                html = IOUtils.httpGet(cxt, urlPath, cancelledListener, new Listener() {

                    @Override
                    public void update(int bytesReadSoFar, int expectedLength, int percent0to100) {
                        progressReport(
                                percent0to100 / 2,    // convert to 0-50%
                                res.getString(R.string.retrieve_page),
                                Util.humanDownloadCounter(bytesReadSoFar, expectedLength));
                    }
                });


            } catch (IOException e) {
                Logger.e("Exception downloading login page", e);
                throw new FailureException(res.getString(R.string.login_download_fail), e);
            }





            //
            // Parse the response
            // Check for some common problems and detect if already logged in or not
            // Can take a long time an old CPU but good way to update progress

            // 70%

            progressReport(70, res.getString(R.string.parsing), "");
            pageParser = new GeocachingPage(html);

            ifCancelledThrow();


            // Check for a completely wrong page returned that doesn't mention
            // Geocaching in the title
            // Likely to be a wifi login page

            if (!pageParser.isGeocachingPage())
                throw new FailurePermanentException(res.getString(R.string.not_geocaching_page));


            // Check the response. Detecting login and premium state
            if (pageParser.isLoggedIn()) {
                if (!pageParser.isPremium())
                    throw new FailurePermanentException(res.getString(R.string.not_premium));

                // This is Good. User already logged in and proper
                // page retrieved. Cookies must be good

                Logger.d("Detected already logged in");
                return pageParser.parsedHtml;
            }






            throw new FailurePermanentException("Unknown error");


        } catch (GeocachingPage.ParseException e) {
            throw new FailurePermanentException(res.getString(R.string.error_parsing), e);
        }
    }


}



