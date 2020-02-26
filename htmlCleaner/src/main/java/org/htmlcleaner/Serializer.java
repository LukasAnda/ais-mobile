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

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

/**
 * <p>Basic abstract serializer - contains common logic for descendants (methods <code>writeXXX()</code>.</p>
 */
public abstract class Serializer {

    protected CleanerProperties props;

    protected Serializer(CleanerProperties props) {
        this.props = props;
    }

    /**
     * Writes specified TagNode to the output stream, using specified charset and optionally omits node envelope
     * (skips open and close tags of the node).
     *
     * @param tagNode      Node to be written
     * @param out          Output stream
     * @param charset      Charset of the output
     * @param omitEnvelope Tells whether to skip open and close tag of the node.
     * @throws IOException
     */
    public void writeToStream(TagNode tagNode, OutputStream out, String charset, boolean omitEnvelope) throws IOException {
        write(tagNode, new OutputStreamWriter(out, charset), charset, omitEnvelope);
    }

    /**
     * Writes specified TagNode to the output stream, using specified charset.
     *
     * @param tagNode Node to be written
     * @param out     Output stream
     * @param charset Charset of the output
     * @throws IOException
     */
    public void writeToStream(TagNode tagNode, OutputStream out, String charset) throws IOException {
        writeToStream(tagNode, out, charset, false);
    }

    /**
     * Writes specified TagNode to the output stream, using system default charset and optionally omits node envelope
     * (skips open and close tags of the node).
     *
     * @param tagNode      Node to be written
     * @param out          Output stream
     * @param omitEnvelope Tells whether to skip open and close tag of the node.
     * @throws IOException
     */
    public void writeToStream(TagNode tagNode, OutputStream out, boolean omitEnvelope) throws IOException {
        writeToStream(tagNode, out, props.getCharset(), omitEnvelope);
    }

    /**
     * Writes specified TagNode to the output stream, using system default charset.
     *
     * @param tagNode Node to be written
     * @param out     Output stream
     * @throws IOException
     */
    public void writeToStream(TagNode tagNode, OutputStream out) throws IOException {
        writeToStream(tagNode, out, false);
    }

    /**
     * Writes specified TagNode to the file, using specified charset and optionally omits node envelope
     * (skips open and close tags of the node).
     *
     * @param tagNode      Node to be written
     * @param fileName     Output file name
     * @param charset      Charset of the output
     * @param omitEnvelope Tells whether to skip open and close tag of the node.
     * @throws IOException
     */
    public void writeToFile(TagNode tagNode, String fileName, String charset, boolean omitEnvelope) throws IOException {
        writeToStream(tagNode, new FileOutputStream(fileName), charset, omitEnvelope);
    }

    /**
     * Writes specified TagNode to the file, using specified charset.
     *
     * @param tagNode  Node to be written
     * @param fileName Output file name
     * @param charset  Charset of the output
     * @throws IOException
     */
    public void writeToFile(TagNode tagNode, String fileName, String charset) throws IOException {
        writeToFile(tagNode, fileName, charset, false);
    }

    /**
     * Writes specified TagNode to the file, using specified charset and optionally omits node envelope
     * (skips open and close tags of the node).
     *
     * @param tagNode      Node to be written
     * @param fileName     Output file name
     * @param omitEnvelope Tells whether to skip open and close tag of the node.
     * @throws IOException
     */
    public void writeToFile(TagNode tagNode, String fileName, boolean omitEnvelope) throws IOException {
        writeToFile(tagNode, fileName, props.getCharset(), omitEnvelope);
    }

    /**
     * Writes specified TagNode to the file, using system default charset.
     *
     * @param tagNode  Node to be written
     * @param fileName Output file name
     * @throws IOException
     */
    public void writeToFile(TagNode tagNode, String fileName) throws IOException {
        writeToFile(tagNode, fileName, false);
    }

    /**
     * @param tagNode      Node to serialize to string
     * @param charset      Charset of the output - stands in xml declaration part
     * @param omitEnvelope Tells whether to skip open and close tag of the node.
     * @return Output as string
     */
    public String getAsString(TagNode tagNode, String charset, boolean omitEnvelope) {
        StringWriter writer = new StringWriter();
        try {
            write(tagNode, writer, charset, omitEnvelope);
        } catch (IOException e) {
            // not writing to the file system so any io errors should be really rare ( and bad)
            throw new HtmlCleanerException(e);
        }
        return writer.getBuffer().toString();
    }

    /**
     * @param tagNode Node to serialize to string
     * @param charset Charset of the output - stands in xml declaration part
     * @return Output as string
     */
    public String getAsString(TagNode tagNode, String charset) {
        return getAsString(tagNode, charset, false);
    }

    /**
     * @param tagNode      Node to serialize to string
     * @param omitEnvelope Tells whether to skip open and close tag of the node.
     * @return Output as string
     * @throws IOException
     */
    public String getAsString(TagNode tagNode, boolean omitEnvelope) {
        return getAsString(tagNode, props.getCharset(), omitEnvelope);
    }

    /**
     * @param tagNode Node to serialize to string
     * @return Output as string
     * @throws IOException
     */
    public String getAsString(TagNode tagNode) {
        return getAsString(tagNode, false);
    }

    public String getAsString(String htmlContent) {
        HtmlCleaner htmlCleaner = new HtmlCleaner(this.props);
        TagNode tagNode = htmlCleaner.clean(htmlContent);
        return getAsString(tagNode, props.getCharset());
    }

    /**
     * Writes specified node using specified writer.
     *
     * @param tagNode Node to serialize.
     * @param writer  Writer instance
     * @param charset Charset of the output
     * @throws IOException
     */
    public void write(TagNode tagNode, Writer writer, String charset) throws IOException {
        write(tagNode, writer, charset, false);
    }

    /**
     * Writes specified node using specified writer.
     *
     * @param tagNode      Node to serialize.
     * @param writer       Writer instance
     * @param charset      Charset of the output
     * @param omitEnvelope Tells whether to skip open and close tag of the node.
     * @throws IOException
     */
    public void write(TagNode tagNode, Writer writer, String charset, boolean omitEnvelope) throws IOException {
        if (omitEnvelope) {
            tagNode = new HeadlessTagNode(tagNode);
        }
        writer = new BufferedWriter(writer);
        if (!props.isOmitXmlDeclaration()) {
            String declaration = "<?xml version=\"1.0\"";
            if (charset != null) {
                declaration += " encoding=\"" + charset + "\"";
            }
            declaration += "?>";
            writer.write(declaration + "\n");
        }

        if (!props.isOmitDoctypeDeclaration()) {
            DoctypeToken doctypeToken = tagNode.getDocType();
            if (doctypeToken != null) {
                doctypeToken.serialize(this, writer);
            }
        }

        serialize(tagNode, writer);

        writer.flush();
        writer.close();
    }

    protected boolean isScriptOrStyle(TagNode tagNode) {
        String tagName = tagNode.getName();
        return "script".equalsIgnoreCase(tagName) || "style".equalsIgnoreCase(tagName);
    }

    protected abstract void serialize(TagNode tagNode, Writer writer) throws IOException;

    /**
     * Used to implement serialization with missing envelope - omiting open and close tags, just
     * serialize children.
     */
    private class HeadlessTagNode extends TagNode {
        private HeadlessTagNode(TagNode wrappedNode) {
            super("");
            getAttributes().putAll(wrappedNode.getAttributes());
            addChildren(wrappedNode.getAllChildren());
            setDocType(wrappedNode.getDocType());
            Map<String, String> nsDecls = getNamespaceDeclarations();
            if (nsDecls != null) {
                Map<String, String> wrappedNSDecls = wrappedNode.getNamespaceDeclarations();
                if (wrappedNSDecls != null) {
                    nsDecls.putAll(wrappedNSDecls);
                }
            }

        }
    }

}