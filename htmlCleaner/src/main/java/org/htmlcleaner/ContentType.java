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

/**
 * @author patmoore
 */
public enum ContentType {
    all("all"),
    /**
     * elements that have no children or content ( for example <img> ). For these elements, the check for null elements must be more than must a children/ content check.
     */
    none("none"),
    text("text");
    private final String dbCode;

    private ContentType(String dbCode) {
        this.dbCode = dbCode;
    }

    public static ContentType toValue(Object value) {
        ContentType result = null;
        if (value instanceof ContentType) {
            result = (ContentType) value;
        } else if (value != null) {
            String dbCode = value.toString().trim();
            for (ContentType contentType : ContentType.values()) {
                if (contentType.getDbCode().equalsIgnoreCase(dbCode) || contentType.name().equalsIgnoreCase(dbCode)) {
                    result = contentType;
                    break;
                }
            }
        }

        return result;
    }

    /**
     * @return the dbCode
     */
    public String getDbCode() {
        return dbCode;
    }
}
