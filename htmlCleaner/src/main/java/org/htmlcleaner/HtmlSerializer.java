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
import java.util.Map;

/**
 * <p>Abstract HTML serializer - contains common logic for descendants.</p>
 */
public abstract class HtmlSerializer extends Serializer {

    protected HtmlSerializer(CleanerProperties props) {
        super(props);
    }


    protected boolean isMinimizedTagSyntax(TagNode tagNode) {
        final TagInfo tagInfo = props.getTagInfoProvider().getTagInfo(tagNode.getName());
        return tagInfo != null && !tagNode.hasChildren() && tagInfo.isEmptyTag();
    }

    protected boolean dontEscape(TagNode tagNode) {
        return isScriptOrStyle(tagNode);
    }

    protected String escapeText(String content) {
        return Utils.escapeHtml(content, props);
    }

    protected void serializeOpenTag(TagNode tagNode, Writer writer, boolean newLine) throws IOException {
        String tagName = tagNode.getName();

        if (Utils.isEmptyString(tagName)) {
            return;
        }

        boolean nsAware = props.isNamespacesAware();

        if (!nsAware && Utils.getXmlNSPrefix(tagName) != null) {
            tagName = Utils.getXmlName(tagName);
        }

        writer.write("<" + tagName);
        for (Map.Entry<String, String> entry : tagNode.getAttributes().entrySet()) {
            String attName = entry.getKey();

            //
            // Note that because we implemented the WHATWG attribute identifier rules
            // during the tokenize stage, we'll never have invalid attribute names at
            // this point.
            //
            if (attName != null) {

                if (!nsAware && Utils.getXmlNSPrefix(attName) != null) {
                    attName = Utils.getXmlName(attName);
                }
                if (!(nsAware && attName.equalsIgnoreCase("xmlns")))
                    writer.write(" " + attName + "=\"" + escapeText(entry.getValue()) + "\"");
            }
        }

        if (nsAware) {
            Map<String, String> nsDeclarations = tagNode.getNamespaceDeclarations();
            if (nsDeclarations != null) {
                for (Map.Entry<String, String> entry : nsDeclarations.entrySet()) {
                    String prefix = entry.getKey();
                    String att = "xmlns";
                    if (prefix.length() > 0) {
                        att += ":" + prefix;
                    }
                    writer.write(" " + att + "=\"" + escapeText(entry.getValue()) + "\"");
                }
            }
        }

        if (isMinimizedTagSyntax(tagNode)) {
            writer.write(" />");
            if (newLine) {
                writer.write("\n");
            }
        } else {
            writer.write(">");
        }
    }

    protected void serializeEndTag(TagNode tagNode, Writer writer, boolean newLine) throws IOException {
        String tagName = tagNode.getName();

        if (Utils.isEmptyString(tagName)) {
            return;
        }

        if (Utils.getXmlNSPrefix(tagName) != null && !props.isNamespacesAware()) {
            tagName = Utils.getXmlName(tagName);
        }

        writer.write("</" + tagName + ">");
        if (newLine) {
            writer.write("\n");
        }
    }

}