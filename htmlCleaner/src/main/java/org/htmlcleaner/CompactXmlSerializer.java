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

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.ListIterator;

/**
 * <p>Compact XML serializer - creates resulting XML by stripping whitespaces.</p>
 */
public class CompactXmlSerializer extends XmlSerializer {

    public CompactXmlSerializer(CleanerProperties props) {
        super(props);
    }

    @Override
    protected void serialize(TagNode tagNode, Writer writer) throws IOException {
        serializeOpenTag(tagNode, writer, false);

        List<? extends BaseToken> tagChildren = tagNode.getAllChildren();
        if (!isMinimizedTagSyntax(tagNode)) {
            ListIterator<? extends BaseToken> childrenIt = tagChildren.listIterator();
            while (childrenIt.hasNext()) {
                Object item = childrenIt.next();
                if (item != null) {
                    if (item instanceof ContentNode) {
                        String content = ((ContentNode) item).getContent().trim();
                        writer.write(dontEscape(tagNode) ? content.replaceAll("]]>", "]]&gt;") : escapeXml(content));

                        if (childrenIt.hasNext()) {
                            if (!isWhitespaceString(childrenIt.next())) {
                                writer.write("\n");
                            }
                            childrenIt.previous();
                        }
                    } else if (item instanceof CommentNode) {
                        String content = ((CommentNode) item).getCommentedContent().trim();
                        writer.write(content);
                    } else {
                        ((BaseToken) item).serialize(this, writer);
                    }
                }
            }

            serializeEndTag(tagNode, writer, false);
        }
    }

    /**
     * Checks whether specified object's string representation is empty string (containing of only whitespaces).
     *
     * @param object Object whose string representation is checked
     * @return true, if empty string, false otherwise
     */
    private boolean isWhitespaceString(Object object) {
        if (object != null) {
            String s = object.toString();
            return s != null && "".equals(s.trim());
        }
        return false;
    }
}