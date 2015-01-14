package org.pquery.webdriver.parser;

import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;

import org.pquery.dao.DownloadablePQ;
import org.pquery.dao.RepeatablePQ;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PocketQueryPage {

    private Source parsedHtml;

    public PocketQueryPage(Source parsedHtml) {
        this.parsedHtml = parsedHtml;
    }

    /**
     * <thead><tr>
     * <tr id="ctl00_ContentBody_PQDownloadList_uxDownloadPQList_ctl01_trPQDownloadRow">
     * <tr class="TableFooter">
     *
     * @return
     * @throws ParseException
     */
    public DownloadablePQ[] getReadyForDownload() throws ParseException {
        Element table = getReadyForDownloadTable();

        List<Element> rows = table.getAllElements(HTMLElementName.TR);

        // Don't think this ever happens
        if (rows.size() <= 2)
            return new DownloadablePQ[0];

        // Detect if middle row says empty

		/*
		 * If there are no downloadable DownloadablePQ then single row (between header and footer)
		 * looks like this
		 * 
		 * <tr class="BorderBottom"><td colspan="4">
		 * No Downloads Available
		 * </td></tr>
		 */
        Attribute classattrib = rows.get(1).getAttributes().get("class");
        if (classattrib != null && classattrib.getValue().equalsIgnoreCase("BorderBottom"))
            return new DownloadablePQ[0];

        ArrayList<DownloadablePQ> ret = new ArrayList<DownloadablePQ>();

        // Table has header and footer we can skip
        for (int i = 1; i < rows.size() - 1; i++) {
            ret.add(decodeDownloadRow(rows.get(i)));
        }
        return ret.toArray(new DownloadablePQ[0]);
    }

    public DownloadablePQ decodeDownloadRow(Element row) throws ParseException {
        List<Element> column = row.getAllElements(HTMLElementName.TD);

        // Trying to catch error seen here once
        if (column.size() <= 5)
            throw new ParseException("Parse Error. Missing columns in download table");

        DownloadablePQ down = new DownloadablePQ();

        // Column 2. Extract title, which is wrapped in a link
        down.name = column.get(2).getTextExtractor().toString();

        // Column 2. Target of hyperlink is URL to download DownloadablePQ
        down.url = column.get(2).getFirstStartTag(HTMLElementName.A).getElement().getAttributeValue("href");

        // Column 3. String size
        down.size = column.get(3).getTextExtractor().toString();
        // Column 4. Number of caches matching DownloadablePQ
        down.waypoints = column.get(4).getTextExtractor().toString();
        // Column 5. Time last generated
        down.age = column.get(5).getTextExtractor().toString();

        return down;
    }

    public Element getReadyForDownloadTable() {
        return parsedHtml.getElementById("uxOfflinePQTable");
    }

    public RepeatablePQ[] getRepeatables() throws ParseException {
        Element table = getActivePocketQueriesTable();

        if (table == null)
            return new RepeatablePQ[0];         // table is missing. no pq

        List<Element> rows = table.getAllElements(HTMLElementName.TR);

        if (rows.size() <= 2)
            return new RepeatablePQ[0];         // table seems to be empty. Can this happen?

        // Extrat abbreviated day of week string (these will be localized I guess)
        List<String> weekdays = new ArrayList<String>();
        Element header = rows.get(0);
        List<Element> columns = header.getAllElements(HTMLElementName.TH);
        for (int i = 5; i < 12; i++) {
            weekdays.add(columns.get(i).getTextExtractor().toString());
        }


        ArrayList<RepeatablePQ> ret = new ArrayList<RepeatablePQ>();

        // Table has header and footer we can skip
        for (int i = 1; i < rows.size() - 1; i++) {
            RepeatablePQ rpq = decodeRepeatableRow(rows.get(i), weekdays);
            if (rpq != null)
                ret.add(rpq);
        }
        return ret.toArray(new RepeatablePQ[0]);
    }


    private RepeatablePQ decodeRepeatableRow(Element row, List<String> weekdays) throws ParseException {
        List<Element> column = row.getAllElements(HTMLElementName.TD);

        // Trying to catch error seen here once
        if (column.size() <= 12)
            throw new ParseException("Parse Error. Missing columns in repeatable table");

        RepeatablePQ repeatable = new RepeatablePQ();

        // Column 4 looks like this "(500) 1-14-15 8.28 PM"

        // Column 4. Extract pocket query title, it is wrapped in a link
        Element nameElement = column.get(3).getFirstStartTag(HTMLElementName.A).getElement();

        // Filter out pocket queries that have been deleted. They are displayed with a line through their name
        // If The A link has a child element, we can assume it is <span style="strike>
        if (nameElement.getContent().getFirstElement() != null)
            return null;

        repeatable.name = nameElement.getTextExtractor().toString();

        // Column 4. Extract number of matching pq
        String waypoints = column.get(3).getTextExtractor().toString();
        Matcher m = Pattern.compile("\\((\\d+)\\)").matcher(waypoints);
        if (m.find()) {
            waypoints = m.group(1);
        }
        repeatable.waypoints = waypoints;

        // Columns 6-12. extract days of week
        for (int i = 5; i < 12; i++) {
            repeatable.addScheduleURL(column.get(i).getFirstStartTag(HTMLElementName.A).getAttributeValue("href"));
        }

        return repeatable;
    }

    /**
     * Get table shown on the first tab 'Active Pocket Queries'
     * The list of pocket queries is *different* to the Downloadable Pocket Queries
     *
     * The table is optional and can be absent if no pocket queries
     *
     * @return null if not there
     */
    private Element getActivePocketQueriesTable() {
        return parsedHtml.getElementById("pqRepeater");
    }

}
