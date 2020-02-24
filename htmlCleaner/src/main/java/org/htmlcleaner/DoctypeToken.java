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

/**
 * <p>HTML doctype token.</p>
 */
public class DoctypeToken extends BaseTokenImpl implements HtmlNode {

    public static final int UNKNOWN = 0;
    public static final int HTML4_0 = 10;
    public static final int HTML4_01 = 20;
    public static final int HTML4_01_STRICT = 21;
    public static final int HTML4_01_TRANSITIONAL = 22;


    //
    // Constants for identified doctypes
    //
    public static final int HTML4_01_FRAMESET = 23;
    public static final int XHTML1_0_STRICT = 31;
    public static final int XHTML1_0_TRANSITIONAL = 32;
    public static final int XHTML1_0_FRAMESET = 33;
    public static final int XHTML1_1 = 40;
    public static final int XHTML1_1_BASIC = 41;
    public static final int HTML5 = 60;
    public static final int HTML5_LEGACY_TOOL_COMPATIBLE = 61;
    //
    // Part 1 is the document type, typically 'html' or 'HTML'
    //
    private String part1;
    //
    // Part 2 is the PUBLIC or SYSTEM token
    //
    private String part2;
    //
    // Part 3 is the PUBLIC identifier, typically '-//W3C//DTD HTML 4.01//EN' or similar
    //
    private String part3;
    //
    // Part 4 is the SYSTEM identifier, typically a URL for the DTD
    //
    private String part4;
    /**
     * The identified DocType, if any
     */
    private Integer type = null;
    //
    // Whether the DocType is valid
    //
    private Boolean valid = null;

    public DoctypeToken(String part1, String part2, String part3, String part4) {
        this.part1 = part1;
        this.part2 = part2 != null ? part2.toUpperCase() : part2;
        this.part3 = clean(part3);
        this.part4 = clean(part4);
        validate();
    }

    /*
     * Constructor for 5-part DocTypes, e.g. <!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" SYSTEM "http://www.w3.org/TR/html4/strict.dtd">.
     * For this we ignore part4 as we assume that must be "SYSTEM".
     */
    public DoctypeToken(String part1, String part2, String part3, String part4, String part5) {
        this.part1 = part1;
        this.part2 = part2 != null ? part2.toUpperCase() : part2;
        this.part3 = clean(part3);
        this.part4 = clean(part5);
        validate();
    }

    private String clean(String s) {
        if (s != null) {
            s = s.replace('>', ' ');
            s = s.replace('<', ' ');
            s = s.replace('&', ' ');
            s = s.replace('\'', ' ');
            s = s.replace('\"', ' ');
        }

        return s;
    }

    public boolean isValid() {
        return valid;
    }

    /**
     * Checks the doctype according to W3C parsing rules and tries to identify
     * the type and validity
     * <p>
     * ee:
     * <ul>
     *   <li>http://www.w3.org/TR/html-markup/syntax.html#doctype-syntax</li>
     *   <li>http://dev.w3.org/html5/html-author/#doctype-declaration</li>
     * </ul>
     */
    private void validate() {

        //
        // No PUBLIC or SYSTEM token
        //
        if (!"public".equalsIgnoreCase(part2) && !"system".equalsIgnoreCase(part2)) {

            //
            // HTML 5
            //
            if ("html".equalsIgnoreCase(part1) && (part2 == null)) {
                type = HTML5;
                valid = true;
            }
        }

        if ("public".equalsIgnoreCase(part2)) {

            //
            // HTML 4.0 is valid without an ID, or with strict DTD ID
            //
            if ("-//W3C//DTD HTML 4.0//EN".equals(getPublicId())) {
                type = HTML4_0;
                if ("http://www.w3.org/TR/REC-html40/strict.dtd".equals(part4) || "".equals(getSystemId())) {
                    valid = true;
                } else {
                    valid = false;
                }
            }

            //
            // HTML 4.0.1 STRICT is valid with Strict dtd ID or empty
            //
            if ("-//W3C//DTD HTML 4.01//EN".equals(getPublicId())) {
                type = HTML4_01_STRICT;
                if ("http://www.w3.org/TR/html4/strict.dtd".equals(part4) || "".equals(getSystemId())) {
                    valid = true;
                } else {
                    valid = false;
                }
            }

            //
            // HTML 4.0.1 TRANSITIONAL valid only with Transitional DTD ID
            //
            if ("-//W3C//DTD HTML 4.01 Transitional//EN".equals(getPublicId())) {
                type = HTML4_01_TRANSITIONAL;
                if ("http://www.w3.org/TR/html4/loose.dtd".equals(getSystemId())) {
                    valid = true;
                } else {
                    valid = false;
                }
            }

            //
            // HTML 4.0.1 FRAMESET valid only with Frameset ID
            //
            if ("-//W3C//DTD HTML 4.01 Frameset//EN".equals(getPublicId())) {
                type = HTML4_01_FRAMESET;

                if ("http://www.w3.org/TR/html4/frameset.dtd".equals(getSystemId())) {
                    valid = true;
                } else {
                    valid = false;
                }
            }


            //
            // XHTML 1.0
            //
            if ("-//W3C//DTD XHTML 1.0 Strict//EN".equals(getPublicId())) {
                type = XHTML1_0_STRICT;
                if ("http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd".equals(getSystemId())) {
                    valid = true;
                } else {
                    valid = false;
                }

            }

            //
            // XHTML 1.0 Transitional
            //
            if ("-//W3C//DTD XHTML 1.0 Transitional//EN".equals(getPublicId())) {
                type = XHTML1_0_TRANSITIONAL;

                if ("http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd".equals(getSystemId())) {
                    valid = true;
                } else {
                    valid = false;
                }
            }

            //
            // XHTML 1.0 Frameset
            //
            if ("-//W3C//DTD XHTML 1.0 Frameset//EN".equals(getPublicId())) {
                type = XHTML1_0_FRAMESET;

                if ("http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd".equals(getSystemId())) {
                    valid = true;
                } else {
                    valid = false;
                }
            }

            //
            // XHTML 1.1
            //
            if ("-//W3C//DTD XHTML 1.1//EN".equals(getPublicId())) {
                type = XHTML1_1;
                if ("http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd".equals(getSystemId())) {
                    valid = true;
                } else {
                    valid = false;
                }
            }

            //
            // XHTML 1.1 Basic
            //
            if ("-//W3C//DTD XHTML Basic 1.1//EN".equals(getPublicId())) {
                type = XHTML1_1_BASIC;

                if ("http://www.w3.org/TR/xhtml11/DTD/xhtml-basic11.dtd".equals(getSystemId())) {
                    valid = true;
                } else {
                    valid = false;
                }
            }
        }

        if ("system".equalsIgnoreCase(part2)) {

            //
            // HTML 5 legacy tool compatible
            //
            if ("about:legacy-compat".equals(getPublicId())) {
                type = HTML5_LEGACY_TOOL_COMPATIBLE;
                valid = true;
            }
        }

        if (type == null) {
            type = UNKNOWN;
            valid = false;
        }
    }

    public String getContent() {

        if (type == UNKNOWN && part1 == null) {
            return "<!DOCTYPE>";
        }

        String result = "<!DOCTYPE ";

        //
        // If the type is XHTML or HTML5, the output is "html", otherwise it should be "HTML"
        //
        if (type != UNKNOWN) {
            if (type >= 30) {
                result += "html";
            } else {
                result += "HTML";
            }
        } else {
            //
            // if its an unknown doctype, just pass through as-is.
            //
            result += part1;
        }


        if (part2 != null) {
            result += " " + part2 + " \"" + part3 + "\"";

            if (!"".equals(part4)) {
                result += " \"" + part4 + "\"";
            }
        }

        result += ">";
        return result;
    }

    @Override
    public String toString() {
        return getContent();
    }

    /**
     * This will retrieve an integer representing the identified DocType
     */
    public int getType() {
        return type;
    }

    public String getName() {
        return "";
    }

    public void serialize(Serializer serializer, Writer writer) throws IOException {
        writer.write(getContent() + "\n");
    }

    /**
     * This will retrieve the public ID of an externally referenced DTD, or an empty String if none is referenced.
     */
    public String getPublicId() {
        return part3;
    }

    /**
     * This will retrieve the system ID of an externally referenced DTD, or an empty String if none is referenced.
     */
    public String getSystemId() {
        return part4;
    }

    public String getPart1() {
        return part1;
    }

    public String getPart2() {
        return part2;
    }

    /**
     * Deprecated - use getPublicId() instead
     *
     * @return
     */
    @Deprecated
    public String getPart3() {
        return part3;
    }

    /**
     * Deprecated - use getSystemId() instead
     *
     * @return
     */
    @Deprecated
    public String getPart4() {
        return part4;
    }
}