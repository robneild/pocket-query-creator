package org.pquery.webdriver;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.List;

import net.htmlparser.jericho.FormControl;
import net.htmlparser.jericho.FormField;
import net.htmlparser.jericho.Source;

import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.pquery.R;
import org.pquery.util.IOUtils;
import org.pquery.util.Logger;
import org.pquery.util.Prefs;
import org.pquery.util.IOUtils.Listener;
import org.pquery.util.Util;
import org.pquery.webdriver.parser.GeocachingPage;
import org.pquery.webdriver.parser.FormFieldsExtra;
import org.pquery.webdriver.parser.ParseException;

import net.htmlparser.jericho.FormFields;
import android.content.Context;
import android.content.res.Resources;

public class SubmitFormPageTask extends RetriableTask<String> {

    private Context cxt;
    private Resources res;
    private String urlPath;
    private List<BasicNameValuePair> form;
    
    public SubmitFormPageTask(List<BasicNameValuePair> form, int numberOfRetries, int fromPercent, int toPercent, ProgressListener progressListener, CancelledListener cancelledListener, Context cxt, String urlPath) {
        super(numberOfRetries, fromPercent, toPercent, progressListener, cancelledListener);
        this.cxt = cxt;
        this.urlPath = urlPath;
        this.res = cxt.getResources();
        this.form = form;
    }

    @Override
    protected String task() throws FailureException, FailurePermanentException {

        Logger.d("enter");

        String html = "";
        DefaultHttpClient client = null;
        List<Cookie> cookies = Prefs.getCookies(cxt);

        try {
            // Initialize to 0%

            progressReport(0, res.getString(R.string.creating),"submitting");

            try {
                // https://www.geocaching.com/login/default.aspx?redir=%2fpocket%2fdefault.aspx%3f

                html = IOUtils.httpPost(client, form, urlPath, true, new Listener() {

                    @Override
                    public void update(int bytesReadSoFar, int expectedLength, int percent0to100) {
                        progressReport(
                                percent0to100,
                                res.getString(R.string.creating),
                                Util.humanDownloadCounter(bytesReadSoFar, expectedLength)); // 18-30%
                    }
                });

                // Retrieve and store cookies in reply
                cookies = client.getCookieStore().getCookies();

            } catch (IOException e) {
                throw new FailureException("Unable to submit creation form", e);
            }
            
            Prefs.saveCookies(cxt, cookies);

            return html;

        }
        finally {
            // Shutdown
            if (client != null && client.getConnectionManager() != null)
                client.getConnectionManager().shutdown();
        }
    }


}



