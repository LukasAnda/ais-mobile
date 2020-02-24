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

/**
 * Implementors can be registered on {@link org.htmlcleaner.CleanerProperties} to receive notifications about
 * modifications made by html cleaner.
 *
 * @author Konstantin Burov (aectann@gmail.com)
 */
public interface HtmlModificationListener {

    /**
     * Fired when cleaner fixes some error in html syntax.
     *
     * @param certain   - true if change made doesn't hurts end document.
     * @param tagNode   - problematic node.
     * @param errorType
     */
    void fireHtmlError(boolean certain, TagNode tagNode, ErrorType errorType);

    /**
     * Fired when cleaner fixes ugly html -- when syntax was correct but task was implemented by weird code.
     * For example when deprecated tags are removed.
     *
     * @param certainty - true if change made doesn't hurts end document.
     * @param tagNode   - problematic node.
     * @param errorType
     */
    void fireUglyHtml(boolean certainty, TagNode tagNode, ErrorType errorType);

    /**
     * Fired when cleaner modifies html due to {@link ITagNodeCondition} match.
     *
     * @param condition that was applied to make the modification
     * @param tagNode   - problematic node.
     */
    void fireConditionModification(ITagNodeCondition condition, TagNode tagNode);

    /**
     * Fired when cleaner modifies html due to user specified rules.
     *
     * @param certainty - true if change made doesn't hurts end document.
     * @param tagNode   - problematic node.
     * @param errorType
     */
    void fireUserDefinedModification(boolean certainty, TagNode tagNode, ErrorType errorType);

}
