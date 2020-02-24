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
 * A {@link TagNode} that only really holds whitespace or comments - allows
 * using {@link ContentNode} in places where a {@link TagNode} is expected.
 * <p/>
 * This class is currently just a short-lived intermediate artifact generated
 * from {@link HtmlCleaner} while cleaning an html file and descarded
 * before the results are returned.
 *
 * @author andyhot
 */
class ProxyTagNode extends TagNode {
    private ContentNode token;
    private CommentNode comment;
    private TagNode bodyNode;

    public ProxyTagNode(ContentNode token, TagNode bodyNode) {
        super("");
        this.token = token;
        this.bodyNode = bodyNode;
    }

    public ProxyTagNode(CommentNode comment, TagNode bodyNode) {
        super("");
        this.comment = comment;
        this.bodyNode = bodyNode;
    }

    @Override
    public TagNode getParent() {
        return null;
    }

    @Override
    public boolean removeFromTree() {
        bodyNode.removeChild(getToken());
        return true;
    }

    public BaseToken getToken() {
        return token != null ? token : comment;
    }

    public String getContent() {
        return token != null ? token.getContent() : comment.getContent();
    }

}
