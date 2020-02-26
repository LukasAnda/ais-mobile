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

public class SpecialEntity {
    private final String key;
    private final int intCode;
    // escaped value outputed when generating html
    private final String htmlString;
    // escaped value when outputting html
    private final String escapedXmlString;
    private boolean htmlSpecialEntity;

    /**
     * @param key               value between & and the ';' example 'amp' for '&amp;'
     * @param intCode
     * @param htmlString
     * @param htmlSpecialEntity entity is affected by translateSpecialEntities property setting.
     */
    public SpecialEntity(String key, int intCode, String htmlString, boolean htmlSpecialEntity) {
        this.key = key;
        this.intCode = intCode;
        String str = "&" + key + ";";
        if (htmlString != null) {
            this.htmlString = htmlString;
        } else {
            this.htmlString = str;
        }
        if (htmlSpecialEntity) {
            this.escapedXmlString = String.valueOf((char) this.intCode);
        } else {
            this.escapedXmlString = str;
        }
        this.htmlSpecialEntity = htmlSpecialEntity;
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @return the intCode
     */
    public int intValue() {
        return intCode;
    }

    /**
     * @return the domString
     */
    public String getHtmlString() {
        return htmlString;
    }

    public String getEscapedXmlString() {
        return this.escapedXmlString;
    }

    public String getEscaped(boolean htmlEscaped) {
        return htmlEscaped ? this.getHtmlString() : this.getEscapedXmlString();
    }

    /**
     * @return the translateSpecialEntities
     */
    public boolean isHtmlSpecialEntity() {
        return htmlSpecialEntity;
    }

    /**
     * @return {@link #intValue()} cast to an char
     */
    public char charValue() {
        return (char) intValue();
    }

    /**
     * @return Numeric Character Reference in decimal format
     */
    public String getDecimalNCR() {
        return "&#" + intCode + ";";
    }

    /**
     * @return Numeric Character Reference in hex format
     */
    public String getHexNCR() {
        return "&#x" + Integer.toHexString(intCode) + ";";
    }

    /**
     * @return Escaped value of the entity
     */
    public String getEscapedValue() {
        return "&" + key + ";";
    }
}