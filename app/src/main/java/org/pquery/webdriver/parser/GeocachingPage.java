package org.pquery.webdriver.parser;

import net.htmlparser.jericho.CharacterReference;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.FormFields;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

public class GeocachingPage {

    protected String html;
    public Source parsedHtml;

    public class ParseException extends Exception {
        private static final long serialVersionUID = 2827583462232890549L;

        public ParseException(String message) {
            super(message);
        }
    }

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

        if (html.indexOf("ctl00_ContentBody_LoggedInPanel") != -1) {
            // This is shown on the page returned by a successfully POST /login/default.aspx
            // The page usually is a 302, with an Object moved body
            return true;
        }

        if (html.indexOf("ctl00_uxLoginStatus_divSignedIn") != -1)
            // Logout link
            return true;

        if (html.indexOf("ctl00_uxLoginStatus_vsSignInWidgetForm") != -1)
            return false;

        if (html.indexOf("ctl00_ContentBody_LoginPanel") != -1) {
            // This is shown on the page returned by a failed POST /login/default.aspx
            // The page usually is a 200. It shows a standard login panel with some kind of error message
            return true;
        }

        if (html.indexOf("id=\"hlSignIn\"") != -1) {
            // Log In Link is shown
            return false;
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
    public FormFields extractForm() {

        // Do all the work

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
