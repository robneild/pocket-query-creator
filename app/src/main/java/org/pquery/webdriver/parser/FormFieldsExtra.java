package org.pquery.webdriver.parser;

import net.htmlparser.jericho.FormControlType;
import net.htmlparser.jericho.FormField;
import net.htmlparser.jericho.FormFields;

import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public class FormFieldsExtra {

    private FormFields form;

    public FormFieldsExtra(FormFields wrappedForm) {
        this.form = wrappedForm;
    }


    public void setValueChecked(String name, String value) throws ParseException {

        if (form.get(name) == null)
            throw new ParseException(name + " missing");

        boolean success = form.setValue(name, value);

        if (!success)
            throw new ParseException(name);
    }

    public void checkValue(String name, String value) throws ParseException {
        if (form.get(name) == null)
            throw new ParseException(name + " missing");

        if (value.equals(form.get(name).getPredefinedValues().iterator().next())) {
            return;
        }
        throw new ParseException(name);
    }

    public void setValue(String name, String value) throws ParseException {
        if (form.get(name) == null)
            throw new ParseException(name);
        form.setValue(name, value);
    }

    public void deleteValue(String name) {
        form.remove(form.get(name));
    }

    //    if (type == FormControlType.SUBMIT) {
    //        form.setValue(name, value);
    //        form.a
    //        for (String predef : field.getPredefinedValues()) {
    //            if (predef.equals(value))
    //                success = true;
    //        }
    //    } else {


    public List<BasicNameValuePair> toNameValuePairs() {

        ArrayList<BasicNameValuePair> pairList = new ArrayList<BasicNameValuePair>();

        for (FormField field : form) {

            if (field.getFormControl().getFormControlType() == FormControlType.SUBMIT)
                pairList.add(new BasicNameValuePair(field.getName(), field.getPredefinedValues().iterator().next()));
            else {
                // Usually the field will only have one value
                // However, for a multiple select control, there will be multiple values e.g. country
                for (String value : field.getValues()) {
                    pairList.add(new BasicNameValuePair(field.getName(), value));
                }
            }
        }

        return pairList;
    }

    public void addValue(String name, String value) throws ParseException {
        boolean success = form.addValue(name, value);

        if (!success)
            throw new ParseException(name);
    }

}