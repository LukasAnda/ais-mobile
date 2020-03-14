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
public enum BelongsTo {

    HEAD_AND_BODY("all"),
    HEAD("head"),
    BODY("body");
    private final String dbCode;

    BelongsTo(String dbCode) {
        this.dbCode = dbCode;
    }

    public static BelongsTo toValue(Object value) {
        BelongsTo result = null;
        if (value instanceof BelongsTo) {
            result = (BelongsTo) value;
        } else if (value != null) {
            String dbCode = value.toString().trim();
            for (BelongsTo belongsTo : BelongsTo.values()) {
                if (belongsTo.getDbCode().equalsIgnoreCase(dbCode) || belongsTo.name().equalsIgnoreCase(dbCode)) {
                    result = belongsTo;
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
