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
package com.gargoylesoftware.htmlunit.javascript.host;

import com.gargoylesoftware.htmlunit.History;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.html.HTMLParser;
import com.gargoylesoftware.htmlunit.html.HtmlBody;
import com.gargoylesoftware.htmlunit.html.HtmlHtml;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.SimpleScriptable;
import com.gargoylesoftware.htmlunit.javascript.background.JavaScriptJobManager;
import com.gargoylesoftware.htmlunit.javascript.host.html.HTMLDocument;

/**
 * A JavaScript object for IE's Popup.
 *
 * @version $Revision: 6701 $
 * @author Marc Guillemot
 * @author David K. Taylor
 * @author Ahmed Ashour
 * @see <a href="http://msdn.microsoft.com/en-us/library/ms535882.aspx">MSDN documentation</a>
 */
public class Popup extends SimpleScriptable {

    private boolean opened_;
    private HTMLDocument document_;

    /**
     * Creates a new instance. JavaScript objects must have a default constructor.
     */
    public Popup() {
        opened_ = false;
    }

    void init(final Window openerJSWindow) {
        // build document

        document_ = new HTMLDocument();
        document_.setPrototype(openerJSWindow.getPrototype(HTMLDocument.class));
        document_.setParentScope(this);

        final WebWindow openerWindow = openerJSWindow.getWebWindow();
        // create the "page" associated to the document
        final WebWindow popupPseudoWindow = new PopupPseudoWebWindow(openerWindow.getWebClient());
        // take the WebResponse of the opener (not really correct, but...)
        final WebResponse webResponse = openerWindow.getEnclosedPage().getWebResponse();
        final HtmlPage popupPage = new HtmlPage(null, webResponse, popupPseudoWindow);
        setDomNode(popupPage);
        popupPseudoWindow.setEnclosedPage(popupPage);
        final HtmlHtml html = (HtmlHtml) HTMLParser.getFactory(HtmlHtml.TAG_NAME).createElement(
                popupPage, HtmlHtml.TAG_NAME, null);
        popupPage.appendChild(html);
        final HtmlBody body = (HtmlBody) HTMLParser.getFactory(HtmlBody.TAG_NAME).createElement(
                popupPage, HtmlBody.TAG_NAME, null);
        html.appendChild(body);

        document_.setDomNode(popupPage);
    }

    /**
     * Returns the HTML document element in the popup.
     * @return the HTML document element in the popup
     */
    public Object jsxGet_document() {
        return document_;
    }

    /**
     * Indicates if the popup is opened.
     * @return <code>true</code> if opened
     */
    public boolean jsxGet_isOpen() {
        return opened_;
    }

    /**
     * Hides the popup.
     */
    public void jsxFunction_hide() {
        opened_ = false;
    }

    /**
     * Shows the popup.
     */
    public void jsxFunction_show() {
        opened_ = true;
    }
}

/**
 * Simple implementation of {@link WebWindow} to allow the construction of the {@link HtmlPage} associated
 * with a {@link Popup}.
 */
class PopupPseudoWebWindow implements WebWindow {

    private final WebClient webClient_;
    private Object scriptObject_;
    private Page enclosedPage_;

    PopupPseudoWebWindow(final WebClient webClient) {
        webClient_ = webClient;
        webClient_.initialize(this);
    }

    /**
     * @see com.gargoylesoftware.htmlunit.WebWindow#getEnclosedPage()
     */
    public Page getEnclosedPage() {
        return enclosedPage_;
    }

    /**
     * @see com.gargoylesoftware.htmlunit.WebWindow#getName()
     */
    public String getName() {
        throw new RuntimeException("Not supported");
    }

    /**
     * @see com.gargoylesoftware.htmlunit.WebWindow#getParentWindow()
     */
    public WebWindow getParentWindow() {
        throw new RuntimeException("Not supported");
    }

    /**
     * @see com.gargoylesoftware.htmlunit.WebWindow#getScriptObject()
     */
    public Object getScriptObject() {
        return scriptObject_;
    }

    /**
     * @see com.gargoylesoftware.htmlunit.WebWindow#getJobManager()
     */
    public JavaScriptJobManager getJobManager() {
        throw new RuntimeException("Not supported");
    }

    /**
     * @see com.gargoylesoftware.htmlunit.WebWindow#getTopWindow()
     */
    public WebWindow getTopWindow() {
        throw new RuntimeException("Not supported");
    }

    /**
     * @see com.gargoylesoftware.htmlunit.WebWindow#getWebClient()
     */
    public WebClient getWebClient() {
        return webClient_;
    }

    /**
     * @see com.gargoylesoftware.htmlunit.WebWindow#getHistory()
     */
    public History getHistory() {
        throw new RuntimeException("Not supported");
    }

    /**
     * @see com.gargoylesoftware.htmlunit.WebWindow#setEnclosedPage(com.gargoylesoftware.htmlunit.Page)
     */
    public void setEnclosedPage(final Page page) {
        enclosedPage_ = page;
        webClient_.initialize(page);
    }

    /**
     * @see com.gargoylesoftware.htmlunit.WebWindow#setName(java.lang.String)
     */
    public void setName(final String name) {
        throw new RuntimeException("Not supported");
    }

    /**
     * @see com.gargoylesoftware.htmlunit.WebWindow#setScriptObject(java.lang.Object)
     */
    public void setScriptObject(final Object scriptObject) {
        scriptObject_ = scriptObject;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isClosed() {
        return false;
    }
}
