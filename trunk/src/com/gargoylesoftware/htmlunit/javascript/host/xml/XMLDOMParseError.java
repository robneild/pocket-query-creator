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
package com.gargoylesoftware.htmlunit.javascript.host.xml;

import com.gargoylesoftware.htmlunit.javascript.SimpleScriptable;

/**
 * A JavaScript object for XMLDOMParseError.
 * @see <a href="http://msdn2.microsoft.com/en-us/library/ms757019.aspx">MSDN documentation</a>
 *
 * @version $Revision: 6701 $
 * @author Ahmed Ashour
 */
public class XMLDOMParseError extends SimpleScriptable {

    private int errorCode_;
    private int filepos_;
    private int line_;
    private int linepos_;
    private String reason_ = "";
    private String srcText_ = "";
    private String url_ = "";

    /**
     * Returns the error code of the last parse error.
     * @return the error code of the last parse error
     */
    public int jsxGet_errorCode() {
        return errorCode_;
    }

    /**
     * Returns the absolute file position where the error occurred.
     * @return the absolute file position where the error occurred
     */
    public int jsxGet_filepos() {
        return filepos_;
    }

    /**
     * Returns the line number that contains the error.
     * @return the line number that contains the error
     */
    public int jsxGet_line() {
        return line_;
    }

    /**
     * Returns the character position within the line where the error occurred.
     * @return the character position within the line where the error occurred
     */
    public int jsxGet_linepos() {
        return linepos_;
    }

    /**
     * Returns the reason for the error.
     * @return the reason for the error
     */
    public String jsxGet_reason() {
        return reason_;
    }

    /**
     * Returns the full text of the line containing the error.
     * @return the full text of the line containing the error
     */
    public String jsxGet_srcText() {
        return srcText_;
    }

    /**
     * Returns the URL of the XML document containing the last error.
     * @return the URL of the XML document containing the last error
     */
    public String jsxGet_url() {
        return url_;
    }

    void setErrorCode(final int errorCode) {
        errorCode_ = errorCode;
    }

    void setFilepos(final int filepos) {
        filepos_ = filepos;
    }

    void setLine(final int line) {
        line_ = line;
    }

    void setLinepos(final int linepos) {
        linepos_ = linepos;
    }

    void setReason(final String reason) {
        reason_ = reason;
    }

    void setSrcText(final String srcText) {
        srcText_ = srcText;
    }

    void setUrl(final String url) {
        url_ = url;
    }
}
