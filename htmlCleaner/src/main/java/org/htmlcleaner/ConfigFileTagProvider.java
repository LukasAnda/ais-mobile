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

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Configuration file tag provider - reads XML file in specified format and creates a Tag Provider.
 * Used to create custom tag providers when used on the command line.
 */
public class ConfigFileTagProvider extends HashMap implements ITagInfoProvider {

    // obtaining instance of the SAX parser factory
    static SAXParserFactory parserFactory = SAXParserFactory.newInstance();

    static {
        parserFactory.setValidating(false);
        parserFactory.setNamespaceAware(false);
    }

    // tells whether to generate code of the tag provider class based on XML configuration file
    // to the standard output
    private boolean generateCode = false;

    private ConfigFileTagProvider() {
    }

    public ConfigFileTagProvider(InputSource inputSource) {
        try {
            new ConfigParser(this).parse(inputSource);
        } catch (Exception e) {
            throw new HtmlCleanerException("Error parsing tag configuration file!", e);
        }
    }

    public ConfigFileTagProvider(File file) {
        try {
            new ConfigParser(this).parse(new InputSource(new FileReader(file)));
        } catch (Exception e) {
            throw new HtmlCleanerException("Error parsing tag configuration file!", e);
        }
    }

    public ConfigFileTagProvider(URL url) {
        try {
            Object content = url.getContent();
            if (content instanceof InputStream) {
                InputStreamReader reader = new InputStreamReader((InputStream) content);
                new ConfigParser(this).parse(new InputSource(reader));
            }
        } catch (Exception e) {
            throw new HtmlCleanerException("Error parsing tag configuration file!", e);
        }
    }

    /**
     * Generates code for tag provider class from specified configuration XML file.
     * In order to create custom tag info provider, make config file and call this main method
     * with the specified file. Output will be generated on the standard output. This way a custom
     * tag provider (class CustomTagProvider) is generated from an XML file. An example XML file,
     * "example.xml", can be found in the source distribution.
     *
     * @param args
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {
        final ConfigFileTagProvider provider = new ConfigFileTagProvider();
        provider.generateCode = true;

        String fileName = "default.xml";
        if (args != null && args.length > 0) {
            fileName = args[0];
        }

        File configFile = new File(fileName);
        String packagePath = "org.htmlcleaner";
        String className = "CustomTagProvider";

        final ConfigParser parser = provider.new ConfigParser(provider);
        System.out.println("package " + packagePath + ";");
        System.out.println("import java.util.HashMap;");
        System.out.println("public class " + className + " extends HashMap implements ITagInfoProvider {");
        System.out.println("private ConcurrentMap<String, TagInfo> tagInfoMap = new ConcurrentHashMap<String, TagInfo>();");
        System.out.println("// singleton instance, used if no other TagInfoProvider is specified");
        System.out.println("public final static " + className + " INSTANCE= new " + className + "();");
        System.out.println("public " + className + "() {");
        System.out.println("TagInfo tagInfo;");
        parser.parse(new InputSource(new FileReader(configFile)));
        System.out.println("}");
        System.out.println("}");
    }

    public TagInfo getTagInfo(String tagName) {
        return (TagInfo) get(tagName);
    }

    /**
     * SAX parser for tag configuration files.
     */
    private class ConfigParser extends DefaultHandler {
        private TagInfo tagInfo = null;
        private String dependencyName = null;
        private Map tagInfoMap;

        ConfigParser(Map tagInfoMap) {
            this.tagInfoMap = tagInfoMap;
        }

        public void parse(InputSource in) throws ParserConfigurationException, SAXException, IOException {
            SAXParser parser = parserFactory.newSAXParser();
            parser.parse(in, this);
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (tagInfo != null) {
                String value = new String(ch, start, length).trim();
                if ("fatal-tags".equals(dependencyName)) {
                    tagInfo.defineFatalTags(value);
                    if (generateCode) {
                        System.out.println("tagInfo.defineFatalTags(\"" + value + "\");");
                    }
                } else if ("req-enclosing-tags".equals(dependencyName)) {
                    tagInfo.defineRequiredEnclosingTags(value);
                    if (generateCode) {
                        System.out.println("tagInfo.defineRequiredEnclosingTags(\"" + value + "\");");
                    }
                } else if ("forbidden-tags".equals(dependencyName)) {
                    tagInfo.defineForbiddenTags(value);
                    if (generateCode) {
                        System.out.println("tagInfo.defineForbiddenTags(\"" + value + "\");");
                    }
                } else if ("allowed-children-tags".equals(dependencyName)) {
                    tagInfo.defineAllowedChildrenTags(value);
                    if (generateCode) {
                        System.out.println("tagInfo.defineAllowedChildrenTags(\"" + value + "\");");
                    }
                } else if ("higher-level-tags".equals(dependencyName)) {
                    tagInfo.defineHigherLevelTags(value);
                    if (generateCode) {
                        System.out.println("tagInfo.defineHigherLevelTags(\"" + value + "\");");
                    }
                } else if ("close-before-copy-inside-tags".equals(dependencyName)) {
                    tagInfo.defineCloseBeforeCopyInsideTags(value);
                    if (generateCode) {
                        System.out.println("tagInfo.defineCloseBeforeCopyInsideTags(\"" + value + "\");");
                    }
                } else if ("close-inside-copy-after-tags".equals(dependencyName)) {
                    tagInfo.defineCloseInsideCopyAfterTags(value);
                    if (generateCode) {
                        System.out.println("tagInfo.defineCloseInsideCopyAfterTags(\"" + value + "\");");
                    }
                } else if ("close-before-tags".equals(dependencyName)) {
                    tagInfo.defineCloseBeforeTags(value);
                    if (generateCode) {
                        System.out.println("tagInfo.defineCloseBeforeTags(\"" + value + "\");");
                    }
                }
            }
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if ("tag".equals(qName)) {
                String name = attributes.getValue("name");
                String content = attributes.getValue("content");
                String section = attributes.getValue("section");
                String deprecated = attributes.getValue("deprecated");
                String unique = attributes.getValue("unique");
                String ignorePermitted = attributes.getValue("ignore-permitted");
                ContentType contentType = ContentType.toValue(content);
                BelongsTo belongsTo = BelongsTo.toValue(section);
                tagInfo = new TagInfo(name, contentType,
                        belongsTo,
                        deprecated != null && "true".equals(deprecated),
                        unique != null && "true".equals(unique),
                        ignorePermitted != null && "true".equals(ignorePermitted), CloseTag.required, Display.any);
                if (generateCode) {
                    String s = "tagInfo = new TagInfo(\"#1\", #2, #3, #4, #5, #6);";
                    s = s.replaceAll("#1", name);
                    s = s.replaceAll("#2", ContentType.class.getCanonicalName() + "." + contentType.name());
                    s = s.replaceAll("#3", BelongsTo.class.getCanonicalName() + "." + belongsTo.name());
                    s = s.replaceAll("#4", Boolean.toString(deprecated != null && "true".equals(deprecated)));
                    s = s.replaceAll("#5", Boolean.toString(unique != null && "true".equals(unique)));
                    s = s.replaceAll("#6", Boolean.toString(ignorePermitted != null && "true".equals(ignorePermitted)));
                    System.out.println(s);
                }
            } else if (!"tags".equals(qName)) {
                dependencyName = qName;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if ("tag".equals(qName)) {
                if (tagInfo != null) {
                    tagInfoMap.put(tagInfo.getName(), tagInfo);
                    if (generateCode) {
                        System.out.println("this.put(\"" + tagInfo.getName() + "\", tagInfo);\n");
                    }
                }
                tagInfo = null;
            } else if (!"tags".equals(qName)) {
                dependencyName = null;
            }
        }
    }

}