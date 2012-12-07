package org.pquery.webdriver.parser;

import java.util.ArrayList;
import java.util.List;
import org.pquery.dao.PQ;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

public class PocketQueryPage {
    
    private Source parsedHtml;
    
    public PocketQueryPage(Source parsedHtml) {
        this.parsedHtml = parsedHtml;
    }
    
    /**
     * <thead><tr>
     * <tr id="ctl00_ContentBody_PQDownloadList_uxDownloadPQList_ctl01_trPQDownloadRow">
     * <tr class="TableFooter">
     * @return
     */
    public PQ[] getReadyForDownload() {
        Element table = getReadyForDownloadTable();
        
        List<Element> rows = table.getAllElements(HTMLElementName.TR);
        if (rows.size()<=2)
            return new PQ[0];
        
        ArrayList<PQ> ret = new ArrayList<PQ>();
        
        for (int i=1; i<rows.size()-1; i++)
        {
            //if (rows.get(i).getAttributes().get("class")==null)
                ret.add(decodeDownloadRow(rows.get(i)));
        }
        return ret.toArray(new PQ[0]);
    }
    
    public PQ decodeDownloadRow(Element row) {
        List<Element> column = row.getAllElements(HTMLElementName.TD);
        
        PQ down = new PQ();
        
        // Column 2. Extract title, which is wrapped in a link
        down.name = column.get(2).getTextExtractor().toString();
        
        // Column 2. Target of hyperlink is URL to download PQ
        down.url = column.get(2).getFirstStartTag(HTMLElementName.A).getElement().getAttributeValue("href");
        
        // Column 3. String size
        down.size = column.get(3).getTextExtractor().toString();
        // Column 4. Number of caches matching PQ
        down.waypoints = column.get(4).getTextExtractor().toString();
        // Column 5. Time last generated
        down.age = column.get(5).getTextExtractor().toString();
        
        return down;
    }
    
    public Element getReadyForDownloadTable() {
        return parsedHtml.getElementById("uxOfflinePQTable");
    }
    
//        Matcher m = Pattern.compile("<table id=\"uxOfflinePQTable\"(.+?)<\table>", DOTALL+CASE_INSENSITIVE).matcher(html);
//        if (m.find()) {
//            return m.group(1);
//        }
//        throw new ParseException("Unable to find 'Ready to download' table");
//    }
    
    
    /**
     * <a href="/pocket/downloadpq.ashx?g=e6fe1c12-a8ca-4f39-ab43-d1f4c6b6978c">
                    10-20-12 6.57 PM</a>
            </td>
    <td class="AlignRight">
                174.75 KB
            </td>
    <td class="AlignCenter">
                159
            </td>
     */
    

}