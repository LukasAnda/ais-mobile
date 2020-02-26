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
 * Remove empty autogenerated nodes. These nodes are created when an unclosed tag is immediately closed.
 *
 * @author patmoore
 */
public class TagNodeAutoGeneratedCondition implements ITagNodeCondition {

    public static final TagNodeAutoGeneratedCondition INSTANCE = new TagNodeAutoGeneratedCondition();

    /**
     * @see ITagNodeCondition#satisfy(org.htmlcleaner.TagNode)
     */
    public boolean satisfy(TagNode tagNode) {
        // auto-generated node that is not needed.
        return tagNode.isAutoGenerated() && tagNode.isEmpty();
    }

    @Override
    public String toString() {
        return "auto generated tagNode";
    }
}
