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

import org.htmlcleaner.HtmlCleaner.NestingState;
import org.htmlcleaner.conditional.ITagNodeCondition;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

/**
 * This class is for thread-safe handling of private instance variables from HtmlCleaner
 */
class CleanTimeValues {

    boolean _headOpened = false;
    boolean _bodyOpened = false;
    @SuppressWarnings("rawtypes")
    Set _headTags = new LinkedHashSet();
    @SuppressWarnings("rawtypes")
    Set allTags = new TreeSet();
    transient Stack<NestingState> nestingStates = new Stack<NestingState>();

    TagNode htmlNode;
    TagNode bodyNode;
    TagNode headNode;
    TagNode rootNode;

    Set<ITagNodeCondition> pruneTagSet = new HashSet<ITagNodeCondition>();
    Set<TagNode> pruneNodeSet = new HashSet<TagNode>();
    Set<ITagNodeCondition> allowTagSet;

    /**
     * A stack of namespaces for currently open tags. Every xmlns declaration
     * on a tag adds another namespace to the stack, which is removed when the
     * tag is closed. In this way you can keep track of what namespace a tag
     * belongs to.
     */
    transient Stack<String> namespace = new Stack<String>();

    /**
     * A map of all the namespace prefixes and URIs declared within the document.
     * We use this to check whether any prefixes remain undeclared.
     */
    transient HashMap<String, String> namespaceMap = new HashMap<String, String>();
}