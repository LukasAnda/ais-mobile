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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Contains transformation collection.
 */
public class CleanerTransformations {

    private Map mappings = new HashMap();
    private TagTransformation globalTransformations = new TagTransformation();

    public CleanerTransformations() {

    }

    /**
     * @param transInfos
     */
    public CleanerTransformations(Map transInfos) {
        updateTagTransformations(transInfos);
    }

    /**
     * Adds specified tag transformation to the collection.
     *
     * @param tagTransformation
     */
    public void addTransformation(TagTransformation tagTransformation) {
        if (tagTransformation != null) {
            mappings.put(tagTransformation.getSourceTag(), tagTransformation);
        }
    }

    public void addGlobalTransformation(AttributeTransformation attributeTransformation) {
        globalTransformations.addAttributePatternTransformation(attributeTransformation);
    }

    public boolean hasTransformationForTag(String tagName) {
        return tagName != null && mappings.containsKey(tagName.toLowerCase());
    }

    public TagTransformation getTransformation(String tagName) {
        return tagName != null ? (TagTransformation) mappings.get(tagName.toLowerCase()) : null;
    }

    public void updateTagTransformations(String key, String value) {
        int index = key.indexOf('.');

        // new tag transformation case (tagname[=destname[,preserveatts]])
        if (index <= 0) {
            String destTag = null;
            boolean preserveSourceAtts = true;
            if (value != null) {
                String[] tokens = Utils.tokenize(value, ",;");
                if (tokens.length > 0) {
                    destTag = tokens[0];
                }
                if (tokens.length > 1) {
                    preserveSourceAtts = "true".equalsIgnoreCase(tokens[1]) ||
                            "yes".equalsIgnoreCase(tokens[1]) ||
                            "1".equals(tokens[1]);
                }
            }
            TagTransformation newTagTrans = new TagTransformation(key, destTag, preserveSourceAtts);
            addTransformation(newTagTrans);
        } else {    // attribute transformation description
            String[] parts = Utils.tokenize(key, ".");
            String tagName = parts[0];
            TagTransformation trans = getTransformation(tagName);
            if (trans != null) {
                trans.addAttributeTransformation(parts[1], value);
            }
        }
    }

    public void updateTagTransformations(Map transInfos) {
        Iterator iterator = transInfos.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String tag = (String) entry.getKey();
            String value = (String) entry.getValue();
            updateTagTransformations(tag, value);
        }
    }

    public Map<String, String> transformAttributes(String originalTagName, Map<String, String> attributes) {
        TagTransformation tagTrans = getTransformation(originalTagName);
        Map<String, String> results;
        if (tagTrans != null) {
            results = tagTrans.applyTagTransformations(attributes);
        } else {
            results = attributes;
        }
        return this.globalTransformations.applyTagTransformations(results);
    }

    public String getTagName(String tagName) {
        TagTransformation tagTransformation = null;
        if (hasTransformationForTag(tagName)) {
            tagTransformation = getTransformation(tagName);
            if (tagTransformation != null) {
                return tagTransformation.getDestTag();
            }
        }
        return tagName;
    }

    /**
     *
     */
    public void clear() {
        this.mappings.clear();
    }
}