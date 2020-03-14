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
 * Most HTML 4 elements permitted within the BODY are classified as either
 * block-level elements or inline elements. This enumeration contains
 * corresponding constants to distinguish them.
 *
 * @author Konstantin Burov (aectann@gmail.com)
 */
public enum Display {
    /**
     * Block-level elements typically contain inline elements and other
     * block-level elements. When rendered visually, block-level elements
     * usually begin on a new line.
     */
    block(true, false),
    /**
     * Inline elements typically may only contain text and other inline
     * elements. When rendered visually, inline elements do not usually begin on
     * a new line.
     */
    inline(false, true),

    /**
     * The following elements may be used as either block-level elements or
     * inline elements. If used as inline elements (e.g., within another inline
     * element or a P), these elements should not contain any block-level
     * elements.
     */
    any(true, false),

    /**
     * Elements that are not actually inline or block, usually such elements are
     * not rendered at all.
     */
    none(true, false);

    private boolean afterTagLineBreakNeeded;
    private boolean leadingAndEndWhitespacesAllowed;

    Display(boolean afterTagLineBreakNeeded, boolean leadingAndEndWhitespacesAllowed) {
        this.afterTagLineBreakNeeded = afterTagLineBreakNeeded;
        this.leadingAndEndWhitespacesAllowed = leadingAndEndWhitespacesAllowed;
    }

    /**
     * @return true to advise serializers to put line break after tags with such a display type.
     */
    public boolean isAfterTagLineBreakNeeded() {
        return afterTagLineBreakNeeded;
    }

    /**
     * @return true if tag contents can have single leading or end whitespace
     */
    public boolean isLeadingAndEndWhitespacesAllowed() {
        return leadingAndEndWhitespacesAllowed;
    }


}
