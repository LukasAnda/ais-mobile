/*
 * Copyright 2020 Lukáš Anda. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.htmlcleaner;

public class CData extends ContentNode implements HtmlNode {

    public static final String BEGIN_CDATA = "<![CDATA[";
    public static final String END_CDATA = "]]>";
    public static final String SAFE_BEGIN_CDATA = "/*<![CDATA[*/";
    public static final String SAFE_END_CDATA = "/*]]>*/";
    public static final String SAFE_BEGIN_CDATA_ALT = "//<![CDATA[";
    public static final String SAFE_END_CDATA_ALT = "//]]>";

    public CData(String content) {
        super(content);
    }

    public String getContentWithoutStartAndEndTokens() {
        return this.content;
    }

    /* (non-Javadoc)
     * @see org.htmlcleaner.ContentNode#getContent()
     */
    @Override
    public String getContent() {
        return getContentWithoutStartAndEndTokens();
    }

    /* (non-Javadoc)
     * @see org.htmlcleaner.ContentNode#toString()
     */
    @Override
    public String toString() {
        return getContentWithStartAndEndTokens();
    }

    public String getContentWithStartAndEndTokens() {
        return SAFE_BEGIN_CDATA + this.content + SAFE_END_CDATA;
    }


}
