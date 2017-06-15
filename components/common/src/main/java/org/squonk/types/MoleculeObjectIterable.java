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

package org.squonk.types;


import org.squonk.types.MoleculeObject;

/**
 * Interface that wraps an Iterable&lt;MoleculeObject&gt; to allow stronger typing.
 * Note that some implementations will implement Closeable as they may use underlying resources that
 * need to be closed, so whenever an instance is finished with you should check if 
 * the instance implements Closeable and if so call close().
 * 
 * Note: Iterables/Iterators are generally being phased out in preference to Streams.
 *
 * @author timbo
 * @ see {@link MoleculeObjectStream}
 */
public interface MoleculeObjectIterable extends Iterable<MoleculeObject> {
    
}
