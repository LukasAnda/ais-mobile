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
import java.util.Iterator;
import java.util.List;

/**
 * <p>Simple XML serializer - creates resulting XML without indenting lines.</p>
 */
public class SimpleXmlSerializer extends XmlSerializer {

    public SimpleXmlSerializer(CleanerProperties props) {
        super(props);
    }

    @Override
    protected void serialize(TagNode tagNode, Writer writer) throws IOException {
        serializeOpenTag(tagNode, writer, false);

        List<? extends BaseToken> tagChildren = tagNode.getAllChildren();
        if (!isMinimizedTagSyntax(tagNode)) {
            Iterator<? extends BaseToken> childrenIt = tagChildren.iterator();
            while (childrenIt.hasNext()) {
                Object item = childrenIt.next();

                if (item != null) {
                    if (item instanceof CData) {
                        serializeCData((CData) item, tagNode, writer);
                    } else if (item instanceof ContentNode) {
                        serializeContentToken((ContentNode) item, tagNode, writer);
                    } else {
                        ((BaseToken) item).serialize(this, writer);
                    }
                }
            }

            serializeEndTag(tagNode, writer, false);
        }
    }

}