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

package org.htmlcleaner.audit;

import org.htmlcleaner.TagNode;
import org.htmlcleaner.conditional.ITagNodeCondition;

import java.util.logging.Logger;

public class HtmlModificationListenerLogger implements HtmlModificationListener {


    private Logger log;

    public HtmlModificationListenerLogger(Logger log) {
        this.log = log;
    }

    public void fireConditionModification(ITagNodeCondition condition, TagNode tagNode) {
        this.log.info("fireConditionModification:" + condition + " at " + tagNode);
    }

    public void fireHtmlError(boolean safety, TagNode tagNode, ErrorType errorType) {
        this.log.info("fireHtmlError:" + errorType + "(" + safety + ") at " + tagNode);
    }

    public void fireUglyHtml(boolean safety, TagNode tagNode, ErrorType errorType) {
        this.log.info("fireConditionModification:" + errorType + "(" + safety + ") at " + tagNode);
    }

    public void fireUserDefinedModification(boolean safety, TagNode tagNode, ErrorType errorType) {
        this.log.info("fireConditionModification" + errorType + "(" + safety + ") at " + tagNode);
    }

}
