package org.pquery.util;


import java.util.HashMap;
import java.util.Map;

public class Parser {

    private String html;

    public Parser(String html) {
        this.html = html;
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

    /**
     * Page at http://www.geocaching.com/pocket/default.aspx
     * <p/>
     * In = ctl00_divSignedIn
     * Out = ctl00_divNotSignedIn
     */
    public boolean isLoggedIn() throws ParseException {

        if (html.indexOf("www.geocaching.com/login/default.aspx?RESET=Y") != -1)        // Logout link
            return true;
        if (html.indexOf("ctl00_divNotSignedIn") != -1)
            return false;

        throw new ParseException("Unable to detect login status on geocaching.com page");
    }

    /**
     * Page at http://www.geocaching.com/pocket/default.aspx
     * <p/>
     * Normal = ctl00_hlUpgrade
     * Premium = ctl00_litPMLevel
     *
     * @throws ParseException
     */
    public boolean isPremium() throws ParseException {

        if (html.contains("ctl00_litPMLevel") || html.contains("account/EditMembership.aspx"))
            return true;
        if (html.indexOf("ctl00_hlUpgrade") != -1)
            return false;

        throw new ParseException("Unable to detect user status on geocaching.com page");
    }


    public Map<String, String> extractViewState() {

        HashMap<String, String> viewStateMap = new HashMap<String, String>();

        String VIEWSTATE = "name=\"__VIEWSTATE\" id=\"__VIEWSTATE\" value=\"";

        int start = html.indexOf(VIEWSTATE);
        int end = html.indexOf("\"", start + VIEWSTATE.length());

        viewStateMap.put("__VIEWSTATE", html.substring(start + VIEWSTATE.length(), end));

        // Loop around extracting __VIEWSTATE1 etc...

        int i = 1;
        String viewState;

        while ((viewState = extractViewState(html, i)) != null) {
            Logger.d("extracted viewstate " + i);
            viewStateMap.put("__VIEWSTATE" + i, viewState);
            i++;
        }

        return viewStateMap;
    }


    private String extractViewState(String loginHtml, int i) {
        String VIEWSTATE = "name=\"__VIEWSTATE" + i + "\" id=\"__VIEWSTATE" + i + "\" value=\"";

        int start = loginHtml.indexOf(VIEWSTATE);
        int end = loginHtml.indexOf("\"", start + VIEWSTATE.length());

        if (start == -1 || end == -1)
            return null;        // not found

        return loginHtml.substring(start + VIEWSTATE.length(), end);

    }


    public class ParseException extends Exception {
        private static final long serialVersionUID = 2827583462232890549L;

        public ParseException(String message) {
            super(message);
        }
    }
}
