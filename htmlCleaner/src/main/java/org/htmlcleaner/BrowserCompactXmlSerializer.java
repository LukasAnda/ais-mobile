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
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.StringTokenizer;

/**
 * <p>
 * Browser compact XML serializer - creates resulting XML by stripping whitespaces wherever possible,
 * but preserving single whitespace where at least one exists. This behaviour is well suited
 * for web-browsers, which usually treat multiple whitespaces as single one, but make difference
 * between single whitespace and empty text.
 * </p>
 */
public class BrowserCompactXmlSerializer extends XmlSerializer {

    private static final String PRE_TAG = "pre";
    private static final String BR_TAG = "<br />";
    private static final String LINE_BREAK = "\n";

    public BrowserCompactXmlSerializer(CleanerProperties props) {
        super(props);
    }

    @Override
    protected void serialize(TagNode tagNode, Writer writer) throws IOException {
        serializeOpenTag(tagNode, writer, false);
        TagInfo tagInfo = props.getTagInfoProvider().getTagInfo(tagNode.getName());
        String tagName = tagInfo != null ? tagInfo.getName() : null;
        List<? extends BaseToken> tagChildren = new ArrayList<BaseToken>(tagNode.getAllChildren());
        if (!isMinimizedTagSyntax(tagNode)) {
            ListIterator<? extends BaseToken> childrenIt = tagChildren.listIterator();
            while (childrenIt.hasNext()) {
                Object item = childrenIt.next();
                if (item != null) {
                    if (item instanceof ContentNode && !PRE_TAG.equals(tagName)) {
                        String content = ((ContentNode) item).getContent();
                        content = dontEscape(tagNode) ? content.replaceAll("]]>", "]]&gt;") : escapeXml(content);
                        content = content.replaceAll("^" + SpecialEntities.NON_BREAKABLE_SPACE + "+", " ");
                        content = content.replaceAll(SpecialEntities.NON_BREAKABLE_SPACE + "+$", " ");
                        boolean whitespaceAllowed = tagInfo != null && tagInfo.getDisplay().isLeadingAndEndWhitespacesAllowed();
                        boolean writeLeadingSpace = content.length() > 0 && (Character.isWhitespace(content.charAt(0)));
                        boolean writeEndingSpace = content.length() > 1 && Character.isWhitespace(content.charAt(content.length() - 1));
                        content = content.trim();
                        if (content.length() != 0) {
                            boolean hasPrevContent = false;
                            int order = tagChildren.indexOf(item);
                            if (order >= 2) {
                                Object prev = tagChildren.get(order - 1);
                                hasPrevContent = isContentOrInline(prev);
                            }

                            if (writeLeadingSpace && (whitespaceAllowed || hasPrevContent)) {
                                writer.write(' ');
                            }

                            StringTokenizer tokenizer = new StringTokenizer(content, LINE_BREAK, true);
                            String prevToken = "";
                            while (tokenizer.hasMoreTokens()) {
                                String token = tokenizer.nextToken();
                                if (prevToken.equals(token) && prevToken.equals(LINE_BREAK)) {
                                    writer.write(BR_TAG);
                                    prevToken = "";
                                } else if (LINE_BREAK.equals(token)) {
                                    writer.write(' ');
                                } else {
                                    writer.write(token.trim());
                                }
                                prevToken = token;
                            }

                            boolean hasFollowingContent = false;
                            if (childrenIt.hasNext()) {
                                Object next = childrenIt.next();
                                hasFollowingContent = isContentOrInline(next);
                                childrenIt.previous();
                            }

                            if (writeEndingSpace && (whitespaceAllowed || hasFollowingContent)) {
                                writer.write(' ');
                            }
                        } else {
                            childrenIt.remove();
                        }
                    } else if (item instanceof ContentNode) {
                        String content = ((ContentNode) item).getContent();
                        writer.write(content);
                    } else if (item instanceof CommentNode) {
                        String content = ((CommentNode) item).getCommentedContent().trim();
                        writer.write(content);
                    } else {
                        ((BaseToken) item).serialize(this, writer);
                    }
                }
            }

            serializeEndTag(tagNode, writer, tagInfo != null && tagInfo.getDisplay().isAfterTagLineBreakNeeded());
        }
    }

    private boolean isContentOrInline(Object node) {
        boolean result = false;
        if (node instanceof ContentNode) {
            result = true;
        } else if (node instanceof TagNode) {
            TagInfo nextInfo = props.getTagInfoProvider().getTagInfo(((TagNode) node).getName());
            result = nextInfo != null && nextInfo.getDisplay() == Display.inline;
        }
        return result;
    }

}