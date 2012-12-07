package org.pquery.webdriver.parser;

import java.io.IOException;
import java.io.InputStream;

import net.htmlparser.jericho.Source;

import org.pquery.dao.PQ;
import org.pquery.util.Util;

import android.test.AndroidTestCase;
import android.util.Log;

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
    
    public void testDownload() {
        PQ[] download = pocketQueryPage.getReadyForDownload();
        assertEquals(1, download.length);
        
        assertEquals("10-20-12 6.57 PM", download[0].name);
        assertEquals("/pocket/downloadpq.ashx?g=e6fe1c12-a8ca-4f39-ab43-d1f4c6b6978c", download[0].url);
        assertEquals("174.75 KB", download[0].size);
        assertEquals("159", download[0].waypoints);
        
    }
    
    public void testDownload2() throws IOException {
        
        String html = loadFromResource("pocket_query_2.htm");
        PocketQueryPage pocketQueryPage2 = new PocketQueryPage(new Source(html));
        
        PQ[] download = pocketQueryPage2.getReadyForDownload();
        assertEquals(2, download.length);
        
        assertEquals("Bobby", download[0].name);

        assertEquals("Bobby1234", download[1].name);
    }

}
