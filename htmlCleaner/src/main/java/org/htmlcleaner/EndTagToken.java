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

import java.io.Writer;


/**
 * <p>HTML tag end token.</p>
 */
public class EndTagToken extends TagToken {

    public EndTagToken() {
    }

    public EndTagToken(String name) {
        super(name == null ? null : name);
    }

    @Override
    void addAttribute(String attName, String attValue) {
        // do nothing - simply ignore attributes in closing tag
    }

    public void serialize(Serializer serializer, Writer writer) {
        // do nothing - simply ignore serialization
    }

    @Override
    public String toString() {
        return "endtoken" + super.toString();
    }

}