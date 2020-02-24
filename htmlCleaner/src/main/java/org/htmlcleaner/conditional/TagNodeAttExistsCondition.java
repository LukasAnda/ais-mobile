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
 * Checks if node contains specified attribute.
 */
public class TagNodeAttExistsCondition implements ITagNodeCondition {
    private String attName;

    public TagNodeAttExistsCondition(String attName) {
        this.attName = attName;
    }

    public boolean satisfy(TagNode tagNode) {
        return tagNode == null ? false : tagNode.getAttributes().containsKey(attName.toLowerCase());
    }
}