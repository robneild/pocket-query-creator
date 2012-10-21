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

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.NamedNodeMap;

import net.sourceforge.htmlunit.corejs.javascript.Context;

import com.gargoylesoftware.htmlunit.BrowserVersionFeatures;
import com.gargoylesoftware.htmlunit.html.DomDocumentType;

/**
 * A JavaScript object for a DocumentType.
 *
 * @version $Revision: 6801 $
 * @author Ahmed Ashour
 * @see <a href="http://msdn.microsoft.com/en-us/library/ms762752.aspx">MSDN documentation</a>
 * @see <a href="http://www.xulplanet.com/references/objref/DocumentType.html">XUL Planet</a>
 */
public class DocumentType extends Node {

    /**
     * Returns the name.
     * @return the name
     */
    public String jsxGet_name() {
        final String name = ((DomDocumentType) getDomNodeOrDie()).getName();
        if ("html".equals(name) && "FF3".equals(getBrowserVersion().getNickname())) {
            return "HTML";
        }
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String jsxGet_nodeName() {
        return jsxGet_name();
    }

    /**
     * Returns the publicId.
     * @return the publicId
     */
    public String jsxGet_publicId() {
        return ((DomDocumentType) getDomNodeOrDie()).getPublicId();
    }

    /**
     * Returns the systemId.
     * @return the systemId
     */
    public String jsxGet_systemId() {
        return ((DomDocumentType) getDomNodeOrDie()).getSystemId();
    }

    /**
     * Returns the internal subset.
     * @return the internal subset
     */
    public String jsxGet_internalSubset() {
        final String subset = ((DomDocumentType) getDomNodeOrDie()).getInternalSubset();
        if (StringUtils.isNotEmpty(subset)) {
            return subset;
        }

        if (getBrowserVersion().hasFeature(BrowserVersionFeatures.JS_DOCTYPE_INTERNALSUBSET_EMPTY_STRING)) {
            return "";
        }
        return null;
    }

    /**
     * Returns entities.
     * @return entities
     */
    public Object jsxGet_entities() {
        final NamedNodeMap entities = ((DomDocumentType) getDomNodeOrDie()).getEntities();
        if (null != entities) {
            return entities;
        }

        if (getBrowserVersion().hasFeature(BrowserVersionFeatures.JS_DOCTYPE_ENTITIES_NULL)) {
            return null;
        }
        if (getBrowserVersion().hasFeature(BrowserVersionFeatures.JS_DOCTYPE_ENTITIES_EMPTY_STRING)) {
            return "";
        }
        return Context.getUndefinedValue();
    }

    /**
     * Returns notations.
     * @return notations
     */
    public Object jsxGet_notations() {
        final NamedNodeMap notations = ((DomDocumentType) getDomNodeOrDie()).getNotations();
        if (null != notations) {
            return notations;
        }

        if (getBrowserVersion().hasFeature(BrowserVersionFeatures.JS_DOCTYPE_NOTATIONS_NULL)) {
            return null;
        }
        if (getBrowserVersion().hasFeature(BrowserVersionFeatures.JS_DOCTYPE_NOTATIONS_EMPTY_STRING)) {
            return "";
        }
        return Context.getUndefinedValue();
    }
}
