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

import org.htmlcleaner.ContentNode;
import org.htmlcleaner.ITagInfoProvider;
import org.htmlcleaner.TagInfo;
import org.htmlcleaner.TagNode;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.htmlcleaner.Display.none;
import static org.htmlcleaner.Utils.isEmptyString;

/**
 * Checks if node is an <b>inline</b>  0r block element and has empty contents or white/non-breakable spaces only. Nodes that have
 * non-empty id attribute are considered to be non-empty, since they can be used in javascript scenarios.
 * <p>
 * Examples that should be pruned,
 * <pre>
 * <u>  </u>
 * <table><tr><td></td</tr></table>
 * </pre>
 * <p>
 * Examples of code that should NOT be pruned:
 *
 * <pre>
 * <p><img/></p> - no content but image tags do not have text content.
 * <table<tr><td/><td>hi</td></tr> - the first (empty) td is a placeholder so the second td is in the correct column
 * </pre>
 *
 * @author Konstantin Burov
 */
public class TagNodeEmptyContentCondition implements ITagNodeCondition {

    private static final String ID_ATTRIBUTE_NAME = "id";

    /**
     * Removal of element from this set can affect layout too hard.
     */
    private static final Set<String> unsafeBlockElements = new HashSet<String>();

    static {
        // cannot just remove a td unless removing the entire row. td's are place holders
        unsafeBlockElements.add("td");
        unsafeBlockElements.add("th");
    }

    private ITagInfoProvider tagInfoProvider;

    public TagNodeEmptyContentCondition(ITagInfoProvider provider) {
        this.tagInfoProvider = provider;
    }

    public boolean satisfy(TagNode tagNode) {
        return satisfy(tagNode, false);
    }

    private boolean satisfy(TagNode tagNode, boolean override) {
        String name = tagNode.getName();
        TagInfo tagInfo = tagInfoProvider.getTagInfo(name);
        //Only _block_ elements can match.
        if (tagInfo != null && !hasIdAttributeSet(tagNode) && none != tagInfo.getDisplay() && !tagInfo.isEmptyTag() && (override || !unsafeBlockElements.contains(name))) {
            CharSequence contentString = tagNode.getText();
            if (isEmptyString(contentString)) {
                // even though there may be no text need to make sure all children are empty or can be pruned
                if (tagNode.isEmpty()) {
                    return true;
                } else {
                    for (Object child : tagNode.getAllChildren()) {
                        // TODO : similar check as in tagNode.isEmpty() argues for a visitor pattern
                        // but allow empty td, ths to be pruned.
                        if (child instanceof TagNode) {
                            if (!satisfy((TagNode) child, true)) {
                                return false;
                            }
                        } else if (child instanceof ContentNode) {
                            if (!((ContentNode) child).isBlank()) {
                                return false;
                            }
                        } else {
                            return false;
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasIdAttributeSet(TagNode tagNode) {
        Map<String, String> attributes = tagNode.getAttributes();
        return !isEmptyString(attributes.get(ID_ATTRIBUTE_NAME));
    }

}