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

import com.gargoylesoftware.htmlunit.html.DomText;

/**
 * A JavaScript object for Text.
 *
 * @version $Revision: 6701 $
 * @author David K. Taylor
 * @author Chris Erskine
 * @author Ahmed Ashour
 */
public class Text extends CharacterDataImpl {

    /**
     * Creates an instance. JavaScript objects must have a default constructor.
     */
    public Text() {
    }

    /**
     * Initialize this object.
     */
    public void initialize() {
    }

    /**
     * Split a Text node in two.
     * @param offset the character position at which to split the Text node
     * @return the Text node that was split from this node
     */
    public Object jsxFunction_splitText(final int offset) {
        final DomText domText = (DomText) getDomNodeOrDie();
        return getScriptableFor(domText.splitText(offset));
    }
}
