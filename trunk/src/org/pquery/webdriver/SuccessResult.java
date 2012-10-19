package org.pquery.webdriver;

import android.os.Bundle;
import android.text.Html;

public class SuccessResult extends ResultInfo {

	public String successMessageHtml;
	public String successMessage;
	
	public SuccessResult(String successMessageHtml) {
		this.successMessageHtml = successMessageHtml;
		this.successMessage = Html.fromHtml(successMessageHtml).toString();       // strip html
	}
	
	public SuccessResult(Bundle bundle) {
		this(bundle.getString("successMessageHtml"));
	}
	
	public void saveToBundle(Bundle bundle) {
		bundle.putInt("msgType", SUCCESS);
		bundle.putString("successMessageHtml", successMessageHtml);
	}
}
