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

package org.squonk.chemaxon.molecule;

/**
 *
 * @author timbo
 */
public interface MoleculeConstants {
    
    /** Default name for setting the molecule property in text representation */
    public static final String STRUCTURE_FIELD_NAME = "MOLECULE_AS_STRING";
    
    /** Default name for setting the molecule property as a chemaxon.struc.Molecule 
     Double leading underscores used as a convention to signify that this property
     is transient
     */
    public static final String __MOLECULE_FIELD_NAME = "__MOLECULE";
}
