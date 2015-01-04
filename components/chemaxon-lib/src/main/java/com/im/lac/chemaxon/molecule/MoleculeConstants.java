package com.im.lac.chemaxon.molecule;

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
