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

package org.htmlcleaner.conditional;

import org.htmlcleaner.TagNode;

/**
 * Checks if node has specified attribute with specified value.
 */
public class TagNodeAttValueCondition implements ITagNodeCondition {
    private String attName;
    private String attValue;
    private boolean isCaseSensitive;

    public TagNodeAttValueCondition(String attName, String attValue, boolean isCaseSensitive) {
        this.attName = attName;
        this.attValue = attValue;
        this.isCaseSensitive = isCaseSensitive;
    }

    public boolean satisfy(TagNode tagNode) {
        if (tagNode == null || attName == null || attValue == null) {
            return false;
        } else {
            return isCaseSensitive ?
                    attValue.equals(tagNode.getAttributeByName(attName)) :
                    attValue.equalsIgnoreCase(tagNode.getAttributeByName(attName));
        }
    }
}