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
public enum CloseTag {
    /**
     * <div></div> is required. Minimizing to <div/> is not permitted.
     */
    required(false, true),
    /**
     * <hr> or <hr/> is permitted
     */
    optional(true, true),
    /**
     * <img/> is not permitted
     */
    forbidden(true, false);
    private final boolean minimizedTagPermitted;
    private final boolean endTagPermitted;

    /**
     * @param minimizedTagPermitted if true tag can be reduced to <x/>
     * @param endTagPermitted       TODO
     */
    CloseTag(boolean minimizedTagPermitted, boolean endTagPermitted) {
        this.minimizedTagPermitted = minimizedTagPermitted;
        this.endTagPermitted = endTagPermitted;
    }

    /**
     * @return true if <x/> form is allowed
     */
    public boolean isMinimizedTagPermitted() {
        return this.minimizedTagPermitted;
    }

    /**
     * @return true if <x/> or </x> is permitted.
     */
    public boolean isEndTagPermitted() {
        return endTagPermitted;
    }
}
