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
 * <p>HTML text token.</p>
 */
public class ContentNode extends BaseTokenImpl implements HtmlNode {

    protected final String content;
    protected final boolean blank;

    public ContentNode(String content) {
        this.content = content;
        this.blank = Utils.isEmptyString(this.content);
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return getContent();
    }

    public void serialize(Serializer serializer, Writer writer) throws IOException {
        writer.write(getContent());
    }

    public boolean isBlank() {
        return this.blank;
    }
}