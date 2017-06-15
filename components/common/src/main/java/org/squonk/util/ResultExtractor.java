/*
 * Copyright (c) 2017 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.squonk.util;

import java.util.Map;

/** Interface to allow a callback to collect results.
 * This class needs a better home, but right now its only used from the chemaxon-camel
 * module so it can stay there until its needed elsewhere.
 *
 * @author timbo
 */
public interface ResultExtractor<T> {
    
    public Map<String, Object> extractResults(T from);
    
}
