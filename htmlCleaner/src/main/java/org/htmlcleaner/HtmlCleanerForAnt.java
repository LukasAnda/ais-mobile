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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

/**
 * <p>Support for ANT.</p>
 */
public class HtmlCleanerForAnt extends Task {

    private String text;
    private String src;
    private String dest;
    private String incharset = CleanerProperties.DEFAULT_CHARSET;
    private String outcharset = CleanerProperties.DEFAULT_CHARSET;
    private String taginfofile = null;
    private String outputtype = "simple";
    private boolean advancedxmlescape = true;
    private boolean usecdata = true;
    private String usecdatafor = "script,style";
    private boolean specialentities = true;
    private boolean unicodechars = true;
    private boolean omitunknowntags = false;
    private boolean treatunknowntagsascontent = false;
    private boolean omitdeprtags = false;
    private boolean treatdeprtagsascontent = false;
    private boolean omitcomments = false;
    private boolean omitxmldecl = false;
    private boolean omitdoctypedecl = true;
    private boolean omithtmlenvelope = false;
    private boolean useemptyelementtags = true;
    private boolean allowmultiwordattributes = true;
    private boolean allowhtmlinsideattributes = false;
    private boolean ignoreqe = false;
    private boolean namespacesaware = true;
    private String hyphenreplacement = "=";
    private String prunetags = "";
    private String booleanatts = CleanerProperties.BOOL_ATT_SELF;
    private String nodebyxpath = null;

    private String transform = null;

    private boolean allowInvalidAttributeNames = false;
    private String invalidAttributeNamePrefix = "";

    public void setText(String text) {
        this.text = text;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public void setDest(String dest) {
        this.dest = dest;
    }

    public void setIncharset(String incharset) {
        this.incharset = incharset;
    }

    public void setOutcharset(String outcharset) {
        this.outcharset = outcharset;
    }

    public void setTaginfofile(String taginfofile) {
        this.taginfofile = taginfofile;
    }

    public void setOutputtype(String outputtype) {
        this.outputtype = outputtype;
    }

    public void setAdvancedxmlescape(boolean advancedxmlescape) {
        this.advancedxmlescape = advancedxmlescape;
    }

    public void setUsecdata(boolean usecdata) {
        this.usecdata = usecdata;
    }

    public void setUsecdatafor(String usecdatafor) {
        this.usecdatafor = usecdatafor;
    }

    public void setSpecialentities(boolean specialentities) {
        this.specialentities = specialentities;
    }

    public void setUnicodechars(boolean unicodechars) {
        this.unicodechars = unicodechars;
    }

    public void setOmitunknowntags(boolean omitunknowntags) {
        this.omitunknowntags = omitunknowntags;
    }

    public void setTreatunknowntagsascontent(boolean treatunknowntagsascontent) {
        this.treatunknowntagsascontent = treatunknowntagsascontent;
    }

    public void setOmitdeprtags(boolean omitdeprtags) {
        this.omitdeprtags = omitdeprtags;
    }


    public void setTreatdeprtagsascontent(boolean treatdeprtagsascontent) {
        this.treatdeprtagsascontent = treatdeprtagsascontent;
    }

    public void setOmitcomments(boolean omitcomments) {
        this.omitcomments = omitcomments;
    }

    public void setOmitxmldecl(boolean omitxmldecl) {
        this.omitxmldecl = omitxmldecl;
    }

    public void setOmitdoctypedecl(boolean omitdoctypedecl) {
        this.omitdoctypedecl = omitdoctypedecl;
    }

    public void setOmithtmlenvelope(boolean omithtmlenvelope) {
        this.omithtmlenvelope = omithtmlenvelope;
    }

    public void setUseemptyelementtags(boolean useemptyelementtags) {
        this.useemptyelementtags = useemptyelementtags;
    }

    public void setAllowmultiwordattributes(boolean allowmultiwordattributes) {
        this.allowmultiwordattributes = allowmultiwordattributes;
    }

    public void setAllowhtmlinsideattributes(boolean allowhtmlinsideattributes) {
        this.allowhtmlinsideattributes = allowhtmlinsideattributes;
    }

    public void setIgnoreqe(boolean ignoreqe) {
        this.ignoreqe = ignoreqe;
    }

    public void setNamespacesaware(boolean namespacesaware) {
        this.namespacesaware = namespacesaware;
    }

    public void setHyphenreplacement(String hyphenreplacement) {
        this.hyphenreplacement = hyphenreplacement;
    }

    public void setPrunetags(String prunetags) {
        this.prunetags = prunetags;
    }

    public void setBooleanatts(String booleanatts) {
        this.booleanatts = booleanatts;
    }

    public void setNodebyxpath(String nodebyxpath) {
        this.nodebyxpath = nodebyxpath;
    }

    public void setTransform(String transform) {
        this.transform = transform;
    }

    public void addText(String text) {
        this.text = text;
    }

    /**
     * Implementation of Ant task execution.
     *
     * @throws BuildException
     */
    @Override
    public void execute() throws BuildException {
        HtmlCleaner cleaner;

        if (this.taginfofile != null) {
            cleaner = new HtmlCleaner(new ConfigFileTagProvider(new File(this.taginfofile)));
        } else {
            cleaner = new HtmlCleaner();
        }

        if (text == null && src == null) {
            throw new BuildException("Eather attribute 'src' or text body containing HTML must be specified!");
        }

        CleanerProperties props = cleaner.getProperties();

        props.setAdvancedXmlEscape(this.advancedxmlescape);
        props.setUseCdataFor(this.usecdatafor);
        props.setUseCdataForScriptAndStyle(this.usecdata);
        props.setTranslateSpecialEntities(this.specialentities);
        props.setRecognizeUnicodeChars(this.unicodechars);
        props.setOmitUnknownTags(this.omitunknowntags);
        props.setTreatUnknownTagsAsContent(this.treatunknowntagsascontent);
        props.setOmitDeprecatedTags(this.omitdeprtags);
        props.setTreatDeprecatedTagsAsContent(this.treatdeprtagsascontent);
        props.setOmitComments(this.omitcomments);
        props.setOmitXmlDeclaration(this.omitxmldecl);
        props.setOmitDoctypeDeclaration(this.omitdoctypedecl);
        props.setOmitHtmlEnvelope(this.omithtmlenvelope);
        props.setUseEmptyElementTags(this.useemptyelementtags);
        props.setAllowMultiWordAttributes(this.allowmultiwordattributes);
        props.setAllowHtmlInsideAttributes(this.allowhtmlinsideattributes);
        props.setIgnoreQuestAndExclam(this.ignoreqe);
        props.setNamespacesAware(this.namespacesaware);
        props.setHyphenReplacementInComment(this.hyphenreplacement);
        props.setPruneTags(this.prunetags);
        props.setBooleanAttributeValues(this.booleanatts);
        props.setAllowInvalidAttributeNames(this.allowInvalidAttributeNames);
        props.setInvalidXmlAttributeNamePrefix(this.invalidAttributeNamePrefix);

        // set cleaner transformation if specified in "transform" attribute
        // format of attribute is expected to be <transkey1>[=<transvalue1>]|<transkey2>[=<transvalue2>...
        // (separator is pipe character)
        if (!Utils.isEmptyString(transform)) {
            String[] transItems = Utils.tokenize(transform, "|");
            Map transInfos = new TreeMap();
            for (String item : transItems) {
                int index = item.indexOf('=');
                String key = index <= 0 ? item : item.substring(0, index);
                String value = index <= 0 ? null : item.substring(index + 1);
                transInfos.put(key, value);
            }

            cleaner.initCleanerTransformations(transInfos);
        }

        try {
            TagNode node;
            try {
                if (src != null && (src.startsWith("http://") || src.startsWith("https://"))) {
                    node = cleaner.clean(new URL(src), incharset);
                } else if (src != null) {
                    node = cleaner.clean(new File(src), incharset);
                } else {
                    node = cleaner.clean(text);
                }
            } catch (IOException e) {
                throw new BuildException(e);
            }

            // if user specifies XPath expresssion to choose node for serialization, then
            // try to evaluate XPath and look for first TagNode instance in the resulting array
            if (nodebyxpath != null) {
                final Object[] xpathResult = node.evaluateXPath(nodebyxpath);
                for (Object element : xpathResult) {
                    if (element instanceof TagNode) {
                        node = (TagNode) element;
                        break;
                    }
                }
            }

            OutputStream out;

            String antPropertyName = "";

            if (dest == null || "".equals(dest.trim())) {
                out = System.out;
            } else if (dest.startsWith("property:")) {
                out = new ByteArrayOutputStream();
                antPropertyName = dest.substring(dest.indexOf(':') + 1);
                getProject().log("Setting property " + antPropertyName);
            } else {
                out = new FileOutputStream(dest);
            }

            if ("compact".equals(outputtype)) {
                new CompactXmlSerializer(props).writeToStream(node, out, outcharset);
            } else if ("browser-compact".equals(outputtype)) {
                new BrowserCompactXmlSerializer(props).writeToStream(node, out, outcharset);
            } else if ("pretty".equals(outputtype)) {
                new PrettyXmlSerializer(props).writeToStream(node, out, outcharset);
            } else {
                new SimpleXmlSerializer(props).writeToStream(node, out, outcharset);
            }

            if (antPropertyName != null && antPropertyName.length() > 0) {
                getProject().setNewProperty(antPropertyName, out.toString());
            }

        } catch (IOException e) {
            throw new BuildException(e);
        } catch (XPatherException e) {
            throw new BuildException(e);
        }
    }

    public boolean isAllowInvalidAttributeNames() {
        return allowInvalidAttributeNames;
    }

    public void setAllowInvalidAttributeNames(boolean allowInvalidAttributeNames) {
        this.allowInvalidAttributeNames = allowInvalidAttributeNames;
    }

    public String getInvalidAttributeNamePrefix() {
        return invalidAttributeNamePrefix;
    }

    public void setInvalidAttributeNamePrefix(String invalidAttributeNamePrefix) {
        this.invalidAttributeNamePrefix = invalidAttributeNamePrefix;
    }

}