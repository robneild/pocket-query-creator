package org.pquery.webdriver;

import android.os.Bundle;
import android.text.Html;

public class FailureResult extends ResultInfo{

	public String failMessageHtml;
	public String failMessage;
	
	public FailureResult(String failMessageHtml) {
		this.failMessageHtml = failMessageHtml;
		this.failMessage = Html.fromHtml(failMessageHtml).toString();
	}
	
	public FailureResult(Bundle bundle) {
		this(bundle.getString("failMessageHtml"));
	}
	
	public void saveToBundle(Bundle bundle) {
		bundle.putString("failMessageHtml", failMessageHtml);
		bundle.putInt("msgType", FAILURE);
	}
	
	
	
}
