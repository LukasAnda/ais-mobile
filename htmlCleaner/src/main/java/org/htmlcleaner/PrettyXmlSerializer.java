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
 * <p>Pretty XML serializer - creates resulting XML with indenting lines.</p>
 */
public class PrettyXmlSerializer extends XmlSerializer {

    private static final String DEFAULT_INDENTATION_STRING = "\t";

    private String indentString = DEFAULT_INDENTATION_STRING;
    private List<String> indents = new ArrayList<String>();

    public PrettyXmlSerializer(CleanerProperties props) {
        this(props, DEFAULT_INDENTATION_STRING);
    }

    public PrettyXmlSerializer(CleanerProperties props, String indentString) {
        super(props);
        this.indentString = indentString;
    }

    @Override
    protected void serialize(TagNode tagNode, Writer writer) throws IOException {
        serializePrettyXml(tagNode, writer, 0);
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

                // if first item trims it from left
                if (isFirst) {
                    content = ltrim(content);
                }

                // if last item trims it from right
                if (!childrenIt.hasNext()) {
                    content = rtrim(content);
                }

                if (content.indexOf("\n") >= 0 || content.indexOf("\r") >= 0) {
                    return null;
                }
                result.append(content);
            }

            isFirst = false;
        }

        return result.toString();
    }

    protected void serializePrettyXml(TagNode tagNode, Writer writer, int level) throws IOException {
        List<? extends BaseToken> tagChildren = tagNode.getAllChildren();
        boolean isHeadlessNode = Utils.isEmptyString(tagNode.getName());
        String indent = isHeadlessNode ? "" : getIndent(level);

        writer.write(indent);
        serializeOpenTag(tagNode, writer, true);

        if (!isMinimizedTagSyntax(tagNode)) {
            String singleLine = getSingleLineOfChildren(tagChildren);
            boolean dontEscape = dontEscape(tagNode);
            if (singleLine != null) {
                if (!dontEscape(tagNode)) {
                    writer.write(escapeXml(singleLine));
                } else {
                    writer.write(singleLine.replaceAll("]]>", "]]&gt;"));
                }
            } else {
                if (!isHeadlessNode) {
                    writer.write("\n");
                }
                for (Object child : tagChildren) {
                    if (child instanceof TagNode) {
                        serializePrettyXml((TagNode) child, writer, isHeadlessNode ? level : level + 1);
                    } else if (child instanceof CData) {
                        serializeCData((CData) child, tagNode, writer);
                    } else if (child instanceof ContentNode) {
                        String content = dontEscape ? child.toString().replaceAll("]]>", "]]&gt;") : escapeXml(child.toString());
                        writer.write(getIndentedText(content, isHeadlessNode ? level : level + 1));
                    } else if (child instanceof CommentNode) {
                        CommentNode commentNode = (CommentNode) child;
                        String content = commentNode.getCommentedContent();
                        writer.write(getIndentedText(content, isHeadlessNode ? level : level + 1));
                    }
                }
            }

            if (singleLine == null) {
                writer.write(indent);
            }

            serializeEndTag(tagNode, writer, true);
        }
    }

    /**
     * Trims specified string from left.
     *
     * @param s
     */
    private String ltrim(String s) {
        if (s == null) {
            return null;
        }

        int index = 0;
        int len = s.length();

        while (index < len && Character.isWhitespace(s.charAt(index))) {
            index++;
        }

        return (index >= len) ? "" : s.substring(index);
    }

    /**
     * Trims specified string from right.
     *
     * @param s
     */
    private String rtrim(String s) {
        if (s == null) {
            return null;
        }

        int len = s.length();
        int index = len;

        while (index > 0 && Character.isWhitespace(s.charAt(index - 1))) {
            index--;
        }

        return (index <= 0) ? "" : s.substring(0, index);
    }
}