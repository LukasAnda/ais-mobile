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
 * <p>Simple HTML serializer - creates resulting HTML without indenting and/or compacting.</p>
 */
public class SimpleHtmlSerializer extends HtmlSerializer {

    public SimpleHtmlSerializer(CleanerProperties props) {
        super(props);
    }

    protected void serialize(TagNode tagNode, Writer writer) throws IOException {
        serializeOpenTag(tagNode, writer, false);

        if (!isMinimizedTagSyntax(tagNode)) {
            for (Object item : tagNode.getAllChildren()) {
                if (item instanceof ContentNode) {
                    String content = item.toString();
                    writer.write(dontEscape(tagNode) ? content : escapeText(content));
                } else if (item instanceof BaseToken) {
                    ((BaseToken) item).serialize(this, writer);
                }
            }

            serializeEndTag(tagNode, writer, false);
        }
    }

}