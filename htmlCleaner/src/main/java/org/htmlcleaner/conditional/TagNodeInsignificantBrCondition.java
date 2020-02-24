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

import java.util.List;

/**
 * Checks if node is an insignificant br tag -- is placed at the end or at the
 * start of a block.
 *
 * @author Konstantin Burov (aectann@gmail.com)
 */
public class TagNodeInsignificantBrCondition implements ITagNodeCondition {

    private static final String BR_TAG = "br";

    public TagNodeInsignificantBrCondition() {
    }

    public boolean satisfy(TagNode tagNode) {
        if (!isBrNode(tagNode)) {
            return false;
        }
        TagNode parent = tagNode.getParent();
        List children = parent.getAllChildren();
        int brIndex = children.indexOf(tagNode);
        return checkSublist(0, brIndex, children) || checkSublist(brIndex, children.size(), children);
    }

    private boolean isBrNode(TagNode tagNode) {
        return tagNode != null && BR_TAG.equals(tagNode.getName());
    }

    private boolean checkSublist(int start, int end, List list) {
        List sublist = list.subList(start, end);
        for (Object object : sublist) {
            if (!(object instanceof TagNode)) {
                return false;
            }
            TagNode node = (TagNode) object;
            if (!isBrNode(node) && !node.isPruned()) {
                return false;
            }
        }
        return true;
    }
}
