package org.pquery.webdriver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.pquery.R;
import org.pquery.util.HTTPStatusCodeException;
import org.pquery.util.IOUtils;
import org.pquery.util.Logger;
import org.pquery.util.Prefs;
import org.pquery.util.Util;
import org.pquery.util.IOUtils.Listener;
import android.content.Context;
import android.content.res.Resources;

public class DownloadTask extends RetriableTask<Integer> {

    private Context cxt;
    private Resources res;
    private String url;
    private File output;

    public DownloadTask(int numberOfRetries, int fromPercent, int toPercent, ProgressListener progressListener, CancelledListener cancelledListener, Context cxt, String url, File output) {
        super(numberOfRetries, fromPercent, toPercent, progressListener, cancelledListener);
        this.cxt = cxt;
        this.url = url;
        this.res = cxt.getResources();
        this.output = output;
    }

    @Override
    protected Integer task() throws FailureException, FailurePermanentException {

        byte [] pq;
        DefaultHttpClient client = new DefaultHttpClient();
        List<Cookie> cookies = Prefs.getCookies(cxt);

        for (Cookie c : cookies) {
            Logger.d("restored cookie " + c);
            client.getCookieStore().addCookie(c);
        }

        progressReport(0,res.getString(R.string.downloading),"requesting");

        // Get the pocket query creation page
        // and read the response. Need to detect if logged in or no

        try {
            pq = IOUtils.httpGetBytes(client, url, new Listener() {

                @Override
                public void update(int bytesReadSoFar, int expectedLength, int percent0to100) {
                    progressReport(
                            percent0to100,
                            res.getString(R.string.downloading),
                            Util.humanDownloadCounter(bytesReadSoFar, expectedLength));
                }
            });

        } catch (HTTPStatusCodeException e) {
            // When PQ not run, we get back 302 redirect to <a href="/pocket/">
            if (e.code == HttpStatus.SC_MOVED_TEMPORARILY && e.body.indexOf("<a href=\"/pocket/\">") != -1)
                throw new FailureException(res.getString(R.string.download_not_ready));

            // Treat any other status code as error
            throw new FailureException(res.getString(R.string.download_failed), e);

        } catch (IOException e) {
            throw new FailureException(res.getString(R.string.download_failed), e);
        }

        // Write to output file
        try {
            Logger.d("Going to write to file");
            FileOutputStream fout = new FileOutputStream(output);
            fout.write(pq);
            fout.close();
            Logger.d("Written to file ok");

        } catch (IOException e) {
            throw new FailurePermanentException("Unable to write to output file");
        }

        return pq.length;
    }

}



