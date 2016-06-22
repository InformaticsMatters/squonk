package org.squonk.rdkit.db.dsl;

import org.squonk.types.MoleculeObject;

import java.util.List;

/**
 * Created by timbo on 13/12/2015.
 */
public interface IExecutable {


    List<MoleculeObject> execute();


}
