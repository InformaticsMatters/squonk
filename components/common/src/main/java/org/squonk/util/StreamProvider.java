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

import java.io.IOException;
import java.util.stream.Stream;


/**
 * Wraps an Stream&lt;MoleculeObject&gt; to allow stronger typing and allow some
 * flexibility in the type of Stream generated.
 * Note that some stream implementations will need to be closed as they use underlying resources
 * The golden run is if you are performing the terminal operation on the stream you MUST
 * close it. A good way to do this is in a try-with-resources statement.
 *
 * @author timbo
 */
public interface StreamProvider<T> {
    
    Stream<T> getStream() throws IOException;
    
    Class<T> getType();
    
}
