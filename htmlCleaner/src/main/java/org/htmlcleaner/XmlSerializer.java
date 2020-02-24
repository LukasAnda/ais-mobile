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
import java.io.OutputStream;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

/**
 * <p>Abstract XML serializer - contains common logic for descendants.</p>
 */
public abstract class XmlSerializer extends Serializer {

    public static final String XMLNS_NAMESPACE = "xmlns";
    private boolean creatingHtmlDom;

    protected XmlSerializer(CleanerProperties props) {
        super(props);
    }

    /**
     * @return the creatingHtmlDom
     */
    public boolean isCreatingHtmlDom() {
        return creatingHtmlDom;
    }

    /**
     * @param creatingHtmlDom the creatingHtmlDom to set
     */
    public void setCreatingHtmlDom(boolean creatingHtmlDom) {
        this.creatingHtmlDom = creatingHtmlDom;
    }

    /**
     * @deprecated Use writeToStream() instead.
     */
    @Deprecated
    public void writeXmlToStream(TagNode tagNode, OutputStream out, String charset) throws IOException {
        super.writeToStream(tagNode, out, charset);
    }

    /**
     * @deprecated Use writeToStream() instead.
     */
    @Deprecated
    public void writeXmlToStream(TagNode tagNode, OutputStream out) throws IOException {
        super.writeToStream(tagNode, out);
    }

    /**
     * @deprecated Use writeToFile() instead.
     */
    @Deprecated
    public void writeXmlToFile(TagNode tagNode, String fileName, String charset) throws IOException {
        super.writeToFile(tagNode, fileName, charset);
    }

    /**
     * @deprecated Use writeToFile() instead.
     */
    @Deprecated
    public void writeXmlToFile(TagNode tagNode, String fileName) throws IOException {
        super.writeToFile(tagNode, fileName);
    }

    /**
     * @deprecated Use getAsString() instead.
     */
    @Deprecated
    public String getXmlAsString(TagNode tagNode, String charset) {
        return super.getAsString(tagNode, charset);
    }

    /**
     * @deprecated Use getAsString() instead.
     */
    @Deprecated
    public String getXmlAsString(TagNode tagNode) {
        return super.getAsString(tagNode);
    }

    /**
     * @deprecated Use write() instead.
     */
    @Deprecated
    public void writeXml(TagNode tagNode, Writer writer, String charset) throws IOException {
        super.write(tagNode, writer, charset);
    }

    protected String escapeXml(String xmlContent) {
        return Utils.escapeXml(xmlContent, props, isCreatingHtmlDom());
    }

    protected boolean dontEscape(TagNode tagNode) {
        return props.isUseCdataFor(tagNode.getName());
    }

    protected boolean isMinimizedTagSyntax(TagNode tagNode) {
        final TagInfo tagInfo = props.getTagInfoProvider().getTagInfo(tagNode.getName());
        return tagNode.isEmpty() && (tagInfo == null || tagInfo.isMinimizedTagPermitted()) &&
                (props.isUseEmptyElementTags() || (tagInfo != null && tagInfo.isEmptyTag()));
    }

    protected void serializeOpenTag(TagNode tagNode, Writer writer) throws IOException {
        serializeOpenTag(tagNode, writer, true);
    }

    /**
     * Serialize a CDATA section. If the context is a script or style tag, and
     * using CDATA for script and style is set to true, then we just write the
     * actual content, as the whole section is wrapped in CDATA tokens.
     * Otherwise we escape the content as if it were regular text.
     *
     * @param item    the CDATA instance
     * @param tagNode the TagNode within which the CDATA appears
     * @param writer  the writer to output to
     * @throws IOException
     */
    protected void serializeCData(CData item, TagNode tagNode, Writer writer) throws IOException {
        if (dontEscape(tagNode)) {
            writer.write(item.getContentWithoutStartAndEndTokens());
        } else {
            writer.write(escapeXml(item.getContentWithStartAndEndTokens()));
        }
    }

    /**
     * Serialize a content token, escaping where necessary.
     *
     * @param item    the content token to serialize
     * @param tagNode the TagNode within which the content token appears
     * @param writer  the writer to output to
     * @throws IOException
     */
    protected void serializeContentToken(ContentNode item, TagNode tagNode, Writer writer) throws IOException {
        if (dontEscape(tagNode)) {
            writer.write(item.getContent());
        } else {
            writer.write(escapeXml(item.getContent()));
        }
    }

    protected void serializeOpenTag(TagNode tagNode, Writer writer, boolean newLine) throws IOException {
        if (!isForbiddenTag(tagNode)) {
            String tagName = tagNode.getName();

            //
            // Ensure we use valid XML element names
            //
            tagName = Utils.sanitizeXmlIdentifier(tagName);

            Map<String, String> tagAtttributes = tagNode.getAttributes();

            // always have head and body in newline
            if (props.isAddNewlineToHeadAndBody() && isHeadOrBody(tagName)) {
                writer.write("\n");
            }

            writer.write("<" + tagName);
            Iterator<Map.Entry<String, String>> it = tagAtttributes.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> entry = (Map.Entry<String, String>) it.next();
                String attName = (String) entry.getKey();
                String attValue = (String) entry.getValue();
                serializeAttribute(tagNode, writer, attName, attValue);
            }

            if (isMinimizedTagSyntax(tagNode)) {
                writer.write(" />");
                if (newLine) {
                    writer.write("\n");
                }
            } else if (dontEscape(tagNode)) {
                // because we are not considering if the file is xhtml or html,
                // we need to put a javascript comment in front of the CDATA in case this is NOT xhtml
                writer.write(">");
                if (!tagNode.getText().toString().startsWith(CData.SAFE_BEGIN_CDATA)) {
                    writer.write(CData.SAFE_BEGIN_CDATA);
                    //
                    // Insert a newline after the CDATA start marker if there isn't
                    // already a newline character there
                    //
                    if (!tagNode.getText().toString().equals("")) {
                        char firstchar = tagNode.getText().toString().charAt(0);
                        if (firstchar != '\n' && firstchar != '\r') writer.write("\n");
                    }
                }
            } else {
                writer.write(">");
            }
        }
    }

    /**
     * @param tagNode
     * @return true if the tag is forbidden
     */
    protected boolean isForbiddenTag(TagNode tagNode) {
        // null tagName when rootNode is a dummy node.
        // this happens when omitting the html envelope elements ( <html>, <head>, <body> elements )
        String tagName = tagNode.getName();
        return tagName == null;
    }

    protected boolean isHeadOrBody(String tagName) {
        return "head".equalsIgnoreCase(tagName) || "body".equalsIgnoreCase(tagName);
    }

    /**
     * This allows overriding to eliminate forbidden attributes (for example javascript attributes onclick, onblur, etc. )
     *
     * @param writer
     * @param attName
     * @param attValue
     * @throws IOException
     */
    protected void serializeAttribute(TagNode tagNode, Writer writer, String attName, String attValue) throws IOException {
        //
        // For XML, we can't use the lax definition of attribute names used in HTML5, so
        // we have to replace any invalid ones with a generated attribute name, or skip
        // them entirely.
        //
        if (!props.isAllowInvalidAttributeNames()) {
            attName = Utils.sanitizeXmlIdentifier(attName, props.getInvalidXmlAttributeNamePrefix());
        }

        if (attName != null && (Utils.isValidXmlIdentifier(attName) || props.isAllowInvalidAttributeNames()) && !isForbiddenAttribute(tagNode, attName, attValue)) {
            writer.write(" " + attName + "=\"" + escapeXml(attValue) + "\"");
        }
    }

    /**
     * Override to add additional conditions.
     *
     * @param tagNode
     * @param attName
     * @param value
     * @return true if the attribute should not be outputed.
     */
    protected boolean isForbiddenAttribute(TagNode tagNode, String attName, String value) {
        return !props.isNamespacesAware() && (XMLNS_NAMESPACE.equals(attName) || attName.startsWith(XMLNS_NAMESPACE + ":"));
    }

    protected void serializeEndTag(TagNode tagNode, Writer writer) throws IOException {
        serializeEndTag(tagNode, writer, true);
    }

    protected void serializeEndTag(TagNode tagNode, Writer writer, boolean newLine) throws IOException {
        if (!isForbiddenTag(tagNode)) {
            String tagName = tagNode.getName();
            //
            // Ensure we use valid XML element names
            //
            tagName = Utils.sanitizeXmlIdentifier(tagName);
            if (dontEscape(tagNode)) {
                // because we are not considering if the file is xhtml or html,
                // we need to put a javascript comment in front of the CDATA in case this is NOT xhtml

                if (!tagNode.getText().toString().trim().endsWith(CData.SAFE_END_CDATA)) {
                    //
                    // Insert a newline character before the CDATA end marker if there isn't one
                    // already at the end of the tag node content
                    //
                    if (tagNode.getText().toString().length() > 0) {
                        char lastchar = tagNode.getText().toString().charAt(tagNode.getText().toString().length() - 1);
                        if (lastchar != '\n' && lastchar != '\r') writer.write("\n");
                    }
                    // Write the CDATA end marker
                    writer.write(CData.SAFE_END_CDATA);
                }
            }

            writer.write("</" + tagName + ">");

            if (newLine) {
                writer.write("\n");
            }
        }
    }

}