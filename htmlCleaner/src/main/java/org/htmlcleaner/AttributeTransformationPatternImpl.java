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

import java.util.regex.Pattern;

public class AttributeTransformationPatternImpl implements AttributeTransformation {
    private final Pattern attNamePattern;
    private final Pattern attValuePattern;
    private final String template;

    public AttributeTransformationPatternImpl(Pattern attNamePattern, Pattern attValuePattern, String template) {
        this.attNamePattern = attNamePattern;
        this.attValuePattern = attValuePattern;
        this.template = template;
    }

    public AttributeTransformationPatternImpl(String attNamePattern, String attValuePattern, String template) {
        this.attNamePattern = attNamePattern == null ? null : Pattern.compile(attNamePattern);
        this.attValuePattern = attValuePattern == null ? null : Pattern.compile(attValuePattern);
        this.template = template;
    }

    public boolean satisfy(String attName, String attValue) {
        if ((attNamePattern == null || attNamePattern.matcher(attName).find()) && (attValuePattern == null || attValuePattern.matcher(attValue).find())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return the template
     */
    public String getTemplate() {
        return template;
    }
}