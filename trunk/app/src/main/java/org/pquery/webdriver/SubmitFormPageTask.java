package org.pquery.webdriver;

import android.content.Context;
import android.content.res.Resources;

import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.pquery.R;
import org.pquery.util.IOUtils;
import org.pquery.util.IOUtils.Listener;
import org.pquery.util.Logger;
import org.pquery.util.Prefs;
import org.pquery.util.Util;

import java.io.IOException;
import java.util.List;

import static org.pquery.webdriver.HttpClientFactory.createHttpClient;

public class SubmitFormPageTask extends RetriableTask<String> {

    private Context cxt;
    private String urlPath;
    private List<BasicNameValuePair> form;

    public SubmitFormPageTask(List<BasicNameValuePair> form, int numberOfRetries, int fromPercent, int toPercent, ProgressListener progressListener, CancelledListener cancelledListener, Context cxt, String urlPath) {
        super(numberOfRetries, fromPercent, toPercent, progressListener, cancelledListener, cxt.getResources());
        this.cxt = cxt;
        this.urlPath = urlPath;
        this.form = form;
    }

    @Override
    protected String task() throws FailureException, FailurePermanentException, InterruptedException {

        Logger.d("enter");

        String html = "";

        // Create client and restore cookies so we will be logged in
        // ASSUMING already logged in at this stage

        DefaultHttpClient client = createHttpClient();

        List<Cookie> cookies = Prefs.getCookies(cxt);
        for (Cookie c : cookies) {
            Logger.d("restored cookie " + c);
            client.getCookieStore().addCookie(c);
        }

        try {
            // Initialize to 0%

            progressReport(0, res.getString(R.string.creating), res.getString(R.string.submitting));

            try {
                // https://www.geocaching.com/login/default.aspx?redir=%2fpocket%2fdefault.aspx%3f

                html = IOUtils.httpPost(client, form, urlPath, false, cancelledListener, new Listener() {

                    @Override
                    public void update(int bytesReadSoFar, int expectedLength, int percent0to100) {
                        progressReport(
                                percent0to100,
                                res.getString(R.string.creating),
                                Util.humanDownloadCounter(bytesReadSoFar, expectedLength));
                    }
                });

                // Retrieve and store cookies in reply
                cookies = client.getCookieStore().getCookies();
                Prefs.saveCookies(cxt, cookies);

            } catch (IOException e) {
                throw new FailureException(res.getString(R.string.unable_to_submit_creation_form), e);
            }

            return html;

        } finally {
            // Shutdown
            if (client != null && client.getConnectionManager() != null)
                client.getConnectionManager().shutdown();
        }
    }


}



