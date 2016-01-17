package org.pquery.webdriver;

import android.content.Context;
import android.content.res.Resources;

import net.htmlparser.jericho.FormFields;
import net.htmlparser.jericho.Source;

import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.pquery.R;
import org.pquery.util.HTTPStatusCodeException;
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

import static org.pquery.webdriver.HttpClientFactory.createHttpClient;

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
        DefaultHttpClient client = null;
        List<Cookie> cookies = Prefs.getCookies(cxt);

        try {
            // Initialize to 0%

            progressReport(0, res.getString(R.string.retrieving_page), res.getString(R.string.requesting));

            client = createHttpClient();


            // Restore cookies
            // This gives us a chance for server to think we are logged in

            for (Cookie c : cookies) {
                Logger.d("restored cookie " + c);
                client.getCookieStore().addCookie(c);
            }


            //
            // Get the target page
            // Not sure if logged in yet

            // 0 - 33%

            try {

                    html = IOUtils.httpGet(client, urlPath, cancelledListener, new Listener() {

                        @Override
                        public void update(int bytesReadSoFar, int expectedLength, int percent0to100) {
                            progressReport(
                                    percent0to100 / 3,    // convert to 0-33%
                                    res.getString(R.string.retrieve_page),
                                    Util.humanDownloadCounter(bytesReadSoFar, expectedLength));
                        }
                    });


            } catch (IOException e) {
                cookies = client.getCookieStore().getCookies();

                Logger.e("Exception downloading login page", e);
                throw new FailureException(res.getString(R.string.login_download_fail), e);
            }


            // Make sure cookies are upto date
            cookies = client.getCookieStore().getCookies();




            //
            // Parse the response
            // Check for some common problems and detect if already logged in or not
            // Can take a long time an old CPU but good way to update progress

            // 50%

            progressReport(50, res.getString(R.string.parsing), "");
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
            // So we now need to POST the login form
            // Fill in the form values

            // 60% - 93%

            FormFields loginForm = pageParser.extractForm();

            FormFieldsExtra loginFormExtra = new FormFieldsExtra(loginForm);
            try {
                loginFormExtra.setValueChecked("ctl00$uxLoginStatus$tbUsername", username);
                loginFormExtra.setValueChecked("ctl00$uxLoginStatus$tbPassword", password);
                loginFormExtra.setValueChecked("ctl00$uxLoginStatus$cbRememberMe", "on");

                loginFormExtra.checkValue("ctl00$uxLoginStatus$btnSignIn", "Sign In");
            } catch (ParseException e) {
                throw new FailurePermanentException(res.getString(R.string.failed_login_form));
            }

            List<BasicNameValuePair> nameValuePairs = loginFormExtra.toNameValuePairs();


            progressReport(60, res.getString(R.string.login_geocaching_com), res.getString(R.string.requesting));

            try {
                // https://www.geocaching.com/login/default.aspx?redir=%2fpocket%2fdefault.aspx%3f

                html = IOUtils.httpPost(client, nameValuePairs, "/login/default.aspx?redir=" + URLEncoder.encode(urlPath),
                        true, cancelledListener, new Listener() {

                            @Override
                            public void update(int bytesReadSoFar, int expectedLength, int percent0to100) {
                                progressReport(
                                        60 + percent0to100 / 3,
                                        res.getString(R.string.login_geocaching_com),
                                        Util.humanDownloadCounter(bytesReadSoFar, expectedLength)); // 18-30%
                            }
                        });

                // Retrieve and store cookies in reply
                cookies = client.getCookieStore().getCookies();

            } catch (IOException e) {
                throw new FailureException(res.getString(R.string.unable_to_submit_login), e);
            }




            //
            // Parse response to verify we are now logged in
            //

            progressReport(95, res.getString(R.string.parsing), "");

            pageParser = new GeocachingPage(html);
            ifCancelledThrow();

            if (pageParser.atLoginPage() || !pageParser.isLoggedIn()) {
                throw new FailurePermanentException(res.getString(R.string.bad_credentials));
            }

            if (!pageParser.isPremium())
                throw new FailurePermanentException(res.getString(R.string.not_premium));

            Prefs.saveCookies(cxt, cookies);

            return pageParser.parsedHtml;

        } catch (GeocachingPage.ParseException e) {
            throw new FailurePermanentException(res.getString(R.string.error_parsing), e);
        } finally {
            // Shutdown
            if (client != null && client.getConnectionManager() != null)
                client.getConnectionManager().shutdown();
        }
    }


}



