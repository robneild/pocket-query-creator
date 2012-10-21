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

import com.gargoylesoftware.htmlunit.javascript.SimpleScriptable;

/**
 * A JavaScript object for a Screen. Combines properties from both Mozilla's DOM
 * and IE's DOM.
 *
 * @version $Revision: 6701 $
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * @author Daniel Gredler
 * @author Chris Erskine
 *
 * @see <a href="http://msdn.microsoft.com/en-us/library/ms535868.aspx">
 * MSDN documentation</a>
 * @see <a href="http://www.mozilla.org/docs/dom/domref/dom_window_ref.html">Mozilla documentation</a>
 */
public class Screen extends SimpleScriptable {

    private int left_;
    private int top_;
    private int width_;
    private int height_;
    private int colorDepth_;
    private int bufferDepth_;
    private int dpi_;
    private boolean fontSmoothingEnabled_;
    private int updateInterval_;

    /**
     * Creates an instance. JavaScript objects must have a default constructor.
     */
    public Screen() {
        left_ = 0;
        top_ = 0;
        width_ = 1024;
        height_ = 768;
        colorDepth_ = 24;
        bufferDepth_ = 24;
        dpi_ = 96;
        fontSmoothingEnabled_ = true;
        updateInterval_ = 0;
    }

    /**
     * Returns the <tt>availHeight</tt> property.
     * @return the <tt>availHeight</tt> property
     */
    public int jsxGet_availHeight() {
        return height_;
    }

    /**
     * Returns the <tt>availLeft</tt> property.
     * @return the <tt>availLeft</tt> property
     */
    public int jsxGet_availLeft() {
        return left_;
    }

    /**
     * Returns the <tt>availTop</tt> property.
     * @return the <tt>availTop</tt> property
     */
    public int jsxGet_availTop() {
        return top_;
    }

    /**
     * Returns the <tt>availWidth</tt> property.
     * @return the <tt>availWidth</tt> property
     */
    public int jsxGet_availWidth() {
        return width_;
    }

    /**
     * Returns the <tt>bufferDepth</tt> property.
     * @return the <tt>bufferDepth</tt> property
     */
    public int jsxGet_bufferDepth() {
        return bufferDepth_;
    }

    /**
     * Sets the <tt>bufferDepth</tt> property.
     * @param bufferDepth the <tt>bufferDepth</tt> property
     */
    public void jsxSet_bufferDepth(final int bufferDepth) {
        bufferDepth_ = bufferDepth;
    }

    /**
     * Returns the <tt>colorDepth</tt> property.
     * @return the <tt>colorDepth</tt> property
     */
    public int jsxGet_colorDepth() {
        return colorDepth_;
    }

    /**
     * Returns the <tt>deviceXDPI</tt> property.
     * @return the <tt>deviceXDPI</tt> property
     */
    public int jsxGet_deviceXDPI() {
        return dpi_;
    }

    /**
     * Returns the <tt>deviceYDPI</tt> property.
     * @return the <tt>deviceYDPI</tt> property
     */
    public int jsxGet_deviceYDPI() {
        return dpi_;
    }

    /**
     * Returns the <tt>fontSmoothingEnabled</tt> property.
     * @return the <tt>fontSmoothingEnabled</tt> property
     */
    public boolean jsxGet_fontSmoothingEnabled() {
        return fontSmoothingEnabled_;
    }

    /**
     * Returns the <tt>height</tt> property.
     * @return the <tt>height</tt> property
     */
    public int jsxGet_height() {
        return height_;
    }

    /**
     * Returns the <tt>left</tt> property.
     * @return the <tt>left</tt> property
     */
    public int jsxGet_left() {
        return left_;
    }

    /**
     * Sets the <tt>left</tt> property.
     * @param left the <tt>left</tt> property
     */
    public void jsxSet_left(final int left) {
        left_ = left;
    }

    /**
     * Returns the <tt>logicalXDPI</tt> property.
     * @return the <tt>logicalXDPI</tt> property
     */
    public int jsxGet_logicalXDPI() {
        return dpi_;
    }

    /**
     * Returns the <tt>logicalYDPI</tt> property.
     * @return the <tt>logicalYDPI</tt> property
     */
    public int jsxGet_logicalYDPI() {
        return dpi_;
    }

    /**
     * Returns the <tt>pixelDepth</tt> property.
     * @return the <tt>pixelDepth</tt> property
     */
    public int jsxGet_pixelDepth() {
        return colorDepth_;
    }

    /**
     * Returns the <tt>top</tt> property.
     * @return the <tt>top</tt> property
     */
    public int jsxGet_top() {
        return top_;
    }

    /**
     * Sets the <tt>top</tt> property.
     * @param top the <tt>top</tt> property
     */
    public void jsxSet_top(final int top) {
        top_ = top;
    }

    /**
     * Returns the <tt>updateInterval</tt> property.
     * @return the <tt>updateInterval</tt> property
     */
    public int jsxGet_updateInterval() {
        return updateInterval_;
    }

    /**
     * Sets the <tt>updateInterval</tt> property.
     * @param updateInterval the <tt>updateInterval</tt> property
     */
    public void jsxSet_updateInterval(final int updateInterval) {
        updateInterval_ = updateInterval;
    }

    /**
     * Returns the <tt>width</tt> property.
     * @return the <tt>width</tt> property
     */
    public int jsxGet_width() {
        return width_;
    }
}
