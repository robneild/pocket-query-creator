/*
 * Copyright (c) 2002-2012 Gargoyle Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gargoylesoftware.htmlunit.javascript.host.html;

import org.xml.sax.helpers.AttributesImpl;

import com.gargoylesoftware.htmlunit.BrowserVersionFeatures;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomText;
import com.gargoylesoftware.htmlunit.html.HTMLParser;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.javascript.host.FormChild;

/**
 * The JavaScript object that represents an option.
 *
 * @version $Revision: 6701 $
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * @author David K. Taylor
 * @author Chris Erskine
 * @author Marc Guillemot
 * @author Ahmed Ashour
 */
public class HTMLOptionElement extends FormChild {

    /**
     * Creates an instance.
     */
    public HTMLOptionElement() {
        // Empty.
    }

    /**
     * JavaScript constructor. This must be declared in every JavaScript file because
     * the rhino engine won't walk up the hierarchy looking for constructors.
     * @param newText the text
     * @param newValue the value
     * @param defaultSelected Whether the option is initially selected
     * @param selected the current selection state of the option
     */
    public void jsConstructor(final String newText, final String newValue,
            final boolean defaultSelected, final boolean selected) {
        final HtmlPage page = (HtmlPage) getWindow().getWebWindow().getEnclosedPage();
        AttributesImpl attributes = null;
        if (defaultSelected) {
            attributes = new AttributesImpl();
            attributes.addAttribute(null, "selected", "selected", null, "selected");
        }

        final HtmlOption htmlOption = (HtmlOption) HTMLParser.getFactory(HtmlOption.TAG_NAME).createElement(
                page, HtmlOption.TAG_NAME, attributes);
        htmlOption.setSelected(selected);
        setDomNode(htmlOption);

        if (!"undefined".equals(newText)) {
            htmlOption.appendChild(new DomText(page, newText));
        }
        if (!"undefined".equals(newValue)) {
            htmlOption.setValueAttribute(newValue);
        }
    }

    /**
     * Returns the value of the "value" property.
     * @return the value property
     */
    public String jsxGet_value() {
        String value = getDomNodeOrNull().getAttribute("value");
        if (value == DomElement.ATTRIBUTE_NOT_DEFINED
                && getBrowserVersion().hasFeature(BrowserVersionFeatures.GENERATED_170)) {
            value = getDomNodeOrNull().getText();
        }
        return value;
    }

    /**
     * Sets the value of the "value" property.
     * @param newValue the value property
     */
    public void jsxSet_value(final String newValue) {
        getDomNodeOrNull().setValueAttribute(newValue);
    }

    /**
     * Returns the value of the "text" property.
     * @return the text property
     */
    @Override
    public String jsxGet_text() {
        return getDomNodeOrNull().getText();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlOption getDomNodeOrNull() {
        return (HtmlOption) super.getDomNodeOrNull();
    }

    /**
     * Sets the value of the "text" property.
     * @param newText the text property
     */
    public void jsxSet_text(final String newText) {
        getDomNodeOrNull().setText(newText);
    }

    /**
     * Returns the value of the "selected" property.
     * @return the text property
     */
    public boolean jsxGet_selected() {
        return getDomNodeOrNull().isSelected();
    }

    /**
     * Sets the value of the "selected" property.
     * @param selected the new selected property
     */
    public void jsxSet_selected(final boolean selected) {
        final HtmlOption optionNode = getDomNodeOrNull();
        final HtmlSelect enclosingSelect = optionNode.getEnclosingSelect();
        if (!selected && optionNode.isSelected()
                && enclosingSelect != null && !enclosingSelect.isMultipleSelectEnabled()) {

            // un-selecting selected option has no effect in IE and selects first option in FF
            if (getBrowserVersion().hasFeature(BrowserVersionFeatures.HTMLOPTION_UNSELECT_SELECTS_FIRST)) {
                enclosingSelect.getOption(0).setSelected(true, false);
            }
        }
        else {
            optionNode.setSelected(selected, false);
        }
    }

    /**
     * Returns the value of the "defaultSelected" property.
     * @return the text property
     */
    public boolean jsxGet_defaultSelected() {
        return getDomNodeOrNull().isDefaultSelected();
    }

    /**
     * Returns the value of the "label" property.
     * @return the label property
     */
    public String jsxGet_label() {
        return getDomNodeOrNull().getLabelAttribute();
    }

    /**
     * Sets the value of the "label" property.
     * @param label the new label property
     */
    public void jsxSet_label(final String label) {
        getDomNodeOrNull().setLabelAttribute(label);
    }
}
