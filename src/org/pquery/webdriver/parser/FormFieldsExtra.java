package org.pquery.webdriver.parser;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import net.htmlparser.jericho.FormControl;
import net.htmlparser.jericho.FormControlType;
import net.htmlparser.jericho.FormField;
import net.htmlparser.jericho.FormFields;

public class FormFieldsExtra {

    private FormFields form;

    public FormFieldsExtra(FormFields wrappedForm) {
        this.form = wrappedForm;
    }

    public void setValueChecked(String name, String value) throws ParseException {

        if (form.get(name)==null)
            throw new ParseException(name + " missing");

        boolean success = form.setValue(name, value);

        if (!success)
            throw new ParseException(name);
    }

    public void checkValue(String name, String value) throws ParseException {
        if (form.get(name)==null)
            throw new ParseException(name + " missing");

        if (value.equals(form.get(name).getPredefinedValues().iterator().next())) {
            return;
        }
        throw new ParseException(name);
    }

    public void setValue(String name, String value) throws ParseException {
        if (form.get(name)==null)
            throw new ParseException(name);
        form.setValue(name, value);
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
   
        for (FormField field: form) {
            
            if (field.getValues().size()>1) {
                int i = 10;
                i++;
            }

            
            if (field.getFormControl().getFormControlType() == FormControlType.CHECKBOX)
                if (field.getValues().size() == 0)
                    continue;
            
            
            
            
            int len = field.getValues().size();
            
            if (field.getValues().size()==0) {
                int i = 10;
                i++;
            }
            
            try {
                field.getValues().get(0);
            }
            catch (Exception e) {
                int i = 20;
                i++;
            }
            
            
            if (field.getFormControl().getFormControlType() == FormControlType.SUBMIT)
                pairList.add(new BasicNameValuePair(field.getName(), field.getPredefinedValues().iterator().next()));
            else
                pairList.add(new BasicNameValuePair(field.getName(), field.getValues().get(0)));
        }
        

//        ArrayList<BasicNameValuePair> pairList = new ArrayList<BasicNameValuePair>();
//
//        String[] names = form.getColumnLabels();
//        String[] values = form.getColumnValues();
//
//        for (i=0; i<names.length; i++) {
//
//            if (fields[i].getFormControl().getFormControlType() == FormControlType.SUBMIT)
//                pairList.add(new BasicNameValuePair(names[i], fields[i].getPredefinedValues().iterator().next()));
//            else
//                pairList.add(new BasicNameValuePair(names[i], values[i]));
//        }
        return pairList;
    }
}