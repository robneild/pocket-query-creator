package org.pquery.webdriver.parser;

import android.test.AndroidTestCase;

import net.htmlparser.jericho.Source;

import org.pquery.R;
import org.pquery.dao.DownloadablePQ;
import org.pquery.dao.RepeatablePQ;
import org.pquery.util.Util;

import java.io.IOException;
import java.io.InputStream;

public class PocketQueryPageTest extends AndroidTestCase {

    private PocketQueryPage pocketQueryPage;

    @Override
    protected void setUp() throws Exception {
        String html = loadFromResource("pocket_query.htm");
        pocketQueryPage = new PocketQueryPage(new Source(html));
    }

    private String loadFromResource(String name) throws IOException {
        InputStream in = getClass().getClassLoader().getResourceAsStream("res/raw/" + name);
        assertNotNull(in);
        String ret = Util.inputStreamIntoString(in);
        assertNotNull(ret);
        return ret;
    }

    public void testDownload() throws ParseException {
        DownloadablePQ[] download = pocketQueryPage.getReadyForDownload();
        assertEquals(1, download.length);

        assertEquals("10-20-12 6.57 PM", download[0].name);
        assertEquals("/pocket/downloadpq.ashx?g=e6fe1c12-a8ca-4f39-ab43-d1f4c6b6978c", download[0].url);
        assertEquals("174.75 KB", download[0].size);
        assertEquals("159", download[0].waypoints);

    }

    public void testDownload2() throws IOException, ParseException {

        String html = loadFromResource("pocket_query_2.htm");
        PocketQueryPage pocketQueryPage2 = new PocketQueryPage(new Source(html));

        DownloadablePQ[] download = pocketQueryPage2.getReadyForDownload();
        assertEquals(2, download.length);

        assertEquals("Bobby", download[0].name);

        assertEquals("Bobby1234", download[1].name);
    }

    public void testDownload_no_downloads() throws IOException, ParseException {

        String html = loadFromResource("pocket_query_no_downloads.htm");
        PocketQueryPage pqp = new PocketQueryPage(new Source(html));

        DownloadablePQ[] download = pqp.getReadyForDownload();

        assertEquals(0, download.length);
    }

    public void testRepeater() throws IOException, ParseException {

        String html = loadFromResource("pocket_query_2.htm");
        PocketQueryPage pocketQueryPage2 = new PocketQueryPage(new Source(html));

        RepeatablePQ[] repeatable = pocketQueryPage2.getRepeatables();
        assertEquals(2, repeatable.length);

        assertEquals("Bobby", repeatable[0].name);
        assertEquals("10", repeatable[0].waypoints);
        String[] weekdayNames = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        assertEquals("Sunday, Monday, Tuesday, Wednesday, Thursday, Saturday", repeatable[0].getCheckedWeekdaysAsText(weekdayNames));


        assertEquals("Bobby1234", repeatable[1].name);
        assertEquals("20", repeatable[1].waypoints);
        assertEquals("Sunday, Monday, Tuesday, Thursday, Saturday", repeatable[1].getCheckedWeekdaysAsText(weekdayNames));

    }

}
