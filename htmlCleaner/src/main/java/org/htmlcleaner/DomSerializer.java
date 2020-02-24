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

import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * <p>DOM serializer - creates xml DOM.</p>
 */
public class DomSerializer {

    private static final String CSS_COMMENT_START = "/*";

    private static final String CSS_COMMENT_END = "*/";

    private static final String NEW_LINE = "\n";

    /**
     * The HTML Cleaner properties set by the user to control the HTML cleaning.
     */
    protected CleanerProperties props;

    /**
     * Whether XML entities should be escaped or not.
     */
    protected boolean escapeXml = true;

    protected boolean deserializeCdataEntities = false;

    protected boolean strictErrorChecking = true;

    /**
     * @param props                    the HTML Cleaner properties set by the user to control the HTML cleaning.
     * @param escapeXml                if true then escape XML entities
     * @param deserializeCdataEntities if true then deserialize entities in CData sections
     * @param strictErrorChecking      if false then Document strict error checking is turned off
     */
    public DomSerializer(CleanerProperties props, boolean escapeXml, boolean deserializeCdataEntities, boolean strictErrorChecking) {
        this.props = props;
        this.escapeXml = escapeXml;
        this.deserializeCdataEntities = deserializeCdataEntities;
        this.strictErrorChecking = strictErrorChecking;
    }

    /**
     * @param props                    the HTML Cleaner properties set by the user to control the HTML cleaning.
     * @param escapeXml                if true then escape XML entities
     * @param deserializeCdataEntities if true then deserialize entities in CData sections
     */
    public DomSerializer(CleanerProperties props, boolean escapeXml, boolean deserializeCdataEntities) {
        this.props = props;
        this.escapeXml = escapeXml;
        this.deserializeCdataEntities = deserializeCdataEntities;
    }

    /**
     * @param props     the HTML Cleaner properties set by the user to control the HTML cleaning.
     * @param escapeXml if true then escape XML entities
     */
    public DomSerializer(CleanerProperties props, boolean escapeXml) {
        this.props = props;
        this.escapeXml = escapeXml;
    }

    /**
     * @param props the HTML Cleaner properties set by the user to control the HTML cleaning.
     */
    public DomSerializer(CleanerProperties props) {
        this(props, true);
    }

    //
    // Allow overriding of serialization for implementations. See bug #167.
    //
    protected Document createDocument(TagNode rootNode) throws ParserConfigurationException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        DOMImplementation impl = builder.getDOMImplementation();

        Document document;

        //
        // Where a DOCTYPE is supplied in the input, ensure that this is in the output DOM. See issue #27
        //
        // Note that we may want to fix incorrect DOCTYPEs in future; there are some fairly
        // common patterns for errors with the older HTML4 doctypes.
        //
        if (rootNode.getDocType() != null) {
            String qualifiedName = rootNode.getDocType().getPart1();
            String publicId = rootNode.getDocType().getPublicId();
            String systemId = rootNode.getDocType().getSystemId();

            //
            // If there is no qualified name, set it to html. See bug #153.
            //
            if (qualifiedName == null) qualifiedName = "html";

            DocumentType documentType = impl.createDocumentType(qualifiedName, publicId, systemId);

            //
            // While the qualified name is "HTML" for some DocTypes, we want the actual document root name to be "html". See bug #116
            //
            if (qualifiedName.equals("HTML")) qualifiedName = "html";
            document = impl.createDocument(rootNode.getNamespaceURIOnPath(""), qualifiedName, documentType);
        } else {
            document = builder.newDocument();
            Element rootElement = document.createElement(rootNode.getName());
            document.appendChild(rootElement);
        }

        //
        // Turn off error checking if we're allowing invalid attribute names, or if we've chosen to turn it off
        //
        if (props.isAllowInvalidAttributeNames() || strictErrorChecking == false) {
            document.setStrictErrorChecking(false);
        }


        //
        // Copy across root node attributes - see issue 127. Thanks to rasifiel for the patch
        //
        Map<String, String> attributes = rootNode.getAttributes();
        Iterator<Map.Entry<String, String>> entryIterator = attributes.entrySet().iterator();
        while (entryIterator.hasNext()) {
            Map.Entry<String, String> entry = entryIterator.next();
            String attrName = entry.getKey();
            String attrValue = entry.getValue();

            //
            // Fix any invalid attribute names
            //
            if (!props.isAllowInvalidAttributeNames()) {
                attrName = Utils.sanitizeXmlIdentifier(attrName, props.getInvalidXmlAttributeNamePrefix());
            }

            if (attrName != null && (Utils.isValidXmlIdentifier(attrName) || props.isAllowInvalidAttributeNames())) {

                if (escapeXml) {
                    attrValue = Utils.escapeXml(attrValue, props, true);
                }

                document.getDocumentElement().setAttribute(attrName, attrValue);

                //
                // Flag the attribute as an ID attribute if appropriate. Thanks to Chris173
                //
                if (attrName.equalsIgnoreCase("id")) {
                    document.getDocumentElement().setIdAttribute(attrName, true);
                }
            }

        }

        return document;
    }

    /**
     * @param rootNode the HTML Cleaner root node to serialize
     * @return the W3C Document object
     * @throws ParserConfigurationException if there's an error during serialization
     */
    public Document createDOM(TagNode rootNode) throws ParserConfigurationException {
        Document document = createDocument(rootNode);
        createSubnodes(document, (Element) document.getDocumentElement(), rootNode.getAllChildren());

        return document;
    }

    /**
     * @param element the element to check
     * @return true if the passed element is a script or style element
     */
    protected boolean isScriptOrStyle(Element element) {
        String tagName = element.getNodeName();
        return "script".equalsIgnoreCase(tagName) || "style".equalsIgnoreCase(tagName);
    }

    /**
     * encapsulate content with <[CDATA[ ]]> for things like script and style elements
     *
     * @param element
     * @return true if <[CDATA[ ]]> should be used.
     */
    protected boolean dontEscape(Element element) {
        // make sure <script src=..></script> doesn't get turned into <script src=..><[CDATA[]]></script>
        return props.isUseCdataFor(element.getNodeName()) && (!element.hasChildNodes() || element.getTextContent() == null || element.getTextContent().trim().length() == 0);
    }

    protected String outputCData(CData cdata) {
        return cdata.getContentWithoutStartAndEndTokens();
    }

    protected String deserializeCdataEntities(String input) {
        return Utils.deserializeEntities(input, props.isRecognizeUnicodeChars());
    }

    /**
     * Serialize a given HTML Cleaner node.
     *
     * @param document    the W3C Document to use for creating new DOM elements
     * @param element     the W3C element to which we'll add the subnodes to
     * @param tagChildren the HTML Cleaner nodes to serialize for that node
     */
    protected void createSubnodes(Document document, Element element, List<? extends BaseToken> tagChildren) {

        if (tagChildren != null) {

            CDATASection cdata = null;

            //
            // For script and style nodes, check if we're set to use CDATA
            //
            if (props.isUseCdataFor(element.getTagName())) {
                cdata = document.createCDATASection("");
                element.appendChild(document.createTextNode(CSS_COMMENT_START));
                element.appendChild(cdata);
            }

            Iterator<? extends BaseToken> it = tagChildren.iterator();
            while (it.hasNext()) {

                Object item = it.next();
                if (item instanceof CommentNode) {

                    CommentNode commentNode = (CommentNode) item;
                    Comment comment = document.createComment(commentNode.getContent());
                    element.appendChild(comment);

                } else if (item instanceof ContentNode) {

                    ContentNode contentNode = (ContentNode) item;
                    String content = contentNode.getContent();
                    boolean specialCase = props.isUseCdataFor(element.getTagName());

                    if (shouldEscapeOrTranslateEntities() && !specialCase) {
                        content = Utils.escapeXml(content, props, true);
                    }

                    if (specialCase && item instanceof CData) {
                        //
                        // For CDATA sections we don't want to return the start and
                        // end tokens. See issue #106.
                        //
                        content = ((CData) item).getContentWithoutStartAndEndTokens();
                    }

                    if (specialCase && deserializeCdataEntities) {
                        content = this.deserializeCdataEntities(content);
                    }

                    if (cdata != null) {
                        cdata.appendData(content);
                    } else {
                        element.appendChild(document.createTextNode(content));
                    }


                } else if (item instanceof TagNode) {

                    TagNode subTagNode = (TagNode) item;

                    //
                    // XML element names are more strict in their definition
                    // than  HTML tag identifiers.
                    // See https://www.w3.org/TR/xml/#NT-Name
                    // vs. https://html.spec.whatwg.org/multipage/parsing.html#tag-name-state
                    //
                    String name = Utils.sanitizeXmlIdentifier(subTagNode.getName(), props.getInvalidXmlAttributeNamePrefix());

                    //
                    // If the element name is completely invalid, treat it as text
                    //
                    if (name == null) {
                        ContentNode contentNode = new ContentNode(subTagNode.getName() + subTagNode.getText().toString());
                        String content = contentNode.getContent();
                        content = Utils.escapeXml(content, props, true);
                        element.appendChild(document.createTextNode(content));

                    } else {

                        Element subelement = document.createElement(name);
                        Map<String, String> attributes = subTagNode.getAttributes();
                        Iterator<Map.Entry<String, String>> entryIterator = attributes.entrySet().iterator();
                        while (entryIterator.hasNext()) {
                            Map.Entry<String, String> entry = entryIterator.next();
                            String attrName = entry.getKey();
                            String attrValue = entry.getValue();
                            if (escapeXml) {
                                attrValue = Utils.escapeXml(attrValue, props, true);
                            }

                            //
                            // Fix any invalid attribute names by adding a prefix
                            //
                            if (!props.isAllowInvalidAttributeNames()) {
                                attrName = Utils.sanitizeXmlIdentifier(attrName, props.getInvalidXmlAttributeNamePrefix());
                            }

                            if (attrName != null && (Utils.isValidXmlIdentifier(attrName) || props.isAllowInvalidAttributeNames())) {
                                subelement.setAttribute(attrName, attrValue);

                                //
                                // Flag the attribute as an ID attribute if appropriate. Thanks to Chris173
                                //
                                if (attrName.equalsIgnoreCase("id")) {
                                    subelement.setIdAttribute(attrName, true);
                                }
                            }

                        }

                        // recursively create subnodes
                        createSubnodes(document, subelement, subTagNode.getAllChildren());

                        element.appendChild(subelement);
                    }
                } else if (item instanceof List) {
                    List<? extends BaseToken> sublist = (List<? extends BaseToken>) item;
                    createSubnodes(document, element, sublist);
                }

            }
            if (cdata != null) {

                if (!cdata.getData().startsWith(NEW_LINE)) {
                    cdata.setData(CSS_COMMENT_END + NEW_LINE + cdata.getData());
                } else {
                    cdata.setData(CSS_COMMENT_END + cdata.getData());
                }
                if (!cdata.getData().endsWith(NEW_LINE)) {

                    cdata.appendData(NEW_LINE);
                }
                cdata.appendData(CSS_COMMENT_START);
                element.appendChild(document.createTextNode(CSS_COMMENT_END));
            }
        }
    }

    private boolean shouldEscapeOrTranslateEntities() {
        return escapeXml || props.isRecognizeUnicodeChars() || props.isTranslateSpecialEntities();
    }

}