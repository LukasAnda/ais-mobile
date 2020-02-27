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
 * <p>
 * Provides set of TagInfo instances. The instance of this interface is used as a
 * collection of tag definitions used in cleanup process. Implementing this interface
 * desired behaviour of cleaner can be achived.<br/>
 * In most cases implementation will be or contain a kind of Map.
 * </p>
 */
public interface ITagInfoProvider {

    public TagInfo getTagInfo(String tagName);

}