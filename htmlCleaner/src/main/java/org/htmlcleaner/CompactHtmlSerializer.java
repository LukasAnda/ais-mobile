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
 * <p>Compact HTML serializer - creates resulting HTML by stripping whitespaces wherever possible.</p>
 */
public class CompactHtmlSerializer extends HtmlSerializer {

    private int openPreTags = 0;

    public CompactHtmlSerializer(CleanerProperties props) {
        super(props);
    }

    protected void serialize(TagNode tagNode, Writer writer) throws IOException {
        boolean isPreTag = "pre".equalsIgnoreCase(tagNode.getName());
        if (isPreTag) {
            openPreTags++;
        }

        serializeOpenTag(tagNode, writer, false);

        List<? extends BaseToken> tagChildren = tagNode.getAllChildren();
        if (!isMinimizedTagSyntax(tagNode)) {
            ListIterator<? extends BaseToken> childrenIt = tagChildren.listIterator();
            while (childrenIt.hasNext()) {
                Object item = childrenIt.next();
                if (item instanceof ContentNode) {
                    String content = item.toString();
                    if (openPreTags > 0) {
                        writer.write(content);
                    } else {
                        boolean startsWithSpace = content.length() > 0 && Character.isWhitespace(content.charAt(0));
                        boolean endsWithSpace = content.length() > 1 && Character.isWhitespace(content.charAt(content.length() - 1));
                        content = dontEscape(tagNode) ? content.trim() : escapeText(content.trim());

                        if (startsWithSpace) {
                            writer.write(' ');
                        }

                        if (content.length() != 0) {
                            writer.write(content);
                            if (endsWithSpace) {
                                writer.write(' ');
                            }
                        }

                        //Removed due to issue #199
                        //if (childrenIt.hasNext()) {
                        //    if ( !Utils.isWhitespaceString(childrenIt.next()) ) {
                        //        writer.write("\n");
                        //    }
                        //    childrenIt.previous();
                        //}

                    }
                } else if (item instanceof CommentNode) {
                    String content = ((CommentNode) item).getCommentedContent().trim();
                    writer.write(content);
                } else if (item instanceof BaseToken) {
                    ((BaseToken) item).serialize(this, writer);
                }
            }

            serializeEndTag(tagNode, writer, false);
            if (isPreTag) {
                openPreTags--;
            }
        }
    }

}