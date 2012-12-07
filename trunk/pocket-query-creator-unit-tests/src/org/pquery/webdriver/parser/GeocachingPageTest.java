package org.pquery.webdriver.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import net.htmlparser.jericho.FormFields;

import org.apache.http.message.BasicNameValuePair;
import org.pquery.util.Util;
import org.pquery.webdriver.parser.GeocachingPage.ParseException;

import android.test.AndroidTestCase;

public class GeocachingPageTest extends AndroidTestCase {

    private GeocachingPage simple;
    private GeocachingPage createNewPocketQuery;
    private GeocachingPage createNewPocketQueryLoggedOut;
    private GeocachingPage createNewPocketQueryNonPremium;

    @Override
    protected void setUp() throws Exception {

        simple = new GeocachingPage(loadFromResource("simple_form.htm"));
        createNewPocketQuery = new GeocachingPage(loadFromResource("create_new_pocket_query.htm"));
        createNewPocketQueryLoggedOut = new GeocachingPage(loadFromResource("create_new_pocket_query_logged_out.htm"));
        createNewPocketQueryNonPremium = new GeocachingPage(loadFromResource("create_new_pocket_query_non_premium.htm"));
    }

    private String loadFromResource(String name) throws IOException {
        InputStream in = getClass().getClassLoader().getResourceAsStream("res/raw/" + name);
        assertNotNull(in);
        String ret = Util.inputStreamIntoString(in);
        assertNotNull(ret);
        return ret;
    }

    /**
     * Test Geocaching page detected on create page
     */
    public void testGeocachingDetected() throws IOException {
        assertTrue(createNewPocketQuery.isGeocachingPage());
    }

    /**
     * Test Non Geocaching page detected on simple page
     */
    public void testNonGeocachingDetected() throws IOException {
        assertFalse(simple.isGeocachingPage());
    }

    public void testLoggedIn() throws ParseException {
        assertTrue(createNewPocketQuery.isLoggedIn());
    }

    public void testLoggedOut() throws ParseException {
        assertFalse(createNewPocketQueryLoggedOut.isLoggedIn());
    }

    /**
     * If can't ascertain logged in/out status then excepton should be thrown
     */
    public void testLoggedInError() {
        try {
            assertFalse(simple.isLoggedIn());
            fail();
        } catch (ParseException e) {
        }
    }
    
    public void testPremium() throws ParseException {
        assertTrue(createNewPocketQuery.isPremium());
    }
    
    public void testNonPremium() throws ParseException {
        assertFalse(createNewPocketQueryNonPremium.isPremium());
    }
    
    public void testTitle() {
        assertTrue(createNewPocketQuery.title().contains("Pocket Queries"));
    }
    
    /**
     * Test form extraction on main, pocket query creation page
     */
    public void testTitleExtraction() throws IOException {
        String title = createNewPocketQuery.title();
        assertTrue(title.contains("Create/Edit Geocache Pocket Query"));
    }
    
    public void testTitleMissing() {
        assertEquals("", simple.title());
    }
    
    /**
     * Test on the logged out page. Test we can find the important username and password fields
     */
    public void testLoginFormExtract() {
        FormFields formFields = createNewPocketQueryLoggedOut.extractForm();
        
        assertNotNull(formFields.get("ctl00$tbUsername"));      
        assertNotNull(formFields.get("ctl00$tbPassword")); 
    }

}
