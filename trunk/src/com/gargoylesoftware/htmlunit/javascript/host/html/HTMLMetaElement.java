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

/**
 * The JavaScript object "HTMLMetaElement".
 *
 * @version $Revision: 6701 $
 * @author Ahmed Ashour
 */
public class HTMLMetaElement extends HTMLElement {

    /**
     * Creates an instance.
     */
    public HTMLMetaElement() {
        // Empty.
    }

    /**
     * Returns "charset" attribute.
     * @return the charset attribute
     */
    public String jsxGet_charset() {
        return "";
    }

    /**
     * Sets the "charset" attribute.
     * @param charset the charset attribute
     */
    public void jsxSet_charset(final String charset) {
        //empty
    }

    /**
     * Returns "content" attribute.
     * @return the content attribute
     */
    public String jsxGet_content() {
        return getDomNodeOrDie().getAttribute("content");
    }

    /**
     * Sets the "content" attribute.
     * @param content the content attribute
     */
    public void jsxSet_content(final String content) {
        getDomNodeOrDie().setAttribute("content", content);
    }

    /**
     * Returns "http-equiv" attribute.
     * @return the http-equiv attribute
     */
    public String jsxGet_httpEquiv() {
        return getDomNodeOrDie().getAttribute("http-equiv");
    }

    /**
     * Sets the "http-equiv" attribute.
     * @param httpEquiv the http-equiv attribute
     */
    public void jsxSet_httpEquiv(final String httpEquiv) {
        getDomNodeOrDie().setAttribute("http-equiv", httpEquiv);
    }

    /**
     * Returns "name" attribute.
     * @return the name attribute
     */
    public String jsxGet_name() {
        return getDomNodeOrDie().getAttribute("name");
    }

    /**
     * Sets the "name" attribute.
     * @param name the name attribute
     */
    public void jsxSet_name(final String name) {
        getDomNodeOrDie().setAttribute("name", name);
    }

    /**
     * Returns "scheme" attribute.
     * @return the scheme attribute
     */
    public String jsxGet_scheme() {
        return getDomNodeOrDie().getAttribute("scheme");
    }

    /**
     * Sets the "scheme" attribute.
     * @param scheme the scheme attribute
     */
    public void jsxSet_scheme(final String scheme) {
        getDomNodeOrDie().setAttribute("scheme", scheme);
    }

    /**
     * Returns "url" attribute.
     * @return the url attribute
     */
    public String jsxGet_url() {
        return "";
    }

    /**
     * Sets the "url" attribute.
     * @param url the url attribute
     */
    public void jsxSet_url(final String url) {
        //empty
    }

}
