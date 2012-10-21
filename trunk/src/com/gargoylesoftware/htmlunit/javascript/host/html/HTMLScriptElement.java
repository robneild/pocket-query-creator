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

import net.sourceforge.htmlunit.corejs.javascript.Context;

import com.gargoylesoftware.htmlunit.BrowserVersionFeatures;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomText;

/**
 * The JavaScript object that represents an "HTMLScriptElement".
 *
 * @version $Revision: 6987 $
 * @author Daniel Gredler
 * @author Marc Guillemot
 * @author Ahmed Ashour
 * @author Ronald Brill
 */
public class HTMLScriptElement extends HTMLElement {

    /**
     * Creates an instance.
     */
    public HTMLScriptElement() {
        // Empty.
    }

    /**
     * Returns the <tt>src</tt> attribute.
     * @return the <tt>src</tt> attribute
     */
    public String jsxGet_src() {
        return getDomNodeOrDie().getAttribute("src");
    }

    /**
     * Sets the <tt>src</tt> attribute.
     * @param src the <tt>src</tt> attribute
     */
    public void jsxSet_src(final String src) {
        getDomNodeOrDie().setAttribute("src", src);
    }

    /**
     * Returns the <tt>text</tt> attribute.
     * @return the <tt>text</tt> attribute
     */
    @Override
    public String jsxGet_text() {
        final StringBuilder scriptCode = new StringBuilder();
        for (final DomNode node : getDomNodeOrDie().getChildren()) {
            if (node instanceof DomText) {
                final DomText domText = (DomText) node;
                scriptCode.append(domText.getData());
            }
        }
        return scriptCode.toString();
    }

    /**
     * Sets the <tt>text</tt> attribute.
     * @param text the <tt>text</tt> attribute
     */
    public void jsxSet_text(final String text) {
        final DomNode htmlElement = getDomNodeOrDie();
        htmlElement.removeAllChildren();
        final DomNode textChild = new DomText(htmlElement.getPage(), text);
        htmlElement.appendChild(textChild);
    }

    /**
     * Returns the <tt>type</tt> attribute.
     * @return the <tt>type</tt> attribute
     */
    public String jsxGet_type() {
        return getDomNodeOrDie().getAttribute("type");
    }

    /**
     * Sets the <tt>type</tt> attribute.
     * @param type the <tt>type</tt> attribute
     */
    public void jsxSet_type(final String type) {
        getDomNodeOrDie().setAttribute("type", type);
    }

    /**
     * Returns the event handler that fires on every state change.
     * @return the event handler that fires on every state change
     */
    public Object jsxGet_onreadystatechange() {
        return getEventHandlerProp("onreadystatechange");
    }

    /**
     * Sets the event handler that fires on every state change.
     * @param handler the event handler that fires on every state change
     */
    public void jsxSet_onreadystatechange(final Object handler) {
        setEventHandlerProp("onreadystatechange", handler);
    }

    /**
     * Returns the event handler that fires on load.
     * @return the event handler that fires on load
     */
    public Object jsxGet_onload() {
        return getEventHandlerProp("onload");
    }

    /**
     * Sets the event handler that fires on load.
     * @param handler the event handler that fires on load
     */
    public void jsxSet_onload(final Object handler) {
        setEventHandlerProp("onload", handler);
    }

    /**
     * Returns the ready state of the script. This is an IE-only property.
     * @return the ready state of the script
     * @see DomNode#READY_STATE_UNINITIALIZED
     * @see DomNode#READY_STATE_LOADING
     * @see DomNode#READY_STATE_LOADED
     * @see DomNode#READY_STATE_INTERACTIVE
     * @see DomNode#READY_STATE_COMPLETE
     */
    public String jsxGet_readyState() {
        final DomNode node = getDomNodeOrDie();
        return node.getReadyState();
    }

    /**
     * Overwritten for special IE handling.
     *
     * @param childObject the node to add to this node
     * @return the newly added child node
     */
    public Object jsxFunction_appendChild(final Object childObject) {
        if (getBrowserVersion().hasFeature(
                BrowserVersionFeatures.JS_SCRIPT_APPEND_CHILD_THROWS_EXCEPTION)) {
            throw Context.reportRuntimeError("Unexpected call to method or property access");
        }
        return super.jsxFunction_appendChild(childObject);
    }

    /**
     * Overwritten for special IE handling.
     *
     * @param args the arguments
     * @return the newly added child node
     */
    protected Object jsxFunction_insertBefore(final Object[] args) {
        if (getBrowserVersion().hasFeature(
                BrowserVersionFeatures.JS_SCRIPT_INSERT_BEFORE_THROWS_EXCEPTION)) {
            throw Context.reportRuntimeError("Unexpected call to method or property access");
        }
        return super.jsxFunction_insertBefore(args);
    }
}
