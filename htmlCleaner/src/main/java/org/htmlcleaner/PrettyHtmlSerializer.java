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
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * <p>Pretty HTML serializer - creates resulting HTML with indenting lines.</p>
 */
public class PrettyHtmlSerializer extends HtmlSerializer {

    private static final String DEFAULT_INDENTATION_STRING = "\t";

    private String indentString = DEFAULT_INDENTATION_STRING;
    private List<String> indents = new ArrayList<String>();

    public PrettyHtmlSerializer(CleanerProperties props) {
        this(props, DEFAULT_INDENTATION_STRING);
    }

    public PrettyHtmlSerializer(CleanerProperties props, String indentString) {
        super(props);
        this.indentString = indentString;
    }

    protected void serialize(TagNode tagNode, Writer writer) throws IOException {
        serializePrettyHtml(tagNode, writer, 0, false, true);
    }

    /**
     * @param level
     * @return Appropriate indentation for the specified depth.
     */
    private synchronized String getIndent(int level) {
        int size = indents.size();
        if (size <= level) {
            String prevIndent = size == 0 ? null : indents.get(size - 1);
            for (int i = size; i <= level; i++) {
                String currIndent = prevIndent == null ? "" : prevIndent + indentString;
                indents.add(currIndent);
                prevIndent = currIndent;
            }
        }

        return indents.get(level);
    }

    private String getIndentedText(String content, int level) {
        String indent = getIndent(level);
        StringBuilder result = new StringBuilder(content.length());
        StringTokenizer tokenizer = new StringTokenizer(content, "\n\r");

        while (tokenizer.hasMoreTokens()) {
            String line = tokenizer.nextToken().trim();
            if (!"".equals(line)) {
                result.append(indent).append(line).append("\n");
            }
        }

        return result.toString();
    }

    private String getSingleLineOfChildren(List<? extends BaseToken> children) {
        StringBuilder result = new StringBuilder();
        Iterator<? extends BaseToken> childrenIt = children.iterator();
        boolean isFirst = true;

        while (childrenIt.hasNext()) {
            Object child = childrenIt.next();

            if (!(child instanceof ContentNode)) {
                return null;
            } else {
                String content = child.toString();

                //
                // Removed the trim function as this has the potential
                // to cause issues with actual content without adding
                // any value
                //
                
                /*
                // if first item trims it from left
                if (isFirst) {
                	content = Utils.ltrim(content);
                }

                // if last item trims it from right
                if (!childrenIt.hasNext()) {
                	content = Utils.rtrim(content);
                }
                */

                if (content.indexOf("\n") >= 0 || content.indexOf("\r") >= 0) {
                    return null;
                }
                result.append(content);
            }

            isFirst = false;
        }

        return result.toString();
    }

    protected void serializePrettyHtml(TagNode tagNode, Writer writer, int level, boolean isPreserveWhitespaces, boolean isLastNewLine) throws IOException {
        List<? extends BaseToken> tagChildren = tagNode.getAllChildren();
        String tagName = tagNode.getName();
        boolean isHeadlessNode = Utils.isEmptyString(tagName);
        String indent = isHeadlessNode ? "" : getIndent(level);

        if (!isPreserveWhitespaces) {
            if (!isLastNewLine) {
                writer.write("\n");
            }
            writer.write(indent);
        }
        serializeOpenTag(tagNode, writer, true);

        boolean preserveWhitespaces = isPreserveWhitespaces || "pre".equalsIgnoreCase(tagName);

        boolean lastWasNewLine = false;

        if (!isMinimizedTagSyntax(tagNode)) {
            String singleLine = getSingleLineOfChildren(tagChildren);
            boolean dontEscape = dontEscape(tagNode);
            if (!preserveWhitespaces && singleLine != null) {
                writer.write(!dontEscape(tagNode) ? escapeText(singleLine) : singleLine);
            } else {
                Iterator<? extends BaseToken> childIterator = tagChildren.iterator();
                while (childIterator.hasNext()) {
                    Object child = childIterator.next();
                    if (child instanceof TagNode) {
                        serializePrettyHtml((TagNode) child, writer, isHeadlessNode ? level : level + 1, preserveWhitespaces, lastWasNewLine);
                        lastWasNewLine = false;
                    } else if (child instanceof ContentNode) {
                        String content = dontEscape ? child.toString() : escapeText(child.toString());
                        if (content.length() > 0) {
                            if (dontEscape || preserveWhitespaces) {
                                writer.write(content);
                            } else if (Character.isWhitespace(content.charAt(0))) {
                                if (!lastWasNewLine) {
                                    writer.write("\n");
                                    lastWasNewLine = false;
                                }
                                if (content.trim().length() > 0) {
                                    writer.write(getIndentedText(Utils.rtrim(content), isHeadlessNode ? level : level + 1));
                                } else {
                                    lastWasNewLine = true;
                                }
                            } else {
                                if (content.trim().length() > 0) {
                                    writer.write(Utils.rtrim(content));
                                }
                                if (!childIterator.hasNext()) {
                                    writer.write("\n");
                                    lastWasNewLine = true;
                                }
                            }
                        }
                    } else if (child instanceof CommentNode) {

                        if (!lastWasNewLine && !preserveWhitespaces) {
                            writer.write("\n");
                            lastWasNewLine = false;
                        }
                        CommentNode commentNode = (CommentNode) child;
                        String content = commentNode.getCommentedContent();
                        writer.write(dontEscape ? content : getIndentedText(content, isHeadlessNode ? level : level + 1));
                    }
                }
            }

            if (singleLine == null && !preserveWhitespaces) {
                if (!lastWasNewLine) {
                    writer.write("\n");
                }
                writer.write(indent);
            }

            serializeEndTag(tagNode, writer, false);
        }
    }

}