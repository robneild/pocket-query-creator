package org.pquery.webdriver.parser;

import android.util.Pair;

import net.htmlparser.jericho.FormControl;
import net.htmlparser.jericho.FormControlType;
import net.htmlparser.jericho.FormField;
import net.htmlparser.jericho.FormFields;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to help putting values into a HTML form
 * And verify that we have actually changed it
 */
public class FormFieldsExtra {

    private FormFields form;

    public FormFieldsExtra(FormFields wrappedForm) {
        this.form = wrappedForm;
    }

    /**
     * Enable a checkbox. Verifies that everything is as expected. Expects that checkbox
     * will have a value="xx" that will be used when enabled
     *
     * @param name name of checkbox
     * @throws ParseException
     */
    public void enableCheckbox(String name) throws ParseException {

        if (form.get(name) == null)
            throw new ParseException(name + " missing");

        FormControl control = form.get(name).getFormControl();

        if (control.getFormControlType() != FormControlType.CHECKBOX)
            throw new ParseException(name + " not a checkbox");

        if (control.getPredefinedValue() == null)
            throw new ParseException(name + " checkbox has no value");

        boolean success = control.setValue(control.getPredefinedValue());

        if (!success)
            throw new ParseException(name);
    }

    public void setValueChecked(String name, String value) throws ParseException {

        if (form.get(name) == null)
            throw new ParseException(name + " missing");

        boolean success = form.setValue(name, value);

        if (!success)
            throw new ParseException(name);
    }

    /**
     * Check if something exists in the form
     * @param name html 'name'
     * @throws ParseException
     */
    public void checkValue(String name) throws ParseException {
        if (form.get(name) == null)
            throw new ParseException(name + " missing");

        return;
    }

    public void deleteValue(String name) {
        form.remove(form.get(name));
    }

    public List<Pair<String,String>> toNameValuePairs() {

        ArrayList<Pair<String,String>> pairList = new ArrayList<>();

        for (FormField field : form) {

            if (field.getFormControl().getFormControlType() == FormControlType.SUBMIT)
                pairList.add(new Pair(field.getName(), field.getPredefinedValues().iterator().next()));
            else {
                // Usually the field will only have one value
                // However, for a multiple select control, there will be multiple values e.g. country
                for (String value : field.getValues()) {
                    pairList.add(new Pair(field.getName(), value));
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