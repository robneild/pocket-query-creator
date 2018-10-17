package org.pquery.webdriver.parser;

import net.htmlparser.jericho.CharacterReference;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.FormFields;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.util.List;

public class GeocachingPage {

    protected String html;
    public Source parsedHtml;

    public GeocachingPage(String html) {
        this.html = html;
        this.parsedHtml = new Source(html);
    }

    public GeocachingPage(Source parsedHtml) {
        this.parsedHtml = parsedHtml;
    }

    /**
     * Look for the "Down for maintenance" Geocaching web page
     */
    public boolean isDownFormMaintenance() {
        if (html.contains("maintenance"))
            return true;
        return false;
    }

    /**
     * Does the page look like a Geocaching page. Used to detect wifi
     * logon pages etc
     * <p/>
     * All Geocaching.com pages have a title containing 'Geocaching'
     */
    public boolean isGeocachingPage() {
        if (title().contains("Geocaching"))
            return true;
        return false;
    }

    /**
     * Page at http://www.geocaching.com/pocket/default.aspx
     * <p/>
     * In = ctl00_divSignedIn Out = ctl00_divNotSignedIn
     */
    public boolean isLoggedIn() throws ParseException {

        if (html.indexOf("Object moved") != -1) {
            // After a successful login POST to the login.aspx page page is useless and doesn't
            // indicate if there was a successful login
            //
            // However, on success we get back a 302 Object moved
            // The html page also has a "Object Moved" message
            return true;
        }

        if (html.indexOf("ctl00_uxLoginStatus_divNotSignedIn") != -1) {
            return false;
        }

        if (html.indexOf("validation-summary-errors") != -1) {
            return false;       // Your password is incorrect
        }

        if (html.indexOf("ctl00_uxLoginStatus_divSignedIn") != -1) {
            return true;
        }

        throw new ParseException("Unable to detect login status on geocaching.com page");
    }

    public boolean isPremium() throws ParseException {

        // Does this appear when premium membership is coming to an end?
        if (html.contains("account/EditMembership.aspx"))
            return true;
        // Usually this means member is basic and is offered an upgrade link
        if (parsedHtml.getElementById("hlUpgrade") != null)
            return false;

        // At the moment can't find good way to detect premium
        // so assume so (must use along with login state detection)
        return true;
    }

    /**
     * Try to extract title of html page
     */
    public String title() {
        Element titleElement = parsedHtml.getFirstElement(HTMLElementName.TITLE);
        if (titleElement == null)
            return "";

        return CharacterReference.decodeCollapseWhiteSpace(titleElement.getContent());
    }

    /**
     * Extract out form values on page
     */
    public FormFields extractLoginForm() throws ParseException {

        // Do all the work
        Element loginDiv = parsedHtml.getElementById("SignupSignin");

        if (loginDiv == null) {
            throw new ParseException("Can't find login div");
        }

        FormFields formFields = loginDiv.getFormFields();

        return formFields;
    }


    /**
     * Extract out form values on page
     */
    public FormFields extractForm() throws ParseException {

        FormFields formFields = parsedHtml.getFormFields();

        return formFields;
    }

    /**
     * Detect if we appear to be back at the login page
     * This happens is a login attempt fails due to bad credentials
     */
    public boolean atLoginPage() {
        if (html.indexOf("ctl00_ContentBody_LoginPanel") != -1)
            return true;
        return false;
    }
}
